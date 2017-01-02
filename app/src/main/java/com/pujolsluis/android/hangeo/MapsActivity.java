package com.pujolsluis.android.hangeo;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.multidex.MultiDex;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

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
import com.google.android.gms.maps.model.PolylineOptions;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.util.ArrayList;
import java.util.HashMap;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, PlaceSelectionListener {

    static final String LOG_TAG = MapsActivity.class.getSimpleName();

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

    private HashMap<Marker, Integer> mSelectedMarkersMap = new HashMap<>();
    private ArrayList<Marker> mSelectedMarkerslist = new ArrayList<>();
    private ArrayList<LatLng> mPolyLineSelectedPointList = new ArrayList<>();

    //Planel Layout
    private SlidingUpPanelLayout mSlidingPanelLayout;

    //Dialogs to ADD and DELETE Locations
    private Dialog addAlertDialog;
    private Dialog deleteAlertDialog;

    //Method to allow multidexing in our app, making it compatible with android versions <4.4
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);
        MultiDex.install(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

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


        //Initialize Google Places Search Box Fragment
        initializeGoogleMapsPlacesFragment();

        //Initialize USER_LOCATION
        mLastLocationLatLng = new LatLng(18.6976745, -71.2865409);
        USER_LOCATION = CameraPosition.builder()
                .target(mLastLocationLatLng)
                .zoom((float) 15)
                .bearing(0)
                .build();

        //Initialize Sliding Panel
        initializeSlidingPanel();

        final ImageButton addPlaceButton = (ImageButton) findViewById(R.id.add_location_to_plan);

        addPlaceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mLastMarker != null && !mSelectedMarkersMap.containsKey(mLastMarker)){
                    addLocationWithDialog();
                }else if(mLastMarker != null && mSelectedMarkersMap.containsKey(mLastMarker)){
                    deleteLocationWithDialog();
                }
            }
        });


    }


    //This Method gets called when the map is ready to execute
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

        //Initializing the Polyline
        mPolyLine = mGoogleMap.addPolyline(new PolylineOptions());
        mPolyLine.setColor(Color.BLUE);

        //Set Map Click listeners
        setmapClickListeners();


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

    //Method that gets called when a place is selected from the search box
    @Override
    public void onPlaceSelected(Place place) {

        //Verify if last marker that needs to be removed from map because it was not selected
        if(mLastMarker != null && !mSelectedMarkersMap.containsKey(mLastMarker)){
            mLastMarker.remove();
        }

        LatLng target = place.getLatLng();
        MarkerOptions selectedPlaceMarker = new MarkerOptions()
                .position(target)
                .title(place.getName().toString());
        Marker placeMarker = mGoogleMap.addMarker(selectedPlaceMarker);
       // mSelectedMarkerslist.add(placeMarker);
        mLastMarker = placeMarker;
//        mPolyLinePointList.add(placeMarker.getPosition());
//        mPolyLine.setPoints(mPolyLinePointList);
        CameraPosition newPosition = CameraPosition.builder()
                .target(target)
                .zoom(17)
                .bearing(0)
                .build();
        flyTo(newPosition);
        updatePanelHeader(mLastMarker);

    }

    //Method that gets called when an error occurs with a place selected from the search box
    @Override
    public void onError(Status status) {
        return;
    }

    //This Method moves the camera to the position indicated with an animation
    public void flyTo(CameraPosition cameraPosition) {
        mGoogleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    //This method initializes trhe google places fragment
    private void initializeGoogleMapsPlacesFragment(){
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
    }

    //This method initializes the sliding panel for the layout
    private void initializeSlidingPanel(){

        //Find Sliding Panel Root Layout element
        mSlidingPanelLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
        //Setting Panel with a anchor point in the middle of screen
        mSlidingPanelLayout.setAnchorPoint(0.7f);
        mSlidingPanelLayout.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
                Log.i(LOG_TAG, "onPanelSlide, offset " + slideOffset);
            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
                Log.i(LOG_TAG, "onPanelStateChanged " + newState);
            }
        });
        mSlidingPanelLayout.setFadeOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSlidingPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
            }
        });

    }

    //This method updates the panel header text view
    private void updatePanelHeader(Marker marker){

        if (mSlidingPanelLayout != null && mSlidingPanelLayout.getPanelState() == SlidingUpPanelLayout.PanelState.HIDDEN)
            mSlidingPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        TextView panelHeaderTextView = (TextView) findViewById(R.id.panelHeader_TextView_dragView);

        panelHeaderTextView.setText(marker.getTitle());

    }

    //This method sets the google maps click listeners
    private void setmapClickListeners(){

        //On Long Click Listener for map to set temporal marker
        mGoogleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng point) {
                //Verify if last marker that needs to be removed from map because it was not selected
                if(mLastMarker != null && !mSelectedMarkersMap.containsKey(mLastMarker)){
                    mLastMarker.remove();
                }
                Log.d(LOG_TAG, "Hello you did a long click on the map");
                MarkerOptions tempMarker = new MarkerOptions().position(point).title(getResources().getString(R.string.dropped_pin_text));
                mLastMarker = mGoogleMap.addMarker(tempMarker);
//                mLastMarkerslist.add(mLastMarker);
//                mPolyLinePointList.add(mLastMarker.getPosition());
//                mPolyLine.setPoints(mPolyLinePointList);
                // updateCamera();
            }
        });

        //On Marker Click Listener
        mGoogleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
