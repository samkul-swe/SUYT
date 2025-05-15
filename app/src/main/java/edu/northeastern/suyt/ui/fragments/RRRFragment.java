package edu.northeastern.suyt.ui.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import edu.northeastern.suyt.R;
import edu.northeastern.suyt.controller.RecyclableItemController;
import edu.northeastern.suyt.model.RecyclableItem;


public class RRRFragment extends Fragment implements View.OnClickListener {


    private static final int REQUEST_CAMERA_PERMISSION = 100;
    private static final int REQUEST_STORAGE_PERMISSION = 101;
    private static final int REQUEST_IMAGE_CAPTURE = 102;
    private static final int REQUEST_PICK_IMAGE = 103;

    private ImageView itemImageView;
    private TextView itemNameTextView;
    private Button recycleButton;
    private Button reuseButton;
    private Button reduceButton;
    private FloatingActionButton camereFab;
    private FloatingActionButton downloadsFab;
    private TextView infoContentTextView;
    private CardView infoCardView;
    private ProgressBar progressBar;

    private RecyclableItemController itemController;
    private RecyclableItem currentItem;
    private String currentPhotoPath;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_rrr, container, false);

        // Initialize controllers
        itemController = new RecyclableItemController();

        // Initialize views
        itemImageView = view.findViewById(R.id.item_image_view);
        itemNameTextView = view.findViewById(R.id.item_name_text_view);
        recycleButton = view.findViewById(R.id.recycle_button);
        reuseButton = view.findViewById(R.id.reuse_button);
        reduceButton = view.findViewById(R.id.reduce_button);
        camereFab = view.findViewById(R.id.camera_fab);
        downloadsFab = view.findViewById(R.id.gallery_fab);
        infoContentTextView = view.findViewById(R.id.info_content_text_view);
        infoCardView = view.findViewById(R.id.info_card_view);
        progressBar = view.findViewById(R.id.progress_bar);

        // Set click listeners
        recycleButton.setOnClickListener(this);
        reuseButton.setOnClickListener(this);
        reduceButton.setOnClickListener(this);

        // Set FAB listeners
        camereFab.setOnClickListener(v -> {
            if (checkPermission(Manifest.permission.CAMERA, REQUEST_CAMERA_PERMISSION)) {
                openCamera();
            }
        });

        // Update the gallery FAB to open file picker
        downloadsFab.setOnClickListener(v -> {
            if (checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE, REQUEST_STORAGE_PERMISSION)) {
                openFilePicker();
            }
        });

        // Initialize with placeholder state
        displayPlaceholderState();

        return view;
    }

    private void displayPlaceholderState() {
        itemImageView.setImageResource(R.drawable.ic_image_placeholder);
        itemNameTextView.setText("Scan an item");
        infoContentTextView.setText("Take a photo or select an image from your device to see recycling, reusing, and reducing options.");
        infoCardView.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary));

        // Default button state
        resetButtons();
    }

    private boolean checkPermission(String permission, int requestCode) {
        if (ContextCompat.checkSelfPermission(requireContext(), permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{permission}, requestCode);
            return false;
        }
        return true;
    }

    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Ensure there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
            // Create the file where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                Toast.makeText(requireContext(), "Error creating image file", Toast.LENGTH_SHORT).show();
            }

            // Continue only if the file was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(requireContext(),
                        "com.example.suyt.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        } else {
            Toast.makeText(requireContext(), "No camera app available", Toast.LENGTH_SHORT).show();
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void openFilePicker() {
        // Use a simple approach that works across Android versions
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");

        try {
            startActivityForResult(Intent.createChooser(intent, "Select Image"), REQUEST_PICK_IMAGE);
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Error opening file picker", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            progressBar.setVisibility(View.VISIBLE);

            if (requestCode == REQUEST_IMAGE_CAPTURE) {
                // Process the full-size image from photoUri
                processCapturedPhoto();

            } else if (requestCode == REQUEST_PICK_IMAGE && data != null) {
                // Handle document selection from Downloads or other location
                Uri selectedFileUri = data.getData();

                if (selectedFileUri != null) {
                    try {
                        // Log the URI for debugging
                        System.out.println("Selected URI: " + selectedFileUri.toString());

                        // Load and display the image
                        Bitmap bitmap = getBitmapFromUri(selectedFileUri);
                        itemImageView.setImageBitmap(bitmap);

                        // Analyze image
                        analyzeImage(bitmap);

                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(requireContext(), "Failed to load selected image", Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                    }
                } else {
                    Toast.makeText(requireContext(), "Error: No image was selected", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                }
            }
        } else if (resultCode == Activity.RESULT_CANCELED) {
            Toast.makeText(requireContext(), "Operation cancelled", Toast.LENGTH_SHORT).show();
        }
    }

    private void processCapturedPhoto() {
        if (currentPhotoPath != null) {
            try {
                // Get the dimensions of the View
                int targetW = itemImageView.getWidth();
                int targetH = itemImageView.getHeight();

                // If the view dimensions are zero, use default values
                if (targetW <= 0) targetW = 800;
                if (targetH <= 0) targetH = 800;

                // Get the dimensions of the bitmap
                BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                bmOptions.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(currentPhotoPath, bmOptions);
                int photoW = bmOptions.outWidth;
                int photoH = bmOptions.outHeight;

                // Determine how much to scale down the image
                int scaleFactor = Math.max(1, Math.min(photoW/targetW, photoH/targetH));

                // Decode the image file into a Bitmap sized to fill the View
                bmOptions.inJustDecodeBounds = false;
                bmOptions.inSampleSize = scaleFactor;
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
                    bmOptions.inPurgeable = true;
                }

                Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath, bmOptions);
                itemImageView.setImageBitmap(bitmap);

                // Analyze the image
                analyzeImage(bitmap);

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(requireContext(), "Error loading captured image", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
            }
        } else {
            Toast.makeText(requireContext(), "Error: No image was captured", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
        }
    }

    private Bitmap getBitmapFromUri(Uri uri) throws IOException {
        // Get input stream from the URI
        InputStream input = requireActivity().getContentResolver().openInputStream(uri);

        // First decode with inJustDecodeBounds=true to check dimensions
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(input, null, options);
        input.close();

        // The new size we want to scale to
        int targetW = 800;
        int targetH = 800;

        // Calculate inSampleSize
        int scaleFactor = Math.max(1, Math.min(
                options.outWidth / targetW,
                options.outHeight / targetH));

        // Decode bitmap with inSampleSize set
        options = new BitmapFactory.Options();
        options.inSampleSize = scaleFactor;
        options.inJustDecodeBounds = false;

        input = requireActivity().getContentResolver().openInputStream(uri);
        Bitmap bitmap = BitmapFactory.decodeStream(input, null, options);
        input.close();

        return bitmap;
    }

    private void analyzeImage(Bitmap bitmap) {
        // In a real app, this would use ML to identify the item
        // For now, we'll simulate this with a delay and random item

        new android.os.Handler().postDelayed(() -> {
            // Get a random item (for demo purposes)
            currentItem = itemController.getRandomItem();

            // Update UI with the item
            itemNameTextView.setText(currentItem.getName());

            // Reset buttons
            resetButtons();

            // Set default content to Recycle
            setSelectedButton(recycleButton);
            showRecycleInfo();

            progressBar.setVisibility(View.GONE);
        }, 1500); // Simulate processing delay
    }

    @Override
    public void onClick(View v) {
        if (currentItem == null) {
            Toast.makeText(requireContext(), "Please scan an item first", Toast.LENGTH_SHORT).show();
            return;
        }

        // Reset all buttons
        resetButtons();

        // Set selected button and show appropriate info
        if (v.getId() == R.id.recycle_button) {
            setSelectedButton(recycleButton);
            showRecycleInfo();
        } else if (v.getId() == R.id.reuse_button) {
            setSelectedButton(reuseButton);
            showReuseInfo();
        } else if (v.getId() == R.id.reduce_button) {
            setSelectedButton(reduceButton);
            showReduceInfo();
        }
    }

    private void resetButtons() {
        recycleButton.setBackgroundResource(R.drawable.button_normal);
        reuseButton.setBackgroundResource(R.drawable.button_normal);
        reduceButton.setBackgroundResource(R.drawable.button_normal);

        recycleButton.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.black));
        reuseButton.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.black));
        reduceButton.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.black));
    }

    private void setSelectedButton(Button button) {
        if (button == recycleButton) {
            button.setBackgroundResource(R.drawable.button_selected_recycle);
            infoCardView.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.colorRecycle));
        } else if (button == reuseButton) {
            button.setBackgroundResource(R.drawable.button_selected_reuse);
            infoCardView.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.colorReuse));
        } else if (button == reduceButton) {
            button.setBackgroundResource(R.drawable.button_selected_reduce);
            infoCardView.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.colorReduce));
        }

        button.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white));
    }

    private void showRecycleInfo() {
        if (currentItem != null) {
            infoContentTextView.setText(currentItem.getRecycleInfo());
        }
    }

    private void showReuseInfo() {
        if (currentItem != null) {
            infoContentTextView.setText(currentItem.getReuseInfo());
        }
    }

    private void showReduceInfo() {
        if (currentItem != null) {
            infoContentTextView.setText(currentItem.getReduceInfo());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (requestCode == REQUEST_CAMERA_PERMISSION) {
                openCamera();
            } else if (requestCode == REQUEST_STORAGE_PERMISSION) {
                openFilePicker();
            }
        } else {
            String permissionType = (requestCode == REQUEST_CAMERA_PERMISSION) ? "Camera" : "Storage";
            Toast.makeText(requireContext(), permissionType + " permission is needed to use this feature", Toast.LENGTH_SHORT).show();
        }
    }
}