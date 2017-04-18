package com.pujolsluis.android.hangeo;

import android.Manifest;
import android.app.Dialog;
import android.app.LoaderManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.multidex.MultiDex;
import android.support.v4.app.ActivityCompat;
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

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.PolyUtil;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.pujolsluis.android.hangeo.R.id.map;

/**
 * Created by Oficina on 17/04/2017.
 */

public class NearbyPlacesMapsActivity  extends AppCompatActivity implements OnMapReadyCallback, PlaceSelectionListener,
        ActivityCompat.OnRequestPermissionsResultCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        LoaderManager.LoaderCallbacks<List<Route>> {
    static final String LOG_TAG = NearbyPlacesMapsActivity.class.getSimpleName();

    private DrawerLayout mDrawerLayout;
    Context context = this;


    // Google Map Object
    private GoogleMap mGoogleMap;
    //Map Ready Indicator
    private boolean mapReady = false;
    //View to get the main activity layout and use it in the permission method
    private View mLayout;
    //Google API Cliente to retrieve user location data
    private GoogleApiClient mGoogleApiClient;
    //First OnStart of the activity
    private Boolean mFirstOnStart = true;
    //Last Marker on long click
    private Marker mLastMarker;
    //PolyLine Between created markers
    private Polyline mPolyLine;

    private Marker mLastPlaceMarker;

    //Collections of markers and its LatLng Points in the map
    private HashMap<Marker, Integer> mSelectedMarkersMap = new HashMap<>();
    private ArrayList<Marker> mSelectedMarkerslist = new ArrayList<>();
    private ArrayList<LatLng> mPolyLineSelectedPointList = new ArrayList<>();

    //Panel Layout
    private SlidingUpPanelLayout mSlidingPanelLayout;

    //Panel Header Button
    private ImageButton mPanelButton;

    //Request Location Identifier
    private static final int REQUEST_LOCATION = 0;

    //Loader Manager
    android.app.LoaderManager loaderManager;

    //Loader to Request Directions For the selected locations ID
    private static final int DIRECTIONS_LOADER_ID = 1;

    //First Polyline Start
    private Boolean mFirstPolylineStart = true;

    //Directions API Base Request URL
    private static final String DIRECTIONSAPI_REQUEST_URL = "https://maps.googleapis.com/maps/api/directions/json";

    //Firebase Objects
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mPlansFirebaseReference;
    private String mPlanKey;

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

        //Initialization of the Drawer menu
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout_map);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view_map);
        if (navigationView != null) {
            setupDrawerContent(navigationView);
            Log.v("NearbyPlacesMapActivity", "Hi what chu doing");
        }

        navigationView.setCheckedItem(R.id.nav_home);

        //CODE FOR MAP STARTS HERE

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(map);
        mapFragment.getMapAsync(this);

        //Get Map Activity Layout
        mLayout = findViewById(R.id.map_main_content);


        //Initialize Google Places Search Box Fragment
        initializeGoogleMapsPlacesFragment();

        //Initialize USER_LOCATION
        LatLng mLastLocationLatLng = new LatLng(18.6976745, -71.2865409);

        CameraPosition USER_LOCATION = CameraPosition.builder()
                .target(mLastLocationLatLng)
                .zoom((float) 15)
                .bearing(0)
                .build();

        //Initialize Sliding Panel
        initializeSlidingPanel();

        //Add Location Button
        mPanelButton = (ImageButton) findViewById(R.id.add_location_to_plan);

        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        // Get a reference to the LoaderManager, in order to interact with loaders.
        loaderManager = getLoaderManager();

        Intent intent = getIntent();
        mPlanKey = intent.getStringExtra(PlanDetailsActivity.EXTRA_PLAN_KEY);

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mPlansFirebaseReference = mFirebaseDatabase.getReference().child("plans");

        //Save Plan Button
        Button saveButton = (Button) findViewById(R.id.activity_map_save_button);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Saving Plan information
                mPlansFirebaseReference.child(mPlanKey).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // Get user information
                        PlanTemp planTemp = dataSnapshot.getValue(PlanTemp.class);

                        List<CustomLatLngPoints> planSelectedPointList = new ArrayList<CustomLatLngPoints>();
                        List<String> planSelectedLocationsNames = new ArrayList<String>();

                        for(int i=0; i<mPolyLineSelectedPointList.size(); i++){
                            CustomLatLngPoints newPoint = new CustomLatLngPoints();
                            LatLng tempLatLng = mPolyLineSelectedPointList.get(i);
                            newPoint.setLat(tempLatLng.latitude);
                            newPoint.setLng(tempLatLng.longitude);
                            planSelectedPointList.add(newPoint);

                            String tempLocationName = mSelectedMarkerslist.get(i).getTitle();
                            planSelectedLocationsNames.add(tempLocationName);
                        }
                        planTemp.setmPlanLocationsLatLng(planSelectedPointList);
                        planTemp.setmPlanLocationsNames(planSelectedLocationsNames);

                        String planPolyline = PolyUtil.encode(mPolyLine.getPoints());

                        planTemp.setmOverviewPolyline(planPolyline);


                        Map<String, Object> profileUpdatedValues = planTemp.toMap();

                        Map<String, Object> childUpdates = new HashMap<>();

                        childUpdates.put(mPlanKey, profileUpdatedValues);
                        mPlansFirebaseReference.updateChildren(childUpdates);


                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.e(LOG_TAG, databaseError.toString());
                    }
                });

                //Return result to MainActivity
                setResult(RESULT_OK);
                finish();
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


        //Verifying and Asking for the users device location, to initialize the MyLocations Google Maps Ui Tool
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mGoogleMap.setMyLocationEnabled(true);
        } else requestLocationPermission();


        mPlansFirebaseReference.child(mPlanKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                List<String> locations = new ArrayList<String>();

                PlanTemp mPlanTemp = dataSnapshot.getValue(PlanTemp.class);



                List<String> planLocations = mPlanTemp.getmPlanLocationsNames();

                mGoogleMap.clear();
                //Initializing the Polyline
                mPolyLine = mGoogleMap.addPolyline(new PolylineOptions());
                mPolyLine.setColor(Color.BLUE);


                String locationsInPlan = "";
                List<LatLng> locationPoints = new ArrayList<LatLng>();
                if(planLocations != null) {
                    locations = planLocations;
                    for (int i = 0; i < planLocations.size(); i++) {
                        LatLng tempPoint = new LatLng(mPlanTemp.getmPlanLocationsLatLng().get(i).getLat(),mPlanTemp.getmPlanLocationsLatLng().get(i).getLng());
                        locationPoints.add(tempPoint);
                        mPolyLineSelectedPointList.add(tempPoint);
                        MarkerOptions tempMarker = new MarkerOptions().position(tempPoint).title(mPlanTemp.getmPlanLocationsNames().get(i));
                        Marker tempMarkerObject = mGoogleMap.addMarker(tempMarker);
                        mSelectedMarkerslist.add(tempMarkerObject);
                        mSelectedMarkersMap.put(tempMarkerObject, i);

                    }
                    List<LatLng> polyLinePoints = PolyUtil.decode(mPlanTemp.getmOverviewPolyline());
                    mPolyLine.setPoints(polyLinePoints);
                    updateMapBounds();
                }else{
                    locations.add("No destinations");
                }



            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(LOG_TAG, databaseError.toString());
            }
        });


    }

    private void setupDrawerContent(final NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        switch (menuItem.getItemId()) {
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
                Log.v("NearbyPlacesMapActivity", "Hi what chu doing, stop pressing me");
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
        if (mLastMarker != null && !mSelectedMarkersMap.containsKey(mLastMarker)) {
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
                .zoom(15)
                .bearing(0)
                .build();
        flyTo(newPosition);
        updatePanelHeader(mLastMarker);
        updatePanelHeaderButton(mLastMarker);

    }

    //Method that gets called when an error occurs with a place selected from the search box
    @Override
    public void onError(Status status) {
        Log.e(LOG_TAG, "There has been an error retrieving the place Status: " + status.getStatusMessage());
        return;
    }

    //This Method moves the camera to the position indicated with an animation
    public void flyTo(CameraPosition cameraPosition) {
        mGoogleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    //This method initializes trhe google places fragment
    private void initializeGoogleMapsPlacesFragment() {
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
    private void initializeSlidingPanel() {

        //addPlaceButton of Panel initialization
        final ImageButton addPlaceButton = (ImageButton) findViewById(R.id.add_location_to_plan);

        addPlaceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mLastMarker != null && !mSelectedMarkersMap.containsKey(mLastMarker)) {
                    addLocationWithDialog();
                } else if (mLastMarker != null && mSelectedMarkersMap.containsKey(mLastMarker)) {
                    deleteLocationWithDialog();
                }
            }
        });

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

        //Setting the panel to be initially hidden
        mSlidingPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);

    }

    //This method updates the panel header text view
    private void updatePanelHeader(Marker marker) {

        if (mSlidingPanelLayout != null && mSlidingPanelLayout.getPanelState() == SlidingUpPanelLayout.PanelState.HIDDEN)
            mSlidingPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        TextView panelHeaderTextView = (TextView) findViewById(R.id.panelHeader_TextView_dragView);

        panelHeaderTextView.setText(marker.getTitle());

    }

    //Updating the Panel Header button
    private void updatePanelHeaderButton(Marker marker) {
        if (mSelectedMarkersMap.containsKey(marker)) {
            mPanelButton.setImageResource(R.drawable.ic_delete_forever_black_36dp);
            mPanelButton.setColorFilter(Color.RED);
        } else {
            mPanelButton.setImageResource(R.drawable.ic_add_location_black);
            mPanelButton.setColorFilter(Color.argb(255, 100, 181, 246));
        }
    }

    //This method sets the google maps click listeners
    private void setmapClickListeners() {

        //On Long Click Listener for map to set temporal marker
        mGoogleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng point) {
                //Verify if last marker that needs to be removed from map because it was not selected
                if (mLastMarker != null && !mSelectedMarkersMap.containsKey(mLastMarker)) {
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
                updatePanelHeader(marker);
                updatePanelHeaderButton(marker);
                mLastMarker = marker;
                return true;
            }
        });

        //On Map Click Listener
        mGoogleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if (mSlidingPanelLayout != null && (mSlidingPanelLayout.getPanelState() != SlidingUpPanelLayout.PanelState.HIDDEN)) {

                    mSlidingPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);

                }
            }
        });
    }

    //Adding a location to the map that contains all locations for the plan
    private void addLocationWithDialog() {
        AlertDialog.Builder builderForAddingLocationDialog = new AlertDialog.Builder(context);
        builderForAddingLocationDialog.setTitle("Add place?");
        builderForAddingLocationDialog.setMessage("Are you sure you wish to add this place to your plan?");
        builderForAddingLocationDialog.setCancelable(true);

        builderForAddingLocationDialog.setPositiveButton(
                "ADD",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //Adding marker to the map that contains all plan locations with its insertion order
                        mSelectedMarkersMap.put(mLastMarker, mSelectedMarkersMap.size() + 1);
                        mSelectedMarkerslist.add(mLastMarker);
                        mPolyLineSelectedPointList.add(mLastMarker.getPosition());

                        if(mPolyLineSelectedPointList.size() > 1) {
                            //Updating Map Polyline
                            updatePolylineOfLocations();


                            //Updating Bounds Builder for Map
                            updateMapBounds();
                        }

                        mPanelButton.setImageResource(R.drawable.ic_delete_forever_black_36dp);
                        mPanelButton.setColorFilter(Color.RED);

                        //Close Alert Dialog
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

        Dialog addAlertDialog = builderForAddingLocationDialog.create();
        addAlertDialog.show();
    }

    //Deleting a location from the map that contains all locations for the plan
    private void deleteLocationWithDialog() {
        AlertDialog.Builder builderForAddingLocationDialog = new AlertDialog.Builder(context);
        builderForAddingLocationDialog.setTitle("Delete this place?");
        builderForAddingLocationDialog.setMessage("Are you sure you wish to delete this place from your plan?");
        builderForAddingLocationDialog.setCancelable(true);

        builderForAddingLocationDialog.setPositiveButton(
                "Delete",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //Removing marker from list of locations in plan
                        mSelectedMarkersMap.remove(mLastMarker);
                        mSelectedMarkerslist.remove(mLastMarker);
                        mPolyLineSelectedPointList.remove(mLastMarker.getPosition());
                        mLastMarker.remove();

                        if(mPolyLineSelectedPointList.size() > 1) {
                            //Updating Map Polyline
                            updatePolylineOfLocations();
                        }else{
                            List<LatLng> dummyPointList = new ArrayList<LatLng>();
                            dummyPointList.add(new LatLng(0.0,0.0));
                            mPolyLine.setPoints(dummyPointList);
                        }

                        mSlidingPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
                        //Updating Map Bounds
                        if (!mPolyLineSelectedPointList.isEmpty() && mPolyLineSelectedPointList.size() > 1)
                            updateMapBounds();
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

        Dialog deleteAlertDialog = builderForAddingLocationDialog.create();
        deleteAlertDialog.show();
    }

    //Update the polyline between locations
    private void updatePolylineOfLocations() {

        if(mPolyLineSelectedPointList.size() < 2) return;

        if(mFirstPolylineStart){

            // Initialize the loader. Pass in the int ID constant defined above and pass in null for
            // the bundle. Pass in this activity for the LoaderCallbacks parameter (which is valid
            // because this activity implements the LoaderCallbacks interface).
            loaderManager.initLoader(DIRECTIONS_LOADER_ID, null, this);
            mFirstPolylineStart = false;

        }else loaderManager.restartLoader(DIRECTIONS_LOADER_ID, null, this);

    }

    //Updating the map bounds to move camera so it includes all new locations in the map camera
    private void updateMapBounds() {
        LatLngBounds.Builder mMapLatLngBoundsBuilder = new LatLngBounds.Builder();

        //Include points to consider for map Bounds
        for (int i = 0; i < mPolyLineSelectedPointList.size(); i++) {
            mMapLatLngBoundsBuilder.include(mPolyLineSelectedPointList.get(i));
        }

        //Create new Camera Update to indicate where to move camera
        CameraUpdate newMapBounds =
                CameraUpdateFactory.newLatLngBounds(mMapLatLngBoundsBuilder.build(), 240);

        //Update Camera to new Bounds
        mGoogleMap.moveCamera(newMapBounds);
    }

    //Verifying everything is closed in the UI to confirm that the back click was to exit the application
    @Override
    public void onBackPressed() {
        if (mSlidingPanelLayout != null &&
                (mSlidingPanelLayout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED || mSlidingPanelLayout.getPanelState() == SlidingUpPanelLayout.PanelState.ANCHORED)) {
            mSlidingPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        } else {
            super.onBackPressed();
        }
    }

    private void requestLocationPermission() {
        Log.i(LOG_TAG, "Location permission has NOT been granted. Requesting permission.");

        // BEGIN_INCLUDE(camera_permission_request)
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)) {
            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // For example if the user has previously denied the permission.
            Log.i(LOG_TAG,
                    "Displaying location permission rationale to provide additional context.");
            Snackbar.make(mLayout, R.string.permission_location_rationale,
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ActivityCompat.requestPermissions(NearbyPlacesMapsActivity.this,
                                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                                    REQUEST_LOCATION);
                        }
                    })
                    .show();
        } else {

            // Camera permission has not been granted yet. Request it directly.
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_LOCATION);
        }
        // END_INCLUDE(camera_permission_request)
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        if (requestCode == REQUEST_LOCATION) {
            // BEGIN_INCLUDE(permission_result)
            // Received permission result for camera permission.
            Log.i(LOG_TAG, "Received response for Location permission request.");

            // Check if the only required permission has been granted
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Camera permission has been granted, preview can be displayed
                Log.i(LOG_TAG, "Location permission has now been granted. Showing preview.");
//                Snackbar.make(mLayout, R.string.permision_available_camera,
//                        Snackbar.LENGTH_SHORT).show();
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    mGoogleMap.setMyLocationEnabled(true);
                }
            } else {
                Log.i(LOG_TAG, "Location permission was NOT granted.");
                Snackbar.make(mLayout, R.string.permissions_not_granted,
                        Snackbar.LENGTH_SHORT).show();

            }
            // END_INCLUDE(permission_result)

        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    //On Connected to the Google Api
    @Override
    public void onConnected(Bundle connectionHint) {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            //Moving Map Camara to user position if its the first time he opens the app
            if(mFirstOnStart) {
                Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                        mGoogleApiClient);
                if (mLastLocation != null) {
                    CameraPosition cameraStartPosition = CameraPosition.builder()
                            .target(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()))
                            .zoom(15)
                            .bearing(0)
                            .build();
                    flyTo(cameraStartPosition);
                }
                mFirstOnStart = false;
            }
        } else requestLocationPermission();

    }



    //On Connection Suspended
    @Override
    public void onConnectionSuspended(int i) {
        Log.d(LOG_TAG, "Connection to the Google Api Client to retrieve user location has been Suspended");

    }

    //On Connection Failed
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e(LOG_TAG, "Connection to the Google Api Client to retrieve user location has failed");
    }

    //On Activity Start
    @Override
    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    //On Activity Stop
    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public Loader onCreateLoader(int i, Bundle bundle) {

        Uri baseUri = Uri.parse(DIRECTIONSAPI_REQUEST_URL);
        Uri.Builder uriBuilder = baseUri.buildUpon();

        LatLng origin, destination;
        origin = mPolyLineSelectedPointList.get(0);
        String originLatLng = "" + origin.latitude + "," + origin.longitude;
        destination = mPolyLineSelectedPointList.get(mPolyLineSelectedPointList.size()-1);
        String destinationLatLng = "" + destination.latitude + "," + destination.longitude;
        String waypoints = "";
        int sizeOfList = mPolyLineSelectedPointList.size()-1;

        for(int j=1; j<sizeOfList; j++){
            LatLng tempLatLng = mPolyLineSelectedPointList.get(j);
            if(j<sizeOfList) waypoints += tempLatLng.latitude + "," + tempLatLng.longitude + "|";
            else waypoints += tempLatLng.latitude + "," + tempLatLng.longitude;
        }

        uriBuilder.appendQueryParameter("origin", originLatLng );
        uriBuilder.appendQueryParameter("destination", destinationLatLng);

        if(waypoints != "") uriBuilder.appendQueryParameter("waypoints", waypoints);

        String key = getResources().getString(R.string.google_maps_key);
        uriBuilder.appendQueryParameter("key", key);
        Log.d(LOG_TAG, "URL TO API: " + uriBuilder.toString());
        return new DirectionsLoader(this, uriBuilder.toString());

    }

    @Override
    public void onLoadFinished(android.content.Loader<List<Route>> loader, List<Route> data) {
        if (data != null && !data.isEmpty() && !mPolyLineSelectedPointList.isEmpty()) {
            Route route = data.get(0);
            mPolyLine.setPoints(route.getOverviewPolyLine());
        }
    }

    @Override
    public void onLoaderReset(android.content.Loader<List<Route>> loader) {
        List<LatLng> emptyList = new ArrayList<LatLng>();
        mPolyLine.setPoints(emptyList);
    }

}
