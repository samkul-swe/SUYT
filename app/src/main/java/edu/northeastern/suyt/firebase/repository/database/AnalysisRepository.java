package edu.northeastern.suyt.firebase.repository.database;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import edu.northeastern.suyt.firebase.DatabaseConnector;
import edu.northeastern.suyt.model.AnalysisResult;

public class AnalysisRepository {
    private static final String TAG = "AnalysisRepository";
    private final DatabaseConnector databaseConnector;

    public AnalysisRepository() {
        this.databaseConnector = DatabaseConnector.getInstance();
    }

    public Task<Void> saveAnalysisResult(AnalysisResult analysisResult) {
        if (analysisResult == null || analysisResult.getId() == null) {
            throw new IllegalArgumentException("Analysis result and ID cannot be null");
        }

        DatabaseReference analysisRef = databaseConnector.getAnalysisResultReference(analysisResult.getId());

        return analysisRef.setValue(analysisResult.toMap())
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Analysis result saved successfully: " + analysisResult.getId());
                    updateUserAnalysisHistory(analysisResult);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to save analysis result: " + e.getMessage());
                });
    }

    private void updateUserAnalysisHistory(AnalysisResult analysisResult) {
        String userId = analysisResult.getUserId();
        String analysisId = analysisResult.getId();

        Map<String, Object> historyEntry = new HashMap<>();
        historyEntry.put("analysisId", analysisId);
        historyEntry.put("itemName", analysisResult.getItemName());
        historyEntry.put("timestamp", analysisResult.getTimestamp());
        historyEntry.put("imageUrl", analysisResult.getImageUrl());

        DatabaseReference historyRef = databaseConnector.getUserSpecificAnalysisReference(userId, analysisId);
        historyRef.setValue(historyEntry);

        DatabaseReference recentRef = databaseConnector.getUserRecentAnalysisReference(userId);
        recentRef.setValue(analysisId)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Updated user recent analysis: " + analysisId);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to update recent analysis: " + e.getMessage());
                });
    }

    public void getUserRecentAnalysis(String userId, AnalysisCallback callback) {
        DatabaseReference recentRef = databaseConnector.getUserRecentAnalysisReference(userId);

        recentRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String recentAnalysisId = snapshot.getValue(String.class);
                    if (recentAnalysisId != null) {
                        getAnalysisResult(recentAnalysisId, callback);
                    } else {
                        callback.onResult(null);
                    }
                } else {
                    callback.onResult(null);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to get recent analysis: " + error.getMessage());
                callback.onError(error.getMessage());
            }
        });
    }

    public void getAnalysisResult(String analysisId, AnalysisCallback callback) {
        DatabaseReference analysisRef = databaseConnector.getAnalysisResultReference(analysisId);

        analysisRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    try {
                        AnalysisResult result = snapshot.getValue(AnalysisResult.class);
                        callback.onResult(result);
                    } catch (Exception e) {
                        Log.e(TAG, "Failed to parse analysis result: " + e.getMessage());
                        callback.onError("Failed to parse analysis result");
                    }
                } else {
                    callback.onResult(null);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to get analysis result: " + error.getMessage());
                callback.onError(error.getMessage());
            }
        });
    }

    public void hasRecentAnalysis(String userId, BooleanCallback callback) {
        DatabaseReference recentRef = databaseConnector.getUserRecentAnalysisReference(userId);

        recentRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                callback.onResult(snapshot.exists() && snapshot.getValue() != null);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to check recent analysis: " + error.getMessage());
                callback.onResult(false);
            }
        });
    }

    public Task<Void> clearUserRecentAnalysis(String userId) {
        DatabaseReference recentRef = databaseConnector.getUserRecentAnalysisReference(userId);
        return recentRef.removeValue()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Cleared recent analysis for user: " + userId);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to clear recent analysis: " + e.getMessage());
                });
    }

    public interface AnalysisCallback {
        void onResult(AnalysisResult result);
        void onError(String error);
    }

    public interface BooleanCallback {
        void onResult(boolean result);
    }
}