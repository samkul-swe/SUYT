package edu.northeastern.suyt.ui.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import edu.northeastern.suyt.R;
import edu.northeastern.suyt.controller.RecyclingPostController;

public class CreatePostActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_PICK_IMAGE = 2;

    private ImageView postImageView;
    private EditText titleEditText;
    private EditText descriptionEditText;
    private RadioGroup categoryRadioGroup;
    private Button takePhotoButton;
    private Button choosePhotoButton;
    private Button createPostButton;

    private RecyclingPostController postController;
    private String currentPhotoPath;
    private boolean hasImage = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);

        // Set up toolbar with back button
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Create Post");
        }

        // Initialize controller
        postController = new RecyclingPostController();

        // Initialize views
        postImageView = findViewById(R.id.post_image_view);
        titleEditText = findViewById(R.id.title_edit_text);
        descriptionEditText = findViewById(R.id.description_edit_text);
        categoryRadioGroup = findViewById(R.id.category_radio_group);
        takePhotoButton = findViewById(R.id.take_photo_button);
        choosePhotoButton = findViewById(R.id.choose_photo_button);
        createPostButton = findViewById(R.id.create_post_button);

        // Set up button click listeners
        takePhotoButton.setOnClickListener(v -> takePhoto());
        choosePhotoButton.setOnClickListener(v -> choosePhotoFromGallery());
        createPostButton.setOnClickListener(v -> createPost());
    }

    private void takePhoto() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Ensure there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the file where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                Toast.makeText(this, "Error creating image file", Toast.LENGTH_SHORT).show();
            }

            // Continue only if the file was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.suyt.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        } else {
            Toast.makeText(this, "No camera app available", Toast.LENGTH_SHORT).show();
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void choosePhotoFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE) {
                // Show the captured image
                postImageView.setImageURI(Uri.parse(currentPhotoPath));
                hasImage = true;
            } else if (requestCode == REQUEST_PICK_IMAGE && data != null) {
                // Show the selected image from gallery
                Uri selectedImageUri = data.getData();
                postImageView.setImageURI(selectedImageUri);
                hasImage = true;
            }
        }
    }

    private void createPost() {
        // Validate inputs
        String title = titleEditText.getText().toString().trim();
        String description = descriptionEditText.getText().toString().trim();

        if (title.isEmpty()) {
            titleEditText.setError("Title is required");
            return;
        }

        if (description.isEmpty()) {
            descriptionEditText.setError("Description is required");
            return;
        }

        if (!hasImage) {
            Toast.makeText(this, "Please add an image for your post", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get selected category
        int selectedRadioButtonId = categoryRadioGroup.getCheckedRadioButtonId();
        if (selectedRadioButtonId == -1) {
            Toast.makeText(this, "Please select a category", Toast.LENGTH_SHORT).show();
            return;
        }

        RadioButton selectedRadioButton = findViewById(selectedRadioButtonId);
        String category = selectedRadioButton.getText().toString();

        // In a real app, create the post in the database
        // For now, just simulate success
        boolean success = true;

        if (success) {
            Toast.makeText(this, "Post created successfully!", Toast.LENGTH_SHORT).show();

            // Return to previous screen
            finish();
        } else {
            Toast.makeText(this, "Failed to create post. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // Handle back button click
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}