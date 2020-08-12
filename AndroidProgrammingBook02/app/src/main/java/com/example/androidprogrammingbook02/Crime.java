package com.example.androidprogrammingbook02;

import java.util.Date;
import java.util.UUID;

public class Crime {

    public UUID getID() {
        return mID;
    }

    public void setID(UUID ID) {
        mID = ID;
    }

    private UUID mID;

    ///////////////////////////
    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    private String mTitle;

    /////////////////////////
    public Date getDate() {
        return mDate;
    }

    public void setDate(Date date) {
        mDate = date;
    }

    private Date mDate;

    //////////////////////
    public boolean isSolved() {
        return mSolved;
    }

    public void setSolved(boolean solved) {
        mSolved = solved;
    }

    private boolean mSolved;

    //////////////////////////
    // 1 = serious, need police
    // 0 = normal

    public int isRequirePolice() {
        return mSeriousness;
    }

    public void setSeriousness(int seriousness) {
        mSeriousness = seriousness;
    }

    public int getSeriousness() {
        return mSeriousness;
    }

    private int mSeriousness;

    /////////////////////////////

    public String getSuspect(){
        return mSuspect;
    }

    public void setSuspect(String suspect){
        mSuspect = suspect;
    }

    private String mSuspect;

    ////////////////////////////

    public String getPhotoFilename() {
        return "IMG_"+getID().toString()+".jpg";
    }

    public Crime() {
        this(UUID.randomUUID());
    }

    public Crime(UUID uuid){
        mID = uuid;
        mDate = new Date();
    }
}
