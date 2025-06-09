package edu.northeastern.suyt.ui.viewmodel;

import android.app.Application;
import android.graphics.Bitmap;
import android.location.Location;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.ai.type.Content;
import com.google.firebase.ai.type.GenerateContentResponse;
import com.google.firebase.ai.type.Schema;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import edu.northeastern.suyt.firebase.repository.database.AnalysisRepository;
import edu.northeastern.suyt.gemini.GeminiClient;
import edu.northeastern.suyt.model.AnalysisResult;
import edu.northeastern.suyt.model.TrashItem;

public class RRRViewModel extends ViewModel {

    private static final String TAG = "RRRViewModel";

    private final MutableLiveData<UIState> _uiState = new MutableLiveData<>(UIState.PLACEHOLDER);
    public final LiveData<UIState> uiState = _uiState;

    private final MutableLiveData<TrashItem> _currentItem = new MutableLiveData<>();
    public final LiveData<TrashItem> currentItem = _currentItem;

    private final MutableLiveData<AnalysisResult> _currentAnalysisResult = new MutableLiveData<>();
    public final LiveData<AnalysisResult> currentAnalysisResult = _currentAnalysisResult;

    private final MutableLiveData<RRRTab> _selectedTab = new MutableLiveData<>(RRRTab.RECYCLE);
    public final LiveData<RRRTab> selectedTab = _selectedTab;

    private final MutableLiveData<String> _statusMessage = new MutableLiveData<>();
    public final LiveData<String> statusMessage = _statusMessage;

    private final MutableLiveData<String> _errorMessage = new MutableLiveData<>();
    public final LiveData<String> errorMessage = _errorMessage;

    private final MutableLiveData<Location> _currentLocation = new MutableLiveData<>();
    public final LiveData<Location> currentLocation = _currentLocation;

    private final MutableLiveData<Boolean> _hasRecentAnalysis = new MutableLiveData<>(false);
    public final LiveData<Boolean> hasRecentAnalysis = _hasRecentAnalysis;

    private FusedLocationProviderClient fusedLocationProviderClient;
    private ThreadPoolExecutor geminiExecutor;
    private AnalysisRepository analysisRepository;
    private FirebaseAuth firebaseAuth;
    private String currentUserId;

    public RRRViewModel() {
        super();

        analysisRepository = new AnalysisRepository();
        firebaseAuth = FirebaseAuth.getInstance();

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            currentUserId = currentUser.getUid();
        } else {
            currentUserId = "anonymous_user";
            Log.w(TAG, "No authenticated user found");
        }

        int numThreads = Runtime.getRuntime().availableProcessors();
        geminiExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(numThreads);

        setPlaceholderState();

