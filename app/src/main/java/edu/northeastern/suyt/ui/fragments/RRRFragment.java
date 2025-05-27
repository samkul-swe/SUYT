package edu.northeastern.suyt.ui.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.ai.type.Content;
import com.google.firebase.ai.type.GenerateContentResponse;
import com.google.firebase.ai.type.Schema;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import edu.northeastern.suyt.R;
import edu.northeastern.suyt.gemini.GeminiClient;
import edu.northeastern.suyt.model.TrashItem;


public class RRRFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "RRRFragment";

    private ImageView itemImageView;
    private TextView itemNameTextView;
    private Button recycleButton;
    private Button reuseButton;
    private Button reduceButton;
    private TextView infoContentTextView;
    private CardView infoCardView;
    private ProgressBar progressBar;

    private TrashItem currentItem;
    private String currentPhotoPath;
    private ThreadPoolExecutor geminiExecutor;

    private ActivityResultLauncher<Uri> takePictureLauncher;

    private ActivityResultLauncher<Intent> pickImageFromGalleryLauncher;

    private ActivityResultLauncher<String> requestCameraPermissionLauncher;

    private ActivityResultLauncher<String[]> requestStoragePermissionLauncher;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setupActivityResultLaunchers();

        int numThreads = Runtime.getRuntime().availableProcessors();
        geminiExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(numThreads);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_rrr, container, false);

        itemImageView = view.findViewById(R.id.item_image_view);
        itemNameTextView = view.findViewById(R.id.item_name_text_view);
        recycleButton = view.findViewById(R.id.recycle_button);
        reuseButton = view.findViewById(R.id.reuse_button);
        reduceButton = view.findViewById(R.id.reduce_button);
        FloatingActionButton cameraFab = view.findViewById(R.id.camera_fab);
        FloatingActionButton downloadsFab = view.findViewById(R.id.gallery_fab);
        infoContentTextView = view.findViewById(R.id.info_content_text_view);
        infoCardView = view.findViewById(R.id.info_card_view);
        progressBar = view.findViewById(R.id.progress_bar);

        recycleButton.setOnClickListener(this);
        reuseButton.setOnClickListener(this);
        reduceButton.setOnClickListener(this);

        cameraFab.setOnClickListener(v -> checkAndRequestCameraPermission());
        downloadsFab.setOnClickListener(v -> checkAndRequestStoragePermissions());

        displayPlaceholderState();

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (geminiExecutor != null && !geminiExecutor.isShutdown()) {
            geminiExecutor.shutdown();
        }
    }

    private void setupActivityResultLaunchers() {
        takePictureLauncher = registerForActivityResult(new ActivityResultContracts.TakePicture(), result -> {
            if (result) {
                progressBar.setVisibility(View.VISIBLE);
                processCapturedPhoto();
            } else {
                Toast.makeText(requireContext(), "Photo capture cancelled", Toast.LENGTH_SHORT).show();
            }
        });

        pickImageFromGalleryLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                progressBar.setVisibility(View.VISIBLE);
                Uri selectedFileUri = result.getData().getData();

                if (selectedFileUri != null) {
                    try {
                        Log.d(TAG, "Selected URI: " + selectedFileUri);
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

        requestCameraPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
                openCamera();
            } else {
                Toast.makeText(requireContext(), "Camera permission is needed to take photos.", Toast.LENGTH_SHORT).show();
            }
        });

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


    @SuppressLint("SetTextI18n")
    private void displayPlaceholderState() {
        itemImageView.setImageResource(R.drawable.ic_image_placeholder); // Ensure this drawable exists
        itemNameTextView.setText("Scan an item");
        infoContentTextView.setText("Take a photo or select an image from your device to see recycling, reusing, and reducing options.");
        infoCardView.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary)); // Ensure colorPrimary exists

        resetButtons();
    }

    private void checkAndRequestCameraPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            openCamera();
        } else {
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void checkAndRequestStoragePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED) {
                openFilePicker();
            } else {
                requestStoragePermissionLauncher.launch(new String[]{Manifest.permission.READ_MEDIA_IMAGES});
            }
        } else {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                openFilePicker();
            } else {
                requestStoragePermissionLauncher.launch(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE});
            }
        }
    }

    private void openCamera() {
        try {
            File photoFile = createImageFile();
            Uri cameraPhotoUri = FileProvider.getUriForFile(
                    requireContext(),
                    requireContext().getPackageName() + ".fileprovider",
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
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");

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
                int targetW = itemImageView.getWidth();
                int targetH = itemImageView.getHeight();

                if (targetW <= 0) targetW = 1024;
                if (targetH <= 0) targetH = 1024;

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
        int targetW = itemImageView.getWidth();
        int targetH = itemImageView.getHeight();

        if (targetW <= 0) targetW = 1024;
        if (targetH <= 0) targetH = 1024;

        InputStream input = requireContext().getContentResolver().openInputStream(uri);
        if (input == null) throw new IOException("Unable to open input stream for URI: " + uri);

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(input, null, options);
        input.close();

        options.inSampleSize = calculateInSampleSize(options, targetW, targetH);

        options.inJustDecodeBounds = false;
        input = requireContext().getContentResolver().openInputStream(uri);
        Bitmap bitmap = BitmapFactory.decodeStream(input, null, options);
        if (input != null) {
            input.close();
        }
        return bitmap;
    }

    private Bitmap decodeSampledBitmapFromFile(String path, int reqWidth, int reqHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(path, options);
    }

    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    public void itemFromString(String itemString) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            currentItem = objectMapper.readValue(itemString, TrashItem.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @SuppressLint("SetTextI18n")
    private void analyzeImage(Bitmap bitmap) {
        if (!isAdded()) return;

        progressBar.setVisibility(View.VISIBLE);
        itemNameTextView.setText("Analyzing image...");
        infoContentTextView.setText("Please wait while AI analyzes the item.");

        Content prompt = new Content.Builder()
                .addImage(bitmap) // Add your Bitmap here
                .addText("What is this item? If it is recyclable, give me the nearest recycling center, what bin to use and more information about it. If it is reducible, give me more information on if I can collect it and sell to get money and how much money I can get. If it is reusable, give me more information as well find the best crafts I can do, with how much time it will take to complete the project and the money needed.")
                .build();
        Schema schema = currentItem.getSchema();

        // Call the Gemini model asynchronously
        ListenableFuture<GenerateContentResponse> response = new GeminiClient(schema).generateResult(prompt);
        Futures.addCallback(
                response,
                new FutureCallback<>() {
                    @Override
                    public void onSuccess(GenerateContentResponse result) {
                        itemFromString(result.getText());
                        requireActivity().runOnUiThread(() -> {
                            progressBar.setVisibility(View.GONE);
                            String textResponse = result.getText();
                            if (textResponse != null && !textResponse.isEmpty()) {
                                Log.d(TAG, "Gemini Response: " + textResponse);
                                updateUIWithGeminiResponse();
                            } else {
                                Toast.makeText(requireContext(), "Gemini returned an empty response.", Toast.LENGTH_SHORT).show();
                                displayPlaceholderState();
                            }
                        });
                    }

                    @Override
                    public void onFailure(@NonNull Throwable t) {
                        requireActivity().runOnUiThread(() -> {
                            progressBar.setVisibility(View.GONE);
                            Log.e(TAG, "Gemini API call failed", t);
                            Toast.makeText(requireContext(), "Gemini analysis failed: " + t.getMessage(), Toast.LENGTH_LONG).show();
                            displayPlaceholderState();
                        });
                    }
                },
                geminiExecutor
        );
    }

    private void updateUIWithGeminiResponse() {

        itemNameTextView.setText(currentItem.getName());

        if (currentItem.isRecyclable()) {
            setSelectedButton(recycleButton);
            showRecycleInfo();
        } else if (currentItem.isReusable()) {
            setSelectedButton(reuseButton);
            showReuseInfo();
        } else if (currentItem.isReducible()) {
            setSelectedButton(reduceButton);
            showReduceInfo();
        } else {
            setSelectedButton(recycleButton);
        }
    }

    @Override
    public void onClick(View v) {
        if (currentItem == null) {
            Toast.makeText(requireContext(), "Please scan an item first", Toast.LENGTH_SHORT).show();
            return;
        }

        resetButtons();

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
            infoContentTextView.setText(currentItem.getRecycleInfo().toString());
        }
    }

    private void showReuseInfo() {
        if (currentItem != null) {
            infoContentTextView.setText(currentItem.getReuseInfo().toString());
        }
    }

    private void showReduceInfo() {
        if (currentItem != null) {
            infoContentTextView.setText(currentItem.getReduceInfo().toString());
        }
    }
}