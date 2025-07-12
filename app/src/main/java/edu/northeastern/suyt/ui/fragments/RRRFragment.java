package edu.northeastern.suyt.ui.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import edu.northeastern.suyt.R;
import edu.northeastern.suyt.model.Recycle;
import edu.northeastern.suyt.model.Reduce;
import edu.northeastern.suyt.model.Reuse;
import edu.northeastern.suyt.model.TrashItem;
import edu.northeastern.suyt.ui.activities.CreatePostActivity;
import edu.northeastern.suyt.ui.viewmodel.RRRViewModel;

public class RRRFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "RRRFragment";

    private RRRViewModel viewModel;

    private ImageView itemImageView;
    private TextView itemNameTextView;
    private Button recycleButton;
    private Button reuseButton;
    private Button reduceButton;
    private TextView infoContentGeneralTextView;
    private androidx.cardview.widget.CardView infoCardView;
    private ProgressBar progressBar;
    private LinearLayout buttonsContainer;
    private TextView initialHintTextView;

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

    private androidx.cardview.widget.CardView completionSection;
    private Button completedYesButton;
    private Button completedNoButton;
    private LinearLayout shareOptionLayout;
    private Button shareWithPhotoButton;
    private Button skipSharingButton;

    private String currentPhotoPath;

    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;

    private ActivityResultLauncher<Uri> takePictureLauncher;
    private ActivityResultLauncher<Intent> pickImageFromGalleryLauncher;
    private ActivityResultLauncher<String> requestCameraPermissionLauncher;
    private ActivityResultLauncher<String[]> requestStoragePermissionLauncher;
    private ActivityResultLauncher<String[]> requestLocationPermissionLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewModel = new ViewModelProvider(this).get(RRRViewModel.class);

        setupActivityResultLaunchers();
        setupLocationServices();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_rrr, container, false);

        initializeViews(view);
        setupClickListeners(view);
        observeViewModel();

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopLocationUpdates();
    }

    private void initializeViews(View view) {
        itemImageView = view.findViewById(R.id.item_image_view);
        itemNameTextView = view.findViewById(R.id.item_name_text_view);
        recycleButton = view.findViewById(R.id.recycle_button);
        reuseButton = view.findViewById(R.id.reuse_button);
        reduceButton = view.findViewById(R.id.reduce_button);
        infoCardView = view.findViewById(R.id.info_card_view);
        progressBar = view.findViewById(R.id.progress_bar);
        buttonsContainer = view.findViewById(R.id.buttons_container);
        initialHintTextView = view.findViewById(R.id.initial_hint_text_view);

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

        completionSection = view.findViewById(R.id.completion_section);
        completedYesButton = view.findViewById(R.id.completed_yes_button);
        completedNoButton = view.findViewById(R.id.completed_no_button);
        shareOptionLayout = view.findViewById(R.id.share_option_layout);
        shareWithPhotoButton = view.findViewById(R.id.share_with_photo_button);
        skipSharingButton = view.findViewById(R.id.skip_sharing_button);
    }

    private void setupClickListeners(View view) {
        FloatingActionButton cameraFab = view.findViewById(R.id.camera_fab);
        FloatingActionButton downloadsFab = view.findViewById(R.id.gallery_fab);

        recycleButton.setOnClickListener(this);
        reuseButton.setOnClickListener(this);
        reduceButton.setOnClickListener(this);

        cameraFab.setOnClickListener(v -> checkAndRequestCameraPermission());
        downloadsFab.setOnClickListener(v -> checkAndRequestStoragePermissions());

        completedYesButton.setOnClickListener(v -> handleCompletionYes());
        completedNoButton.setOnClickListener(v -> handleCompletionNo());
        shareWithPhotoButton.setOnClickListener(v -> handleShareWithPhoto());
        skipSharingButton.setOnClickListener(v -> handleSkipSharing());
    }

    private void observeViewModel() {
        viewModel.uiState.observe(getViewLifecycleOwner(), this::handleUIStateChange);

        viewModel.currentItem.observe(getViewLifecycleOwner(), this::handleCurrentItemChange);

        viewModel.selectedTab.observe(getViewLifecycleOwner(), this::handleSelectedTabChange);

        viewModel.statusMessage.observe(getViewLifecycleOwner(), message -> {
            if (message != null && !message.isEmpty()) {
                itemNameTextView.setText(message);
            }
        });

        viewModel.errorMessage.observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void handleUIStateChange(RRRViewModel.UIState state) {
        switch (state) {
            case PLACEHOLDER:
                displayPlaceholderState();
                break;
            case LOADING:
                setLoadingState();
                break;
            case ANALYSIS_COMPLETE:
                setAnalysisCompleteState();
                break;
        }
    }

    private void handleCurrentItemChange(TrashItem item) {
        if (item != null) {
            itemNameTextView.setText(item.getName());
        } else {
            itemNameTextView.setText(viewModel.getCurrentItemName());
        }
    }

    private void handleSelectedTabChange(RRRViewModel.RRRTab tab) {
        if (tab == null) return;

        resetButtons();
        hideAllInfoLayouts();
        infoContentGeneralTextView.setVisibility(View.GONE);

        switch (tab) {
            case RECYCLE:
                setSelectedButton(recycleButton);
                showRecycleInfo();
                break;
            case REUSE:
                setSelectedButton(reuseButton);
                showReuseInfo();
                break;
            case REDUCE:
                setSelectedButton(reduceButton);
                showReduceInfo();
                break;
        }

        // Only show completion section if analysis is complete AND a tab is selected
        if (viewModel.uiState.getValue() == RRRViewModel.UIState.ANALYSIS_COMPLETE) {
            showCompletionSection();
        }
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

        // Show hint card when in placeholder state
        View hintCard = getView() != null ? getView().findViewById(R.id.hint_card) : null;
        if (hintCard != null) {
            hintCard.setVisibility(View.VISIBLE);
        }

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
        View hintCard = getView() != null ? getView().findViewById(R.id.hint_card) : null;
        if (hintCard != null) {
            hintCard.setVisibility(View.GONE);
        }
        initialHintTextView.setVisibility(View.GONE);
        hideAllInfoLayouts();
    }

    private void setAnalysisCompleteState() {
        progressBar.setVisibility(View.GONE);
        buttonsContainer.setVisibility(View.VISIBLE);
        infoCardView.setVisibility(View.VISIBLE);
        initialHintTextView.setVisibility(View.GONE);

        // Hide the hint card after analysis is complete
        View hintCard = getView() != null ? getView().findViewById(R.id.hint_card) : null;
        if (hintCard != null) {
            hintCard.setVisibility(View.GONE);
        }

        infoContentGeneralTextView.setVisibility(View.GONE);

        // Don't show completion section here - only show it when user selects a tab
        // The completion section will be shown in handleSelectedTabChange()
    }

    private void hideAllInfoLayouts() {
        recycleInfoLayout.setVisibility(View.GONE);
        reuseInfoLayout.setVisibility(View.GONE);
        reduceInfoLayout.setVisibility(View.GONE);
        completionSection.setVisibility(View.GONE);
        if (shareOptionLayout != null) {
            shareOptionLayout.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.recycle_button) {
            viewModel.selectTab(RRRViewModel.RRRTab.RECYCLE);
        } else if (v.getId() == R.id.reuse_button) {
            viewModel.selectTab(RRRViewModel.RRRTab.REUSE);
        } else if (v.getId() == R.id.reduce_button) {
            viewModel.selectTab(RRRViewModel.RRRTab.REDUCE);
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

        TrashItem currentItem = viewModel.currentItem.getValue();
        if (currentItem != null && currentItem.getRecycleInfo() != null) {
            recycleInfoLayout.setVisibility(View.VISIBLE);
            Recycle info = currentItem.getRecycleInfo();

            recycleDescription.setText(formatField("Description", info.getRecycleInfo()));
            recycleCenter.setText(formatField("Nearest Center", info.getNearestRecyclingCenter()));
            recycleBin.setText(formatField("Suggested Bin", info.getSuggestedBin()));
            recycleHours.setText(formatField("Hours", info.getRecyclingHours()));

            recycleDescription.setVisibility(info.getRecycleInfo() != null && !info.getRecycleInfo().isEmpty() ? View.VISIBLE : View.GONE);
            recycleCenter.setVisibility(info.getNearestRecyclingCenter() != null && !info.getNearestRecyclingCenter().isEmpty() ? View.VISIBLE : View.GONE);
            recycleBin.setVisibility(info.getSuggestedBin() != null && !info.getSuggestedBin().isEmpty() ? View.VISIBLE : View.GONE);
            recycleHours.setVisibility(info.getRecyclingHours() != null && !info.getRecyclingHours().isEmpty() ? View.VISIBLE : View.GONE);

            if (!viewModel.hasValidDataForSelectedTab()) {
                showNoInfoMessage("No specific recycling information available for this item.");
            }
        } else {
            showNoInfoMessage("No specific recycling information available for this item.");
        }
    }

    private void showReuseInfo() {
        hideAllInfoLayouts();

        TrashItem currentItem = viewModel.currentItem.getValue();
        if (currentItem != null && currentItem.getReuseInfo() != null) {
            reuseInfoLayout.setVisibility(View.VISIBLE);
            Reuse info = currentItem.getReuseInfo();

            reuseDescription.setText(formatField("Description", info.getReuseInfo()));

            // Handle multiple crafts
            LinearLayout craftsContainer = getView() != null ? getView().findViewById(R.id.crafts_container) : null;
            if (craftsContainer != null) {
                craftsContainer.removeAllViews(); // Clear existing craft cards

                String craftsText = info.getCraftsPossible();
                if (craftsText != null && !craftsText.isEmpty()) {
                    // Split crafts by numbered list (1., 2., 3., etc.) or by line breaks
                    String[] crafts = splitCrafts(craftsText);

                    if (crafts.length > 1) {
                        // Multiple crafts - create individual cards
                        for (int i = 0; i < crafts.length; i++) {
                            if (!crafts[i].trim().isEmpty()) {
                                createCraftCard(craftsContainer, crafts[i].trim(), i + 1);
                            }
                        }
                    } else {
                        // Single craft - use simple text view
                        createSingleCraftView(craftsContainer, craftsText);
                    }
                } else {
                    // No crafts available
                    createNoCraftView(craftsContainer);
                }
            }

            reuseTime.setText(formatField("Time", info.getTimeNeededForCraft()));
            reuseMoney.setText(formatField("Cost", info.getMoneyNeededForCraft()));

            reuseDescription.setVisibility(info.getReuseInfo() != null && !info.getReuseInfo().isEmpty() ? View.VISIBLE : View.GONE);
            reuseTime.setVisibility(info.getTimeNeededForCraft() != null && !info.getTimeNeededForCraft().isEmpty() ? View.VISIBLE : View.GONE);
            reuseMoney.setVisibility(info.getMoneyNeededForCraft() != null && !info.getMoneyNeededForCraft().isEmpty() ? View.VISIBLE : View.GONE);

            if (!viewModel.hasValidDataForSelectedTab()) {
                showNoInfoMessage("No specific reusing information available for this item.");
            }
        } else {
            showNoInfoMessage("No specific reusing information available for this item.");
        }
    }

    private void showReduceInfo() {
        hideAllInfoLayouts();

        TrashItem currentItem = viewModel.currentItem.getValue();
        if (currentItem != null && currentItem.getReduceInfo() != null) {
            reduceInfoLayout.setVisibility(View.VISIBLE);
            Reduce info = currentItem.getReduceInfo();

            reduceDescription.setText(formatField("Description", info.getReduceInfo()));
            reduceCollect.setText(formatField("Collection Suggestion", info.getHowManyShouldICollect()));
            reduceMoneyExpected.setText(formatField("Money Expected", info.getMoneyExpected()));
            reduceOtherSuggestions.setText(formatField("Other Suggestions", info.getOtherSuggestions()));

            reduceDescription.setVisibility(info.getReduceInfo() != null && !info.getReduceInfo().isEmpty() ? View.VISIBLE : View.GONE);
            reduceCollect.setVisibility(info.getHowManyShouldICollect() != null && !info.getHowManyShouldICollect().isEmpty() ? View.VISIBLE : View.GONE);
            reduceMoneyExpected.setVisibility(info.getMoneyExpected() != null && !info.getMoneyExpected().isEmpty() ? View.VISIBLE : View.GONE);
            reduceOtherSuggestions.setVisibility(info.getOtherSuggestions() != null && !info.getOtherSuggestions().isEmpty() ? View.VISIBLE : View.GONE);

            if (!viewModel.hasValidDataForSelectedTab()) {
                showNoInfoMessage("No specific reducing information available for this item.");
            }
        } else {
            showNoInfoMessage("No specific reducing information available for this item.");
        }
    }

    private void showNoInfoMessage(String message) {
        infoContentGeneralTextView.setText(message);
        infoContentGeneralTextView.setVisibility(View.VISIBLE);
        hideAllInfoLayouts();
    }

    private String formatField(String label, String value) {
        return (value != null && !value.isEmpty()) ? (label + ": " + value) : "";
    }

    // Craft handling methods
    private String[] splitCrafts(String craftsText) {
        // Try different splitting patterns
        String[] crafts;

        // Pattern 1: Split by numbered list (1., 2., 3., etc.)
        if (craftsText.matches(".*\\d+\\..*")) {
            crafts = craftsText.split("(?=\\d+\\.)");
        }
        // Pattern 2: Split by line breaks
        else if (craftsText.contains("\n")) {
            crafts = craftsText.split("\n");
        }
        // Pattern 3: Split by bullets or dashes
        else if (craftsText.contains("•") || craftsText.contains("-")) {
            crafts = craftsText.split("(?=[•-])");
        }
        // Pattern 4: Split by semicolons or commas (if long)
        else if (craftsText.length() > 100 && (craftsText.contains(";") || craftsText.contains(","))) {
            crafts = craftsText.split("[;,]");
        }
        // Default: Return as single craft
        else {
            crafts = new String[]{craftsText};
        }

        return crafts;
    }

    private void createCraftCard(LinearLayout container, String craftText, int craftNumber) {
        // Create card view for individual craft
        androidx.cardview.widget.CardView craftCard = new androidx.cardview.widget.CardView(requireContext());

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(0, 0, 0, 12); // Bottom margin between cards
        craftCard.setLayoutParams(cardParams);

        craftCard.setCardBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.transparent));
        craftCard.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.craft_card_background));
        craftCard.setRadius(8f);
        craftCard.setCardElevation(0f);

        // Create inner layout
        LinearLayout innerLayout = new LinearLayout(requireContext());
        innerLayout.setOrientation(LinearLayout.HORIZONTAL);
        innerLayout.setPadding(16, 12, 16, 12);
        innerLayout.setGravity(Gravity.CENTER_VERTICAL);

        // Create craft number badge
        TextView numberBadge = new TextView(requireContext());
        numberBadge.setText(String.valueOf(craftNumber));
        numberBadge.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white));
        numberBadge.setTextSize(12f);
        numberBadge.setTypeface(numberBadge.getTypeface(), Typeface.BOLD);
        numberBadge.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.craft_number_badge));
        numberBadge.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams badgeParams = new LinearLayout.LayoutParams(24, 24);
        badgeParams.setMargins(0, 0, 12, 0);
        numberBadge.setLayoutParams(badgeParams);

        // Create craft text
        TextView craftTextView = new TextView(requireContext());
        craftTextView.setText(cleanCraftText(craftText));
        craftTextView.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white));
        craftTextView.setTextSize(14f);
        craftTextView.setLineSpacing(4f, 1f);

        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f
        );
        craftTextView.setLayoutParams(textParams);

        // Add views to layout
        innerLayout.addView(numberBadge);
        innerLayout.addView(craftTextView);
        craftCard.addView(innerLayout);
        container.addView(craftCard);
    }

    private void createSingleCraftView(LinearLayout container, String craftText) {
        TextView singleCraftView = new TextView(requireContext());
        singleCraftView.setText(craftText);
        singleCraftView.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white));
        singleCraftView.setTextSize(14f);
        singleCraftView.setPadding(16, 12, 16, 12);
        singleCraftView.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.craft_card_background));
        singleCraftView.setLineSpacing(4f, 1f);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        singleCraftView.setLayoutParams(params);

        container.addView(singleCraftView);
    }

    private void createNoCraftView(LinearLayout container) {
        TextView noCraftView = new TextView(requireContext());
        noCraftView.setText("No craft ideas available for this item");
        noCraftView.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white));
        noCraftView.setTextSize(14f);
        noCraftView.setPadding(16, 12, 16, 12);
        noCraftView.setAlpha(0.7f);
        noCraftView.setGravity(Gravity.CENTER);
        noCraftView.setTypeface(noCraftView.getTypeface(), Typeface.ITALIC);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        noCraftView.setLayoutParams(params);

        container.addView(noCraftView);
    }

    private String cleanCraftText(String text) {
        // Remove number prefixes like "1.", "2.", etc.
        text = text.replaceFirst("^\\d+\\.\\s*", "");
        // Remove bullet points
        text = text.replaceFirst("^[•-]\\s*", "");
        // Trim whitespace
        return text.trim();
    }

    // Completion section methods
    private void showCompletionSection() {
        // Only show completion section if analysis is complete and we have a current item
        TrashItem currentItem = viewModel.currentItem.getValue();
        RRRViewModel.UIState currentState = viewModel.uiState.getValue();

        if (currentItem != null && currentState == RRRViewModel.UIState.ANALYSIS_COMPLETE) {
            completionSection.setVisibility(View.VISIBLE);
            shareOptionLayout.setVisibility(View.GONE);

            // Reset button states
            completedYesButton.setAlpha(1.0f);
            completedNoButton.setAlpha(1.0f);
        } else {
            completionSection.setVisibility(View.GONE);
        }
    }

    private void handleCompletionYes() {
        // Log the completion (you can add analytics here)
        logActivityCompletion(true);

        // Show sharing options
        shareOptionLayout.setVisibility(View.VISIBLE);

        // Update button states
        completedYesButton.setAlpha(0.7f);
        completedNoButton.setAlpha(0.5f);

        // Show congratulations message
        Toast.makeText(requireContext(), "Great job! 🎉 You're making a difference!", Toast.LENGTH_LONG).show();
    }

    private void handleCompletionNo() {
        // Log the non-completion
        logActivityCompletion(false);

        // Show encouragement message
        Toast.makeText(requireContext(), "That's okay! Every small step counts. Try again when you're ready!", Toast.LENGTH_LONG).show();

        // Navigate to home screen after a brief delay
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            navigateToHome();
        }, 2000);
    }

    private void handleShareWithPhoto() {
        // Get current activity details for the post
        TrashItem currentItem = viewModel.currentItem.getValue();
        RRRViewModel.RRRTab selectedTab = viewModel.selectedTab.getValue();

        if (currentItem != null && selectedTab != null) {
            // Create data to pass to CreatePostActivity
            Bundle postData = new Bundle();
            postData.putString("itemName", currentItem.getName());
            postData.putString("activityType", getActivityTypeString(selectedTab));
            postData.putString("activityDetails", getActivityDetails(currentItem, selectedTab));

            // Navigate to CreatePostActivity
            navigateToCreatePost(postData);
        } else {
            Toast.makeText(requireContext(), "Error preparing post data", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleSkipSharing() {
        Toast.makeText(requireContext(), "Thanks for completing the activity! 🌱", Toast.LENGTH_SHORT).show();

        // Navigate to home screen
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            navigateToHome();
        }, 1500);
    }

    private void logActivityCompletion(boolean completed) {
        TrashItem currentItem = viewModel.currentItem.getValue();
        RRRViewModel.RRRTab selectedTab = viewModel.selectedTab.getValue();

        if (currentItem != null && selectedTab != null) {
            // You can implement analytics logging here
            String activityType = getActivityTypeString(selectedTab);
            String itemName = currentItem.getName();

            Log.d(TAG, "Activity completion logged: " + activityType + " for " + itemName + " - Completed: " + completed);

            // TODO: Add to analytics service
            // AnalyticsService.logActivityCompletion(activityType, itemName, completed);

            // TODO: Add to user's activity history
            // UserActivityService.recordActivity(activityType, itemName, completed);
        }
    }

    private String getActivityTypeString(RRRViewModel.RRRTab tab) {
        switch (tab) {
            case RECYCLE: return "Recycle";
            case REUSE: return "Reuse";
            case REDUCE: return "Reduce";
            default: return "Unknown";
        }
    }

    private String getActivityDetails(TrashItem item, RRRViewModel.RRRTab tab) {
        switch (tab) {
            case RECYCLE:
                if (item.getRecycleInfo() != null) {
                    return item.getRecycleInfo().getRecycleInfo();
                }
                break;
            case REUSE:
                if (item.getReuseInfo() != null) {
                    return item.getReuseInfo().getReuseInfo();
                }
                break;
            case REDUCE:
                if (item.getReduceInfo() != null) {
                    return item.getReduceInfo().getReduceInfo();
                }
                break;
        }
        return "";
    }

    private void navigateToCreatePost(Bundle postData) {
        try {
            // Replace "CreatePostActivity" with your actual activity class
            Intent intent = new Intent(requireContext(), CreatePostActivity.class);
            intent.putExtras(postData);
            startActivity(intent);

            // Finish current activity or fragment
            if (getActivity() != null) {
                getActivity().finish();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error navigating to CreatePostActivity", e);
            Toast.makeText(requireContext(), "Error opening post creation", Toast.LENGTH_SHORT).show();
        }
    }

    private void navigateToHome() {
        try {
            // Navigate to home - adjust based on your navigation structure
            if (getActivity() != null) {
                getActivity().finish(); // Close current activity
            }

            // Alternative: If using Navigation Component
            // NavController navController = Navigation.findNavController(requireView());
            // navController.navigate(R.id.action_rrrFragment_to_homeFragment);

        } catch (Exception e) {
            Log.e(TAG, "Error navigating to home", e);
        }
    }

    // --- Location Services Setup ---
    private void setupLocationServices() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        createLocationRequest();
        createLocationCallback();
    }

    private void createLocationRequest() {
        locationRequest = new LocationRequest.Builder(10000)
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
                    viewModel.updateLocation(location);
                    stopLocationUpdates();
                }
            }

            @Override
            public void onLocationAvailability(@NonNull LocationAvailability locationAvailability) {
                super.onLocationAvailability(locationAvailability);
                if (!locationAvailability.isLocationAvailable()) {
                    Log.w(TAG, "Location not available.");
                }
            }
        };
    }

    private boolean checkLocationPermissions() {
        return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
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
                viewModel.resetToPlaceholder();
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
                        viewModel.analyzeImage(bitmap);
                    } catch (IOException e) {
                        Log.e(TAG, "Failed to load selected image", e);
                        Toast.makeText(requireContext(), "Failed to load selected image.", Toast.LENGTH_SHORT).show();
                        viewModel.resetToPlaceholder();
                    }
                } else {
                    Toast.makeText(requireContext(), "Error: No image was selected.", Toast.LENGTH_SHORT).show();
                    viewModel.resetToPlaceholder();
                }
            } else if (result.getResultCode() == Activity.RESULT_CANCELED) {
                Toast.makeText(requireContext(), "Image selection cancelled.", Toast.LENGTH_SHORT).show();
                viewModel.resetToPlaceholder();
            }
        });

        requestCameraPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
                openCamera();
            } else {
                Toast.makeText(requireContext(), "Camera permission is needed to take photos.", Toast.LENGTH_SHORT).show();
                viewModel.resetToPlaceholder();
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
                viewModel.resetToPlaceholder();
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
            }
        });
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
            viewModel.resetToPlaceholder();
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
            viewModel.resetToPlaceholder();
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
                    viewModel.analyzeImage(bitmap);
                } else {
                    Toast.makeText(requireContext(), "Failed to load captured image bitmap.", Toast.LENGTH_SHORT).show();
                    viewModel.resetToPlaceholder();
                }
            } catch (Exception e) {
                Log.e(TAG, "Error processing captured photo", e);
                Toast.makeText(requireContext(), "Error loading captured image.", Toast.LENGTH_SHORT).show();
                viewModel.resetToPlaceholder();
            }
        } else {
            Toast.makeText(requireContext(), "Error: No image path for captured photo.", Toast.LENGTH_SHORT).show();
            viewModel.resetToPlaceholder();
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

    private Bitmap decodeSampledBitmapFromFile(String path, int reqWidth, int reqHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(path, options);
    }
}