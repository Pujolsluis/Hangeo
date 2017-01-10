package com.pujolsluis.android.hangeo;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;


public class WelcomeScreenActivity extends AppCompatActivity {

    Button bSignUp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome_screen_layout);
        addListenerOnSignUpButton();
    }

    public void addListenerOnSignUpButton() {

        final Context context = this;

        bSignUp = (Button) findViewById(R.id.button_signUp);

        bSignUp.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {

                Intent intent = new Intent(context, SignUpActivity.class);
                startActivity(intent);
                finish();
            }

        });

    }
}
