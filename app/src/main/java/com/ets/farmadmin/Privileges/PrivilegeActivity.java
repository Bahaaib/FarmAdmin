package com.ets.farmadmin.Privileges;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.ets.farmadmin.Auth.LoginActivity;
import com.ets.farmadmin.Dialogs.AdminDialog;
import com.ets.farmadmin.R;
import com.ets.farmadmin.Root.AdapterListener;
import com.ets.farmadmin.Root.DialogListener;
import com.ets.farmadmin.Root.HeadModel;
import com.ets.farmadmin.Root.HomeActivity;
import com.ets.farmadmin.Root.ProductModel;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class PrivilegeActivity extends AppCompatActivity implements DialogListener, AdapterListener {

    private final String HEADS_DB = "head";
    private final String PHONE_KEY = "head_phone";
    private final String EMPTY_KEY = "empty";
    private final String TAG = "admin_dialg";
    //Firebase DB
    private FirebaseDatabase database;
    private DatabaseReference mRef;
    //Firebase Auth
    private FirebaseAuth mAuth;
    private boolean isAdmin;
    private SharedPreferences preferences;
    private String headPhoneNumber;
    private ArrayList<HeadModel> headsList;
    private RecyclerView recyclerView;
    private HeadRecyclerAdapter adapter;
    private LinearLayoutManager linearLayoutManager;
    private Toolbar toolbar;
    private Button addButton;
    private AdminDialog adminDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privilege);

        toolbar = findViewById(R.id.priv_toolbar);
        setSupportActionBar(toolbar);

        //Init|Recall SharedPrefs..
        preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        restoreSavedPrefs();

        addButton = findViewById(R.id.admin_add_btn);

        adminDialog = new AdminDialog();

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                Fragment prev = getSupportFragmentManager().findFragmentByTag(TAG);
                if (prev != null) {
                    transaction.remove(prev);
                }

                transaction.add(adminDialog, TAG).commit();
            }
        });

        //Firebase DB
        FirebaseApp.initializeApp(this);
        database = FirebaseDatabase.getInstance();
        mRef = database.getReference();

        //Firebase Auth..
        mAuth = FirebaseAuth.getInstance();

        headsList = new ArrayList<>();
        recyclerView = findViewById(R.id.heads_rv);

        callHeadDatabase();

        adapter = new HeadRecyclerAdapter(this, headsList);
        recyclerView.setAdapter(adapter);
        linearLayoutManager = new LinearLayoutManager(this);

        recyclerView.setLayoutManager(linearLayoutManager);


    }

    private void callHeadDatabase() {
        mRef.child(HEADS_DB).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //Reset List of products
                headsList.clear();
                fetchData(dataSnapshot);
                fetchPersonalData(dataSnapshot);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void fetchData(DataSnapshot dataSnapshot) {
        for (DataSnapshot db : dataSnapshot.getChildren()) {
            HeadModel model = db.getValue(HeadModel.class);
            model.setKey(db.getKey());
            headsList.add(model);
            adapter.notifyDataSetChanged();

            Log.i("Statuss", model.getName());
        }


    }

    private void fetchPersonalData(DataSnapshot dataSnapshot) {
        boolean isMember = false;
        for (DataSnapshot db : dataSnapshot.getChildren()) {
            HeadModel model = db.getValue(HeadModel.class);
            model.setKey(db.getKey());
            if (model.getPhone().equals(headPhoneNumber)) {
                //check admin account in case of De-activation
                isMember = model.getStatus();

                //Check if still Admin
                isAdmin = model.getPrivilege().equals("admin");
            }
        }

        if (!isAdmin) {
            displayToast(getString(R.string.priv_changed));
            forceStepBack();
        }

        if (!isMember) {
            displayToast(getString(R.string.priv_changed));
            forceLogout();
        }
    }

    private void forceStepBack() {
        Intent intent = new Intent(PrivilegeActivity.this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    private void forceLogout() {
        Log.i("Statuss", "Logging out");
        mAuth.signOut();
        Intent intent = new Intent(PrivilegeActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    private void restoreSavedPrefs() {
        headPhoneNumber = preferences.getString(PHONE_KEY, EMPTY_KEY);

    }

    private void displayToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG)
                .show();
    }


    @Override
    public void onFruitsDataChanged(ProductModel product) {

    }

    @Override
    public void onVegetablesDataChanged(ProductModel product) {

    }

    @Override
    public void onAdminDataChanged(HeadModel head) {
        if (head.getKey() != null) {
            mRef.child(HEADS_DB).child(head.getKey())
                    .child("name")
                    .setValue(head.getName());

            mRef.child(HEADS_DB).child(head.getKey())
                    .child("phone")
                    .setValue(head.getPhone());

            mRef.child(HEADS_DB).child(head.getKey())
                    .child("privilege")
                    .setValue(head.getPrivilege());

            mRef.child(HEADS_DB).child(head.getKey())
                    .child("status")
                    .setValue(head.getStatus());
        } else {
            mRef.child(HEADS_DB).push().setValue(head);
        }
    }

    @Override
    public void onDataRemoved(HeadModel head) {
        //Save head key before destroy the head..
        String headKey = head.getKey();
        head.setKey(null);
        mRef.child(HEADS_DB).child(headKey).setValue(head);
    }
}
