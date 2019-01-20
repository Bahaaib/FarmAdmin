package com.ets.farmadmin.Root;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.ets.farmadmin.Auth.LoginActivity;
import com.ets.farmadmin.R;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class HomeActivity extends AppCompatActivity {

    private final String TAG = "Statuss";
    private final String HEADS_DB = "head";
    private final String PHONE_KEY = "head_phone";
    private final String EMPTY_KEY = "empty";

    //Firebase DB
    private FirebaseDatabase database;
    private DatabaseReference mRef;
    private ArrayList<HeadModel> headList;

    //Firebase Auth
    private FirebaseAuth mAuth;

    private Toolbar toolbar;
    private TabLayout tabLayout;

    private SharedPreferences preferences;
    private String headPhoneNumber;

    //Navigation Drawer
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private NavigationView navigationView;

    private View header;
    private TextView headName;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);



        //Firebase DB
        FirebaseApp.initializeApp(this);
        database = FirebaseDatabase.getInstance();
        mRef = database.getReference();

        //Firebase Auth..
        mAuth = FirebaseAuth.getInstance();

        headList = new ArrayList<>();

        //Init|Recall SharedPrefs..
        preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        restoreSavedPrefs();


        //Assigning all used objects to their views
        tabLayout = findViewById(R.id.tab_layout);


        //Adding Three tabs to the screen
        tabLayout.addTab(tabLayout.newTab().setText(R.string.vege_tab));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.fruits_tab));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.offers_tab));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);


        //Setting up the View Pager that allows flipping activity fragments horizontally like a page
        final ViewPager viewPager = findViewById(R.id.pager);
        final PagerAdapter pagerAdapter = new PagerAdapter(getSupportFragmentManager(), tabLayout.getTabCount());

        viewPager.setAdapter(pagerAdapter);
        //Set Viewpager initially to Vegetables tab
        viewPager.setCurrentItem(2);

        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        //Navigation Drawer
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close);

        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.toolbar_text);

        navigationView = findViewById(R.id.nv);
        header  =  navigationView.getHeaderView(0);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                switch (id) {
                    case R.id.logout:
                            displayToast(getString(R.string.logging_out));
                            forceLogout();
                            return true;

                    default:
                        return true;
                }
            }
        });

        //Sync with firebase..
        headName = header.findViewById(R.id.user_name);
        callHeadDatabae();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (actionBarDrawerToggle.onOptionsItemSelected(item))
            return true;

        return super.onOptionsItemSelected(item);
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
        boolean isMember = false;
        for (DataSnapshot db : dataSnapshot.getChildren()) {
            HeadModel model = db.getValue(HeadModel.class);
            headList.add(model);
            if (model.getPhone().equals(headPhoneNumber)) {
                //check admin account in case of De-activation
                if (model.getStatus()) {
                    isMember = true;
                }else {
                    isMember = false;
                }
            }
            headName.setText(model.getName());
            Log.i(TAG, model.getPhone());
        }

        if (!isMember) {
            forceLogout();
        }
    }

    private void restoreSavedPrefs() {
        headPhoneNumber = preferences.getString(PHONE_KEY, EMPTY_KEY);

    }

    private void forceLogout() {
        Log.i("Statuss", "Logging out");
        mAuth.signOut();
        Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    private void displayToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG)
                .show();
    }
}
