package com.pujolsluis.android.hangeo;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.multidex.MultiDex;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;

import java.util.ArrayList;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, PlaceSelectionListener {


    private DrawerLayout mDrawerLayout;
    Context context = this;

    //Identifier for the Location Request
    private static final int REQUEST_LOCATION = 0;
    // Google Map Object
    private GoogleMap mGoogleMap;
    //Map Ready Indicator
    private boolean mapReady = false;
    //View to get the main activity layout and use it in the permission method
    private View mLayout;
    //Camera Position
    private CameraPosition USER_LOCATION;
    //Google API Cliente to retrieve user location data
    private GoogleApiClient mGoogleApiClient;
    //User Last Location
    private Location mLastLocation;
    private LatLng mLastLocationLatLng;
    //FirstOnStart
    private Boolean FirstonStart = true;
    //Last Marker on long click
    private Marker mLastMarker;
    //PolyLine Between created markers
    private Polyline mPolyLine;

    private Marker mLastPlaceMarker;

    private ArrayList<Marker> mLastMarkerslist = new ArrayList<>();
    private ArrayList<LatLng> mPolyLinePointList = new ArrayList<>();

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);
        MultiDex.install(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

/*  Old Toolbar for the layout [SOON TO BE REMOVED]
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setHomeAsUpIndicator(R.drawable.ic_menu);
            ab.setDisplayHomeAsUpEnabled(true);
        }
*/
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout_map);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view_map);
        if (navigationView != null) {
            setupDrawerContent(navigationView);
            Log.v("MapsActivity", "Hi what chu doing");
        }

        navigationView.setCheckedItem(R.id.nav_home);

        //CODE FOR MAP STARTS HERE

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //Get Map Activity Layout
        mLayout = findViewById(R.id.map_main_content);

        //Set home button OnClickListener for the map Place Fragment
        Button homeMenuButton = (Button) findViewById(R.id.home_menu_map_button);
        homeMenuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDrawerLayout.openDrawer(GravityCompat.START);

            }
        });

        // Retrieve the PlaceAutocompleteFragment.
        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        // Register a listener to receive callbacks when a place has been selected or an error has
        // occurred.
        autocompleteFragment.setOnPlaceSelectedListener(this);

        //Initialize USER_LOCATION
        mLastLocationLatLng = new LatLng(18.6976745, -71.2865409);
        USER_LOCATION = CameraPosition.builder()
                .target(mLastLocationLatLng)
                .zoom((float) 15)
                .bearing(0)
                .build();



    }


    //This Method gets called when the map is ready to execute
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sevendips = new LatLng(18.487438, -69.961872);
        mGoogleMap.addMarker(new MarkerOptions().position(sevendips).title("Marker in sevendips"));
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(sevendips));
        mGoogleMap.setMinZoomPreference((float)16.5);
    }

    private void setupDrawerContent(final NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        switch (menuItem.getItemId()){
                            case R.id.nav_plans:
                                Intent intent = new Intent(context, MainActivity.class);
                                mDrawerLayout.closeDrawer(GravityCompat.START);
                                startActivity(intent);
                        }
                        return true;
                    }
                });
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
            case R.id.nav_plans:
                Intent intent = new Intent(context, MainActivity.class);
                startActivity(intent);
                Log.v("MapsActivity", "Hi what chu doing, Stop pressing me ");
            case R.string.menu_settings:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    //Method to get Project resources
    public Resources getResources() {
        return super.getResources();
    }

    @Override
    public void onPlaceSelected(Place place) {
        LatLng target = place.getLatLng();
        MarkerOptions selectedPlaceMarker = new MarkerOptions()
                .position(target)
                .title(place.getName().toString() + " " + place.getPriceLevel());
        Marker placeMarker = mGoogleMap.addMarker(selectedPlaceMarker);
        mLastMarkerslist.add(placeMarker);
        mLastPlaceMarker = placeMarker;
//        mPolyLinePointList.add(placeMarker.getPosition());
//        mPolyLine.setPoints(mPolyLinePointList);
        CameraPosition newPosition = CameraPosition.builder()
                .target(target)
                .zoom(17)
                .bearing(0)
                .build();
        flyTo(newPosition);

    }

    @Override
    public void onError(Status status) {
        return;
    }

    //This Method moves the camera to the position indicated with an animation
    public void flyTo(CameraPosition cameraPosition) {
        mGoogleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }
}
