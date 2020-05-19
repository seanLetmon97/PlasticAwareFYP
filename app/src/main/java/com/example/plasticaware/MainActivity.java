package com.example.plasticaware;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.plasticaware.abstracts.Toolbar_drawer;
import com.example.plasticaware.fragments.AboutFragment;
import com.example.plasticaware.fragments.BarcodeScanerFragment;
import com.example.plasticaware.fragments.CartFragment;
import com.example.plasticaware.fragments.ProductListFragment;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, Toolbar_drawer {
    private DrawerLayout drawer;
    private static final String KEY_TITLE = "title";
    private  NavigationView navigationView;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth firebaseAuth;
    private ActionBarDrawerToggle toggle;
    private Toolbar toolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        navigationView.setNavigationItemSelectedListener(this);


        firebaseAuth = FirebaseAuth.getInstance();
        setUp();
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new BarcodeScanerFragment(),"barcode").commit();
            navigationView.setCheckedItem(R.id.nav_barcode);

        }

    }

    public void setDrawerEnabled(boolean enabled) {
        int lockMode = enabled ? DrawerLayout.LOCK_MODE_UNLOCKED :
                DrawerLayout.LOCK_MODE_LOCKED_CLOSED;
        drawer.setDrawerLockMode(lockMode);
        //toggle.setDrawerIndicatorEnabled(enabled);
    }

    @Override
    public void setAction(boolean flag ) {

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(flag){
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //What to do on back clicked
                    onBackPressed();
                }
            });

        } else {
            toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                    R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawer.addDrawerListener(toggle);
            toggle.syncState();
        }



    }

    @Override
    public void setTitle(String Title) {
        getSupportActionBar().setTitle(Title);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case R.id.nav_barcode:

                if(!navigationView.getMenu().findItem(R.id.nav_barcode).isChecked()) {
                    this.getSupportFragmentManager().popBackStack();
                    getSupportFragmentManager().beginTransaction().addToBackStack(null).replace(R.id.fragment_container,
                            new BarcodeScanerFragment(),"barcode").commit();
                }
                break;
            case R.id.nav_list:
                if(!navigationView.getMenu().findItem(R.id.nav_list).isChecked()) {
                    this.getSupportFragmentManager().popBackStack();
                    getSupportFragmentManager().beginTransaction().addToBackStack(null).replace(R.id.fragment_container,
                            new ProductListFragment(),"list").commit();
        }
                break;
            case R.id.nav_Cart:
                if(!navigationView.getMenu().findItem(R.id.nav_Cart).isChecked()) {
                    this.getSupportFragmentManager().popBackStack();
                    getSupportFragmentManager().beginTransaction().addToBackStack(null).replace(R.id.fragment_container,
                        new CartFragment(),"cart").commit();
                }
                break;

            case R.id.nav_info:
                FragmentTransaction ft =  this.getSupportFragmentManager().beginTransaction();
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                Fragment UnknownProduct= new AboutFragment();
                ft.replace(R.id.fragment_container, UnknownProduct);
                ft.addToBackStack(null);
                ft.commit();
                break;

            case R.id.nav_exit:
                logout();
                break;
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void setUp() {

        NavigationView navigationView = findViewById(R.id.nav_view);
        if(firebaseAuth.getCurrentUser()!=null){
            View mHeaderView = navigationView.getHeaderView(0);
            ImageView ImageView = mHeaderView.findViewById(R.id.nav_Image);
            TextView textViewUsername = mHeaderView.findViewById(R.id.nav_username);
            TextView textViewEmail =  mHeaderView.findViewById(R.id.nav_email);
            Picasso.get().load(firebaseAuth.getCurrentUser().getPhotoUrl()).into(ImageView);//picasso libiary
            textViewUsername.setText(firebaseAuth.getCurrentUser().getDisplayName());
            textViewEmail.setText(firebaseAuth.getCurrentUser().getEmail());

            CreateUserDocument();
            //CreateUserCart();
        }



    }

    private void CreateUserDocument(){
        String title = "Initial Doc Creation";


        Map<String, Object> note = new HashMap<>();
        note.put(KEY_TITLE, title);

        db.collection("UserData").document(firebaseAuth.getUid()).set(note, SetOptions.merge())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, "Error creating profile!", Toast.LENGTH_SHORT).show();

                    }
                });

    }



    @Override
    public void onBackPressed() {

        //FragmentManager fm = getFragmentManager();
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if(getSupportFragmentManager().getBackStackEntryCount()==0){
                Intent a = new Intent(Intent.ACTION_MAIN);
                a.addCategory(Intent.CATEGORY_HOME);
                a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(a);
            }
                super.onBackPressed();
            }
    }

public void logout(){
    FirebaseAuth.getInstance().signOut();
    GoogleSignIn.getClient(this,new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()).
            signOut().addOnSuccessListener(new OnSuccessListener<Void>() {
        @Override
        public void onSuccess(Void aVoid) {
            startActivity(new Intent(MainActivity.this,Login.class));
            finish();
        }
    }).addOnFailureListener(new OnFailureListener() {
        @Override
        public void onFailure(@NonNull Exception e) {
            Toast.makeText(getApplicationContext(), "Signout Failed", Toast.LENGTH_SHORT).show();
        }
    });
}
    public boolean haveNetworkConnection() {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;

        ConnectivityManager cm = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        assert cm != null;
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork != null) {
            // connected to the internet
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                // connected to wifi
                haveConnectedWifi = true;
            } else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                // connected to mobile data
                haveConnectedMobile = true;
            }
        }
        return haveConnectedWifi || haveConnectedMobile;
    }

}
