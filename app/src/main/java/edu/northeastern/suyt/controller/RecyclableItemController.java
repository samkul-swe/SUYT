package edu.northeastern.suyt.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import edu.northeastern.suyt.model.RecyclableItem;

public class RecyclableItemController {

    // In a real app, this would interface with a database or API
    private List<RecyclableItem> items;

    public RecyclableItemController() {
        // Initialize with sample data
        items = new ArrayList<>();
        populateSampleItems();
    }

    private void populateSampleItems() {
        // Plastic Bottle
        items.add(new RecyclableItem(
                1,
                "Plastic Bottle",
                "plastic_bottle.jpg",
                true,
                true,
                true,
                "RECYCLING PLASTIC BOTTLES:\n\n" +
                        "• Rinse the bottle and remove the cap (caps can be recycled separately in many areas)\n" +
                        "• Check the recycling number on the bottom (usually #1 PET or #2 HDPE are widely accepted)\n" +
                        "• Flatten the bottle to save space in your recycling bin\n" +
                        "• Place in your curbside recycling bin\n\n" +
                        "Plastic bottles are one of the most commonly recycled items. When recycled, they can be turned into new bottles, clothing, carpet, and more!",

                "REUSING PLASTIC BOTTLES:\n\n" +
                        "• Create a self-watering planter by cutting the bottle in half\n" +
                        "• Use as storage for small items like buttons or paper clips\n" +
                        "• Make a bird feeder by cutting windows in the sides\n" +
                        "• Create a piggy bank with a small slot cut in the cap\n" +
                        "• Use as a watering can by poking small holes in the cap\n\n" +
                        "Remember to thoroughly clean any bottle before reusing, especially for food or plant-related projects.",

                "REDUCING PLASTIC BOTTLE USAGE:\n\n" +
                        "• Use a reusable water bottle instead of buying single-use bottles\n" +
                        "• Install a water filter at home rather than buying bottled water\n" +
                        "• Choose beverages in glass bottles or aluminum cans which have higher recycling rates\n" +
                        "• Buy concentrates or powdered drinks to mix with water\n" +
                        "• Support companies that use alternative packaging\n\n" +
                        "By reducing plastic bottle usage, you can save money and significantly decrease plastic waste!"
        ));

        // Cardboard Box
        items.add(new RecyclableItem(
                2,
                "Cardboard Box",
                "cardboard_box.jpg",
                true,
                true,
                true,
                "RECYCLING CARDBOARD BOXES:\n\n" +
                        "• Remove any packing materials, tape, and labels\n" +
                        "• Break down the box to save space\n" +
                        "• Keep it dry - wet cardboard is difficult to recycle\n" +
                        "• Bundle with other cardboard or place in recycling bin\n\n" +
                        "Cardboard has one of the highest recycling rates of any material. Recycled cardboard uses 75% less energy than making new cardboard and reduces the need for tree harvesting.",

                "REUSING CARDBOARD BOXES:\n\n" +
                        "• Storage containers for seasonal items\n" +
                        "• Create drawer dividers and organizers\n" +
                        "• Make children's toys like play kitchens or cars\n" +
                        "• Create cat scratching posts or pet toys\n" +
                        "• Use as garden mulch or compost material\n" +
                        "• Make seedling pots for gardening\n\n" +
                        "Sturdy cardboard boxes can be reused multiple times before recycling.",

                "REDUCING CARDBOARD BOX USAGE:\n\n" +
                        "• Opt for minimal packaging when shopping online\n" +
                        "• Choose products with less packaging or bulk options\n" +
                        "• Request that multiple items be shipped together\n" +
                        "• Buy locally when possible to reduce shipping boxes\n" +
                        "• Use reusable bags when shopping in person\n\n" +
                        "By being conscious of packaging when making purchases, you can significantly reduce the amount of cardboard waste you generate."
        ));

        // Aluminum Can
        items.add(new RecyclableItem(
                3,
                "Aluminum Can",
                "aluminum_can.jpg",
                true,
                true,
                true,
                "RECYCLING ALUMINUM CANS:\n\n" +
                        "• Rinse the can to remove any residue\n" +
                        "• You can leave the tab attached - it's recyclable too\n" +
                        "• Crushing cans is optional and depends on your local recycling guidelines\n" +
                        "• Place in your curbside recycling bin\n\n" +
                        "Aluminum can be recycled indefinitely without losing quality. Recycling an aluminum can saves 95% of the energy needed to make a new one from raw materials!",

                "REUSING ALUMINUM CANS:\n\n" +
                        "• Create pen holders or desk organizers\n" +
                        "• Make candle holders by cutting decorative patterns\n" +
                        "• Use as seedling starters for plants\n" +
                        "• Create wind chimes or garden decorations\n" +
                        "• Make camping or emergency stoves\n\n" +
                        "When reusing aluminum cans, be careful of sharp edges when cutting.",

                "REDUCING ALUMINUM CAN USAGE:\n\n" +
                        "• Use a reusable water bottle or travel mug\n" +
                        "• Buy beverages in larger containers to reduce the number of cans\n" +
                        "• Make your own beverages at home (coffee, tea, etc.)\n" +
                        "• Use soda makers for carbonated drinks\n" +
                        "• Choose concentrates or powders when available\n\n" +
                        "While aluminum is highly recyclable, reducing usage still saves resources and energy."
        ));

        // Glass Jar
        items.add(new RecyclableItem(
                4,
                "Glass Jar",
                "glass_jar.jpg",
                true,
                true,
                true,
                "RECYCLING GLASS JARS:\n\n" +
                        "• Remove lids and rinse the jar thoroughly\n" +
                        "• Labels can usually stay on - they're removed during processing\n" +
                        "• Sort by color if required by your local recycling program\n" +
                        "• Never put broken glass in recycling bins\n\n" +
                        "Glass can be recycled endlessly without loss in quality or purity. Recycled glass reduces emissions and consumption of raw materials, and uses less energy than making new glass.",

                "REUSING GLASS JARS:\n\n" +
                        "• Food storage containers for leftovers, bulk foods, or homemade preserves\n" +
                        "• Drinking glasses or cocktail glasses\n" +
                        "• Candle holders or vases\n" +
                        "• Terrariums or small plant containers\n" +
                        "• Bath salt or homemade beauty product containers\n" +
                        "• Organization for small items like buttons, nails, or office supplies\n\n" +
                        "Glass jars are durable and non-toxic, making them excellent for reuse projects.",

                "REDUCING GLASS JAR USAGE:\n\n" +
                        "• Buy items in bulk to reduce packaging\n" +
                        "• Make homemade versions of jarred foods (sauces, jams, etc.)\n" +
                        "• Choose larger containers instead of multiple small ones\n" +
                        "• Reuse the jars you already have for storage and food preservation\n" +
                        "• Shop at farmers markets where packaging is often minimal\n\n" +
                        "While glass is an environmentally friendly packaging option, reducing consumption is still beneficial."
        ));

        // Paper
        items.add(new RecyclableItem(
                5,
                "Paper",
                "paper.jpg",
                true,
                true,
                true,
                "RECYCLING PAPER:\n\n" +
                        "• Keep paper clean and dry\n" +
                        "• Remove plastic windows from envelopes\n" +
                        "• Shredded paper should be contained in a paper bag\n" +
                        "• Staples and paper clips are usually OK, but best to remove\n" +
                        "• Don't recycle tissues, paper towels, or greasy paper\n\n" +
                        "Paper can be recycled 5-7 times before fibers become too short. Recycling paper saves trees, water, and energy!",

                "REUSING PAPER:\n\n" +
                        "• Use the back of printed paper for notes\n" +
                        "• Create notepads from one-sided printed paper\n" +
                        "• Use for arts and crafts projects\n" +
                        "• Make homemade paper from scraps\n" +
                        "• Use as packaging material or gift wrap\n" +
                        "• Create origami or paper decorations\n\n" +
                        "Get creative with paper reuse to extend its life before recycling.",

                "REDUCING PAPER USAGE:\n\n" +
                        "• Go digital for bills, statements, and communications\n" +
                        "• Use digital note-taking apps instead of paper notes\n" +
                        "• Print double-sided when printing is necessary\n" +
                        "• Use cloth napkins instead of paper\n" +
                        "• Opt out of junk mail and catalogs\n" +
                        "• Use reusable cloths instead of paper towels\n\n" +
                        "Reducing paper usage saves resources and reduces waste, even if paper is recyclable."
        ));
    }

    public RecyclableItem getItemById(int id) {
        for (RecyclableItem item : items) {
            if (item.getId() == id) {
                return item;
            }
        }
        return null;
    }

    public RecyclableItem getItemByName(String name) {
        for (RecyclableItem item : items) {
            if (item.getName().equalsIgnoreCase(name)) {
                return item;
            }
        }
        return null;
    }

    public List<RecyclableItem> getAllItems() {
        return items;
    }

    public RecyclableItem getRandomItem() {
        // For demo purposes, return a random item
        Random random = new Random();
        int randomIndex = random.nextInt(items.size());
        return items.get(randomIndex);
    }

    // In a real app, this would use ML to identify items from images
    public RecyclableItem identifyItem(byte[] imageData) {
        // Simulated ML identification - just returns a random item for demo
        return getRandomItem();
    }
}
