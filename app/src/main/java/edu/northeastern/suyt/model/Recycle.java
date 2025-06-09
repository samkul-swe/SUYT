package edu.northeastern.suyt.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class Recycle implements Parcelable {
    private String recycleInfo;
    private String nearestRecyclingCenter;
    private String recyclingHours;
    private String suggestedBin;

    public Recycle() {
    }

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

    @NonNull
    @Override
    public String toString() {
        return "Recycle{" +
                "recycleInfo='" + recycleInfo + '\'' +
                ", nearestRecyclingCenter='" + nearestRecyclingCenter + '\'' +
                ", recyclingHours='" + recyclingHours + '\'' +
                ", suggestedBin='" + suggestedBin + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int flags) {
        parcel.writeString(recycleInfo);
        parcel.writeString(nearestRecyclingCenter);
        parcel.writeString(suggestedBin);
        parcel.writeString(recyclingHours);
    }

    public static final Creator<Recycle> CREATOR = new Creator<Recycle>() {
        @Override
        public Recycle createFromParcel(Parcel in) {
            return new Recycle(in);
        }

        @Override
        public Recycle[] newArray(int size) {
            return new Recycle[size];
        }
    };

    protected Recycle(Parcel in) {
        recycleInfo = in.readString();
        nearestRecyclingCenter = in.readString();
        suggestedBin = in.readString();
        recyclingHours = in.readString();
    }
}
