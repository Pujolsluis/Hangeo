package com.pujolsluis.android.hangeo;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Oficina on 20/01/2017.
 */

@IgnoreExtraProperties
public class PlanTemp {
    private String mAuthorID = "";
    private String mTitle = "";
    private String mDescription = " ";
    private String mImageBannerResource = " ";
    private String mType = " ";
    private Long mCreationDate = (long) 0;
    private List<String> mPlanMembers;

    public PlanTemp() {
        // Default constructor required for calls to DataSnapshot.getValue(Post.class)
    }

    public PlanTemp(String author, String title, Long creationDate){
        mAuthorID = author;
        mTitle = title;
        mCreationDate = creationDate;
        mPlanMembers = new ArrayList<>();
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

    public String getmImageBannerResource() {
        return mImageBannerResource;
    }

    public void setmImageBannerResource(String mImageBannerResource) {
        this.mImageBannerResource = mImageBannerResource;
    }

    public String getmType() {
        return mType;
    }

    public void setmType(String mType) {
        this.mType = mType;
    }

    public List<String> getmPlanMembers() {
        return mPlanMembers;
    }

    public void setmPlanMembers(List<String> mPlanMembers) {
        this.mPlanMembers = mPlanMembers;
    }

    public void addPlanMembers(String newMember){
        mPlanMembers.add(newMember);
    }

    public void removePlanMembers(String oldMember){
        if(!mPlanMembers.isEmpty())
            mPlanMembers.remove(oldMember);
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

        return result;
    }
}

