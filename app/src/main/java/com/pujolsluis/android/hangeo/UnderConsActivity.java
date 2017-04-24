package com.pujolsluis.android.hangeo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

/**
 * Created by Oficina on 19/04/2017.
 */

public class UnderConsActivity extends AppCompatActivity {

    private DrawerLayout mDrawerLayout;
    private FirebaseAuth mFirebaseAuth;
    private TextView mNavDrawerHeaderFirstNameLastName;
    Context context = this;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.under_construction_layout);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout_map);

        //Initializing the Toolbar for the UnderCons
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final ActionBar ab = getSupportActionBar();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        NavigationView mNavigationView = (NavigationView) findViewById(R.id.nav_view);
        if (mNavigationView != null) {
            setupDrawerContent(mNavigationView);
        }
        mNavDrawerHeaderFirstNameLastName = (TextView) mNavigationView.getHeaderView(0).findViewById(R.id.nav_header_user_firstName_lastName);
    }

    //Setting up the drawer content of the navigation view
    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        switch (menuItem.getItemId()){
                            case R.id.nav_home:
                                Intent intent = new Intent(context, UnderConsActivity.class);
                                mDrawerLayout.closeDrawer(GravityCompat.START);
                                context.startActivity(intent);
                                return true;
                            case R.id.nav_sign_out:
                                if(mFirebaseAuth != null){
                                    //sign out
                                    mFirebaseAuth.signOut();
                                    mDrawerLayout.closeDrawer(GravityCompat.START);
                                }
                                return true;

                        }
                        return true;
                    }
                });
    }
}
