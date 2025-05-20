package edu.northeastern.suyt.controller;

import java.util.ArrayList;
import java.util.List;

import edu.northeastern.suyt.model.RecyclingPost;

public class RecyclingPostController {
    // In a real app, this would interface with a database or API
    private List<RecyclingPost> posts;

    public RecyclingPostController() {
        // Initialize with sample data
        posts = new ArrayList<>();
        populateSamplePosts();
    }

    private void populateSamplePosts() {
        posts.add(new RecyclingPost(
                1,
                "EcoCreator",
                "Plastic Bottle Planters",
                "Transformed old plastic bottles into beautiful hanging planters for my herbs! It was a simple project that took about an hour, and now I have a lovely vertical garden in my kitchen window. To make it, I cut the bottles horizontally, punched drainage holes in the bottom, added soil and plants, then hung them with twine. Super easy and effective!",
                "plastic_bottle_planter.jpg",
                "Reuse",
                42,
                "2 hours ago"
        ));

        posts.add(new RecyclingPost(
                2,
                "CraftyCycler",
                "Cardboard Storage Organizer",
                "Made this desk organizer using old cardboard boxes. Perfect for storing pens and stationery! I used cereal boxes, cut them to size, covered them with decorative paper, and assembled with hot glue. Now my workspace is so much tidier, and I didn't have to buy a new organizer. Win-win!",
                "cardboard_organizer.jpg",
                "Reuse",
                28,
                "Yesterday"
        ));

        posts.add(new RecyclingPost(
                3,
                "GreenThumb",
                "Tin Can Herb Garden",
                "Repurposed old tin cans as planters for my windowsill herb garden. Added some paint for color! I removed the labels, sanded any sharp edges, punched drainage holes in the bottom, then painted them with leftover paint. Now I have fresh herbs for cooking, and they look so cute on my windowsill!",
                "tin_can_garden.jpg",
                "Reuse",
                37,
                "2 days ago"
        ));

        posts.add(new RecyclingPost(
                4,
                "EarthSaver",
                "Scrap Fabric Wall Art",
                "Used scrap fabric pieces to create this colorful wall hanging. Great way to use up fabric scraps! I had so many fabric pieces left over from sewing projects, and couldn't bear to throw them away. I arranged them by color to create a rainbow pattern and stitched them onto a backing. Now it's a focal point in my craft room!",
                "fabric_wall_art.jpg",
                "Reuse",
                54,
                "3 days ago"
        ));

        posts.add(new RecyclingPost(
                5,
                "RecycleKing",
                "Paper Mache Lampshade",
                "Created this unique lampshade using old newspapers and paper mache. Love how it turned out! I tore up old newspapers, mixed flour and water to make paste, and layered them over a balloon. After it dried, I popped the balloon, cut out the bottom, and added it to an old lamp base. The light has a lovely warm glow through the paper.",
                "paper_mache_lamp.jpg",
                "Recycle",
                63,
                "Last week"
        ));
    }

    public List<RecyclingPost> getAllPosts() {
        return posts;
    }

    public List<RecyclingPost> getPostsByCategory(String category) {
        // Filter posts by category
        List<RecyclingPost> filteredPosts = new ArrayList<>();

        for (RecyclingPost post : posts) {
            if (post.getCategory().equalsIgnoreCase(category)) {
                filteredPosts.add(post);
            }
        }

        return filteredPosts;
    }

    public RecyclingPost getPostById(int id) {
        for (RecyclingPost post : posts) {
            if (post.getId() == id) {
                return post;
            }
        }
        return null;
    }

    public void likePost(int postId) {
        RecyclingPost post = getPostById(postId);
        if (post != null) {
            post.setLikes(post.getLikes() + 1);
        }
    }
}
