package edu.northeastern.suyt.model;

import com.google.firebase.ai.type.Schema;

import java.util.List;
import java.util.Map;

public class TrashItem {
    private String name;
    private boolean isRecyclable;
    private boolean isReusable;
    private boolean isReducible;
    private String recycleInfo;
    private String reuseInfo;
    private String reduceInfo;

    public TrashItem() {
    }

    public TrashItem(String name, boolean isRecyclable,
                          boolean isReusable, boolean isReducible, String recycleInfo,
                          String reuseInfo, String reduceInfo) {
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

    public Schema getSchema() {
        return Schema.obj(
            Map.of("name", Schema.str(),
                    "isRecyclable", Schema.enumeration(List.of("true", "false")),
                    "isReusable", Schema.enumeration(List.of("true", "false")),
                    "isReducible", Schema.enumeration(List.of("true", "false")),
                    "recycleInfo", Schema.str(),
                    "reuseInfo", Schema.str(),
                    "reduceInfo", Schema.str()
            )
        );
    }
}
