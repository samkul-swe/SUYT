package edu.northeastern.suyt.ui.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
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
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
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
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import edu.northeastern.suyt.R;
import edu.northeastern.suyt.gemini.GeminiClient;
import edu.northeastern.suyt.model.Recycle;
import edu.northeastern.suyt.model.Reduce;
import edu.northeastern.suyt.model.Reuse;
import edu.northeastern.suyt.model.TrashItem;

public class RRRFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "RRRFragment";

    private ImageView itemImageView;
    private TextView itemNameTextView;
    private Button recycleButton;
    private Button reuseButton;
    private Button reduceButton;
    private TextView infoContentGeneralTextView; // Replaced infoContentTextView
    private CardView infoCardView;
    private ProgressBar progressBar;
    private LinearLayout buttonsContainer;
    private TextView initialHintTextView;

    // New UI elements for detailed info
    private LinearLayout recycleInfoLayout;
    private TextView recycleDescription;
    private TextView recycleCenter;
    private TextView recycleBin;
    private TextView recycleHours;

    private LinearLayout reuseInfoLayout;
    private TextView reuseDescription;
    private TextView reuseCrafts;
    private TextView reuseTime;
    private TextView reuseMoney;

    private LinearLayout reduceInfoLayout;
    private TextView reduceDescription;
    private TextView reduceCollect;
    private TextView reduceMoneyExpected;
    private TextView reduceOtherSuggestions;

    private TrashItem currentItem;
    private String currentPhotoPath;
    private ThreadPoolExecutor geminiExecutor;

    // Location related variables
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private double currentLatitude = 0.0;
    private double currentLongitude = 0.0;
    private boolean locationObtained = false;
    private Bitmap pendingBitmap;

    // ActivityResultLaunchers
    private ActivityResultLauncher<Uri> takePictureLauncher;
    private ActivityResultLauncher<Intent> pickImageFromGalleryLauncher;
    private ActivityResultLauncher<String> requestCameraPermissionLauncher;
    private ActivityResultLauncher<String[]> requestStoragePermissionLauncher;
    private ActivityResultLauncher<String[]> requestLocationPermissionLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setupActivityResultLaunchers();
        setupLocationServices();

        int numThreads = Runtime.getRuntime().availableProcessors();
        geminiExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(numThreads);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_rrr, container, false);

        // Initialize existing views
        itemImageView = view.findViewById(R.id.item_image_view);
        itemNameTextView = view.findViewById(R.id.item_name_text_view);
        recycleButton = view.findViewById(R.id.recycle_button);
        reuseButton = view.findViewById(R.id.reuse_button);
        reduceButton = view.findViewById(R.id.reduce_button);
        FloatingActionButton cameraFab = view.findViewById(R.id.camera_fab);
        FloatingActionButton downloadsFab = view.findViewById(R.id.gallery_fab);
        infoCardView = view.findViewById(R.id.info_card_view);
        progressBar = view.findViewById(R.id.progress_bar);
        buttonsContainer = view.findViewById(R.id.buttons_container);
        initialHintTextView = view.findViewById(R.id.initial_hint_text_view);

        // Initialize new views for detailed info
        infoContentGeneralTextView = view.findViewById(R.id.info_content_general_text_view);

        recycleInfoLayout = view.findViewById(R.id.recycle_info_layout);
        recycleDescription = view.findViewById(R.id.recycle_description);
        recycleCenter = view.findViewById(R.id.recycle_center);
        recycleBin = view.findViewById(R.id.recycle_bin);
        recycleHours = view.findViewById(R.id.recycle_hours);

        reuseInfoLayout = view.findViewById(R.id.reuse_info_layout);
        reuseDescription = view.findViewById(R.id.reuse_description);
        reuseCrafts = view.findViewById(R.id.reuse_crafts);
        reuseTime = view.findViewById(R.id.reuse_time);
        reuseMoney = view.findViewById(R.id.reuse_money);

        reduceInfoLayout = view.findViewById(R.id.reduce_info_layout);
        reduceDescription = view.findViewById(R.id.reduce_description);
        reduceCollect = view.findViewById(R.id.reduce_collect);
        reduceMoneyExpected = view.findViewById(R.id.reduce_money_expected);
        reduceOtherSuggestions = view.findViewById(R.id.reduce_other_suggestions);

        recycleButton.setOnClickListener(this);
        reuseButton.setOnClickListener(this);
        reduceButton.setOnClickListener(this);

        cameraFab.setOnClickListener(v -> checkAndRequestCameraPermission());
        downloadsFab.setOnClickListener(v -> checkAndRequestStoragePermissions());

        displayPlaceholderState(); // Set initial UI state

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (geminiExecutor != null && !geminiExecutor.isShutdown()) {
            geminiExecutor.shutdown();
        }

        stopLocationUpdates();
    }

    // --- Location Services Setup ---
    private void setupLocationServices() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        createLocationRequest();
        createLocationCallback();
    }

    private void createLocationRequest() {
        locationRequest = new LocationRequest.Builder(
                10000
        )
        .setMinUpdateIntervalMillis(5000)
        .build();
    }

    private void createLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);

                Location location = locationResult.getLastLocation();
                if (location != null) {
                    currentLatitude = location.getLatitude();
                    currentLongitude = location.getLongitude();
                    locationObtained = true;
                    Log.d(TAG, "Location obtained: " + currentLatitude + ", " + currentLongitude);

                    if (pendingBitmap != null) {
                        processImageWithLocation(pendingBitmap);
                        pendingBitmap = null;
                    }
                    stopLocationUpdates();
                }
            }

            @Override
            public void onLocationAvailability(@NonNull LocationAvailability locationAvailability) {
                super.onLocationAvailability(locationAvailability);
                if (!locationAvailability.isLocationAvailable()) {
                    Log.w(TAG, "Location not available.");
                    if (pendingBitmap != null) {
                        processImageWithLocation(pendingBitmap);
                        pendingBitmap = null;
                    }
                }
            }
        };
    }

    private boolean checkLocationPermissions() {
        return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermissions() {
        requestLocationPermissionLauncher.launch(new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        });
    }

    @SuppressLint("MissingPermission")
    private void requestNewLocationData() {
        if (!checkLocationPermissions()) {
            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
        );
    }

    private void stopLocationUpdates() {
        if (fusedLocationProviderClient != null && locationCallback != null) {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        }
    }

    // --- Activity Result Launchers Setup ---
    private void setupActivityResultLaunchers() {
        takePictureLauncher = registerForActivityResult(new ActivityResultContracts.TakePicture(), result -> {
            if (result) {
                processCapturedPhoto();
            } else {
                Toast.makeText(requireContext(), "Photo capture cancelled.", Toast.LENGTH_SHORT).show();
                displayPlaceholderState();
            }
        });

        pickImageFromGalleryLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                Uri selectedFileUri = result.getData().getData();
                if (selectedFileUri != null) {
                    try {
                        Log.d(TAG, "Selected URI: " + selectedFileUri);
                        Bitmap bitmap = getBitmapFromUri(selectedFileUri);
                        itemImageView.setImageBitmap(bitmap);
                        analyzeImage(bitmap);
                    } catch (IOException e) {
                        Log.e(TAG, "Failed to load selected image", e);
                        Toast.makeText(requireContext(), "Failed to load selected image.", Toast.LENGTH_SHORT).show();
                        displayPlaceholderState();
                    }
                } else {
                    Toast.makeText(requireContext(), "Error: No image was selected.", Toast.LENGTH_SHORT).show();
                    displayPlaceholderState();
                }
            } else if (result.getResultCode() == Activity.RESULT_CANCELED) {
                Toast.makeText(requireContext(), "Image selection cancelled.", Toast.LENGTH_SHORT).show();
                displayPlaceholderState();
            }
        });

        requestCameraPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
                openCamera();
            } else {
                Toast.makeText(requireContext(), "Camera permission is needed to take photos.", Toast.LENGTH_SHORT).show();
                displayPlaceholderState();
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
                displayPlaceholderState();
            }
        });

        requestLocationPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
            boolean locationGranted = false;
            for (Map.Entry<String, Boolean> entry : result.entrySet()) {
                if (entry.getValue()) {
                    locationGranted = true;
                    break;
                }
            }
            if (locationGranted) {
                Log.d(TAG, "Location permission granted. Requesting new location data.");
                requestNewLocationData();
            } else {
                Log.w(TAG, "Location permission denied. Proceeding without location.");
                if (pendingBitmap != null) {
                    processImageWithLocation(pendingBitmap);
                    pendingBitmap = null;
                } else {
                    displayPlaceholderState();
                }
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void displayPlaceholderState() {
        itemImageView.setImageResource(R.drawable.rounded_image_placeholder);
        itemNameTextView.setText("Ready to discover an item?");
        initialHintTextView.setText("Tap the camera icon to take a picture or the gallery icon to select an image from your device.");
        infoContentGeneralTextView.setText("");
        infoContentGeneralTextView.setVisibility(View.GONE);
        infoCardView.setVisibility(View.GONE);
        buttonsContainer.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
        initialHintTextView.setVisibility(View.VISIBLE);

        hideAllInfoLayouts();
        resetButtons();
    }

    private void setLoadingState() {
        progressBar.setVisibility(View.VISIBLE);
        itemNameTextView.setText("Analyzing image...");
        infoContentGeneralTextView.setText("Please wait while AI analyzes the item.");
        infoContentGeneralTextView.setVisibility(View.VISIBLE);
        infoCardView.setVisibility(View.VISIBLE);
        buttonsContainer.setVisibility(View.GONE);
        initialHintTextView.setVisibility(View.GONE);
        hideAllInfoLayouts();
    }

    private void setAnalysisCompleteState() {
        progressBar.setVisibility(View.GONE);
        buttonsContainer.setVisibility(View.VISIBLE);
        infoCardView.setVisibility(View.VISIBLE);
        initialHintTextView.setVisibility(View.GONE);
        infoContentGeneralTextView.setVisibility(View.GONE);
    }

    private void hideAllInfoLayouts() {
        recycleInfoLayout.setVisibility(View.GONE);
        reuseInfoLayout.setVisibility(View.GONE);
        reduceInfoLayout.setVisibility(View.GONE);
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
            Toast.makeText(requireContext(), "Error creating image file.", Toast.LENGTH_SHORT).show();
            displayPlaceholderState();
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
            displayPlaceholderState();
        }
    }

    private void processCapturedPhoto() {
        if (currentPhotoPath != null) {
            try {
                int targetW = itemImageView.getWidth() > 0 ? itemImageView.getWidth() : 1024;
                int targetH = itemImageView.getHeight() > 0 ? itemImageView.getHeight() : 1024;

                Bitmap bitmap = decodeSampledBitmapFromFile(currentPhotoPath, targetW, targetH);
                if (bitmap != null) {
                    itemImageView.setImageBitmap(bitmap);
                    analyzeImage(bitmap);
                } else {
                    Toast.makeText(requireContext(), "Failed to load captured image bitmap.", Toast.LENGTH_SHORT).show();
                    displayPlaceholderState();
                }
            } catch (Exception e) {
                Log.e(TAG, "Error processing captured photo", e);
                Toast.makeText(requireContext(), "Error loading captured image.", Toast.LENGTH_SHORT).show();
                displayPlaceholderState();
            }
        } else {
            Toast.makeText(requireContext(), "Error: No image path for captured photo.", Toast.LENGTH_SHORT).show();
            displayPlaceholderState();
        }
    }

    private Bitmap getBitmapFromUri(Uri uri) throws IOException {
        int targetW = itemImageView.getWidth() > 0 ? itemImageView.getWidth() : 1024;
        int targetH = itemImageView.getHeight() > 0 ? itemImageView.getHeight() : 1024;

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

    private void analyzeImage(Bitmap bitmap) {
        if (!isAdded()) return;

        setLoadingState();

        locationObtained = false;
        currentLatitude = 0.0;
        currentLongitude = 0.0;

        if (checkLocationPermissions()) {
            Log.d(TAG, "Location permissions granted. Requesting current location.");
            pendingBitmap = bitmap;
            itemNameTextView.setText("Getting location data...");
            requestNewLocationData();
        } else {
            Log.d(TAG, "Location permissions not granted. Requesting permissions.");
            pendingBitmap = bitmap;
            itemNameTextView.setText("Requesting location permissions...");
            requestLocationPermissions();
        }
    }

    private void processImageWithLocation(Bitmap bitmap) {
        if (!isAdded()) return;

        itemNameTextView.setText("Analyzing image...");

        String locationContext = "";
        if (locationObtained && currentLatitude != 0.0 && currentLongitude != 0.0) {
            locationContext = String.format(Locale.US,
                    " My current coordinates are latitude %.6f and longitude %.6f.",
                    currentLatitude, currentLongitude);
        }

        String promptText = "Analyze this image and identify the item. " +
                "For 'recycleInfo': If recyclable, provide a concise 'description'. Then, specify the 'nearestRecyclingCenter' (e.g., Mountain View Recycling Center), 'suggestedBin' (e.g., Blue bin for plastics), and 'recyclingHours' (e.g., Mon-Sat, 8 AM - 5 PM). Prioritize specific information relevant to " + locationContext + ".\n" +
                "For 'reuseInfo': If reusable, provide a concise 'description'. Then, list 3-4 'craftsPossible' (e.g., '1. Bottle Cap Mosaic: ..., 2. Plastic Bottle Planter: ...'), an overall 'timeNeededForCraft' (e.g., '1-2 hours per craft'), and 'moneyNeededForCraft' (e.g., '$0-5 per craft'). Make them appealing and practical.\n" +
                "For 'reduceInfo': If reducible, provide a concise 'description'. Then, include 'howManyShouldICollect' for selling (e.g., '500 bottles'), 'moneyExpected' (e.g., '$25 depending on rate'), and 'otherSuggestions' for reduction (e.g., 'Use a reusable water bottle'). Ensure all information is directly related to the item in the image.";


        Content prompt = new Content.Builder()
                .addImage(bitmap)
                .addText(promptText)
                .build();

        TrashItem trashItem = new TrashItem();
        Schema trashItemSchema = trashItem.getSchema();

        ListenableFuture<GenerateContentResponse> response = new GeminiClient(trashItemSchema).generateResult(prompt);

        Futures.addCallback(
                response,
                new FutureCallback<>() {
                    @Override
                    public void onSuccess(GenerateContentResponse result) {
                        requireActivity().runOnUiThread(() -> {
                            setAnalysisCompleteState();

                            String jsonResponse = result.getText();
                            Log.d(TAG, "Gemini Raw JSON Response: " + jsonResponse);

                            if (jsonResponse != null && !jsonResponse.isEmpty()) {
                                try {
                                    ObjectMapper objectMapper = new ObjectMapper();
                                    currentItem = objectMapper.readValue(jsonResponse, TrashItem.class);

                                    if (currentItem != null) {
                                        updateUIWithGeminiResponse();
                                    } else {
                                        Toast.makeText(requireContext(), "Failed to parse item data from Gemini.", Toast.LENGTH_LONG).show();
                                        displayPlaceholderState();
                                    }
                                } catch (Exception e) {
                                    Log.e(TAG, "Error parsing Gemini JSON response", e);
                                    Toast.makeText(requireContext(), "Error parsing Gemini response: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                    displayPlaceholderState();
                                }
                            } else {
                                Toast.makeText(requireContext(), "Gemini returned an empty response.", Toast.LENGTH_SHORT).show();
                                displayPlaceholderState();
                            }
                        });
                    }

                    @Override
                    public void onFailure(@NonNull Throwable t) {
                        requireActivity().runOnUiThread(() -> {
                            setAnalysisCompleteState();
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
            hideAllInfoLayouts();
            infoContentGeneralTextView.setText("No specific RRR information available for this item. Please try another item.");
            infoContentGeneralTextView.setVisibility(View.VISIBLE);
            infoCardView.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.textColorSecondary));
        }
    }

    @Override
    public void onClick(View v) {
        if (currentItem == null) {
            Toast.makeText(requireContext(), "Please scan an item first.", Toast.LENGTH_SHORT).show();
            return;
        }

        resetButtons();
        hideAllInfoLayouts();
        infoContentGeneralTextView.setVisibility(View.GONE);

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

        recycleButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.buttonTextNormal));
        reuseButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.buttonTextNormal));
        reduceButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.buttonTextNormal));
    }

    private void setSelectedButton(Button button) {
        if (button == recycleButton) {
            button.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorRecycle));
            infoCardView.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.colorRecycle));
        } else if (button == reuseButton) {
            button.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorReuse));
            infoCardView.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.colorReuse));
        } else if (button == reduceButton) {
            button.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorReduce));
            infoCardView.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.colorReduce));
        }
    }

    private void showRecycleInfo() {
        hideAllInfoLayouts();
        recycleInfoLayout.setVisibility(View.VISIBLE);

        if (currentItem != null && currentItem.getRecycleInfo() != null) {
            Recycle info = currentItem.getRecycleInfo();
            recycleDescription.setText(formatField("Description", info.getRecycleInfo()));
            recycleCenter.setText(formatField("Nearest Center", info.getNearestRecyclingCenter()));
            recycleBin.setText(formatField("Suggested Bin", info.getSuggestedBin()));
            recycleHours.setText(formatField("Hours", info.getRecyclingHours()));

            recycleDescription.setVisibility(info.getRecycleInfo() != null && !info.getRecycleInfo().isEmpty() ? View.VISIBLE : View.GONE);
            recycleCenter.setVisibility(info.getNearestRecyclingCenter() != null && !info.getNearestRecyclingCenter().isEmpty() ? View.VISIBLE : View.GONE);
            recycleBin.setVisibility(info.getSuggestedBin() != null && !info.getSuggestedBin().isEmpty() ? View.VISIBLE : View.GONE);
            recycleHours.setVisibility(info.getRecyclingHours() != null && !info.getRecyclingHours().isEmpty() ? View.VISIBLE : View.GONE);

            if (info.getRecycleInfo().isEmpty() && info.getNearestRecyclingCenter().isEmpty() &&
                    info.getSuggestedBin().isEmpty() && info.getRecyclingHours().isEmpty()) {
                infoContentGeneralTextView.setText("No specific recycling information available for this item.");
                infoContentGeneralTextView.setVisibility(View.VISIBLE);
                recycleInfoLayout.setVisibility(View.GONE);
            }
        } else {
            infoContentGeneralTextView.setText("No specific recycling information available for this item.");
            infoContentGeneralTextView.setVisibility(View.VISIBLE);
            recycleInfoLayout.setVisibility(View.GONE);
        }
    }

    private void showReuseInfo() {
        hideAllInfoLayouts();
        reuseInfoLayout.setVisibility(View.VISIBLE);

        if (currentItem != null && currentItem.getReuseInfo() != null) {
            Reuse info = currentItem.getReuseInfo();
            reuseDescription.setText(formatField("Description", info.getReuseInfo()));
            reuseCrafts.setText(formatField("Crafts Possible", info.getCraftsPossible()));
            reuseTime.setText(formatField("Estimated Time", info.getTimeNeededForCraft()));
            reuseMoney.setText(formatField("Estimated Cost", info.getMoneyNeededForCraft()));

            reuseDescription.setVisibility(info.getReuseInfo() != null && !info.getReuseInfo().isEmpty() ? View.VISIBLE : View.GONE);
            reuseCrafts.setVisibility(info.getCraftsPossible() != null && !info.getCraftsPossible().isEmpty() ? View.VISIBLE : View.GONE);
            reuseTime.setVisibility(info.getTimeNeededForCraft() != null && !info.getTimeNeededForCraft().isEmpty() ? View.VISIBLE : View.GONE);
            reuseMoney.setVisibility(info.getMoneyNeededForCraft() != null && !info.getMoneyNeededForCraft().isEmpty() ? View.VISIBLE : View.GONE);

            if (info.getReuseInfo().isEmpty() && info.getCraftsPossible().isEmpty() &&
                    info.getTimeNeededForCraft().isEmpty() && info.getMoneyNeededForCraft().isEmpty()) {
                infoContentGeneralTextView.setText("No specific reusing information available for this item.");
                infoContentGeneralTextView.setVisibility(View.VISIBLE);
                reuseInfoLayout.setVisibility(View.GONE);
            }
        } else {
            infoContentGeneralTextView.setText("No specific reusing information available for this item.");
            infoContentGeneralTextView.setVisibility(View.VISIBLE);
            reuseInfoLayout.setVisibility(View.GONE);
        }
    }

    private void showReduceInfo() {
        hideAllInfoLayouts();
        reduceInfoLayout.setVisibility(View.VISIBLE);

        if (currentItem != null && currentItem.getReduceInfo() != null) {
            Reduce info = currentItem.getReduceInfo();
            reduceDescription.setText(formatField("Description", info.getReduceInfo()));
            reduceCollect.setText(formatField("Collection Suggestion", info.getHowManyShouldICollect()));
            reduceMoneyExpected.setText(formatField("Money Expected", info.getMoneyExpected()));
            reduceOtherSuggestions.setText(formatField("Other Suggestions", info.getOtherSuggestions()));

            reduceDescription.setVisibility(info.getReduceInfo() != null && !info.getReduceInfo().isEmpty() ? View.VISIBLE : View.GONE);
            reduceCollect.setVisibility(info.getHowManyShouldICollect() != null && !info.getHowManyShouldICollect().isEmpty() ? View.VISIBLE : View.GONE);
            reduceMoneyExpected.setVisibility(info.getMoneyExpected() != null && !info.getMoneyExpected().isEmpty() ? View.VISIBLE : View.GONE);
            reduceOtherSuggestions.setVisibility(info.getOtherSuggestions() != null && !info.getOtherSuggestions().isEmpty() ? View.VISIBLE : View.GONE);

            if (info.getReduceInfo().isEmpty() && info.getHowManyShouldICollect().isEmpty() &&
                    info.getMoneyExpected().isEmpty() && info.getOtherSuggestions().isEmpty()) {
                infoContentGeneralTextView.setText("No specific reducing information available for this item.");
                infoContentGeneralTextView.setVisibility(View.VISIBLE);
                reduceInfoLayout.setVisibility(View.GONE);
            }
        } else {
            infoContentGeneralTextView.setText("No specific reducing information available for this item.");
            infoContentGeneralTextView.setVisibility(View.VISIBLE);
            reduceInfoLayout.setVisibility(View.GONE);
        }
    }

    private String formatField(String label, String value) {
        return (value != null && !value.isEmpty()) ? (label + ": " + value) : "";
    }

    private Bitmap decodeSampledBitmapFromFile(String path, int reqWidth, int reqHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(path, options);
    }
}