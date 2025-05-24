package edu.northeastern.suyt.model;

public class UserStats {
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
}
