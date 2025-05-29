package edu.northeastern.suyt.model;

import androidx.annotation.NonNull;

public class Reduce {
    private String reduceInfo;
    private String howManyShouldICollect;
    private String moneyExpected;
    private String otherSuggestions;

    public Reduce() {
    }

    public Reduce(String reduceInfo, String howManyShouldICollect, String moneyExpected, String otherSuggestions) {
        this.reduceInfo = reduceInfo;
        this.howManyShouldICollect = howManyShouldICollect;
        this.moneyExpected = moneyExpected;
        this.otherSuggestions = otherSuggestions;
    }

    public String getReduceInfo() {
        return reduceInfo;
    }

    public void setReduceInfo(String reduceInfo) {
        this.reduceInfo = reduceInfo;
    }

    public String getHowManyShouldICollect() {
        return howManyShouldICollect;
    }

    public void setHowManyShouldICollect(String howManyShouldICollect) {
        this.howManyShouldICollect = howManyShouldICollect;
    }

    public String getMoneyExpected() {
        return moneyExpected;
    }

    public void setMoneyExpected(String moneyExpected) {
        this.moneyExpected = moneyExpected;
    }

    public String getOtherSuggestions() {
        return otherSuggestions;
    }

    public void setOtherSuggestions(String otherSuggestions) {
        this.otherSuggestions = otherSuggestions;
    }

    @NonNull
    @Override
    public String toString() {
        return "Reduce{" +
                "reduceInfo='" + reduceInfo + '\'' +
                ", howManyShouldICollect='" + howManyShouldICollect + '\'' +
                ", moneyExpected='" + moneyExpected + '\'' +
                ", otherSuggestions='" + otherSuggestions + '\'' +
                '}';
    }
}
