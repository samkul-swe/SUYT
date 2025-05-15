package edu.northeastern.suyt.model;

public class UserStats {
    private int recyclePoints;
    private int reusePoints;
    private int reducePoints;
    private int totalActivities;

    public UserStats() {
        // Default constructor
        this.recyclePoints = 0;
        this.reusePoints = 0;
        this.reducePoints = 0;
        this.totalActivities = 0;
    }

    public UserStats(int recyclePoints, int reusePoints, int reducePoints, int totalActivities) {
        this.recyclePoints = recyclePoints;
        this.reusePoints = reusePoints;
        this.reducePoints = reducePoints;
        this.totalActivities = totalActivities;
    }

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

    public int getTotalActivities() {
        return totalActivities;
    }

    public void setTotalActivities(int totalActivities) {
        this.totalActivities = totalActivities;
    }

    public int getTotalPoints() {
        return recyclePoints + reusePoints + reducePoints;
    }
}
