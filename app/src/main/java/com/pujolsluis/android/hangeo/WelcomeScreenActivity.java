package com.pujolsluis.android.hangeo;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;


public class WelcomeScreenActivity extends AppCompatActivity {

    private Button bSignUp;
    private Button bLogin;

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
                startActivity(intent);
            }

        });


        bLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent intent = new Intent(WelcomeScreenActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
    }
}
