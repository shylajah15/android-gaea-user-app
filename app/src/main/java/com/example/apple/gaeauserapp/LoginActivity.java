package com.example.apple.gaeauserapp;

import android.content.Context;
import android.content.Intent;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.poovam.pinedittextfield.LinePinField;
import com.poovam.pinedittextfield.PinField;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    private Button continueButton,resetButton;
    private FirebaseAuth mAuth;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallBacks;

    private View phone_number_layout,verify_code_layout,phone_view_layout,verify_view_layout,initial_view_layout;
    private EditText phone_number_field,verify_code_field;
    private TextView phone_number_text,verify_code_text,enter_mobile_number;
    private String verificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private boolean mVerificationInProgress = false;

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String KEY_VERIFY_IN_PROGRESS = "key_verify_in_progress";

    private static final int STATE_INITIALIZED = 1;
    private static final int STATE_CODE_SENT = 2;
    private static final int STATE_VERIFY_FAILED = 3;
    private static final int STATE_VERIFY_SUCCESS = 4;
    private static final int STATE_SIGNIN_FAILED = 5;
    private static final int STATE_SIGNIN_SUCCESS = 6;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if(savedInstanceState != null){
            onRestoreInstanceState(savedInstanceState);
        }

        mAuth = FirebaseAuth.getInstance();

        phone_number_field = findViewById(R.id.phone_number);
