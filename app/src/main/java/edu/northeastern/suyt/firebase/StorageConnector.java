package edu.northeastern.suyt.firebase;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class StorageConnector {

    private static StorageConnector instance;
    private final FirebaseStorage firebaseStorage;

    private StorageConnector() {
        // Initialize the Firebase Realtime Database
        firebaseStorage = FirebaseStorage.getInstance();
    }

    public static synchronized StorageConnector getInstance() {
        if (instance == null) {
            instance = new StorageConnector();
        }
        return instance;
    }

    public StorageReference getPostsReference() {
        return firebaseStorage.getReference("Posts");
    }
}
