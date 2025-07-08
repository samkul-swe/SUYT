package edu.northeastern.suyt.model;

public class Achievement {
    private String id;
    private String title;
    private String description;
    private int flowerResourceId;
    private boolean unlocked;
    private int pointsToUnlock;
    private String unlockDate;

    public Achievement() {
    }

    public Achievement(String id, String title, String description, int flowerResourceId,
                       boolean unlocked, int pointsToUnlock, String unlockDate) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.flowerResourceId = flowerResourceId;
        this.unlocked = unlocked;
        this.pointsToUnlock = pointsToUnlock;
        this.unlockDate = unlockDate;
    }

    public static Achievement createLocked(String id, String title, String description,
                                           int flowerResourceId, int pointsToUnlock) {
        return new Achievement(id, title, description, flowerResourceId,
                false, pointsToUnlock, null);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getFlowerResourceId() {
        return flowerResourceId;
    }

    public void setFlowerResourceId(int flowerResourceId) {
        this.flowerResourceId = flowerResourceId;
    }

    public boolean isUnlocked() {
        return unlocked;
    }

    public void setUnlocked(boolean unlocked) {
        this.unlocked = unlocked;
    }

    public int getPointsToUnlock() {
        return pointsToUnlock;
    }

    public void setPointsToUnlock(int pointsToUnlock) {
        this.pointsToUnlock = pointsToUnlock;
        if (this.pointsToUnlock >= this.pointsToUnlock) {
            this.unlocked = true;
        }
    }

    public String getUnlockDate() {
        return unlockDate;
    }

    public void setUnlockDate(String unlockDate) {
        this.unlockDate = unlockDate;
    }
}