//        verify_code_field = findViewById(R.id.verify_code_number);
        initial_view_layout = findViewById(R.id.initialPhoneView);
        phone_number_layout = findViewById(R.id.phone_text_layout);
        verify_code_layout = findViewById(R.id.verify_text_layout);

        phone_view_layout = findViewById(R.id.phoneGroupView);
        verify_view_layout = findViewById(R.id.verifyGroupView);

        enter_mobile_number = findViewById(R.id.enter_mobile_number);
        phone_number_text = findViewById(R.id.phone_number_text);
        verify_code_text = findViewById(R.id.verify_code_text);

        continueButton = findViewById(R.id.send_code);
        resetButton = findViewById(R.id.buttonResend);


        final LinePinField linePinField = findViewById(R.id.lineField);

        linePinField.isFocused();
        linePinField.setOnTextCompleteListener(new PinField.OnTextCompleteListener() {
            @Override
            public boolean onTextComplete (@NotNull String enteredText) {
//                Toast.makeText(MainActivity.this,enteredText,Toast.LENGTH_SHORT).show();
                verifyPhoneNumberWithCode(verificationId,enteredText);
                return true; // Return true to keep the keyboard open else return false to close the keyboard
            }
        });

        continueButton.setOnClickListener(this);
        resetButton.setOnClickListener(this);
        enter_mobile_number.setOnClickListener(this);

        enableViews(initial_view_layout);

    }

    @Override
    protected void onStart() {
        super.onStart();
        initiateCallBack();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            startSocialMediaIntent();
        }
        updateUI(currentUser);

        if(mVerificationInProgress && validatePhoneNumber()){
            startPhoneNumberVerification("+91" +phone_number_field.getText().toString());
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        outState.putBoolean(KEY_VERIFY_IN_PROGRESS,mVerificationInProgress);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mVerificationInProgress = savedInstanceState.getBoolean(KEY_VERIFY_IN_PROGRESS);
    }

    private void initiateCallBack() {
        mCallBacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                Log.d(TAG,"onVerificationCompleted:" + phoneAuthCredential);
                mVerificationInProgress = false;
                updateUI(STATE_VERIFY_SUCCESS,phoneAuthCredential);

            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                Log.w(TAG, "onVerificationFailed", e);
                Toast.makeText(getApplicationContext(), "failed "+e.getMessage(), Toast.LENGTH_SHORT).show();
                mVerificationInProgress = false;
                if(e instanceof FirebaseAuthInvalidCredentialsException){
                    phone_number_field.setError("Invalid Phone number");
                }
                else if(e instanceof FirebaseTooManyRequestsException){
//                    Snackbar.make(findViewById(android.R.id.content), "Quota exceeded.",
//                            Snackbar.LENGTH_SHORT).show();
                }

                updateUI(STATE_VERIFY_FAILED);
            }

            @Override
            public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken token) {
                super.onCodeSent(s, token);
                Log.d(TAG,"on code sent"+ s);
                verificationId = s;
                mResendToken = token;
//                resend TODO
                updateUI(STATE_CODE_SENT);
            }
        };
    }

    private void startPhoneNumberVerification(String phoneNumber) {
        Toast.makeText(this, "OTP has been sent to the Mobile Number", Toast.LENGTH_SHORT).show();
        PhoneAuthProvider.getInstance().verifyPhoneNumber(phoneNumber,60, TimeUnit.SECONDS,this,mCallBacks);
        mVerificationInProgress = true;
    }


    private void verifyPhoneNumberWithCode(String verificationId, String code){
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId,code);
        signInWithPhoneCredential(credential);
    }

    private void signInWithPhoneCredential(PhoneAuthCredential credential){
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = task.getResult().getUser();
                            updateUI(STATE_SIGNIN_SUCCESS,user);
                        }
                        else {
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            if(task.getException() instanceof FirebaseAuthInvalidCredentialsException){
//                                verify_code_field.setError("Invalid code");
                            }

                            updateUI(STATE_SIGNIN_FAILED);
                        }
                    }
                });
    }

    private void resendVerificationCode(String phoneNumber,PhoneAuthProvider.ForceResendingToken token){
        Toast.makeText(this, "Resending OTP...", Toast.LENGTH_SHORT).show();
        PhoneAuthProvider.getInstance().verifyPhoneNumber(phoneNumber,60,TimeUnit.SECONDS,this,mCallBacks,token);
    }

    private boolean validatePhoneNumber(){
        String phoneNumber = phone_number_field.getText().toString();

        if(TextUtils.isEmpty(phoneNumber)){
            phone_number_field.setError("Invalid phone number");
            return false;
        }
        return true;
    }
    private void updateUI(int uiState){
        updateUI(uiState,mAuth.getCurrentUser(),null);
    }

    private void updateUI(FirebaseUser user){
        if(user != null){
            updateUI(STATE_SIGNIN_SUCCESS,user);
        }
        else{
            updateUI(STATE_INITIALIZED);
        }
    }

    private void updateUI(int uiState,FirebaseUser user){
        updateUI(uiState,user,null);
    }

    private void updateUI(int uiState,PhoneAuthCredential cred){
        updateUI(uiState,null,cred);
    }

    private void updateUI(int uiState,FirebaseUser user,PhoneAuthCredential cred){
        switch (uiState){
//            initial state
            case STATE_INITIALIZED:
                enableViews(phone_view_layout);
                disableViews(verify_view_layout);
                break;
            case STATE_CODE_SENT:
                enableViews(verify_view_layout);
                disableViews(phone_view_layout);
                break;
            case STATE_VERIFY_SUCCESS:
                disableViews(verify_view_layout,phone_view_layout);
                break;
            case STATE_SIGNIN_FAILED:
                Toast.makeText(this, "Please enter a valid OTP", Toast.LENGTH_SHORT).show();
                break;
            case STATE_SIGNIN_SUCCESS:
//                Toast.makeText(this, "sigin success", Toast.LENGTH_SHORT).show();
                startSocialMediaIntent();
                break;
        }
    }

    private void startSocialMediaIntent() {
        Intent intent = new Intent(this,SocialMediaActivity.class);
        startActivity(intent);
    }

    private void enableViews(View... views){
        for (View v : views){
            Log.d(TAG,"views"+ v);
            v.setVisibility(View.VISIBLE);
        }
    }

    private void disableViews(View... views){
        for (View v : views){
            v.setVisibility(View.GONE);
        }
    }

    private void signOut(){
        FirebaseAuth.getInstance().signOut();
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id){
            case R.id.enter_mobile_number:
                enableViews(phone_number_layout);
                disableViews(initial_view_layout);
//                initiateEditFieldRequest();
                break;

            case R.id.send_code:
                if(!validatePhoneNumber()){
                    return;
                }
                startPhoneNumberVerification("+91" + phone_number_field.getText().toString());
                break;
            case R.id.buttonResend:
                resendVerificationCode("+91" + phone_number_field.getText().toString(), mResendToken);
                break;
        }
    }

    private void initiateEditFieldRequest() {

        phone_number_field.requestFocus();
        if(phone_number_field.requestFocus()) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(phone_number_field, InputMethodManager.SHOW_IMPLICIT);
        }

    }

}