//                marker.remove();
                marker.showInfoWindow();
                updatePanelHeader(mLastMarker);
                mLastMarker = marker;
                return true;
            }
        });

        //On Map Click Listener
        mGoogleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if (mSlidingPanelLayout != null && (mSlidingPanelLayout.getPanelState() != SlidingUpPanelLayout.PanelState.HIDDEN)){

                    mSlidingPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);

                }
            }
        });
    }

    //Adding a location to the map that contains all locations for plan
    private void addLocationWithDialog(){
        AlertDialog.Builder builderForAddingLocationDialog = new AlertDialog.Builder(context);
        builderForAddingLocationDialog.setTitle("Add place?");
        builderForAddingLocationDialog.setMessage("Are you sure you wish to add this place to your plan?");
        builderForAddingLocationDialog.setCancelable(true);

        builderForAddingLocationDialog.setPositiveButton(
                "ADD",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //Adding marker to the map that contains all plan locations with its insertion order
                        mSelectedMarkersMap.put(mLastMarker, mSelectedMarkersMap.size() + 1 );
                        mSelectedMarkerslist.add(mLastMarker);
                        mPolyLineSelectedPointList.add(mLastMarker.getPosition());
                        updatePolylineOfLocations();
                        dialog.cancel();
                    }
                });

        builderForAddingLocationDialog.setNegativeButton(
                "Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        addAlertDialog = builderForAddingLocationDialog.create();
        addAlertDialog.show();
    }

    private void deleteLocationWithDialog(){
        AlertDialog.Builder builderForAddingLocationDialog = new AlertDialog.Builder(context);
        builderForAddingLocationDialog.setTitle("Delete this place?");
        builderForAddingLocationDialog.setMessage("Are you sure you wish to delete this place from your plan?");
        builderForAddingLocationDialog.setCancelable(true);

        builderForAddingLocationDialog.setPositiveButton(
                "Delete",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //Adding marker to the map that contains all plan locations with its insertion order
                        mSelectedMarkersMap.remove(mLastMarker);
                        mSelectedMarkerslist.remove(mLastMarker);
                        mPolyLineSelectedPointList.remove(mLastMarker.getPosition());
                        mLastMarker.remove();
                        updatePolylineOfLocations();
                        dialog.cancel();
                    }
                });

        builderForAddingLocationDialog.setNegativeButton(
                "Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        deleteAlertDialog = builderForAddingLocationDialog.create();
        deleteAlertDialog.show();
    }

    private void updatePolylineOfLocations(){
        mPolyLine.setPoints(mPolyLineSelectedPointList);
    }

    @Override
    public void onBackPressed() {
        if (mSlidingPanelLayout != null &&
                (mSlidingPanelLayout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED || mSlidingPanelLayout.getPanelState() == SlidingUpPanelLayout.PanelState.ANCHORED)) {
            mSlidingPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        } else {
            super.onBackPressed();
        }
    }
}
