package com.example.apple.gaeauserapp;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Arrays;

public class SocialMediaActivity extends AppCompatActivity {

    private PrefManager prefManager;
    GoogleSignInClient mGoogleSignInClient;

    FirebaseAuth mAuth;

    private static final String TAG = "simplifiedcoding";

    private static final int RC_SIGN_IN = 123;

    private CallbackManager callbackManager;
    private LoginButton loginButton;
    private static final String EMAIL = "email";
    private boolean isSkipped = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_social_media);

        prefManager = new PrefManager(this);
        if(!prefManager.isFirstTimeSocialMediaLaunch()){
            launchHomeScreen();
            finish();
        }

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        SignInButton signInButton = findViewById(R.id.sign_in_button);

        TextView textView = (TextView) signInButton.getChildAt(0);
        textView.setText("Connect with Gmail");

        findViewById(R.id.sign_in_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                googleSignIn();
            }
        });

        findViewById(R.id.skip_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                skipAction();
            }
        });
    }

    private  void initiateFbLogin(){
        callbackManager = CallbackManager.Factory.create();

        loginButton = (LoginButton) findViewById(R.id.login_button);
        loginButton.setReadPermissions(Arrays.<String>asList(EMAIL));

//        callback registeration
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG,loginResult.toString());
//                Toast.makeText(getApplicationContext(), "Login success", Toast.LENGTH_SHORT).show();
                checkUserLogin();
            }

            @Override
            public void onCancel() {
                Toast.makeText(getApplicationContext(), "Login cancel", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException error) {
                Log.e(TAG,"Facebook error "+error.getMessage());
                Toast.makeText(getApplicationContext(), "Login error" + error.toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void launchHomeScreen() {
        Intent intent = new Intent(this,HomeActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkUserLogin();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkUserLogin();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);

        if(requestCode == RC_SIGN_IN){
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                FirebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                Toast.makeText(this,e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void FirebaseAuthWithGoogle(GoogleSignInAccount account) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId());
        String idToken = account.getIdToken();
        String name = account.getDisplayName();
        String email = account.getEmail();
//        Toast.makeText(getApplicationContext(), "User Signed In " + account.getDisplayName(), Toast.LENGTH_SHORT).show();
        checkUserLogin();
    }

    private void checkUserLogin(){
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
//        Log.d(TAG,"google "+ account.getIdToken());
        boolean isGoogleLoggedIn  = account != null && !account.isExpired();

        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        boolean isFbLoggedIn = accessToken != null && !accessToken.isExpired();

        if(isGoogleLoggedIn || isFbLoggedIn || isSkipped){
            Toast.makeText(this, "Home screen", Toast.LENGTH_SHORT).show();
            prefManager.setFirstTimeSocialMediaLaunch(true);
            Intent intent = new Intent(this,HomeActivity.class);
            startActivity(intent);
        }
        else{
            initiateFbLogin();
        }
    }
    private void googleSignIn() {

        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent,RC_SIGN_IN);
    }

    private void skipAction(){
        this.isSkipped = true;
        checkUserLogin();
    }
}