        // fusedLocationProviderClient is null - location services unavailable
        Log.w(TAG, "RRRViewModel created without Application context - location services unavailable");
    }

    public RRRViewModel(@NonNull Application application) {
        super();

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(application);
        analysisRepository = new AnalysisRepository();
        firebaseAuth = FirebaseAuth.getInstance();

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            currentUserId = currentUser.getUid();
        } else {
            currentUserId = "anonymous_user";
            Log.w(TAG, "No authenticated user found");
        }

        int numThreads = Runtime.getRuntime().availableProcessors();
        geminiExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(numThreads);

        checkForRecentAnalysis();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (geminiExecutor != null && !geminiExecutor.isShutdown()) {
            geminiExecutor.shutdown();
        }
    }

    public void analyzeImage(Bitmap bitmap) {
        analyzeImage(bitmap, null);
    }

    public void analyzeImage(Bitmap bitmap, String imagePath) {
        if (bitmap == null) {
            _errorMessage.setValue("Invalid image provided");
            return;
        }

        if (currentUserId != null) {
            analysisRepository.clearUserRecentAnalysis(currentUserId);
        }

        setLoadingState();

        getCurrentLocationAndAnalyze(bitmap, imagePath);
    }

    public void selectTab(RRRTab tab) {
        if (_currentItem.getValue() == null) {
            _errorMessage.setValue("Please scan an item first.");
            return;
        }

        _selectedTab.setValue(tab);
        _uiState.setValue(UIState.ANALYSIS_COMPLETE);
    }

    public void resetToPlaceholder() {
        setPlaceholderState();
    }

    public void startNewAnalysis() {
        _currentItem.setValue(null);
        _currentAnalysisResult.setValue(null);
        _hasRecentAnalysis.setValue(false);
        setPlaceholderState();

        if (currentUserId != null) {
            analysisRepository.clearUserRecentAnalysis(currentUserId);
        }
    }

    public void loadRecentAnalysis() {
        if (currentUserId != null) {
            _uiState.setValue(UIState.LOADING);
            _statusMessage.setValue("Loading recent analysis...");

            analysisRepository.getUserRecentAnalysis(currentUserId, new AnalysisRepository.AnalysisCallback() {
                @Override
                public void onResult(AnalysisResult result) {
                    new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                        if (result != null && result.getTrashItem() != null) {
                            _currentAnalysisResult.setValue(result);
                            _currentItem.setValue(result.getTrashItem());

                            RRRTab defaultTab = determineDefaultTab(result.getTrashItem());
                            _selectedTab.setValue(defaultTab);

                            _hasRecentAnalysis.setValue(true);
                            setAnalysisCompleteState();
                        } else {
                            _hasRecentAnalysis.setValue(false);
                            setPlaceholderState();
                        }
                    });
                }

                @Override
                public void onError(String error) {
                    new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                        Log.e(TAG, "Failed to load recent analysis: " + error);
                        _hasRecentAnalysis.setValue(false);
                        setPlaceholderState();
                    });
                }
            });
        }
    }

    public void updateLocation(Location location) {
        _currentLocation.setValue(location);
    }

    private void checkForRecentAnalysis() {
        if (currentUserId != null) {
            analysisRepository.hasRecentAnalysis(currentUserId, result -> {
                new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                    _hasRecentAnalysis.setValue(result);
                    if (result) {
                        loadRecentAnalysis();
                    } else {
                        setPlaceholderState();
                    }
                });
            });
        } else {
            setPlaceholderState();
        }
    }

    private void setPlaceholderState() {
        _uiState.setValue(UIState.PLACEHOLDER);
        _selectedTab.setValue(RRRTab.RECYCLE);
        _statusMessage.setValue("Ready to discover an item?");
        _errorMessage.setValue(null);
    }

    private void setLoadingState() {
        _uiState.setValue(UIState.LOADING);
        _statusMessage.setValue("Analyzing image...");
        _errorMessage.setValue(null);
    }

    private void setAnalysisCompleteState() {
        _uiState.setValue(UIState.ANALYSIS_COMPLETE);
        _statusMessage.setValue(null);
    }

    private void getCurrentLocationAndAnalyze(Bitmap bitmap, String imagePath) {
        _statusMessage.setValue("Getting location data...");

        // Check if location services are available
        if (fusedLocationProviderClient != null) {
            try {
                fusedLocationProviderClient.getLastLocation()
                        .addOnSuccessListener(location -> {
                            if (location != null) {
                                _currentLocation.setValue(location);
                                Log.d(TAG, "Location obtained: " + location.getLatitude() + ", " + location.getLongitude());
                            }
                            processImageWithGemini(bitmap, imagePath, location);
                        })
                        .addOnFailureListener(e -> {
                            Log.w(TAG, "Failed to get location: " + e.getMessage());
                            processImageWithGemini(bitmap, imagePath, null);
                        });
            } catch (SecurityException e) {
                Log.w(TAG, "Location permission not granted: " + e.getMessage());
                processImageWithGemini(bitmap, imagePath, null);
            }
        } else {
            // Location services not available - proceed without location
            Log.w(TAG, "Location services not available - proceeding without location");
            processImageWithGemini(bitmap, imagePath, null);
        }
    }

    private void processImageWithGemini(Bitmap bitmap, String imagePath, Location location) {
        _statusMessage.setValue("Analyzing image...");

        String locationContext = "";
        if (location != null) {
            locationContext = String.format(Locale.US,
                    " My current coordinates are latitude %.6f and longitude %.6f.",
                    location.getLatitude(), location.getLongitude());
        }

        String promptText = buildAnalysisPrompt(locationContext);

        Content prompt = new Content.Builder()
                .addImage(bitmap)
                .addText(promptText)
                .build();

        TrashItem trashItem = new TrashItem();
        Schema trashItemSchema = trashItem.getSchema();

        ListenableFuture<GenerateContentResponse> response = new GeminiClient(trashItemSchema).generateResult(prompt);

        Futures.addCallback(
                response,
                new FutureCallback<GenerateContentResponse>() {
                    @Override
                    public void onSuccess(GenerateContentResponse result) {
                        handleGeminiSuccess(result, imagePath, location);
                    }

                    @Override
                    public void onFailure(@NonNull Throwable t) {
                        handleGeminiFailure(t);
                    }
                },
                geminiExecutor
        );
    }

    private String buildAnalysisPrompt(String locationContext) {
        return "Analyze this image and identify the item. " +
                "For 'recycleInfo': If recyclable, provide a concise 'description'. Then, specify the 'nearestRecyclingCenter' (e.g., Mountain View Recycling Center), 'suggestedBin' (e.g., Blue bin for plastics), and 'recyclingHours' (e.g., Mon-Sat, 8 AM - 5 PM). Prioritize specific information relevant to " + locationContext + ".\n" +
                "For 'reuseInfo': If reusable, provide a concise 'description'. Then, list 3-4 'craftsPossible' (e.g., '1. Bottle Cap Mosaic: ..., 2. Plastic Bottle Planter: ...'), an overall 'timeNeededForCraft' (e.g., '1-2 hours per craft'), and 'moneyNeededForCraft' (e.g., '$0-5 per craft'). Make them appealing and practical.\n" +
                "For 'reduceInfo': If reducible, provide a concise 'description'. Then, include 'howManyShouldICollect' for selling (e.g., '500 bottles'), 'moneyExpected' (e.g., '$25 depending on rate'), and 'otherSuggestions' for reduction (e.g., 'Use a reusable water bottle'). Ensure all information is directly related to the item in the image.";
    }

    private void handleGeminiSuccess(GenerateContentResponse result, String imagePath, Location location) {
        new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
            String jsonResponse = result.getText();
            Log.d(TAG, "Gemini Raw JSON Response: " + jsonResponse);

            if (jsonResponse != null && !jsonResponse.isEmpty()) {
                try {
                    ObjectMapper objectMapper = new ObjectMapper();
                    TrashItem item = objectMapper.readValue(jsonResponse, TrashItem.class);

                    if (item != null) {
                        _currentItem.setValue(item);

                        AnalysisResult analysisResult = new AnalysisResult(currentUserId, item.getName(), item);
                        if (location != null) {
                            analysisResult.setLatitude(location.getLatitude());
                            analysisResult.setLongitude(location.getLongitude());
                        }
                        if (imagePath != null) {
                            analysisResult.setLocalImagePath(imagePath);
                        }

                        saveAnalysisResult(analysisResult);

                        RRRTab defaultTab = determineDefaultTab(item);
                        _selectedTab.setValue(defaultTab);

                        _hasRecentAnalysis.setValue(true);
                        setAnalysisCompleteState();
                    } else {
                        _errorMessage.setValue("Failed to parse item data from Gemini.");
                        setPlaceholderState();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing Gemini JSON response", e);
                    _errorMessage.setValue("Error parsing Gemini response: " + e.getMessage());
                    setPlaceholderState();
                }
            } else {
                _errorMessage.setValue("Gemini returned an empty response.");
                setPlaceholderState();
            }
        });
    }

    private void saveAnalysisResult(AnalysisResult analysisResult) {
        analysisRepository.saveAnalysisResult(analysisResult)
                .addOnSuccessListener(aVoid -> {
                    _currentAnalysisResult.setValue(analysisResult);
                    Log.d(TAG, "Analysis result saved successfully");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to save analysis result: " + e.getMessage());
                });
    }

    private void handleGeminiFailure(Throwable t) {
        new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
            Log.e(TAG, "Gemini API call failed", t);
            _errorMessage.setValue("Gemini analysis failed: " + t.getMessage());
            setPlaceholderState();
        });
    }

    private RRRTab determineDefaultTab(TrashItem item) {
        if (item.isRecyclable()) {
            return RRRTab.RECYCLE;
        } else if (item.isReusable()) {
            return RRRTab.REUSE;
        } else if (item.isReducible()) {
            return RRRTab.REDUCE;
        } else {
            return RRRTab.RECYCLE;
        }
    }

    public String getCurrentItemName() {
        TrashItem item = _currentItem.getValue();
        return item != null ? item.getName() : "Ready to discover an item?";
    }

    public boolean hasValidDataForSelectedTab() {
        TrashItem item = _currentItem.getValue();
        RRRTab tab = _selectedTab.getValue();

        if (item == null || tab == null) return false;

        switch (tab) {
            case RECYCLE:
                return item.getRecycleInfo() != null && hasValidRecycleData(item);
            case REUSE:
                return item.getReuseInfo() != null && hasValidReuseData(item);
            case REDUCE:
                return item.getReduceInfo() != null && hasValidReduceData(item);
            default:
                return false;
        }
    }

    private boolean hasValidRecycleData(TrashItem item) {
        if (item.getRecycleInfo() == null) return false;

        return !isEmptyOrNull(item.getRecycleInfo().getRecycleInfo()) ||
                !isEmptyOrNull(item.getRecycleInfo().getNearestRecyclingCenter()) ||
                !isEmptyOrNull(item.getRecycleInfo().getSuggestedBin()) ||
                !isEmptyOrNull(item.getRecycleInfo().getRecyclingHours());
    }

    private boolean hasValidReuseData(TrashItem item) {
        if (item.getReuseInfo() == null) return false;

        return !isEmptyOrNull(item.getReuseInfo().getReuseInfo()) ||
                !isEmptyOrNull(item.getReuseInfo().getCraftsPossible()) ||
                !isEmptyOrNull(item.getReuseInfo().getTimeNeededForCraft()) ||
                !isEmptyOrNull(item.getReuseInfo().getMoneyNeededForCraft());
    }

    private boolean hasValidReduceData(TrashItem item) {
        if (item.getReduceInfo() == null) return false;

        return !isEmptyOrNull(item.getReduceInfo().getReduceInfo()) ||
                !isEmptyOrNull(item.getReduceInfo().getHowManyShouldICollect()) ||
                !isEmptyOrNull(item.getReduceInfo().getMoneyExpected()) ||
                !isEmptyOrNull(item.getReduceInfo().getOtherSuggestions());
    }

    private boolean isEmptyOrNull(String str) {
        return str == null || str.trim().isEmpty();
    }

    public enum UIState {
        PLACEHOLDER,
        LOADING,
        ANALYSIS_COMPLETE
    }

    public enum RRRTab {
        RECYCLE,
        REUSE,
        REDUCE
    }
}