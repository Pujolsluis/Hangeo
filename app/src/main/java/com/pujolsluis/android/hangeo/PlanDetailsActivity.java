package com.pujolsluis.android.hangeo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static android.R.attr.key;
import static com.pujolsluis.android.hangeo.R.id.plan_details_map;

public class PlanDetailsActivity extends AppCompatActivity implements OnMapReadyCallback {

    public static final String EXTRA_NAME = "plan_name";
    public static final String EXTRA_PLAN_KEY = "plan_key";
    public static final String EXTRA_PLAN_IMAGE_RESOURCE = "plan_imageResource";
    public static final String LOG_TAG = PlanDetailsActivity.class.getSimpleName();
    private int mPlanImageResource;
    private FloatingActionButton mFloatingActionButton;
    private String mPlanKey;
    private TextView mPlanDescription;
    private TextView mPlanTimeValue;
    private TextView mPlanEstimatedCostValue;

    // Google Map Object
    private GoogleMap mGoogleMap;
    //Map Ready Indicator
    private boolean mapReady = false;
    //View to get the main activity layout and use it in the permission method
    private View mLayout;

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mPlanMembersReference;
    private DatabaseReference mUserProfilesReference;
    private DatabaseReference mPlanDatabaseReference;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plan_details);

        Intent intent = getIntent();
        final String cheeseName = intent.getStringExtra(EXTRA_NAME);
        mPlanKey = intent.getStringExtra(EXTRA_PLAN_KEY);
        mPlanImageResource = intent.getIntExtra(EXTRA_PLAN_IMAGE_RESOURCE, 0);

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mPlanMembersReference = mFirebaseDatabase.getReference().child("plans").child(mPlanKey).child("mPlanMembers");
        mUserProfilesReference = mFirebaseDatabase.getReference().child("userProfiles");
        mPlanDatabaseReference = mFirebaseDatabase.getReference().child("plans");

        mPlanDescription = (TextView) findViewById(R.id.plan_details_description_textView);
        mPlanTimeValue = (TextView) findViewById(R.id.plan_details_time_value);
        mPlanEstimatedCostValue = (TextView) findViewById(R.id.plan_details_estimated_cost_value);


        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        CollapsingToolbarLayout collapsingToolbar =
                (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        collapsingToolbar.setTitle(cheeseName);


        mFloatingActionButton = (FloatingActionButton) findViewById(R.id.plan_details_action_button);

        mFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Context context = view.getContext();
                Intent intent = new Intent(context, MapsActivity.class);
                startActivity(intent);
            }
        });

        List<String> locations = new ArrayList<String>();
        locations.add("Seven Dip's");
        locations.add("Shot's Bar");
        locations.add("Jet Set");
        locations.add("Bowling Center");
        locations.add("Boca Tabu Concert");

        mPlanDatabaseReference.child(mPlanKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                PlanTemp mPlanTemp = dataSnapshot.getValue(PlanTemp.class);

                mPlanDescription.setText(mPlanTemp.getmDescription());
                Calendar tempCalendar = Calendar.getInstance();
                tempCalendar.setTimeInMillis(mPlanTemp.getmCreationDate());

                mPlanImageResource = mPlanTemp.getmImageBannerResource();
                loadBackdrop();

                SimpleDateFormat formatter = new SimpleDateFormat("EEE, d MMM h:mm a");
                String dateString = formatter.format(mPlanTemp.getmCreationDate());

                mPlanTimeValue.setText(dateString);
                mPlanEstimatedCostValue.setText(mPlanTemp.getmEstimatedCost());
                List<String> planLocations = mPlanTemp.getmPlanLocations();
                String locationsInPlan = "";
                if(planLocations != null) {
                    for (int i = 0; i < planLocations.size(); i++) {

                        if (i == 0) locationsInPlan += planLocations.get(i);
                        else locationsInPlan += ", " + planLocations.get(i);

                    }
                }else{

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });




        //Code for Locations List View
        LocationsAdapter locationsAdapter = new LocationsAdapter(this, locations);

        ListView listView = (ListView) findViewById(R.id.plan_details_locations_list_view);
        listView.setAdapter(locationsAdapter);

        listView.setDivider(null);
        setListViewHeightBasedOnItems(listView);


        //Members Recycler view
        mRecyclerView = (RecyclerView) findViewById(R.id.plan_details_members_recycler_view);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);


        // specify an adapter (see also next example)

        mAdapter = new FirebaseRecyclerAdapter<Boolean, PlanDetailsActivity.PlanMembersHolder>(Boolean.class, R.layout.plan_details_members_list_item , PlanDetailsActivity.PlanMembersHolder.class, mPlanMembersReference) {
            @Override
            public void populateViewHolder(final PlanDetailsActivity.PlanMembersHolder planViewHolder, Boolean planItem, int position) {
                Log.e(LOG_TAG, "Ref Position: " + position + " Value: " + key);
                String key = this.getRef(position).getKey();

                mUserProfilesReference.child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        UserProfile mUserProfile = dataSnapshot.getValue(UserProfile.class);

                        planViewHolder.setmPlanMemberFirstNameLastName(mUserProfile.getmFirstName() + " " + mUserProfile.getmLastName());
                        planViewHolder.setmPlanMemberImageResource(mUserProfile.getmImageResource());

                        planViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
//                                Context context = v.getContext();
//                                Intent intent = new Intent(context, PlanDetailsActivity.class);
//                                intent.putExtra(PlanDetailsActivity.EXTRA_NAME, planViewHolder.mPlanTitle.getText().toString());
//
//                                context.startActivity(intent);
                            }
                        });
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }

        };



        mRecyclerView.setAdapter(mAdapter);


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
        Glide.with(this)
                .load(mPlanImageResource)
                .centerCrop()
                .into(imageView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.sample_actions, menu);
        return true;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;

        mGoogleMap.getUiSettings().setScrollGesturesEnabled(false);

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


    public class LocationsAdapter extends ArrayAdapter<String> {
        public LocationsAdapter(Context context, List<String> users) {
            super(context, 0, users);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // Get the data item for this position
            String location = getItem(position);
            // Check if an existing view is being reused, otherwise inflate the view
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.plan_details_locations_list_item, parent, false);
            }
            // Lookup view for data population
            TextView locationName = (TextView) convertView.findViewById(R.id.plan_details_locations_list_item_TextView);

            // Populate the data into the template view using the data object
            locationName.setText(location);
            // Return the completed view to render on screen
            return convertView;
        }
    }

    public static class PlanMembersHolder extends RecyclerView.ViewHolder {
        private final View mView;
        private final ImageView mPlanMemberImageResource;
        private final TextView mPlanMemberFirstNameLastName;

        public PlanMembersHolder(View itemView) {
            super(itemView);
            mView = itemView;
            mPlanMemberFirstNameLastName = (TextView) mView.findViewById(R.id.plan_details_members_list_item_textView);
            mPlanMemberImageResource = (ImageView) mView.findViewById(R.id.plan_details_members_list_item_imageView);
        }


        public void setmPlanMemberFirstNameLastName(String name) {
            mPlanMemberFirstNameLastName.setText(name);
        }

        public void setmPlanMemberImageResource(String imageResource){
//            mPlanMemberImageResource.setImageResource(imageResource);
//            Glide.with(mPlanMemberImageResource.getContext())
//                    .load(imageResource)
//                    .fitCenter()
//                    .into(mPlanMemberImageResource);
            Glide.with(mPlanMemberImageResource.getContext())
                    .load(imageResource)
                    .into(mPlanMemberImageResource);

        }
    }


    public static boolean setListViewHeightBasedOnItems(ListView listView) {

        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter != null) {

            int numberOfItems = listAdapter.getCount();

            // Get total height of all items.
            int totalItemsHeight = 0;
            for (int itemPos = 0; itemPos < numberOfItems; itemPos++) {
                View item = listAdapter.getView(itemPos, null, listView);
                item.measure(0, 0);
                totalItemsHeight += item.getMeasuredHeight();
            }

            // Get total height of all item dividers.
            int totalDividersHeight = listView.getDividerHeight() *
                    (numberOfItems - 1);

            // Set list height.
            ViewGroup.LayoutParams params = listView.getLayoutParams();
            params.height = totalItemsHeight + totalDividersHeight + 24;
            listView.setLayoutParams(params);
            listView.requestLayout();

            return true;

        } else {
            return false;
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        mPlanDatabaseReference.child(mPlanKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                PlanTemp mPlanTemp = dataSnapshot.getValue(PlanTemp.class);

                mPlanDescription.setText(mPlanTemp.getmDescription());
                Calendar tempCalendar = Calendar.getInstance();
                tempCalendar.setTimeInMillis(mPlanTemp.getmCreationDate());

                mPlanImageResource = mPlanTemp.getmImageBannerResource();
                loadBackdrop();

                SimpleDateFormat formatter = new SimpleDateFormat("EEE, d MMM h:mm a");
                String dateString = formatter.format(mPlanTemp.getmCreationDate());

                mPlanTimeValue.setText(dateString);
                mPlanEstimatedCostValue.setText(mPlanTemp.getmEstimatedCost());
                List<String> planLocations = mPlanTemp.getmPlanLocations();
                String locationsInPlan = "";
                if(planLocations != null) {
                    for (int i = 0; i < planLocations.size(); i++) {

                        if (i == 0) locationsInPlan += planLocations.get(i);
                        else locationsInPlan += ", " + planLocations.get(i);

                    }
                }else{

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
