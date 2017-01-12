package com.pujolsluis.android.hangeo;

import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignUpActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText inputEmail, inputPassword ;
    private Button buttonSignUp, buttonForgotPassword;
    private ProgressDialog progressDialog;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        //Get instance for firebase authentification
        firebaseAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
//                if (user != null) {
//                    // User is signed in
//                    Toast.makeText(SignUpActivity.this, "onAuthStateChanged:signed_in", Toast.LENGTH_SHORT).show();
//                } else {
//                    // User is signed out
//                    Toast.makeText(SignUpActivity.this, "onAuthStateChanged:signed_out", Toast.LENGTH_SHORT).show();
//                }
                // ...
            }
        };

        buttonSignUp = (Button) findViewById(R.id.button_SignUpLayout_SignUp);
        buttonForgotPassword = (Button) findViewById(R.id.btn_reset_password); 
        
        inputEmail = (EditText) findViewById(R.id.editText_SignUpLayout_emailInput);
        inputPassword = (EditText) findViewById(R.id.password);
        progressDialog = new ProgressDialog(this);
        
        buttonSignUp.setOnClickListener(this);
        buttonForgotPassword.setOnClickListener(this);
        
    }

    @Override
    public void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            firebaseAuth.removeAuthStateListener(mAuthListener);
        }
    }

    @Override
    public void onClick(View v) {
        if (v == buttonSignUp){
            registerUser();
        }
        
        if (v == buttonForgotPassword){
            goToForgotPassword();
        }

        /*if (v == buttonLogin){
            goToLogin();
        }*/
    }

    private void goToForgotPassword() {
            startActivity(new Intent(SignUpActivity.this, ForgotPassword.class));
    }

   /* private void goToLogin() {
        startActivity(new Intent(SignUpActivity.this, ForgotPassword.class));
    }*/



    private void registerUser() {
        String email = inputEmail.getText().toString().trim();
        String password = inputPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(getApplicationContext(), "Enter email address!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            Toast.makeText(getApplicationContext(), "Enter password!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(getApplicationContext(), "Password too short, enter minimum 6 characters!", Toast.LENGTH_SHORT).show();
            return;
        }

        firebaseAuth.createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            Toast.makeText(SignUpActivity.this, "Registered successfully!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(SignUpActivity.this, "Could not register, please try again!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
