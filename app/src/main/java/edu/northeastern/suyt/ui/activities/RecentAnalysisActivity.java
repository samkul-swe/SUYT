package edu.northeastern.suyt.ui.activities;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;

import edu.northeastern.suyt.R;
import edu.northeastern.suyt.model.AnalysisResult;
import edu.northeastern.suyt.model.TrashItem;

public class RecentAnalysisActivity extends AppCompatActivity {

    private ImageView analysisImageView;
    private TextView itemNameTextView;
    private TextView timestampTextView;
    private TextView recyclableStatusTextView;
    private TextView reusableStatusTextView;
    private TextView reducibleStatusTextView;
    private TextView recyclingInfoTextView;
    private TextView reuseInfoTextView;
    private TextView reduceInfoTextView;
    private TextView locationTextView;

    private AnalysisResult analysisResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recent_analysis);

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Recent Analysis");
        }
    }

    private void displayRecycleInfo(TrashItem trashItem) {
        if (trashItem.isRecyclable() && trashItem.getRecycleInfo() != null) {
            StringBuilder recycleText = new StringBuilder();

            if (trashItem.getRecycleInfo().getRecycleInfo() != null) {
                recycleText.append(trashItem.getRecycleInfo().getRecycleInfo()).append("\n\n");
            }

            if (trashItem.getRecycleInfo().getSuggestedBin() != null) {
                recycleText.append("Suggested Bin: ").append(trashItem.getRecycleInfo().getSuggestedBin()).append("\n\n");
            }

            if (trashItem.getRecycleInfo().getNearestRecyclingCenter() != null) {
                recycleText.append("Nearest Center: ").append(trashItem.getRecycleInfo().getNearestRecyclingCenter()).append("\n\n");
            }

            if (trashItem.getRecycleInfo().getRecyclingHours() != null) {
                recycleText.append("Hours: ").append(trashItem.getRecycleInfo().getRecyclingHours());
            }

            recyclingInfoTextView.setText(recycleText.toString().trim());
        } else {
            recyclingInfoTextView.setText("This item is not recyclable or recycling information is not available.");
        }
    }

    private void displayReuseInfo(TrashItem trashItem) {
        if (trashItem.isReusable() && trashItem.getReuseInfo() != null) {
            StringBuilder reuseText = new StringBuilder();

            if (trashItem.getReuseInfo().getReuseInfo() != null) {
                reuseText.append(trashItem.getReuseInfo().getReuseInfo()).append("\n\n");
            }

            if (trashItem.getReuseInfo().getCraftsPossible() != null) {
                reuseText.append("Possible Crafts: ").append(trashItem.getReuseInfo().getCraftsPossible()).append("\n\n");
            }

            if (trashItem.getReuseInfo().getTimeNeededForCraft() != null) {
                reuseText.append("Time Needed: ").append(trashItem.getReuseInfo().getTimeNeededForCraft()).append("\n\n");
            }

            if (trashItem.getReuseInfo().getMoneyNeededForCraft() != null) {
                reuseText.append("Cost: ").append(trashItem.getReuseInfo().getMoneyNeededForCraft());
            }

            reuseInfoTextView.setText(reuseText.toString().trim());
        } else {
            reuseInfoTextView.setText("This item is not suitable for reuse or reuse information is not available.");
        }
    }

    private void displayReduceInfo(TrashItem trashItem) {
        if (trashItem.isReducible() && trashItem.getReduceInfo() != null) {
            StringBuilder reduceText = new StringBuilder();

            if (trashItem.getReduceInfo().getReduceInfo() != null) {
                reduceText.append(trashItem.getReduceInfo().getReduceInfo()).append("\n\n");
            }

            if (trashItem.getReduceInfo().getHowManyShouldICollect() != null) {
                reduceText.append("Collection Quantity: ").append(trashItem.getReduceInfo().getHowManyShouldICollect()).append("\n\n");
            }

            if (trashItem.getReduceInfo().getMoneyExpected() != null) {
                reduceText.append("Expected Return: ").append(trashItem.getReduceInfo().getMoneyExpected()).append("\n\n");
            }

            if (trashItem.getReduceInfo().getOtherSuggestions() != null) {
                reduceText.append("Other Suggestions: ").append(trashItem.getReduceInfo().getOtherSuggestions());
            }

            reduceInfoTextView.setText(reduceText.toString().trim());
        } else {
            reduceInfoTextView.setText("This item is not suitable for reduction or reduce information is not available.");
        }

        // Initialize views
        initializeViews();

        // Get analysis result from intent
        getAnalysisFromIntent();

        // Display analysis data
        displayAnalysisData();
    }

    private void initializeViews() {
        analysisImageView = findViewById(R.id.analysis_image_view);
        itemNameTextView = findViewById(R.id.item_name_text_view);
        timestampTextView = findViewById(R.id.timestamp_text_view);
        recyclableStatusTextView = findViewById(R.id.recyclable_status_text_view);
        reusableStatusTextView = findViewById(R.id.reusable_status_text_view);
        reducibleStatusTextView = findViewById(R.id.reducible_status_text_view);
        recyclingInfoTextView = findViewById(R.id.recycling_info_text_view);
        reuseInfoTextView = findViewById(R.id.reuse_info_text_view);
        reduceInfoTextView = findViewById(R.id.reduce_info_text_view);
        locationTextView = findViewById(R.id.location_text_view);
    }

    private void getAnalysisFromIntent() {
        analysisResult = getIntent().getParcelableExtra("analysis_result");

        if (analysisResult == null) {
            Toast.makeText(this, "No analysis data found", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void displayAnalysisData() {
        if (analysisResult == null) return;

        // Display basic info
        itemNameTextView.setText(analysisResult.getItemName());
        timestampTextView.setText(analysisResult.getFormattedTimestamp());

        // Load image
        if (analysisResult.getImageUrl() != null && !analysisResult.getImageUrl().isEmpty()) {
            Glide.with(this)
                    .load(analysisResult.getImageUrl())
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.placeholder_image)
                    .into(analysisImageView);
        } else if (analysisResult.getLocalImagePath() != null && !analysisResult.getLocalImagePath().isEmpty()) {
            Glide.with(this)
                    .load(analysisResult.getLocalImagePath())
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.placeholder_image)
                    .into(analysisImageView);
        } else {
            analysisImageView.setImageResource(R.drawable.placeholder_image);
        }

        // Display trash item details using 3Rs approach
        TrashItem trashItem = analysisResult.getTrashItem();
        if (trashItem != null) {
            // Display 3Rs status
            recyclableStatusTextView.setText(trashItem.isRecyclable() ? "✓ Recyclable" : "✗ Not Recyclable");
            recyclableStatusTextView.setTextColor(getResources().getColor(
                    trashItem.isRecyclable() ? R.color.success_color : R.color.error_color));

            reusableStatusTextView.setText(trashItem.isReusable() ? "✓ Reusable" : "✗ Not Reusable");
            reusableStatusTextView.setTextColor(getResources().getColor(
                    trashItem.isReusable() ? R.color.success_color : R.color.error_color));

            reducibleStatusTextView.setText(trashItem.isReducible() ? "✓ Reducible" : "✗ Not Reducible");
            reducibleStatusTextView.setTextColor(getResources().getColor(
                    trashItem.isReducible() ? R.color.success_color : R.color.error_color));

            // Display detailed information for each R
            displayRecycleInfo(trashItem);
            displayReuseInfo(trashItem);
            displayReduceInfo(trashItem);
        } else {
            // Default values when no trash item data
            recyclableStatusTextView.setText("Analysis data not available");
            reusableStatusTextView.setText("Analysis data not available");
            reducibleStatusTextView.setText("Analysis data not available");
            recyclingInfoTextView.setText("Recycling information not available");
            reuseInfoTextView.setText("Reuse information not available");
            reduceInfoTextView.setText("Reduce information not available");
        }

        // Display location info
        if (analysisResult.hasLocation()) {
            locationTextView.setText(String.format("Location: %.6f, %.6f",
                    analysisResult.getLatitude(), analysisResult.getLongitude()));
        } else {
            locationTextView.setText("Location: Not recorded");
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}