package edu.northeastern.suyt.controller;

import java.util.ArrayList;
import java.util.List;

import edu.northeastern.suyt.model.TrashTip;

public class RecyclingTipController {
    public List<TrashTip> getAllTips() {
        // Dummy data for now
        List<TrashTip> tips = new ArrayList<>();
        tips.add(new TrashTip("Reduce Plastic Usage", "Use reusable bags instead of plastic bags", "Reduce"));
        tips.add(new TrashTip("Reuse Glass Containers", "Wash and reuse glass jars for storage", "Reuse"));
        tips.add(new TrashTip("Recycle Paper Properly", "Make sure paper is clean and dry before recycling", "Recycle"));
        tips.add(new TrashTip("Reduce Water Waste", "Fix leaky faucets and install water-saving fixtures", "Reduce"));
        tips.add(new TrashTip("Reuse Clothing", "Donate or repurpose old clothing instead of throwing it away", "Reuse"));
        tips.add(new TrashTip("Recycle Electronics", "Take old electronics to specialized recycling centers", "Recycle"));
        return tips;
    }

    public List<TrashTip> getTipsByCategory(String category) {
        // Filter tips by category
        List<TrashTip> allTips = getAllTips();
        List<TrashTip> filteredTips = new ArrayList<>();

        for (TrashTip tip : allTips) {
            if (tip.getCategory().equalsIgnoreCase(category)) {
                filteredTips.add(tip);
            }
        }

        return filteredTips;
    }
}
