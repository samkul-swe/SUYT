package edu.northeastern.suyt.controller;

import java.util.ArrayList;
import java.util.List;

import edu.northeastern.suyt.model.RecyclingTip;

public class RecyclingTipController {
    public List<RecyclingTip> getAllTips() {
        // Dummy data for now
        List<RecyclingTip> tips = new ArrayList<>();
        tips.add(new RecyclingTip(1, "Reduce Plastic Usage", "Use reusable bags instead of plastic bags", "", "Reduce"));
        tips.add(new RecyclingTip(2, "Reuse Glass Containers", "Wash and reuse glass jars for storage", "", "Reuse"));
        tips.add(new RecyclingTip(3, "Recycle Paper Properly", "Make sure paper is clean and dry before recycling", "", "Recycle"));
        return tips;
    }

    public List<RecyclingTip> getTipsByCategory(String category) {
        // Filter tips by category
        List<RecyclingTip> allTips = getAllTips();
        List<RecyclingTip> filteredTips = new ArrayList<>();

        for (RecyclingTip tip : allTips) {
            if (tip.getCategory().equalsIgnoreCase(category)) {
                filteredTips.add(tip);
            }
        }

        return filteredTips;
    }
}
