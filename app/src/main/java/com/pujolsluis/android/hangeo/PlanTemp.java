package com.pujolsluis.android.hangeo;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Oficina on 20/01/2017.
 */

@IgnoreExtraProperties
public class PlanTemp {
    private String mPlanKey = "";
    private String mAuthorID = "";
    private String mTitle = "";
    private String mDescription = " ";
    private int mImageBannerResource = 0;
    private String mType = " ";
    private Long mCreationDate = (long) 0;
    private Map<String, Boolean> mPlanMembers;
    private List<String> mPlanLocations;
    private String mEstimatedCost = "$$";

    public PlanTemp() {
        // Default constructor required for calls to DataSnapshot.getValue(Post.class)
    }

    public PlanTemp(String author, String title, Long creationDate){
        mAuthorID = author;
        mTitle = title;
        mCreationDate = creationDate;
        mPlanMembers = new HashMap<>();
    }

    public String getmAuthorID() {
        return mAuthorID;
    }

    public void setmAuthorID(String mAuthorID) {
        this.mAuthorID = mAuthorID;
    }

    public Long getmCreationDate() {
        return mCreationDate;
    }

    public void setmCreationDate(Long mCreationDate) {
        this.mCreationDate = mCreationDate;
    }

    public String getmDescription() {
        return mDescription;
    }

    public void setmDescription(String mDescription) {
        this.mDescription = mDescription;
    }

    public String getmTitle() {
        return mTitle;
    }

    public void setmTitle(String mTitle) {
        this.mTitle = mTitle;
    }

    public int getmImageBannerResource() {
        return mImageBannerResource;
    }

    public void setmImageBannerResource(int mImageBannerResource) {
        this.mImageBannerResource = mImageBannerResource;
    }

    public String getmType() {
        return mType;
    }

    public void setmType(String mType) {
        this.mType = mType;
    }

    public Map<String, Boolean> getmPlanMembers() {
        return mPlanMembers;
    }

    public void setmPlanMembers(Map<String, Boolean> mPlanMembers) {
        this.mPlanMembers = mPlanMembers;
    }

    public void addPlanMembers(String newMember){
        mPlanMembers.put(newMember, true);
    }

    public void removePlanMembers(String oldMember){
        if(!mPlanMembers.isEmpty())
            mPlanMembers.remove(oldMember);
    }

    public List<String> getmPlanLocations() {
        return mPlanLocations;
    }

    public void setmPlanLocations(List<String> mPlanLocations) {
        this.mPlanLocations = mPlanLocations;
    }

    public String getmPlanKey() {
        return mPlanKey;
    }

    public void setmPlanKey(String mPlanKey) {
        this.mPlanKey = mPlanKey;
    }

    public String getmEstimatedCost() {
        return mEstimatedCost;
    }

    public void setmEstimatedCost(String mEstimatedCost) {
        this.mEstimatedCost = mEstimatedCost;
    }

    @Exclude
    public Map<String, Object> toMap(){
        HashMap<String, Object> result = new HashMap<>();
        result.put("mAuthorID", mAuthorID);
        result.put("mTitle", mTitle);
        result.put("mDescription", mDescription);
        result.put("mImageBannerResource", mImageBannerResource);
        result.put("mType", mType);
        result.put("mCreationDate", mCreationDate);
        result.put("mPlanMembers", mPlanMembers);
        result.put("mPlanLocations", mPlanLocations);
        result.put("mEstimatedCost", mEstimatedCost);

        return result;
    }
}

