package edu.northeastern.suyt.utils;

public class GeneralHelper {

    public GeneralHelper() {}

    public String calculateUserRank(int totalPoints) {
        if (totalPoints >= 1000) {
            return "Eco Master";
        } else if (totalPoints >= 500) {
            return "Green Champion";
        } else if (totalPoints >= 200) {
            return "Eco Warrior";
        } else if (totalPoints >= 50) {
            return "Green Helper";
        } else {
            return "Plant Soldier";
        }
    }
}
