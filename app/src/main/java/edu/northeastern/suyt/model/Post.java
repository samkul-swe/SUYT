package edu.northeastern.suyt.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Post implements Parcelable {
    private String id;
    private String username;
    private String title;
    private String description;
    private String imageUrl;
    private String category; // e.g., "Recycle", "Reuse", "Reduce" etc.
    private int likes;
    private String date;

    public Post() {}

    public Post(String id, String username, String title, String description,
                String imageUrl, String category, int likes, String date) {
        this.id = id;
        this.username = username;
        this.title = title;
        this.description = description;
        this.imageUrl = imageUrl;
        this.category = category;
        this.likes = likes;
        this.date = date;
    }

    // Constructor for Parcelable
    protected Post(Parcel in) {
        id = in.readString();
        username = in.readString();
        title = in.readString();
        description = in.readString();
        imageUrl = in.readString();
        category = in.readString();
        likes = in.readInt();
        date = in.readString();
    }

    // Parcelable Creator
    public static final Creator<Post> CREATOR = new Creator<Post>() {
        @Override
        public Post createFromParcel(Parcel in) {
            return new Post(in);
        }

        @Override
        public Post[] newArray(int size) {
            return new Post[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(username);
        dest.writeString(title);
        dest.writeString(description);
        dest.writeString(imageUrl);
        dest.writeString(category);
        dest.writeInt(likes);
        dest.writeString(date);
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
