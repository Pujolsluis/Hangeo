package com.pujolsluis.android.hangeo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class SignUpActivity extends AppCompatActivity {

    private EditText inputUsername, inputEmail, inputPassword ;
    private Button buttonSignUp;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        //Get instance for firebase authentification
        auth = FirebaseAuth.getInstance();

        buttonSignUp = (Button) findViewById(R.id.button_SignUpLayout_SignUp);
        inputUsername = (EditText) findViewById(R.id.editText_SignUpLayout_usernameInput);
        inputEmail = (EditText) findViewById(R.id.editText_SignUpLayout_emailInput);
        inputPassword = (EditText) findViewById(R.id.editText_SignUpLayout_passwordInput);

        buttonSignUp.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                String username = inputUsername.getText().toString().trim();
                String password = inputPassword.getText().toString().trim();
                String email = inputEmail.getText().toString().trim();

                if(TextUtils.isEmpty(username)){
                    Toast.makeText(getApplicationContext(), "Please enter a valid username", Toast.LENGTH_SHORT).show();
                }

                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(getApplicationContext(), "Please enter a valid email", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(getApplicationContext(), "Please enter a valid password", Toast.LENGTH_SHORT).show();
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);
            }
        });
    }
}
