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
public class UserProfile {
    private String mFirstName = "";
    private String mLastName = "";
    private String mImageResource = "";
    private String mSex = "";
    private String mCountry = "";
    private Long mFriendsSize = (long) 0;
    private Long mPlansSize = (long) 0;
    private Map<String, Boolean> mPlans;
    private List<String> mFriends;
    private Long mBirthdate = (long) 0;
    private String mEmail = "";

    public UserProfile(){
        mPlans = new HashMap<>();
        mFriends = new ArrayList<String>();

    }

    public String getmFirstName() {
        return mFirstName;
    }

    public void setmFirstName(String mFirstName) {
        this.mFirstName = mFirstName;
    }

    public String getmLastName() {
        return mLastName;
    }

    public void setmLastName(String mLastName) {
        this.mLastName = mLastName;
    }

    public String getmImageResource() {
        return mImageResource;
    }

    public void setmImageResource(String mImageResource) {
        this.mImageResource = mImageResource;
    }

    public String getmSex() {
        return mSex;
    }

    public void setmSex(String mSex) {
        this.mSex = mSex;
    }

    public String getmCountry() {
        return mCountry;
    }

    public void setmCountry(String mCountry) {
        this.mCountry = mCountry;
    }

    public Long getmFriendsSize() {
        return mFriendsSize;
    }

    public void setmFriendsSize(Long mFriendsSize) {
        this.mFriendsSize = mFriendsSize;
    }

    public Long getmPlansSize() {
        return mPlansSize;
    }

    public void setmPlansSize(Long mPlansSize) {
        this.mPlansSize = mPlansSize;
    }

    public Map<String, Boolean> getmPlans() {
        return mPlans;
    }

    public void setmPlans(Map<String, Boolean> mPlans) {
        this.mPlans = mPlans;
    }

    public Long getmBirthdate() {
        return mBirthdate;
    }

    public void setmBirthdate(Long mBirthdate) {
        this.mBirthdate = mBirthdate;
    }

    public String getmEmail() {
        return mEmail;
    }

    public void setmEmail(String mEmail) {
        this.mEmail = mEmail;
    }

    public List<String> getmFriends() {
        return mFriends;
    }

    public void setmFriends(List<String> mFriends) {
        this.mFriends = mFriends;
    }



    @Exclude
    public void addFriend(String newFriend){
        mFriends.add(newFriend);
        mFriendsSize++;
    }

    @Exclude
    public void removeFriend(String friend){
        if(!mFriends.isEmpty()){
            mFriends.remove(friend);
            mPlansSize--;
        }
    }

    @Exclude
    public void addPlan(String newPlan){
        mPlans.put(newPlan, true);
        mPlansSize++;
    }

    @Exclude
    public void removePlan(String plan){
        if(!mPlans.isEmpty()) {
            mPlans.remove(plan);
            mPlansSize--;
        }
    }


    @Exclude
    public Map<String, Object> toMap(){
        HashMap<String, Object> result = new HashMap<>();
        result.put("mFirstName", mFirstName);
        result.put("mLastName", mLastName);
        result.put("mImageResource", mImageResource);
        result.put("mSex", mSex);
        result.put("mCountry", mCountry);
        result.put("mFriendsSize", mFriends.size());
        result.put("mPlansSize", mPlans.size());
        result.put("mPlans", mPlans);
        result.put("mFriends", mFriends);
        result.put("mBirthdate", mBirthdate);
        result.put("mEmail", mEmail);


        return result;
    }
}
