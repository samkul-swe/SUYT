package edu.northeastern.suyt.model;

public class Reuse {
    private String reuseInfo;
    private String craftsPossible;
    private String moneyNeededForCraft;
    private String timeNeededForCraft;

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
}
