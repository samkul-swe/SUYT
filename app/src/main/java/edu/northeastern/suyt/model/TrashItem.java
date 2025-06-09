package edu.northeastern.suyt.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.google.firebase.ai.type.Schema;
import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TrashItem implements Parcelable {
    private String name;
    private boolean isRecyclable;
    private boolean isReusable;
    private boolean isReducible;
    private Recycle recycleInfo;
    private Reuse reuseInfo;
    private Reduce reduceInfo;

    public TrashItem() {
    }

    public TrashItem(String name, boolean isRecyclable,
                          boolean isReusable, boolean isReducible, Recycle recycleInfo,
                          Reuse reuseInfo, Reduce reduceInfo) {
        this.name = name;
        this.isRecyclable = isRecyclable;
        this.isReusable = isReusable;
        this.isReducible = isReducible;
        this.recycleInfo = recycleInfo;
        this.reuseInfo = reuseInfo;
        this.reduceInfo = reduceInfo;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isRecyclable() {
        return isRecyclable;
    }

    public void setIsRecyclable(String recyclable) {
        isRecyclable = Boolean.valueOf(recyclable);
    }

    public boolean isReusable() {
        return isReusable;
    }

    public void setIsReusable(String reusable) {
        isReusable = Boolean.valueOf(reusable);
    }

    public boolean isReducible() {
        return isReducible;
    }

    public void setIsReducible(String reducible) {
        isReducible = Boolean.valueOf(reducible);
    }

    public Recycle getRecycleInfo() {
        return recycleInfo;
    }

    public void setRecycleInfo(Recycle recycleInfo) {
        this.recycleInfo = recycleInfo;
    }

    public Reuse getReuseInfo() {
        return reuseInfo;
    }

    public void setReuseInfo(Reuse reuseInfo) {
        this.reuseInfo = reuseInfo;
    }

    public Reduce getReduceInfo() {
        return reduceInfo;
    }

    public void setReduceInfo(Reduce reduceInfo) {
        this.reduceInfo = reduceInfo;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("name", name);
        result.put("recyclable", isRecyclable);
        result.put("reusable", isReusable);
        result.put("reducible", isReducible);

        if (recycleInfo != null) {
            Map<String, Object> recycleMap = new HashMap<>();
            recycleMap.put("recycleInfo", recycleInfo.getRecycleInfo());
            recycleMap.put("nearestRecyclingCenter", recycleInfo.getNearestRecyclingCenter());
            recycleMap.put("suggestedBin", recycleInfo.getSuggestedBin());
            recycleMap.put("recyclingHours", recycleInfo.getRecyclingHours());
            result.put("recycleInfo", recycleMap);
        }

        if (reuseInfo != null) {
            Map<String, Object> reuseMap = new HashMap<>();
            reuseMap.put("reuseInfo", reuseInfo.getReuseInfo());
            reuseMap.put("craftsPossible", reuseInfo.getCraftsPossible());
            reuseMap.put("timeNeededForCraft", reuseInfo.getTimeNeededForCraft());
            reuseMap.put("moneyNeededForCraft", reuseInfo.getMoneyNeededForCraft());
            result.put("reuseInfo", reuseMap);
        }

        if (reduceInfo != null) {
            Map<String, Object> reduceMap = new HashMap<>();
            reduceMap.put("reduceInfo", reduceInfo.getReduceInfo());
            reduceMap.put("howManyShouldICollect", reduceInfo.getHowManyShouldICollect());
            reduceMap.put("moneyExpected", reduceInfo.getMoneyExpected());
            reduceMap.put("otherSuggestions", reduceInfo.getOtherSuggestions());
            result.put("reduceInfo", reduceMap);
        }

        return result;
    }

    public Schema getSchema() {
        return Schema.obj(
            Map.of("name", Schema.str(),
                "isRecyclable", Schema.enumeration(List.of("true", "false")),
                "isReusable", Schema.enumeration(List.of("true", "false")),
                "isReducible", Schema.enumeration(List.of("true", "false")),
                "recycleInfo", Schema.obj(
                    Map.of("recycleInfo", Schema.str(),
                            "nearestRecyclingCenter", Schema.str(),
                            "recyclingHours", Schema.str(),
                            "suggestedBin", Schema.str()
                    )
                ),
                "reuseInfo", Schema.obj(
                    Map.of("reuseInfo", Schema.str(),
                            "craftsPossible", Schema.str(),
                            "moneyNeededForCraft", Schema.str(),
                            "timeNeededForCraft", Schema.str()
                    )
                ),
                "reduceInfo", Schema.obj(
                    Map.of("reduceInfo", Schema.str(),
                            "howManyShouldICollect", Schema.str(),
                            "moneyExpected", Schema.str(),
                            "otherSuggestions", Schema.str()
                    )
                )
            )
        );
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int flags) {
        parcel.writeString(name);
        parcel.writeByte((byte) (isRecyclable ? 1 : 0));
        parcel.writeByte((byte) (isReusable ? 1 : 0));
        parcel.writeByte((byte) (isReducible ? 1 : 0));
        parcel.writeParcelable(recycleInfo, flags);
        parcel.writeParcelable(reuseInfo, flags);
        parcel.writeParcelable(reduceInfo, flags);
    }

    public static final Creator<TrashItem> CREATOR = new Creator<TrashItem>() {
        @Override
        public TrashItem createFromParcel(Parcel in) {
            return new TrashItem(in);
        }

        @Override
        public TrashItem[] newArray(int size) {
            return new TrashItem[size];
        }
    };

    protected TrashItem(Parcel in) {
        name = in.readString();
        isRecyclable = in.readByte() != 0;
        isReusable = in.readByte() != 0;
        isReducible = in.readByte() != 0;
        recycleInfo = in.readParcelable(Recycle.class.getClassLoader());
        reuseInfo = in.readParcelable(Reuse.class.getClassLoader());
        reduceInfo = in.readParcelable(Reduce.class.getClassLoader());
    }
}
