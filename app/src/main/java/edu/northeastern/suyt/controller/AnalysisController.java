package edu.northeastern.suyt.controller;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import edu.northeastern.suyt.model.AnalysisResult;

public class AnalysisController {

    private static final String ANALYSIS_RESULTS_NODE = "analysis_results";

    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;

    public AnalysisController() {
        databaseReference = FirebaseDatabase.getInstance().getReference();
        firebaseAuth = FirebaseAuth.getInstance();
    }

    // Callback interfaces
    public interface RecentAnalysisCallback {
        void onSuccess(AnalysisResult analysisResult);
        void onFailure(String errorMessage);
    }

    public interface AnalysisListCallback {
        void onSuccess(List<AnalysisResult> analysisList);
        void onFailure(String errorMessage);
    }

    public interface AnalysisOperationCallback {
        void onSuccess();
        void onFailure(String errorMessage);
    }

    /**
     * Get the user's most recent analysis result
     */
    public void getUserRecentAnalysis(RecentAnalysisCallback callback) {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            callback.onFailure("User not authenticated");
            return;
        }

        String userId = currentUser.getUid();

        // Query to get the most recent analysis for the current user
        Query recentAnalysisQuery = databaseReference
                .child(ANALYSIS_RESULTS_NODE)
                .orderByChild("userId")
                .equalTo(userId)
                .limitToLast(1);

        recentAnalysisQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                AnalysisResult mostRecentAnalysis = null;

                for (DataSnapshot analysisSnapshot : dataSnapshot.getChildren()) {
                    AnalysisResult analysis = analysisSnapshot.getValue(AnalysisResult.class);
                    if (analysis != null) {
                        mostRecentAnalysis = analysis;
                        // Since we're using limitToLast(1), there should only be one result
                        break;
                    }
                }

                callback.onSuccess(mostRecentAnalysis);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onFailure("Failed to fetch recent analysis: " + databaseError.getMessage());
            }
        });
    }

    /**
     * Get all analysis results for the current user
     */
    public void getUserAnalysisList(AnalysisListCallback callback) {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            callback.onFailure("User not authenticated");
            return;
        }

        String userId = currentUser.getUid();

        Query userAnalysisQuery = databaseReference
                .child(ANALYSIS_RESULTS_NODE)
                .orderByChild("userId")
                .equalTo(userId);

        userAnalysisQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<AnalysisResult> analysisList = new ArrayList<>();

                for (DataSnapshot analysisSnapshot : dataSnapshot.getChildren()) {
                    AnalysisResult analysis = analysisSnapshot.getValue(AnalysisResult.class);
                    if (analysis != null) {
                        analysisList.add(analysis);
                    }
                }

                // Sort by timestamp (most recent first)
                analysisList.sort((a1, a2) -> Long.compare(a2.getTimestamp(), a1.getTimestamp()));

                callback.onSuccess(analysisList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onFailure("Failed to fetch analysis list: " + databaseError.getMessage());
            }
        });
    }

    /**
     * Get recent analysis results (limited number)
     */
    public void getRecentAnalysisList(int limit, AnalysisListCallback callback) {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            callback.onFailure("User not authenticated");
            return;
        }

        String userId = currentUser.getUid();

        Query recentAnalysisQuery = databaseReference
                .child(ANALYSIS_RESULTS_NODE)
                .orderByChild("userId")
                .equalTo(userId)
                .limitToLast(limit);

        recentAnalysisQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<AnalysisResult> analysisList = new ArrayList<>();

                for (DataSnapshot analysisSnapshot : dataSnapshot.getChildren()) {
                    AnalysisResult analysis = analysisSnapshot.getValue(AnalysisResult.class);
                    if (analysis != null) {
                        analysisList.add(analysis);
                    }
                }

                // Sort by timestamp (most recent first)
                analysisList.sort((a1, a2) -> Long.compare(a2.getTimestamp(), a1.getTimestamp()));

                callback.onSuccess(analysisList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onFailure("Failed to fetch recent analysis: " + databaseError.getMessage());
            }
        });
    }

    /**
     * Save a new analysis result
     */
    public void saveAnalysisResult(AnalysisResult analysisResult, AnalysisOperationCallback callback) {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            callback.onFailure("User not authenticated");
            return;
        }

        // Ensure the userId is set
        if (analysisResult.getUserId() == null) {
            analysisResult.setUserId(currentUser.getUid());
        }

        // Generate ID if not set
        if (analysisResult.getId() == null) {
            String analysisId = databaseReference.child(ANALYSIS_RESULTS_NODE).push().getKey();
            analysisResult.setId(analysisId);
        }

        databaseReference
                .child(ANALYSIS_RESULTS_NODE)
                .child(analysisResult.getId())
                .setValue(analysisResult.toMap())
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure("Failed to save analysis: " + e.getMessage()));
    }

    /**
     * Delete an analysis result
     */
    public void deleteAnalysisResult(String analysisId, AnalysisOperationCallback callback) {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            callback.onFailure("User not authenticated");
            return;
        }

        databaseReference
                .child(ANALYSIS_RESULTS_NODE)
                .child(analysisId)
                .removeValue()
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure("Failed to delete analysis: " + e.getMessage()));
    }

    /**
     * Get analysis count for the current user
     */
    public void getUserAnalysisCount(UserAnalysisCountCallback callback) {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            callback.onFailure("User not authenticated");
            return;
        }

        String userId = currentUser.getUid();

        Query countQuery = databaseReference
                .child(ANALYSIS_RESULTS_NODE)
                .orderByChild("userId")
                .equalTo(userId);

        countQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int count = (int) dataSnapshot.getChildrenCount();
                callback.onSuccess(count);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onFailure("Failed to get analysis count: " + databaseError.getMessage());
            }
        });
    }

    public interface UserAnalysisCountCallback {
        void onSuccess(int count);
        void onFailure(String errorMessage);
    }
}