package com.pujolsluis.android.hangeo;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String LOG_TAG = SignUpActivity.class.getSimpleName();
    private EditText mFirstName, mLastName, mInputEmail, mInputPassword;
    private Button buttonSignUp, buttonForgotPassword, buttonLogin;
    private ProgressDialog progressDialog;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mProfileDatabaseReference;
    private Context context = this;
    private static final int LOGIN_ACTIVITY_RESPONSE = 1;
    private static final int FORGOT_PASSWORD_ACTIVITY_RESPONSE = 2;

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

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mProfileDatabaseReference = mFirebaseDatabase.getReference().child("userProfiles");

        buttonSignUp = (Button) findViewById(R.id.button_SignUpLayout_SignUp);
        buttonForgotPassword = (Button) findViewById(R.id.btn_reset_password);
        buttonLogin = (Button) findViewById(R.id.button_goToLogin);

        mFirstName = (EditText) findViewById(R.id.editText_user_first_name);
        mLastName = (EditText) findViewById(R.id.editText_user_last_name);
        mInputEmail = (EditText) findViewById(R.id.editText_SignUpLayout_emailInput);
        mInputPassword = (EditText) findViewById(R.id.password);
        progressDialog = new ProgressDialog(this);
        
        buttonSignUp.setOnClickListener(this);
        buttonForgotPassword.setOnClickListener(this);
        buttonLogin.setOnClickListener(this);
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

        if (v == buttonLogin){
            goToLogin();
        }
    }

    private void goToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivityForResult(intent, LOGIN_ACTIVITY_RESPONSE);
    }

    private void goToForgotPassword() {
            startActivity(new Intent(SignUpActivity.this, ForgotPassword.class));
    }



    private void registerUser() {
        final String firstName = mFirstName.getText().toString();
        final String lastName = mLastName.getText().toString();
        String email = mInputEmail.getText().toString().trim();
        String password = mInputPassword.getText().toString().trim();



        if(TextUtils.isEmpty(mFirstName.getText())){
            mFirstName.setError("Enter your name");
            return;
        }

        if(TextUtils.isEmpty(mLastName.getText())){
            mFirstName.setError("Enter your last name");
            return;
        }

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
                            Toast.makeText(context, "Registered successfully!", Toast.LENGTH_SHORT).show();
                            final String mUserID = firebaseAuth.getCurrentUser().getUid();

                            mProfileDatabaseReference.child(mUserID).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    // Get user information
                                    UserProfile tempProfile = dataSnapshot.getValue(UserProfile.class);
                                    if(tempProfile == null) {

                                        tempProfile = new UserProfile();
                                        tempProfile.setmFirstName(firstName);
                                        tempProfile.setmLastName(lastName);


                                        Map<String, Object> profileValues = tempProfile.toMap();

                                        Map<String, Object> childUpdates = new HashMap<>();
                                        childUpdates.put(mUserID, profileValues);
                                        mProfileDatabaseReference.updateChildren(childUpdates);

                                    }else{
                                        Toast.makeText(context, "Profile is already created", Toast.LENGTH_SHORT).show();
                                    }

                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    Log.e(LOG_TAG, databaseError.toString());
                                }
                            });

                            Intent dataToReturn = new Intent();
                            setResult(RESULT_OK, dataToReturn);
                            finish();
                        } else {
                            Toast.makeText(SignUpActivity.this, "Could not register, please try again!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });



    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == LOGIN_ACTIVITY_RESPONSE){
            if(resultCode == RESULT_OK){
                Intent dataToReturn = new Intent();
                setResult(RESULT_OK, dataToReturn);
                finish();
            }
        }else if(requestCode == FORGOT_PASSWORD_ACTIVITY_RESPONSE){
            if(resultCode == RESULT_OK){
                goToLogin();
            }
        }
    }
}
