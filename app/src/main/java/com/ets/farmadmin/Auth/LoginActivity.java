package com.ets.farmadmin.Auth;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.ets.farmadmin.R;
import com.ets.farmadmin.Root.HeadModel;
import com.ets.farmadmin.Root.HomeActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class LoginActivity extends AppCompatActivity {

    private final String TAG = "Statuss";
    private final String HEADS_DB = "head";
    private final String PHONE_KEY = "head_phone";

    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    private EditText phoneNumberField, smsCodeVerificationField;
    private Button startVerficationButton, verifyPhoneButton;
    private String verificationid;

    //Firebase Auth
    private FirebaseAuth mAuth;

    private ProgressDialog progressDialog;


    //Firebase DB
    private FirebaseDatabase database;
    private DatabaseReference mRef;
    private ArrayList<HeadModel> headList;

    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //Firebase
        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);

        database = FirebaseDatabase.getInstance();
        mRef = database.getReference();

        //Init|Recall SharedPrefs..
        preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());


        //init Views..
        initViews();

        headList = new ArrayList<>();

        callHeadDatabae();

        startVerficationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String code = smsCodeVerificationField.getText().toString();
                verifyVerificationCode(code);

            }
        });
        verifyPhoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String phoneNumber = phoneNumberField.getText().toString();
                if (isValidMobileNumber(phoneNumber)) {
                    //Check if admin in DB
                    if (isHead(phoneNumber)) {
                        //Register to Sharedpreferences first..
                        saveToSharedpreferences(PHONE_KEY, phoneNumber);
                        //Then Go login..
                        progressDialog.setMessage(getString(R.string.login_dialog_text));
                        progressDialog.show();
                        startPhoneNumberVerification(phoneNumber);
                    } else {
                        phoneNumberField.setError(getString(R.string.not_auth));
                    }

                } else {
                    phoneNumberField.setError(getString(R.string.incomplete_number));
                }
            }
        });


    }

    @Override
    protected void onStart() {
        super.onStart();
        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {

                Log.i(TAG, "onVerificationCompleted:" + credential);

                signInWithPhoneAuthCredential(credential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {

                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                    // ...
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                    // ...
                }

                displayToast("Verification Failed");
            }

            @Override
            public void onCodeSent(String verificationId,
                                   PhoneAuthProvider.ForceResendingToken token) {
                Log.i(TAG, "onCodeSent:" + verificationId);

                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId;
                mResendToken = token;
            }
        };

    }

    // validating mobile number format
    private boolean isValidMobileNumber(String number) {
        return Patterns.PHONE.matcher(number).matches() && (number.length() > 10);
    }

    private void callHeadDatabae() {
        mRef.child(HEADS_DB).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //Reset List of products
                headList.clear();
                fetchData(dataSnapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void fetchData(DataSnapshot dataSnapshot) {
        for (DataSnapshot db : dataSnapshot.getChildren()) {
            HeadModel model = db.getValue(HeadModel.class);
            headList.add(model);
            Log.i(TAG, model.getName());
        }
    }

    private boolean isHead(String phone) {
        for (HeadModel head : headList) {
            //Check account existence
            if (head.getPhone().equals(phone)) {
                //Check account activity
                if (head.getStatus()) {
                    return true;
                }
            }
        }
        return false;
    }

    private void verifyVerificationCode(String code) {
        Log.i(TAG, "ID: " + verificationid);
        Log.i(TAG, "CODE: " + code);
        try {
            //creating the credential
            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, code);
            Log.i(TAG, "Credentials: " + credential.toString());
            //signing the user
            signInWithPhoneAuthCredential(credential);
        } catch (Exception e) {
            Log.i(TAG, "Couldn't get Credentials!");
        }


    }

    private void saveToSharedpreferences(String key, String value) {
        preferences.edit()
                .putString(key, value)
                .apply();
    }

    private void startPhoneNumberVerification(String phoneNumber) {

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                "+2" + phoneNumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks);        // OnVerificationStateChangedCallbacks


    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.i(TAG, "signInWithCredential:success");

                            progressDialog.dismiss();
                            startActivity(new Intent(getApplicationContext(), HomeActivity.class));

                        } else {
                            // Sign in failed, display a message and update the UI
                            progressDialog.dismiss();
                            displayToast("Sign in Problem. Try again!");
                            Log.i(TAG, "signInWithCredential:failure", task.getException());

                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid

                                smsCodeVerificationField.setError("Invalid code.");

                            }

                        }
                    }
                });
    }

    private void displayToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG)
                .show();
    }

    private void initViews() {
        phoneNumberField = findViewById(R.id.phone_number);
        smsCodeVerificationField = findViewById(R.id.verification_code);
        startVerficationButton = findViewById(R.id.verification_btn);
        verifyPhoneButton = findViewById(R.id.login_btn);
    }


}
