package edu.northeastern.suyt.ui.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log; // Added for logging

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

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
import java.util.Map; // Added for ActivityResultContracts.RequestMultiplePermissions

import edu.northeastern.suyt.R;
import edu.northeastern.suyt.controller.RecyclableItemController;
import edu.northeastern.suyt.model.RecyclableItem;


public class RRRFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "RRRFragment"; // Tag for logging

    // Removed old request codes as they are now handled by ActivityResultLauncher
    // private static final int REQUEST_CAMERA_PERMISSION = 100;
    // private static final int REQUEST_STORAGE_PERMISSION = 101;
    // private static final int REQUEST_IMAGE_CAPTURE = 102;
    // private static final int REQUEST_PICK_IMAGE = 103;

    private ImageView itemImageView;
    private TextView itemNameTextView;
    private Button recycleButton;
    private Button reuseButton;
    private Button reduceButton;
    private TextView infoContentTextView;
    private CardView infoCardView;
    private ProgressBar progressBar;

    private RecyclableItemController itemController;
    private RecyclableItem currentItem;
    private String currentPhotoPath; // Path for captured camera image

    // ActivityResultLauncher for Camera
    private ActivityResultLauncher<Uri> takePictureLauncher;
    private Uri cameraPhotoUri; // URI where the camera photo will be saved

    // ActivityResultLauncher for Gallery/File Picker
    private ActivityResultLauncher<String[]> pickImageLauncher; // For Android 13+ permissions
    private ActivityResultLauncher<Intent> pickImageFromGalleryLauncher; // For launching the actual picker

    // ActivityResultLauncher for Camera Permissions
    private ActivityResultLauncher<String> requestCameraPermissionLauncher;

    // ActivityResultLauncher for Storage Permissions (for pre-Android 13)
    private ActivityResultLauncher<String[]> requestStoragePermissionLauncher;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize ActivityResultLaunchers in onCreate or before fragment state is restored
        setupActivityResultLaunchers();
    }

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
        FloatingActionButton cameraFab = view.findViewById(R.id.camera_fab);
        FloatingActionButton downloadsFab = view.findViewById(R.id.gallery_fab); // Renamed from downloadsFab to galleryFab in XML for clarity
        infoContentTextView = view.findViewById(R.id.info_content_text_view);
        infoCardView = view.findViewById(R.id.info_card_view);
        progressBar = view.findViewById(R.id.progress_bar);

        // Set click listeners
        recycleButton.setOnClickListener(this);
        reuseButton.setOnClickListener(this);
        reduceButton.setOnClickListener(this);

        // Set FAB listeners
        cameraFab.setOnClickListener(v -> checkAndRequestCameraPermission());
        downloadsFab.setOnClickListener(v -> checkAndRequestStoragePermissions());

        // Initialize with placeholder state
        displayPlaceholderState();

        return view;
    }

    private void setupActivityResultLaunchers() {
        // Launcher for taking pictures
        takePictureLauncher = registerForActivityResult(new ActivityResultContracts.TakePicture(), result -> {
            if (result) { // Image was successfully captured
                progressBar.setVisibility(View.VISIBLE);
                processCapturedPhoto();
            } else {
                Toast.makeText(requireContext(), "Photo capture cancelled", Toast.LENGTH_SHORT).show();
            }
        });

        // Launcher for picking images from gallery/files (using ACTION_OPEN_DOCUMENT for broader access)
        // This is the actual launcher for the Intent
        pickImageFromGalleryLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                progressBar.setVisibility(View.VISIBLE);
                Uri selectedFileUri = result.getData().getData();

                if (selectedFileUri != null) {
                    try {
                        Log.d(TAG, "Selected URI: " + selectedFileUri.toString());
                        Bitmap bitmap = getBitmapFromUri(selectedFileUri);
                        itemImageView.setImageBitmap(bitmap);
                        analyzeImage(bitmap);
                    } catch (IOException e) {
                        Log.e(TAG, "Failed to load selected image", e);
                        Toast.makeText(requireContext(), "Failed to load selected image", Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                    }
                } else {
                    Toast.makeText(requireContext(), "Error: No image was selected", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                }
            } else if (result.getResultCode() == Activity.RESULT_CANCELED) {
                Toast.makeText(requireContext(), "Image selection cancelled", Toast.LENGTH_SHORT).show();
            }
        });

        // Launcher for requesting Camera permission
        requestCameraPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
                openCamera();
            } else {
                Toast.makeText(requireContext(), "Camera permission is needed to take photos.", Toast.LENGTH_SHORT).show();
            }
        });

        // Launcher for requesting Storage permissions (for pre-Android 13)
        requestStoragePermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
            boolean allGranted = true;
            for (Map.Entry<String, Boolean> entry : result.entrySet()) {
                if (!entry.getValue()) {
                    allGranted = false;
                    break;
                }
            }
            if (allGranted) {
                openFilePicker();
            } else {
                Toast.makeText(requireContext(), "Storage permissions are required to select images from your device.", Toast.LENGTH_LONG).show();
            }
        });
    }


    private void displayPlaceholderState() {
        itemImageView.setImageResource(R.drawable.ic_image_placeholder); // Ensure this drawable exists
        itemNameTextView.setText("Scan an item");
        infoContentTextView.setText("Take a photo or select an image from your device to see recycling, reusing, and reducing options.");
        infoCardView.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary)); // Ensure colorPrimary exists

        // Default button state
        resetButtons();
    }

    // --- Permission Check and Request Methods ---

    private void checkAndRequestCameraPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            openCamera();
        } else {
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void checkAndRequestStoragePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13 (API 33) and higher
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED) {
                openFilePicker();
            } else {
                // Request the new granular media permission
                requestStoragePermissionLauncher.launch(new String[]{Manifest.permission.READ_MEDIA_IMAGES});
            }
        } else { // Android 6 (API 23) to Android 12 (API 32)
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                openFilePicker();
            } else {
                // Request READ_EXTERNAL_STORAGE
                requestStoragePermissionLauncher.launch(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE});
            }
        }
    }


    // --- Image Capture and Selection Logic ---

    private void openCamera() {
        try {
            File photoFile = createImageFile();
            cameraPhotoUri = FileProvider.getUriForFile(
                    requireContext(),
                    requireContext().getPackageName() + ".fileprovider", // Use your actual fileprovider authority
                    photoFile
            );
            takePictureLauncher.launch(cameraPhotoUri);
        } catch (IOException ex) {
            Log.e(TAG, "Error creating image file for camera", ex);
            Toast.makeText(requireContext(), "Error creating image file", Toast.LENGTH_SHORT).show();
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );
        currentPhotoPath = image.getAbsolutePath(); // Save path for processing after capture
        return image;
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT); // ACTION_OPEN_DOCUMENT is preferred
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        // You can add this if you want to include downloads specifically, but ACTION_OPEN_DOCUMENT usually handles it
        // intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);

        try {
            pickImageFromGalleryLauncher.launch(intent);
        } catch (Exception e) {
            Log.e(TAG, "Error opening file picker", e);
            Toast.makeText(requireContext(), "Error opening file picker: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void processCapturedPhoto() {
        if (currentPhotoPath != null) {
            try {
                // Get the dimensions of the View (will be 0 if not yet laid out, so provide defaults)
                int targetW = itemImageView.getWidth();
                int targetH = itemImageView.getHeight();

                if (targetW <= 0) targetW = 1024; // Default sensible size
                if (targetH <= 0) targetH = 1024;

                // Decode bitmap from path, scaled
                Bitmap bitmap = decodeSampledBitmapFromFile(currentPhotoPath, targetW, targetH);
                if (bitmap != null) {
                    itemImageView.setImageBitmap(bitmap);
                    analyzeImage(bitmap);
                } else {
                    Toast.makeText(requireContext(), "Failed to load captured image bitmap.", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                }

            } catch (Exception e) {
                Log.e(TAG, "Error processing captured photo", e);
                Toast.makeText(requireContext(), "Error loading captured image", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
            }
        } else {
            Toast.makeText(requireContext(), "Error: No image path for captured photo.", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
        }
    }

    private Bitmap getBitmapFromUri(Uri uri) throws IOException {
        // Get the dimensions of the View (will be 0 if not yet laid out, so provide defaults)
        int targetW = itemImageView.getWidth();
        int targetH = itemImageView.getHeight();

        if (targetW <= 0) targetW = 1024; // Default sensible size
        if (targetH <= 0) targetH = 1024;

        InputStream input = requireContext().getContentResolver().openInputStream(uri);
        if (input == null) throw new IOException("Unable to open input stream for URI: " + uri);

        // Decode image size
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(input, null, options);
        input.close(); // Close the first input stream

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, targetW, targetH);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false; // Set to false to decode the actual bitmap
        input = requireContext().getContentResolver().openInputStream(uri); // Re-open stream
        Bitmap bitmap = BitmapFactory.decodeStream(input, null, options);
        if (input != null) {
            input.close(); // Close the second input stream
        }
        return bitmap;
    }

    // Helper method for efficient bitmap loading (reduces memory usage)
    private Bitmap decodeSampledBitmapFromFile(String path, int reqWidth, int reqHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true; // First decode with inJustDecodeBounds=true to check dimensions
        BitmapFactory.decodeFile(path, options);

        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight); // Calculate inSampleSize

        options.inJustDecodeBounds = false; // Decode bitmap with inSampleSize set
        return BitmapFactory.decodeFile(path, options);
    }

    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }


    // --- Item Analysis and UI Updates ---

    private void analyzeImage(Bitmap bitmap) {
        // In a real app, this would use ML to identify the item
        // For now, we'll simulate this with a delay and random item

        // It's better to use a Handler associated with the main looper directly
        new android.os.Handler(getContext().getMainLooper()).postDelayed(() -> {
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
        recycleButton.setBackgroundResource(R.drawable.button_normal); // Ensure button_normal exists
        reuseButton.setBackgroundResource(R.drawable.button_normal);
        reduceButton.setBackgroundResource(R.drawable.button_normal);

        recycleButton.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.black));
        reuseButton.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.black));
        reduceButton.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.black));
    }

    private void setSelectedButton(Button button) {
        if (button == recycleButton) {
            button.setBackgroundResource(R.drawable.button_selected_recycle); // Ensure these drawables exist
            infoCardView.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.colorRecycle)); // Ensure colors exist
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
}