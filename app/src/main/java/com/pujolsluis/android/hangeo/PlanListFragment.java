package com.pujolsluis.android.hangeo;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import static android.R.attr.key;

/**
 * A simple {@link Fragment} subclass.
 */
public class PlanListFragment extends Fragment {

    private static final String LOG_TAG = PlanListFragment.class.getSimpleName();
    private FirebaseAuth mFirebaseAuth;
    private FirebaseRecyclerAdapter mAdapter;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mUserPlansReference;
    private DatabaseReference mDatabasePlansReference;
    private String mUserID;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        RecyclerView recyclerView = (RecyclerView) inflater.inflate(R.layout.fragment_plan_list, container, false);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext()));
        //setupRecyclerView(recyclerView);

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mFirebaseAuth = FirebaseAuth.getInstance();
        mUserID = mFirebaseAuth.getCurrentUser().getUid();

        mUserPlansReference = mFirebaseDatabase.getReference().child("userProfiles").child(mUserID).child("mPlans");
        mDatabasePlansReference = mFirebaseDatabase.getReference().child("plans");

        mAdapter = new FirebaseRecyclerAdapter<Boolean, PlanHolder>(Boolean.class, R.layout.list_item , PlanHolder.class, mUserPlansReference) {
            @Override
            public void populateViewHolder(final PlanHolder planViewHolder, Boolean planItem, int position) {
                Log.e(LOG_TAG, "Ref Position: " + position + " Value: " + key);
                String key = this.getRef(position).getKey();

                mDatabasePlansReference.child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        PlanTemp planTemp = dataSnapshot.getValue(PlanTemp.class);

                        planViewHolder.setmPlanTitle(planTemp.getmTitle());
                        planViewHolder.setmPlanImageHeaderImageView(planTemp.getmImageBannerResource());
                        List<String> planLocations = planTemp.getmPlanLocations();
                        String locationsInPlan = "";
                        if(planLocations != null) {
                            for (int i = 0; i < planLocations.size(); i++) {

                                if (i == 0) locationsInPlan += planLocations.get(i);
                                else locationsInPlan += ", " + planLocations.get(i);

                            }
                        }else{
                            locationsInPlan = "Select your destinations";
                        }

                        planViewHolder.setmPlanLocations(locationsInPlan);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }

        };

        recyclerView.setAdapter(mAdapter);

        return recyclerView;
    }


    public static class PlanHolder extends RecyclerView.ViewHolder {
        private final TextView mPlanTitle;
        private final ImageView mPlanImageHeaderImageView;
        private final TextView mPlanLocations;

        public PlanHolder(View itemView) {
            super(itemView);
            mPlanTitle = (TextView) itemView.findViewById(R.id.plan_title_textView);
            mPlanLocations = (TextView) itemView.findViewById(R.id.plan_locations_textView);
            mPlanImageHeaderImageView = (ImageView) itemView.findViewById(R.id.plan_background_header);
        }


        public void setmPlanTitle(String name) {
            mPlanTitle.setText(name);
        }

        public void setmPlanLocations(String text) {
            mPlanLocations.setText(text);
        }

        public void setmPlanImageHeaderImageView(int imageResource){
            mPlanImageHeaderImageView.setImageResource(imageResource);
            Glide.with(mPlanImageHeaderImageView.getContext())
                    .load(imageResource)
                    .fitCenter()
                    .into(mPlanImageHeaderImageView);
        }
    }


}
