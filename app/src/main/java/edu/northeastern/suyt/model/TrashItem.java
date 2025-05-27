package edu.northeastern.suyt.model;

import com.google.firebase.ai.type.Schema;

import java.util.List;
import java.util.Map;

public class TrashItem {
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
}
