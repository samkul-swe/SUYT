package edu.northeastern.suyt.model;

public class Recycle {
    private String recycleInfo;
    private String nearestRecyclingCenter;
    private String recyclingHours;
    private String suggestedBin;

    public Recycle(String recycleInfo, String nearestRecyclingCenter, String recyclingHours, String suggestedBin) {
        this.recycleInfo = recycleInfo;
        this.nearestRecyclingCenter = nearestRecyclingCenter;
        this.recyclingHours = recyclingHours;
        this.suggestedBin = suggestedBin;
    }

    public String getRecycleInfo() {
        return recycleInfo;
    }

    public String getNearestRecyclingCenter() {
        return nearestRecyclingCenter;
    }

    public String getRecyclingHours() {
        return recyclingHours;
    }

    public String getSuggestedBin() {
        return suggestedBin;
    }

    public void setRecycleInfo(String recycleInfo) {
        this.recycleInfo = recycleInfo;
    }

    public void setNearestRecyclingCenter(String nearestRecyclingCenter) {
        this.nearestRecyclingCenter = nearestRecyclingCenter;
    }

    public void setRecyclingHours(String recyclingHours) {
        this.recyclingHours = recyclingHours;
    }

    public void setSuggestedBin(String suggestedBin) {
        this.suggestedBin = suggestedBin;
    }
}
