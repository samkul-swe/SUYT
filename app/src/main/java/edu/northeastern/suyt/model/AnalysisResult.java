package edu.northeastern.suyt.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

public class AnalysisResult implements Parcelable {
    private String id;
    private String userId;
    private String itemName;
    private String imageUrl;
    private String localImagePath;
    private TrashItem trashItem;
    private long timestamp;
    private double latitude;
    private double longitude;

    public AnalysisResult() {
        this.timestamp = System.currentTimeMillis();
    }

    public AnalysisResult(String userId, String itemName, TrashItem trashItem) {
        this.id = generateId();
        this.userId = userId;
        this.itemName = itemName;
        this.trashItem = trashItem;
        this.timestamp = System.currentTimeMillis();
    }

    public AnalysisResult(String userId, String itemName, TrashItem trashItem, double latitude, double longitude) {
        this(userId, itemName, trashItem);
        this.latitude = latitude;
        this.longitude = longitude;
    }

    protected AnalysisResult(Parcel in) {
        id = in.readString();
        userId = in.readString();
        itemName = in.readString();
        imageUrl = in.readString();
        localImagePath = in.readString();
        trashItem = in.readParcelable(TrashItem.class.getClassLoader());
        timestamp = in.readLong();
        latitude = in.readDouble();
        longitude = in.readDouble();
    }

    public static final Creator<AnalysisResult> CREATOR = new Creator<AnalysisResult>() {
        @Override
        public AnalysisResult createFromParcel(Parcel in) {
            return new AnalysisResult(in);
        }

        @Override
        public AnalysisResult[] newArray(int size) {
            return new AnalysisResult[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(userId);
        dest.writeString(itemName);
        dest.writeString(imageUrl);
        dest.writeString(localImagePath);
        dest.writeParcelable(trashItem, flags);
        dest.writeLong(timestamp);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("id", id);
        result.put("userId", userId);
        result.put("itemName", itemName);
        result.put("imageUrl", imageUrl);
        result.put("timestamp", timestamp);
        result.put("latitude", latitude);
        result.put("longitude", longitude);

        if (trashItem != null) {
            result.put("trashItem", trashItem.toMap());
        }

        return result;
    }

    private String generateId() {
        return "analysis_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 1000);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getLocalImagePath() {
        return localImagePath;
    }

    public void setLocalImagePath(String localImagePath) {
        this.localImagePath = localImagePath;
    }

    public TrashItem getTrashItem() {
        return trashItem;
    }

    public void setTrashItem(TrashItem trashItem) {
        this.trashItem = trashItem;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    @Exclude
    public boolean hasLocation() {
        return latitude != 0.0 && longitude != 0.0;
    }

    @Exclude
    public String getFormattedTimestamp() {
        return new java.text.SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", java.util.Locale.getDefault())
                .format(new java.util.Date(timestamp));
    }
}