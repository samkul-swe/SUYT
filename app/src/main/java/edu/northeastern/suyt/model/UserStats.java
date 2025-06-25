package edu.northeastern.suyt.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class UserStats implements Parcelable {
    private int recyclePoints;
    private int reusePoints;
    private int reducePoints;

    public UserStats() {
        // Default constructor
        this.recyclePoints = 0;
        this.reusePoints = 0;
        this.reducePoints = 0;
    }

    public UserStats(int recyclePoints, int reusePoints, int reducePoints) {
        this.recyclePoints = recyclePoints;
        this.reusePoints = reusePoints;
        this.reducePoints = reducePoints;
    }

    protected UserStats(Parcel in) {
        recyclePoints = in.readInt();
        reusePoints = in.readInt();
        reducePoints = in.readInt();
    }

    public static final Creator<UserStats> CREATOR = new Creator<>() {
        @Override
        public UserStats createFromParcel(Parcel in) {
            return new UserStats(in);
        }

        @Override
        public UserStats[] newArray(int size) {
            return new UserStats[size];
        }
    };

    // Getters and Setters
    public int getRecyclePoints() {
        return recyclePoints;
    }

    public void setRecyclePoints(int recyclePoints) {
        this.recyclePoints = recyclePoints;
    }

    public int getReusePoints() {
        return reusePoints;
    }

    public void setReusePoints(int reusePoints) {
        this.reusePoints = reusePoints;
    }

    public int getReducePoints() {
        return reducePoints;
    }

    public void setReducePoints(int reducePoints) {
        this.reducePoints = reducePoints;
    }

    public int getTotalPoints() {
        return recyclePoints + reusePoints + reducePoints;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeInt(recyclePoints);
        parcel.writeInt(reusePoints);
        parcel.writeInt(reducePoints);
    }
}
