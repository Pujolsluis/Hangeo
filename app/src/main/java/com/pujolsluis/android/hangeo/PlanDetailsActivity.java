package com.pujolsluis.android.hangeo;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

import static com.pujolsluis.android.hangeo.R.id.plan_details_map;

public class PlanDetailsActivity extends AppCompatActivity implements OnMapReadyCallback {

    public static final String EXTRA_NAME = "plan_name";
    // Google Map Object
    private GoogleMap mGoogleMap;
    //Map Ready Indicator
    private boolean mapReady = false;
    //View to get the main activity layout and use it in the permission method
    private View mLayout;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plan_details);

        Intent intent = getIntent();
        final String cheeseName = intent.getStringExtra(EXTRA_NAME);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        CollapsingToolbarLayout collapsingToolbar =
                (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        collapsingToolbar.setTitle(cheeseName);

        loadBackdrop();

        //CODE FOR MAP STARTS HERE

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(plan_details_map);
        mapFragment.getMapAsync(this);

        //Get Map Activity Layout
        mLayout = findViewById(R.id.main_content_plan_details);
    }

    private void loadBackdrop() {
        final ImageView imageView = (ImageView) findViewById(R.id.backdrop);
        Glide.with(this).load(Plan.getRandomCheeseDrawable()).centerCrop().into(imageView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.sample_actions, menu);
        return true;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sevendips = new LatLng(18.487438, -69.961872);
        CameraPosition cameraStartPosition = CameraPosition.builder()
                .target(sevendips)
                .zoom(15)
                .bearing(0)
                .build();
        flyTo(cameraStartPosition);
        //Setting the correct Padding for the map ui
        mGoogleMap.setPadding(16, 278, 16, 16);

    }


    //This Method moves the camera to the position indicated with an animation
    public void flyTo(CameraPosition cameraPosition) {
        mGoogleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }
}
