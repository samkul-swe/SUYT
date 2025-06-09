package edu.northeastern.suyt.model;

public class Achievement {
    private String id;
    private String title;
    private String description;
    private int flowerResourceId;
    private boolean unlocked;
    private int progress;
    private int maxProgress;
    private String unlockDate;
    private int gridPosition;

    public Achievement() {
    }

    public Achievement(String id, String title, String description, int flowerResourceId,
                       boolean unlocked, int progress, int maxProgress, String unlockDate, int gridPosition) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.flowerResourceId = flowerResourceId;
        this.unlocked = unlocked;
        this.progress = progress;
        this.maxProgress = maxProgress;
        this.unlockDate = unlockDate;
        this.gridPosition = gridPosition;
    }

    public static Achievement createLocked(String id, String title, String description,
                                           int flowerResourceId, int maxProgress, int gridPosition) {
        return new Achievement(id, title, description, flowerResourceId,
                false, 0, maxProgress, null, gridPosition);
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

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
        if (this.progress >= this.maxProgress) {
            this.unlocked = true;
        }
    }

    public int getMaxProgress() {
        return maxProgress;
    }

    public void setMaxProgress(int maxProgress) {
        this.maxProgress = maxProgress;
    }

    public String getUnlockDate() {
        return unlockDate;
    }

    public void setUnlockDate(String unlockDate) {
        this.unlockDate = unlockDate;
    }

    public int getGridPosition() {
        return gridPosition;
    }

    public void setGridPosition(int gridPosition) {
        this.gridPosition = gridPosition;
    }

    public float getProgressPercentage() {
        if (maxProgress <= 0) {
            return unlocked ? 100f : 0f;
        }
        return ((float) progress / maxProgress) * 100f;
    }
}
