package com.pujolsluis.android.hangeo;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Location;
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
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.pujolsluis.android.hangeo.R.id.map;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, PlaceSelectionListener,
        ActivityCompat.OnRequestPermissionsResultCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    static final String LOG_TAG = MapsActivity.class.getSimpleName();

    private DrawerLayout mDrawerLayout;
    Context context = this;


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
    private Boolean mFirstOnStart = true;
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

    //Panel Header Button
    private ImageButton mPanelButton;

    //Request Location Identifier
    private static final int REQUEST_LOCATION = 0;

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
                .findFragmentById(map);
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

        mPanelButton = (ImageButton) findViewById(R.id.add_location_to_plan);

        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

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


        List<Route> routeList = null;
        try {
            routeList = DirectionsApiUtils.extractRoutes("{\n\"geocoded_waypoints\":[\n{\n\"geocoder_status\":\"OK\",\n\"place_id\":\"ChIJVxCyxRiKr44RhP9iu8MMS8M\",\n\"types\":[\"route\"]\n},\n{\n\"geocoder_status\":\"OK\",\n\"place_id\":\"ChIJf5RID-iJr44RA2bXsKKwUIs\",\n\"types\":[\"street_address\"]\n},\n{\n\"geocoder_status\":\"OK\",\n\"place_id\":\"ChIJlT0h7hdipY4RQoSRuAP-Nzc\",\n\"types\":[\"route\"]\n}\n],\n\"routes\":[\n{\n\"bounds\":{\n\"northeast\":{\n\"lat\":18.4875306,\n\"lng\":-69.92054089999999\n},\n\"southwest\":{\n\"lat\":18.4399629,\n\"lng\":-69.96356\n}\n},\n\"copyrights\":\"Datosdemapas©2017Google\",\n\"legs\":[\n{\n\"distance\":{\n\"text\":\"5,0km\",\n\"value\":5041\n},\n\"duration\":{\n\"text\":\"16min\",\n\"value\":941\n},\n\"end_address\":\"AvenidaGustavoMejiaRicart80,SantoDomingo,RepúblicaDominicana\",\n\"end_location\":{\n\"lat\":18.4754149,\n\"lng\":-69.92633169999999\n},\n\"start_address\":\"CalleCrisantemos,SantoDomingo,RepúblicaDominicana\",\n\"start_location\":{\n\"lat\":18.4874991,\n\"lng\":-69.961871\n},\n\"steps\":[\n{\n\"distance\":{\n\"text\":\"0,2km\",\n\"value\":227\n},\n\"duration\":{\n\"text\":\"1min\",\n\"value\":77\n},\n\"end_location\":{\n\"lat\":18.4870692,\n\"lng\":-69.96356\n},\n\"html_instructions\":\"Dirígetehaciael\\u003cb\\u003eoeste\\u003c/b\\u003ehacia\\u003cb\\u003eCallelosCoralillos\\u003c/b\\u003e\\u003cdivstyle=\\\"font-size:0.9em\\\"\\u003ePasaporParqueoProfesoresIntec(aladerecha).\\u003c/div\\u003e\",\n\"polyline\":{\n\"points\":\"{yyoBtl_jL?j@E~F?J?H@HBDR@`AA\"\n},\n\"start_location\":{\n\"lat\":18.4874991,\n\"lng\":-69.961871\n},\n\"travel_mode\":\"DRIVING\"\n},\n{\n\"distance\":{\n\"text\":\"0,4km\",\n\"value\":427\n},\n\"duration\":{\n\"text\":\"2min\",\n\"value\":106\n},\n\"end_location\":{\n\"lat\":18.4849556,\n\"lng\":-69.96029919999999\n},\n\"html_instructions\":\"Giraala\\u003cb\\u003eizquierda\\u003c/b\\u003eenHeladosBonLosJardinesendireccióna\\u003cb\\u003eAv.JardinesdeFontainebleau\\u003c/b\\u003e\\u003cdivstyle=\\\"font-size:0.9em\\\"\\u003ePasaporLisyBeautyCenter(alaizquierda).\\u003c/div\\u003e\",\n\"maneuver\":\"turn-left\",\n\"polyline\":{\n\"points\":\"ewyoBfw_jLb@cANYb@wARy@@QRkALWZ_@f@g@BE|@u@VSVSZ]f@w@Zc@\"\n},\n\"start_location\":{\n\"lat\":18.4870692,\n\"lng\":-69.96356\n},\n\"travel_mode\":\"DRIVING\"\n},\n{\n\"distance\":{\n\"text\":\"0,7km\",\n\"value\":712\n},\n\"duration\":{\n\"text\":\"3min\",\n\"value\":150\n},\n\"end_location\":{\n\"lat\":18.4815582,\n\"lng\":-69.95481669999999\n},\n\"html_instructions\":\"Giraligeramenteala\\u003cb\\u003ederecha\\u003c/b\\u003ehacia\\u003cb\\u003eAv.JardinesdeFontainebleau\\u003c/b\\u003e\\u003cdivstyle=\\\"font-size:0.9em\\\"\\u003ePasaporPlazaCivilCenter(aladerecha).\\u003c/div\\u003e\",\n\"maneuver\":\"turn-slight-right\",\n\"polyline\":{\n\"points\":\"_jyoBzb_jLLCLIbBoARQ@APSPWP]JUH[vBwIRq@FQFQnCuGL[FODIz@_APITIXK\"\n},\n\"start_location\":{\n\"lat\":18.4849556,\n\"lng\":-69.96029919999999\n},\n\"travel_mode\":\"DRIVING\"\n},\n{\n\"distance\":{\n\"text\":\"1,2km\",\n\"value\":1210\n},\n\"duration\":{\n\"text\":\"2min\",\n\"value\":137\n},\n\"end_location\":{\n\"lat\":18.4828762,\n\"lng\":-69.94347259999999\n},\n\"html_instructions\":\"Giraala\\u003cb\\u003eizquierda\\u003c/b\\u003ehacia\\u003cb\\u003eAv.JohnF.Kennedy\\u003c/b\\u003e\\u003cdivstyle=\\\"font-size:0.9em\\\"\\u003ePasaporlaestacióndemetro(aladerecha)\\u003c/div\\u003e\",\n\"maneuver\":\"turn-left\",\n\"polyline\":{\n\"points\":\"wtxoBr`~iLCSOsAC[UeE?]EkBA_@A]E}AQyIAo@?GEkBCeAO{HGoBCq@Ac@AKASCUAIGc@Ki@AIKs@Mq@Kq@e@iCSeAMg@\"\n},\n\"start_location\":{\n\"lat\":18.4815582,\n\"lng\":-69.95481669999999\n},\n\"travel_mode\":\"DRIVING\"\n},\n{\n\"distance\":{\n\"text\":\"0,7km\",\n\"value\":706\n},\n\"duration\":{\n\"text\":\"1min\",\n\"value\":70\n},\n\"end_location\":{\n\"lat\":18.4837988,\n\"lng\":-69.93718389999999\n},\n\"html_instructions\":\"Tomaelramaldela\\u003cb\\u003eizquierda\\u003c/b\\u003eendirección\\u003cb\\u003eExpresoJohnF.Kennedy\\u003c/b\\u003e\",\n\"polyline\":{\n\"points\":\"_}xoBty{iLa@i@AAq@gCMe@mBqGQg@IYAAI[ACIYGYEUCSCYCSAW?U?W?W@[BQBUD]Fg@pAeH\"\n},\n\"start_location\":{\n\"lat\":18.4828762,\n\"lng\":-69.94347259999999\n},\n\"travel_mode\":\"DRIVING\"\n},\n{\n\"distance\":{\n\"text\":\"0,9km\",\n\"value\":870\n},\n\"duration\":{\n\"text\":\"3min\",\n\"value\":176\n},\n\"end_location\":{\n\"lat\":18.4826155,\n\"lng\":-69.9290965\n},\n\"html_instructions\":\"Tomalasalidaparaincorporartea\\u003cb\\u003eAv.JohnF.Kennedy\\u003c/b\\u003e\\u003cdivstyle=\\\"font-size:0.9em\\\"\\u003ePasaporCentrodeAtenciónEmpresarialClaro(aladerecha).\\u003c/div\\u003e\",\n\"maneuver\":\"ramp-right\",\n\"polyline\":{\n\"points\":\"wbyoBjrziLRWb@cCDUtAcILq@DU@MDWB[@Y@U?YNgHJmE@OF}CLaF\"\n},\n\"start_location\":{\n\"lat\":18.4837988,\n\"lng\":-69.93718389999999\n},\n\"travel_mode\":\"DRIVING\"\n},\n{\n\"distance\":{\n\"text\":\"0,8km\",\n\"value\":841\n},\n\"duration\":{\n\"text\":\"3min\",\n\"value\":193\n},\n\"end_location\":{\n\"lat\":18.4753851,\n\"lng\":-69.9267818\n},\n\"html_instructions\":\"Giraala\\u003cb\\u003ederecha\\u003c/b\\u003ehacia\\u003cb\\u003eAv.Tiradentes\\u003c/b\\u003e\\u003cdivstyle=\\\"font-size:0.9em\\\"\\u003ePasaporCentroCopiadoraNaco(aladerecha).\\u003c/div\\u003e\",\n\"maneuver\":\"turn-right\",\n\"polyline\":{\n\"points\":\"k{xoBz_yiL\\\\EfAOnASf@Iv@Oz@Ub@KbCi@HCpDw@`@I`Cm@|@SnHgBjCq@nA[@A\"\n},\n\"start_location\":{\n\"lat\":18.4826155,\n\"lng\":-69.9290965\n},\n\"travel_mode\":\"DRIVING\"\n},\n{\n\"distance\":{\n\"text\":\"48m\",\n\"value\":48\n},\n\"duration\":{\n\"text\":\"1min\",\n\"value\":32\n},\n\"end_location\":{\n\"lat\":18.4754149,\n\"lng\":-69.92633169999999\n},\n\"html_instructions\":\"Giraala\\u003cb\\u003eizquierda\\u003c/b\\u003eenDrinks2gohacia\\u003cb\\u003eAvenidaGustavoMejiaRicart\\u003c/b\\u003e\\u003cdivstyle=\\\"font-size:0.9em\\\"\\u003eEldestinoestáalaizquierda.\\u003c/div\\u003e\",\n\"maneuver\":\"turn-left\",\n\"polyline\":{\n\"points\":\"enwoBjqxiL?SCeA\"\n},\n\"start_location\":{\n\"lat\":18.4753851,\n\"lng\":-69.9267818\n},\n\"travel_mode\":\"DRIVING\"\n}\n],\n\"traffic_speed_entry\":[],\n\"via_waypoint\":[]\n},\n{\n\"distance\":{\n\"text\":\"7,6km\",\n\"value\":7632\n},\n\"duration\":{\n\"text\":\"17min\",\n\"value\":1013\n},\n\"end_address\":\"Calle1ra,SantoDomingo,RepúblicaDominicana\",\n\"end_location\":{\n\"lat\":18.4429023,\n\"lng\":-69.94241869999999\n},\n\"start_address\":\"AvenidaGustavoMejiaRicart80,SantoDomingo,RepúblicaDominicana\",\n\"start_location\":{\n\"lat\":18.4754149,\n\"lng\":-69.92633169999999\n},\n\"steps\":[\n{\n\"distance\":{\n\"text\":\"0,3km\",\n\"value\":334\n},\n\"duration\":{\n\"text\":\"1min\",\n\"value\":55\n},\n\"end_location\":{\n\"lat\":18.475801,\n\"lng\":-69.92319519999999\n},\n\"html_instructions\":\"Dirígetehaciael\\u003cb\\u003eeste\\u003c/b\\u003een\\u003cb\\u003eAvenidaGustavoMejiaRicart\\u003c/b\\u003ehacia\\u003cb\\u003eCalleAlbertoLarancuent\\u003c/b\\u003e\\u003cdivstyle=\\\"font-size:0.9em\\\"\\u003ePasaporDavidCrockett(aladerecha).\\u003c/div\\u003e\",\n\"polyline\":{\n\"points\":\"inwoBpnxiLKcDGiBGaAGk@M{AAQYeC?A\"\n},\n\"start_location\":{\n\"lat\":18.4754149,\n\"lng\":-69.92633169999999\n},\n\"travel_mode\":\"DRIVING\"\n},\n{\n\"distance\":{\n\"text\":\"0,6km\",\n\"value\":562\n},\n\"duration\":{\n\"text\":\"3min\",\n\"value\":180\n},\n\"end_location\":{\n\"lat\":18.4716233,\n\"lng\":-69.92054089999999\n},\n\"html_instructions\":\"Giraala\\u003cb\\u003ederecha\\u003c/b\\u003eenTRANSLOGIChacia\\u003cb\\u003eCalleLuisAlberti\\u003c/b\\u003e\\u003cdivstyle=\\\"font-size:0.9em\\\"\\u003ePasaporTorreOviedo(alaizquierda).\\u003c/div\\u003e\",\n\"maneuver\":\"turn-right\",\n\"polyline\":{\n\"points\":\"wpwoB~zwiLZIz@Od@EDAp@?ZAR?L?N?VIXIXOROPONMTQVYFEnAkANOtAcAVUvAuA`B_B\"\n},\n\"start_location\":{\n\"lat\":18.475801,\n\"lng\":-69.92319519999999\n},\n\"travel_mode\":\"DRIVING\"\n},\n{\n\"distance\":{\n\"text\":\"81m\",\n\"value\":81\n},\n\"duration\":{\n\"text\":\"1min\",\n\"value\":19\n},\n\"end_location\":{\n\"lat\":18.4711319,\n\"lng\":-69.9211037\n},\n\"html_instructions\":\"Giraala\\u003cb\\u003ederecha\\u003c/b\\u003eenGrupoDelphihacia\\u003cb\\u003eAv27deFebrero\\u003c/b\\u003e\",\n\"maneuver\":\"turn-right\",\n\"polyline\":{\n\"points\":\"svvoBjjwiLn@x@p@t@\"\n},\n\"start_location\":{\n\"lat\":18.4716233,\n\"lng\":-69.92054089999999\n},\n\"travel_mode\":\"DRIVING\"\n},\n{\n\"distance\":{\n\"text\":\"3,7km\",\n\"value\":3736\n},\n\"duration\":{\n\"text\":\"4min\",\n\"value\":212\n},\n\"end_location\":{\n\"lat\":18.4559228,\n\"lng\":-69.9524533\n},\n\"html_instructions\":\"Tomaelramaldela\\u003cb\\u003eizquierda\\u003c/b\\u003eendirección\\u003cb\\u003eExpreso27deFebrero\\u003c/b\\u003e/\\u003cb\\u003eCarretera3\\u003c/b\\u003e\",\n\"polyline\":{\n\"points\":\"qsvoBzmwiLdBbBbFtGf@r@\\\\b@RZBDHLLT^r@Zt@`@dA~BjGtCjHzElLv@nBLXbKtV|DrJPd@rAbDhGjON`@h@nAjBrE^~@b@fAPb@LZLn@fApFvBvJZzAxA|GR|@XnAdAhD`B|FHX@BvAlFFZ^r@P`@Vn@\"\n},\n\"start_location\":{\n\"lat\":18.4711319,\n\"lng\":-69.9211037\n},\n\"travel_mode\":\"DRIVING\"\n},\n{\n\"distance\":{\n\"text\":\"0,3km\",\n\"value\":258\n},\n\"duration\":{\n\"text\":\"1min\",\n\"value\":46\n},\n\"end_location\":{\n\"lat\":18.4548408,\n\"lng\":-69.9546137\n},\n\"html_instructions\":\"Mantenteala\\u003cb\\u003ederecha\\u003c/b\\u003eparacontinuarpor\\u003cb\\u003eAv27deFebrero\\u003c/b\\u003e\\u003cdivstyle=\\\"font-size:0.9em\\\"\\u003ePasaporMini-VallaEudom(aladerecha).\\u003c/div\\u003e\",\n\"maneuver\":\"keep-right\",\n\"polyline\":{\n\"points\":\"otsoBxq}iLF`@FRDN`DpHP^LV\"\n},\n\"start_location\":{\n\"lat\":18.4559228,\n\"lng\":-69.9524533\n},\n\"travel_mode\":\"DRIVING\"\n},\n{\n\"distance\":{\n\"text\":\"1,4km\",\n\"value\":1425\n},\n\"duration\":{\n\"text\":\"5min\",\n\"value\":305\n},\n\"end_location\":{\n\"lat\":18.4439423,\n\"lng\":-69.94774099999999\n},\n\"html_instructions\":\"Giraala\\u003cb\\u003eizquierda\\u003c/b\\u003eenBorbónRodriguezhacia\\u003cb\\u003eAvNúñezdeCáceres\\u003c/b\\u003e\\u003cdivstyle=\\\"font-size:0.9em\\\"\\u003ePasaporVAGImportCxA(aladerecha).\\u003c/div\\u003e\",\n\"maneuver\":\"turn-left\",\n\"polyline\":{\n\"points\":\"wmsoBh_~iLJEFCJGLGnIwDZQNIPGx@_@rAc@VMjAm@RIlLcFRGhCkA|CsAZMj@YxBaAl@YnAq@~@g@XOTQNId@i@lCiDb@g@LQ\\\\e@RS\"\n},\n\"start_location\":{\n\"lat\":18.4548408,\n\"lng\":-69.9546137\n},\n\"travel_mode\":\"DRIVING\"\n},\n{\n\"distance\":{\n\"text\":\"0,3km\",\n\"value\":262\n},\n\"duration\":{\n\"text\":\"1min\",\n\"value\":16\n},\n\"end_location\":{\n\"lat\":18.4420697,\n\"lng\":-69.9462746\n},\n\"html_instructions\":\"Continúapor\\u003cb\\u003eAvenidaJoséNuñezdeCáceres\\u003c/b\\u003e.\",\n\"polyline\":{\n\"points\":\"siqoBjt|iLb@g@b@e@DEPSLOLIVUb@Yb@Yh@]d@Ud@Sb@Q\"\n},\n\"start_location\":{\n\"lat\":18.4439423,\n\"lng\":-69.94774099999999\n},\n\"travel_mode\":\"DRIVING\"\n},\n{\n\"distance\":{\n\"text\":\"0,3km\",\n\"value\":295\n},\n\"duration\":{\n\"text\":\"1min\",\n\"value\":47\n},\n\"end_location\":{\n\"lat\":18.4400655,\n\"lng\":-69.9470144\n},\n\"html_instructions\":\"Incorpóratea\\u003cb\\u003eAv.Independencia\\u003c/b\\u003e\",\n\"polyline\":{\n\"points\":\"}}poBdk|iLtGiAf@jAp@xA^v@\"\n},\n\"start_location\":{\n\"lat\":18.4420697,\n\"lng\":-69.9462746\n},\n\"travel_mode\":\"DRIVING\"\n},\n{\n\"distance\":{\n\"text\":\"0,5km\",\n\"value\":543\n},\n\"duration\":{\n\"text\":\"2min\",\n\"value\":94\n},\n\"end_location\":{\n\"lat\":18.4419472,\n\"lng\":-69.94241599999999\n},\n\"html_instructions\":\"Giratotalmenteala\\u003cb\\u003eizquierda\\u003c/b\\u003eenDrinks2GoAv.Independenciaypermaneceen\\u003cb\\u003eAv.Independencia\\u003c/b\\u003e\\u003cdivstyle=\\\"font-size:0.9em\\\"\\u003ePasaporSubestaciónEléctricaMatadero(aladerechaa350&nbsp;m)\\u003c/div\\u003e\",\n\"maneuver\":\"turn-sharp-left\",\n\"polyline\":{\n\"points\":\"mqpoBxo|iLTCk@qAq@{Ae@_AEKs@mBQg@EOCKe@mBGQi@wBGWMi@Ok@Uu@_@iA\"\n},\n\"start_location\":{\n\"lat\":18.4400655,\n\"lng\":-69.9470144\n},\n\"travel_mode\":\"DRIVING\"\n},\n{\n\"distance\":{\n\"text\":\"0,1km\",\n\"value\":100\n},\n\"duration\":{\n\"text\":\"1min\",\n\"value\":30\n},\n\"end_location\":{\n\"lat\":18.4427897,\n\"lng\":-69.942736\n},\n\"html_instructions\":\"Giraala\\u003cb\\u003eizquierda\\u003c/b\\u003ehacia\\u003cb\\u003eCalleProyecto\\u003c/b\\u003e\",\n\"maneuver\":\"turn-left\",\n\"polyline\":{\n\"points\":\"e}poBbs{iLSFsCv@\"\n},\n\"start_location\":{\n\"lat\":18.4419472,\n\"lng\":-69.94241599999999\n},\n\"travel_mode\":\"DRIVING\"\n},\n{\n\"distance\":{\n\"text\":\"36m\",\n\"value\":36\n},\n\"duration\":{\n\"text\":\"1min\",\n\"value\":9\n},\n\"end_location\":{\n\"lat\":18.4429023,\n\"lng\":-69.94241869999999\n},\n\"html_instructions\":\"Giraala\\u003cb\\u003ederecha\\u003c/b\\u003eenColmaditodelosvierneshacia\\u003cb\\u003eCalle1ra\\u003c/b\\u003e\\u003cdivstyle=\\\"font-size:0.9em\\\"\\u003eEldestinoestáaladerecha.\\u003c/div\\u003e\",\n\"maneuver\":\"turn-right\",\n\"polyline\":{\n\"points\":\"mbqoBbu{iLU_A\"\n},\n\"start_location\":{\n\"lat\":18.4427897,\n\"lng\":-69.942736\n},\n\"travel_mode\":\"DRIVING\"\n}\n],\n\"traffic_speed_entry\":[],\n\"via_waypoint\":[]\n}\n],\n\"overview_polyline\":{\n\"points\":\"{yyoBtl_jLE`IDNtA?r@}Av@qCT}Ah@w@j@m@tAiAr@q@bA{AZMvBaBRUb@u@Tq@jCiKNc@|CqHLYz@_APIn@USgBYaFEiC[uNi@kVI_Aa@kCYcBy@oEMg@a@i@s@iCwCyJW{@Mo@Gm@EyAH{ALeApAeHRWh@yCbBuJPwARqJb@}Q|Es@zG{AzD{@bDw@lJ{BzEmA@UOiFGiBOmBi@uFbCa@nBAN?VIr@YjA_A~B{BlByAxDuD`BnBdBbBbFtGdAvAV`@Vb@z@hB`DpIpJxUfSrf@xQbd@^~@tA`H`GnXXnAdAhDjBvGxApFFZ^r@h@pANt@fExJl@YlK{ElCcAbB{@~QaI~I}D|BkAxAw@d@[rDsEbBsB~AgBvAiAlAw@jAi@b@QtGiAxAdD^v@TC}AmDk@kAeAuCw@{CoAeFu@_CgD~@U_A\"\n},\n\"summary\":\"Av.JohnF.Kennedy\",\n\"warnings\":[],\n\"waypoint_order\":[0]\n}\n],\n\"status\":\"OK\"\n}");
        } catch (Exception e) {
            e.printStackTrace();
        }
        Route route = routeList.get(0);
        mPolyLine.setPoints(route.getOverviewPolyLine());

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
                .zoom(17)
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
            mPanelButton.setImageResource(R.drawable.ic_delete_location_trash_bin);
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
                updatePanelHeader(mLastMarker);
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

                        //Updating Map Polyline
                        updatePolylineOfLocations();

                        //Updating Bounds Builder for Map
                        updateMapBounds();

                        mPanelButton.setImageResource(R.drawable.ic_delete_location_trash_bin);
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

        addAlertDialog = builderForAddingLocationDialog.create();
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

                        //Updating Map Polyline
                        updatePolylineOfLocations();

                        mSlidingPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
                        //Updating Map Bounds
                        if (!mPolyLineSelectedPointList.isEmpty())
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

        deleteAlertDialog = builderForAddingLocationDialog.create();
        deleteAlertDialog.show();
    }

    //Update the polyline between locations
    private void updatePolylineOfLocations() {
        mPolyLine.setPoints(mPolyLineSelectedPointList);
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
                CameraUpdateFactory.newLatLngBounds(mMapLatLngBoundsBuilder.build(), 48);

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
                            ActivityCompat.requestPermissions(MapsActivity.this,
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
                mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
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
            }else return;

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
}
