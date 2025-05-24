package edu.northeastern.suyt.firebase.repository.storage;

import android.net.Uri;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import edu.northeastern.suyt.firebase.StorageConnector;

public class PostImageRepository {
    private final StorageReference postImageRef;
    private Thread workerThread;

    public PostImageRepository() {
        postImageRef = StorageConnector.getInstance().getPostsReference();
    }

    public void uploadPostImage(Uri imageUri, String postID) {
        if (workerThread == null || !workerThread.isAlive()) {
            workerThread = new Thread(() -> {
                try {
                    // Upload the file to Firebase Storage
                    UploadTask uploadTask = postImageRef.child(postID).putFile(imageUri);

                    // Wait for the upload to complete
                    Task<Uri> urlTask = uploadTask.continueWithTask(task -> {
                        if (!task.isSuccessful()) {
                            throw Objects.requireNonNull(task.getException());
                        }

                        // Continue with the task to get the download URL
                        return postImageRef.child(postID + "_" + UUID.randomUUID().toString() + ".jpg").getDownloadUrl();
                    }).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Uri downloadUri = task.getResult();
                            // Use the download URL as needed
                            System.out.println("Image upload successful. Download URL: " + downloadUri.toString());
                        } else {
                            // Handle any errors
                            Exception exception = task.getException();
                            System.err.println("Error uploading image: " + exception.getMessage());
                        }
                    });

                    Tasks.await(urlTask);
                } catch (InterruptedException | ExecutionException e) {
                    System.err.println("Error uploading image: " + e.getMessage());
                    Thread.currentThread().interrupt();
                }
            });
            workerThread.start();
        }
    }

    public Uri getPostImage(String post_image) {
        Task<Uri> urlTask = postImageRef.child(post_image).getDownloadUrl();
        while (!urlTask.isSuccessful()) ;
        return urlTask.getResult();
    }
}
