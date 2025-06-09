package edu.northeastern.suyt.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class Reuse implements Parcelable {
    private String reuseInfo;
    private String craftsPossible;
    private String moneyNeededForCraft;
    private String timeNeededForCraft;

    public Reuse() {
    }

    public Reuse(String reuseInfo, String craftsPossible, String moneyNeededForCraft, String timeNeededForCraft) {
        this.reuseInfo = reuseInfo;
        this.craftsPossible = craftsPossible;
        this.moneyNeededForCraft = moneyNeededForCraft;
        this.timeNeededForCraft = timeNeededForCraft;
    }

    public String getReuseInfo() {
        return reuseInfo;
    }

    public void setReuseInfo(String reuseInfo) {
        this.reuseInfo = reuseInfo;
    }

    public String getCraftsPossible() {
        return craftsPossible;
    }

    public void setCraftsPossible(String craftsPossible) {
        this.craftsPossible = craftsPossible;
    }

    public String getMoneyNeededForCraft() {
        return moneyNeededForCraft;
    }

    public void setMoneyNeededForCraft(String moneyNeededForCraft) {
        this.moneyNeededForCraft = moneyNeededForCraft;
    }

    public String getTimeNeededForCraft() {
        return timeNeededForCraft;
    }

    public void setTimeNeededForCraft(String timeNeededForCraft) {
        this.timeNeededForCraft = timeNeededForCraft;
    }

    @NonNull
    @Override
    public String toString() {
        return "Reuse{" +
                "reuseInfo='" + reuseInfo + '\'' +
                ", craftsPossible='" + craftsPossible + '\'' +
                ", moneyNeededForCraft='" + moneyNeededForCraft + '\'' +
                ", timeNeededForCraft='" + timeNeededForCraft + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int flags) {
        parcel.writeString(reuseInfo);
        parcel.writeString(craftsPossible);
        parcel.writeString(timeNeededForCraft);
        parcel.writeString(moneyNeededForCraft);
    }

    public static final Creator<Reuse> CREATOR = new Creator<Reuse>() {
        @Override
        public Reuse createFromParcel(Parcel in) {
            return new Reuse(in);
        }

        @Override
        public Reuse[] newArray(int size) {
            return new Reuse[size];
        }
    };

    protected Reuse(Parcel in) {
        reuseInfo = in.readString();
        craftsPossible = in.readString();
        timeNeededForCraft = in.readString();
        moneyNeededForCraft = in.readString();
    }
}
