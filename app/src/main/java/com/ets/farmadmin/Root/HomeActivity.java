package com.ets.farmadmin.Root;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.ets.farmadmin.Auth.LoginActivity;
import com.ets.farmadmin.Notifications.NotificationActivity;
import com.ets.farmadmin.Privileges.PrivilegeActivity;
import com.ets.farmadmin.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;

public class HomeActivity extends AppCompatActivity implements DialogListener, AdapterListener {

    private final String TAG = "Statuss";
    private final String HEADS_DB = "head";
    private final String PHONE_KEY = "head_phone";
    private final String EMPTY_KEY = "empty";
    private final String FRUITS_DB = "fruits";
    private final String VEGETABLES_DB = "vegetables";
    private static final int GALLERY_INTENT = 22;

    //Firebase DB
    private FirebaseDatabase database;
    private DatabaseReference mRef;
    private ArrayList<HeadModel> headList;
    private final String OFFER_DB = "offer";

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
    //Firebase Storage..
    StorageReference storage;
    private boolean isAdmin;
    //Gallery image..
    private Button galleryButton;
    private ProgressDialog mProgressDialog;


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

        //Gallery image..
        galleryButton = findViewById(R.id.add_img_btn);
        mProgressDialog = new ProgressDialog(this);

        galleryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, GALLERY_INTENT);
            }
        });

        //Firebase storage
        storage = FirebaseStorage.getInstance().getReference();


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
        drawerLayout = findViewById(R.id.drawer_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close);

        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.toolbar_text);

        navigationView = findViewById(R.id.nv);
        header = navigationView.getHeaderView(0);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                switch (id) {
                    case R.id.add:
                        if (isAdmin) {
                            moveToPrivilegeActivity();
                        } else {
                            displayToast(getString(R.string.no_admin_text));
                        }
                        return true;

                    case R.id.notification:
                        moveToNotificationActivity();
                        return true;

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
        callHeadDatabase();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (actionBarDrawerToggle.onOptionsItemSelected(item))
            return true;

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_INTENT && resultCode == RESULT_OK) {
            Uri uri = null;
            if (data != null) {
                uri = data.getData();
            }

            if (uri != null) {
                String path = uri.getLastPathSegment();

                if (!isNetworkConnected()) {
                    displayToast(getString(R.string.check_connection));
                } else {
                    mProgressDialog.setMessage(getString(R.string.upload_progress));
                    mProgressDialog.show();

                    StorageReference offerRef = storage.child("offers").child(path);

                    offerRef.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            mProgressDialog.dismiss();

                            final Task<Uri> task = taskSnapshot.getMetadata().getReference().getDownloadUrl();
                            task.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    addUriToDatabase(uri);

                                    String uploadMsg = getString(R.string.upload_success);
                                    displayToast(uploadMsg);
                                }
                            });
                        }
                    });
                }
            }
        }

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

                isAdmin = model.getPrivilege().equals("admin");

                headName.setText(model.getName());
            }

            Log.i(TAG, model.getPhone());
        }


        if (!isMember) {
            displayToast(getString(R.string.priv_changed));
            forceLogout();
        }
    }

    private void restoreSavedPrefs() {
        headPhoneNumber = preferences.getString(PHONE_KEY, EMPTY_KEY);
        Log.i("Statuss", "Num: " + headPhoneNumber);

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

    private void addUriToDatabase(Uri uri) {
        OfferModel offer = new OfferModel();
        offer.setImgUrl(String.valueOf(uri));
        mRef.child(OFFER_DB).push().setValue(offer);
    }

    private void moveToPrivilegeActivity() {
        Intent intent = new Intent(HomeActivity.this, PrivilegeActivity.class);
        startActivity(intent);
    }

    private void moveToNotificationActivity() {
        Intent intent = new Intent(HomeActivity.this, NotificationActivity.class);
        startActivity(intent);
    }

    private void displayToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG)
                .show();
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null;
    }

    @Override
    public void onFruitsDataChanged(ProductModel product) {
        mRef.child(FRUITS_DB).child(product.getKey())
                .child("price")
                .setValue(product.getPrice());

        mRef.child(FRUITS_DB).child(product.getKey())
                .child("availability")
                .setValue(product.getAvailability());
    }

    @Override
    public void onVegetablesDataChanged(ProductModel product) {
        mRef.child(VEGETABLES_DB).child(product.getKey())
                .child("price")
                .setValue(product.getPrice());

        mRef.child(VEGETABLES_DB).child(product.getKey())
                .child("availability")
                .setValue(product.getAvailability());
    }

    @Override
    public void onAdminDataChanged(HeadModel head) {

    }

    @Override
    public void onDataRemoved(HeadModel head) {

    }

    @Override
    public void onDataRemoved(OfferModel offer) {
        //Save head key before destroy the head..
        String headKey = offer.getKey();
        offer.setKey(null);
        mRef.child(OFFER_DB).child(headKey).setValue(offer);
    }
}

