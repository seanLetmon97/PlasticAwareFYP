package com.example.plasticaware;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

import java.io.IOException;

public class Login extends AppCompatActivity {

    public static final int GOOGLE_SIGN_IN_CODE = 10005;
    SignInButton signIn;
   GoogleSignInOptions gso;
   GoogleSignInClient signInClient;
   FirebaseAuth firebaseAuth;
    private static final int REQUEST_CAMERA_PERMISSION = 201;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        signIn= findViewById(R.id.signIn);
        firebaseAuth = FirebaseAuth.getInstance();

        if (!(ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)) {
           // Toast.makeText(this, "You must accept this permission in order to use the barcode scanner!!!", Toast.LENGTH_LONG).show();
            ActivityCompat.requestPermissions(this, new
                    String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);


        }

        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).
                        requestIdToken("672424599325-cte5fmeiqqc7q4i6clen16ubn691vgr3.apps.googleusercontent.com").
                        requestEmail().
                        build();

                signInClient = GoogleSignIn.getClient(this, gso);

                GoogleSignInAccount signInAccount = GoogleSignIn.getLastSignedInAccount(this);

                if (signInAccount != null || firebaseAuth.getCurrentUser() != null) {
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                }

                signIn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(haveNetworkConnection()) {
                            Intent sign = signInClient.getSignInIntent();
                            startActivityForResult(sign, GOOGLE_SIGN_IN_CODE);
                        } else {
                            Toast.makeText(getApplicationContext(),"No internet connection is detected, you must be connected to the internet to sign in!!!",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GOOGLE_SIGN_IN_CODE && haveNetworkConnection()){


            try {
                Task<GoogleSignInAccount> signInTask = GoogleSignIn.getSignedInAccountFromIntent(data);

                GoogleSignInAccount signInAcc = signInTask.getResult(ApiException.class);

                AuthCredential authCredential = GoogleAuthProvider.getCredential(signInAcc.getIdToken(),null);
                firebaseAuth.signInWithCredential(authCredential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        GoogleSignInAccount signInAccount = GoogleSignIn.getLastSignedInAccount(getApplicationContext());
                        if(signInAccount!=null && haveNetworkConnection() ) {

                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            Toast.makeText(getApplicationContext(), "Your Google Account is Connected to our Application.", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Toast.makeText(getApplicationContext(),"Something went wrong, please try again",Toast.LENGTH_SHORT).show();
                    }
                });

                //startActivity(new Intent(this,MainActivity.class));
                //Toast.makeText(this,"Your Google Account is Connected to our Application.",Toast.LENGTH_SHORT).show();
            } catch (ApiException e) {

                Toast.makeText(getApplicationContext(),"Something went wrong, please try again",Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getApplicationContext(),"No internet connection is detected, you must be connected to the internet to sign in!!!",Toast.LENGTH_SHORT).show();
        }
    }

    private boolean haveNetworkConnection() {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;

        ConnectivityManager cm = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
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
