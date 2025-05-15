package edu.northeastern.suyt.controller;

import java.util.ArrayList;
import java.util.List;

import edu.northeastern.suyt.model.RecyclingPost;

public class RecyclingPostController {
    public List<RecyclingPost> getAllPosts() {
        // Dummy data for now
        List<RecyclingPost> posts = new ArrayList<>();

        posts.add(new RecyclingPost(
                1,
                "EcoCreator",
                "Plastic Bottle Planters",
                "Transformed old plastic bottles into beautiful hanging planters for my herbs!",
                "plastic_bottle_planter.jpg",
                "Reuse",
                42,
                "2 hours ago"
        ));

        posts.add(new RecyclingPost(
                2,
                "CraftyCycler",
                "Cardboard Storage Organizer",
                "Made this desk organizer using old cardboard boxes. Perfect for storing pens and stationery!",
                "cardboard_organizer.jpg",
                "Reuse",
                28,
                "Yesterday"
        ));

        posts.add(new RecyclingPost(
                3,
                "GreenThumb",
                "Tin Can Herb Garden",
                "Repurposed old tin cans as planters for my windowsill herb garden. Added some paint for color!",
                "tin_can_garden.jpg",
                "Reuse",
                37,
                "2 days ago"
        ));

        posts.add(new RecyclingPost(
                4,
                "EarthSaver",
                "Scrap Fabric Wall Art",
                "Used scrap fabric pieces to create this colorful wall hanging. Great way to use up fabric scraps!",
                "fabric_wall_art.jpg",
                "Reuse",
                54,
                "3 days ago"
        ));

        posts.add(new RecyclingPost(
                5,
                "RecycleKing",
                "Paper Mache Lampshade",
                "Created this unique lampshade using old newspapers and paper mache. Love how it turned out!",
                "paper_mache_lamp.jpg",
                "Recycle",
                63,
                "Last week"
        ));

        return posts;
    }

    public List<RecyclingPost> getPostsByCategory(String category) {
        // Filter posts by category
        List<RecyclingPost> allPosts = getAllPosts();
        List<RecyclingPost> filteredPosts = new ArrayList<>();

        for (RecyclingPost post : allPosts) {
            if (post.getCategory().equalsIgnoreCase(category)) {
                filteredPosts.add(post);
            }
        }

        return filteredPosts;
    }

    public void likePost(int postId) {
        // In a real app, this would update the like count in the database
        // For now, we'll just log it
        System.out.println("Liked post with ID: " + postId);
    }
}
