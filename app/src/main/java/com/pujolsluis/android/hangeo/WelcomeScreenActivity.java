package com.pujolsluis.android.hangeo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;


public class WelcomeScreenActivity extends AppCompatActivity {

    private Button bSignUp;
    private Button bLogin;
    private static final int LOGIN_ACTIVITY_RESPONSE = 1;
    private static final int SIGNUP_ACTIVITY_RESPONSE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome_screen_layout);

        bSignUp = (Button) findViewById(R.id.button_signUp);
        bLogin = (Button) findViewById(R.id.welcome_button_login);

        bSignUp.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {

                Intent intent = new Intent(WelcomeScreenActivity.this, SignUpActivity.class);
                startActivityForResult(intent, SIGNUP_ACTIVITY_RESPONSE);
            }

        });


        bLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent intent = new Intent(WelcomeScreenActivity.this, LoginActivity.class);
                startActivityForResult(intent, LOGIN_ACTIVITY_RESPONSE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == RESULT_OK){
            Intent dataToReturn = new Intent();
            setResult(RESULT_OK, dataToReturn);
            finish();
        }
    }
}
