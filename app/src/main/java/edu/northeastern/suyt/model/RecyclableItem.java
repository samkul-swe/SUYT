package edu.northeastern.suyt.model;

public class RecyclableItem {
    private int id;
    private String name;
    private String imageUrl;
    private boolean isRecyclable;
    private boolean isReusable;
    private boolean isReducible;
    private String recycleInfo;
    private String reuseInfo;
    private String reduceInfo;

    public RecyclableItem(int id, String name, String imageUrl, boolean isRecyclable,
                          boolean isReusable, boolean isReducible, String recycleInfo,
                          String reuseInfo, String reduceInfo) {
        this.id = id;
        this.name = name;
        this.imageUrl = imageUrl;
        this.isRecyclable = isRecyclable;
        this.isReusable = isReusable;
        this.isReducible = isReducible;
        this.recycleInfo = recycleInfo;
        this.reuseInfo = reuseInfo;
        this.reduceInfo = reduceInfo;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public boolean isRecyclable() {
        return isRecyclable;
    }

    public void setRecyclable(boolean recyclable) {
        isRecyclable = recyclable;
    }

    public boolean isReusable() {
        return isReusable;
    }

    public void setReusable(boolean reusable) {
        isReusable = reusable;
    }

    public boolean isReducible() {
        return isReducible;
    }

    public void setReducible(boolean reducible) {
        isReducible = reducible;
    }

    public String getRecycleInfo() {
        return recycleInfo;
    }

    public void setRecycleInfo(String recycleInfo) {
        this.recycleInfo = recycleInfo;
    }

    public String getReuseInfo() {
        return reuseInfo;
    }

    public void setReuseInfo(String reuseInfo) {
        this.reuseInfo = reuseInfo;
    }

    public String getReduceInfo() {
        return reduceInfo;
    }

    public void setReduceInfo(String reduceInfo) {
        this.reduceInfo = reduceInfo;
    }
}
