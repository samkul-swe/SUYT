package edu.northeastern.suyt.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Post implements Parcelable {
    private String postID;
    private String postedBy;
    private String postTitle;
    private String postDescription;
    private String postImage;
    private String postCategory; // e.g., "Recycle", "Reuse", "Reduce" etc.
    private int numberOfLikes;
    private String postedOn;
    private String updatedOn;

    public Post() {}

    public Post(String postID, String postedBy, String postTitle, String postDescription,
                String postImage, String postCategory, int numberOfLikes, String postedOn) {
        this.postID = postID;
        this.postedBy = postedBy;
        this.postTitle = postTitle;
        this.postDescription = postDescription;
        this.postImage = postImage;
        this.postCategory = postCategory;
        this.numberOfLikes = numberOfLikes;
        this.postedOn = postedOn;
    }

    protected Post(Parcel in) {
        postID = in.readString();
        postedBy = in.readString();
        postTitle = in.readString();
        postDescription = in.readString();
        postImage = in.readString();
        postCategory = in.readString();
        numberOfLikes = in.readInt();
        postedOn = in.readString();
    }

    public static final Creator<Post> CREATOR = new Creator<>() {
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
        dest.writeString(postID);
        dest.writeString(postedBy);
        dest.writeString(postTitle);
        dest.writeString(postDescription);
        dest.writeString(postImage);
        dest.writeString(postCategory);
        dest.writeInt(numberOfLikes);
        dest.writeString(postedOn);
    }

    public String getPostID() {
        return postID;
    }

    public void setPostID(String postID) {
        this.postID = postID;
    }

    public String getPostedBy() {
        return postedBy;
    }

    public void setPostedBy(String postedBy) {
        this.postedBy = postedBy;
    }

    public String getPostTitle() {
        return postTitle;
    }

    public void setPostTitle(String postTitle) {
        this.postTitle = postTitle;
    }

    public String getPostDescription() {
        return postDescription;
    }

    public void setPostDescription(String postDescription) {
        this.postDescription = postDescription;
    }

    public String getPostImage() {
        return postImage;
    }

    public void setPostImage(String postImage) {
        this.postImage = postImage;
    }

    public String getPostCategory() {
        return postCategory;
    }

    public void setPostCategory(String postCategory) {
        this.postCategory = postCategory;
    }

    public int getNumberOfLikes() {
        return numberOfLikes;
    }

    public void setNumberOfLikes(int numberOfLikes) {
        this.numberOfLikes = numberOfLikes;
    }

    public String getPostedOn() {
        return postedOn;
    }

    public void setPostedOn(String postedOn) {
        this.postedOn = postedOn;
    }
}
