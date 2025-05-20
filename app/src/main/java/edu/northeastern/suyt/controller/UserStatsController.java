package edu.northeastern.suyt.controller;

import edu.northeastern.suyt.model.UserStats;

public class UserStatsController {
    // In a real app, this would interface with a database or API

    public UserStats getUserStats() {
        // For now, return dummy data to simulate user stats
        return new UserStats(125, 85, 210, 42);
    }

    public String getUserRank() {
        // This would calculate the user's rank based on their stats
        // For now, return a dummy rank
        UserStats stats = getUserStats();
        int totalPoints = stats.getTotalPoints();

        if (totalPoints > 400) {
            return "Rank: Eco Warrior";
        } else if (totalPoints > 300) {
            return "Rank: Sustainability Pro";
        } else if (totalPoints > 200) {
            return "Rank: Green Guardian";
        } else if (totalPoints > 100) {
            return "Rank: Earth Protector";
        } else {
            return "Rank: Recycling Rookie";
        }
    }

    public int getAheadOfUsersPercentage() {
        // This would calculate what percentage of users the current user is ahead of
        // For now, return a dummy percentage
        UserStats stats = getUserStats();
        int totalPoints = stats.getTotalPoints();

        // Simple calculation based on total points for demo purposes
        // In a real app, this would compare to actual user statistics
        return Math.min(100, totalPoints / 5);
    }

    public float calculateCO2Saved() {
        // This would calculate estimated CO2 savings based on activities
        // For now, return a dummy value
        UserStats stats = getUserStats();

        // Simple calculation based on points for demo purposes
        float recycleImpact = stats.getRecyclePoints() * 0.15f;  // kg CO2 per recycle point
        float reuseImpact = stats.getReusePoints() * 0.25f;      // kg CO2 per reuse point
        float reduceImpact = stats.getReducePoints() * 0.3f;     // kg CO2 per reduce point

        return recycleImpact + reuseImpact + reduceImpact;
    }

    public String getRecycleInfo() {
        return "RECYCLING FACTS:\n\n" +
                "• Recycling one aluminum can saves enough energy to run a TV for 3 hours\n" +
                "• The average person has the opportunity to recycle more than 25,000 cans in their lifetime\n" +
                "• Recycling plastic takes 88% less energy than making it from raw materials\n" +
                "• Glass can be recycled endlessly without any loss in quality or purity\n\n" +
                "YOUR IMPACT:\n" +
                "Your recycling efforts have saved approximately " + (getUserStats().getRecyclePoints() * 0.5) + " kg of CO2 emissions!";
    }

    public String getReuseInfo() {
        return "REUSING TIPS:\n\n" +
                "• Reuse glass jars for food storage instead of buying plastic containers\n" +
                "• Turn old t-shirts into cleaning rags instead of buying new ones\n" +
                "• Use reusable shopping bags to reduce plastic waste\n" +
                "• Repurpose furniture instead of buying new pieces\n\n" +
                "YOUR IMPACT:\n" +
                "By reusing items, you've prevented approximately " + (getUserStats().getReusePoints() * 0.3) + " kg of waste from entering landfills!";
    }

    public String getReduceInfo() {
        return "REDUCING WASTE:\n\n" +
                "• Buy in bulk to reduce packaging waste\n" +
                "• Choose products with minimal or recyclable packaging\n" +
                "• Opt for digital subscriptions instead of paper magazines\n" +
                "• Repair items instead of replacing them\n\n" +
                "YOUR IMPACT:\n" +
                "Your waste reduction efforts have saved approximately " + (getUserStats().getReducePoints() * 0.7) + " kg of resources from being consumed!";
    }
}
