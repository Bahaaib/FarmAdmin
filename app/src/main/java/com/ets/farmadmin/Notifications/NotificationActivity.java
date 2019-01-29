package com.ets.farmadmin.Notifications;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.ets.farmadmin.Auth.LoginActivity;
import com.ets.farmadmin.R;
import com.ets.farmadmin.Root.HeadModel;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class NotificationActivity extends AppCompatActivity {

    private final String PHONE_KEY = "head_phone";
    private final String EMPTY_KEY = "empty";
    private final String MSG_DB = "messages";
    private final String HEADS_DB = "head";
    private EditText notificationTitle;
    private EditText notificationBodyField;
    private Button notificationButton;
    //Messages Firebase DB
    private FirebaseDatabase database;
    private DatabaseReference notificationRef;
    //Heads Firebase DB
    private DatabaseReference mRef;
    private ArrayList<HeadModel> headList;
    private SharedPreferences preferences;
    private String headPhoneNumber;

    //Firebase Auth
    private FirebaseAuth mAuth;

    private Toolbar toolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        toolbar = findViewById(R.id.notification_toolbar);

        //Init|Recall SharedPrefs..
        preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        restoreSavedPrefs();

        //Heads Firebase DB
        FirebaseApp.initializeApp(this);
        database = FirebaseDatabase.getInstance();
        notificationRef = database.getReference().child(MSG_DB);

        //Messages Firebase DB
        database = FirebaseDatabase.getInstance();
        mRef = database.getReference();

        //Firebase Auth..
        mAuth = FirebaseAuth.getInstance();

        headList = new ArrayList<>();

        notificationTitle = findViewById(R.id.notification_title);
        notificationBodyField = findViewById(R.id.notification_body);
        notificationButton = findViewById(R.id.notification_button);

        //Track heads DB
        callHeadDatabase();

        notificationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = notificationTitle.getText().toString();
                String message = notificationBodyField.getText().toString();
                //If nothing empty .. push the notification
                if (!message.isEmpty() && !title.isEmpty()) {
                    sendNotification(title, message);
                } else {
                    displayToast(getString(R.string.empty_notification));
                }
            }
        });


    }

    private void sendNotification(String title, String message) {
        MessageModel notification = new MessageModel();

        notification.setTitle(title);
        notification.setMessage(message);

        notificationRef.push().setValue(notification);

        displayToast(getString(R.string.notification_sent));


    }

    private void displayToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG)
                .show();
    }

    private void callHeadDatabase() {
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
        boolean isMember = false;
        for (DataSnapshot db : dataSnapshot.getChildren()) {
            HeadModel model = db.getValue(HeadModel.class);
            model.setKey(db.getKey());
            headList.add(model);
            if (model.getPhone().equals(headPhoneNumber)) {
                //check admin account in case of De-activation
                isMember = model.getStatus();
            }

        }
        if (!isMember) {
            displayToast(getString(R.string.priv_changed));
            forceLogout();
        }
    }

    private void forceLogout() {
        Log.i("ToastStatuss", "Logging out");
        mAuth.signOut();
        Intent intent = new Intent(NotificationActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    private void restoreSavedPrefs() {
        headPhoneNumber = preferences.getString(PHONE_KEY, EMPTY_KEY);

    }
}
