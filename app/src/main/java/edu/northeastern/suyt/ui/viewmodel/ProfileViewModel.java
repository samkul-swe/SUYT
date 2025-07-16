package edu.northeastern.suyt.ui.viewmodel;

import static androidx.core.content.ContextCompat.startActivity;
import static androidx.lifecycle.AndroidViewModel_androidKt.getApplication;

import android.content.Intent;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import edu.northeastern.suyt.controller.AnalysisController;
import edu.northeastern.suyt.controller.UserController;
import edu.northeastern.suyt.model.AnalysisResult;
import edu.northeastern.suyt.ui.activities.LoginActivity;
import edu.northeastern.suyt.utils.GeneralHelper;
import edu.northeastern.suyt.utils.SessionManager;

public class ProfileViewModel extends ViewModel {

    private final MutableLiveData<String> username = new MutableLiveData<>();
    private final MutableLiveData<String> email = new MutableLiveData<>();
    private final MutableLiveData<String> rank = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<AnalysisResult> recentAnalysis = new MutableLiveData<>();

    private final MutableLiveData<Boolean> isLogoutLoading = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> isAnalysisLoading = new MutableLiveData<>(false);

    private final MutableLiveData<Boolean> emailUpdateSuccess = new MutableLiveData<>();
    private final MutableLiveData<Boolean> passwordUpdateSuccess = new MutableLiveData<>();
    private final MutableLiveData<Boolean> logoutSuccess = new MutableLiveData<>();

    private UserController userController;
    private AnalysisController analysisController;
    private SessionManager sessionManager;
    private GeneralHelper generalHelper;

    public ProfileViewModel() {
        analysisController = new AnalysisController();
        generalHelper = new GeneralHelper();
    }

    public void initialize(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
        this.userController = new UserController(sessionManager.getUserId());
        loadProfileData();
    }

    public LiveData<String> getUsername() { return username; }
    public LiveData<String> getEmail() { return email; }
    public LiveData<String> getRank() { return rank; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
    public LiveData<AnalysisResult> getRecentAnalysis() { return recentAnalysis; }
    public LiveData<Boolean> getIsLogoutLoading() { return isLogoutLoading; }
    public LiveData<Boolean> getIsAnalysisLoading() { return isAnalysisLoading; }
    public LiveData<Boolean> getEmailUpdateSuccess() { return emailUpdateSuccess; }
    public LiveData<Boolean> getPasswordUpdateSuccess() { return passwordUpdateSuccess; }
    public LiveData<Boolean> getLogoutSuccess() { return logoutSuccess; }

    public void loadProfileData() {
        if (sessionManager == null) return;

        isLoading.setValue(true);

        String usernameValue = sessionManager.getUsername();
        username.setValue(usernameValue != null ? usernameValue : "Swarley");

        String emailValue = sessionManager.getEmail();
        email.setValue(emailValue != null ? emailValue : "exampler@example.com");

        int totalPoints = sessionManager.getReducePoints() +
                sessionManager.getReusePoints() +
                sessionManager.getRecyclePoints();
        rank.setValue(generalHelper.calculateUserRank(totalPoints));

        isLoading.setValue(false);
    }

    public void loadRecentAnalysis() {
        if (analysisController == null) return;

        isAnalysisLoading.setValue(true);

        analysisController.getUserRecentAnalysis(new AnalysisController.RecentAnalysisCallback() {
            @Override
            public void onSuccess(AnalysisResult analysisResult) {
                isAnalysisLoading.setValue(false);
                recentAnalysis.setValue(analysisResult);

                if (analysisResult == null) {
                    errorMessage.setValue("No recent analysis found. Start analyzing items to see your results here!");
                }
            }

            @Override
            public void onFailure(String error) {
                isAnalysisLoading.setValue(false);
                errorMessage.setValue("Failed to load recent analysis: " + error);
            }
        });
    }

    public void updateEmail(String newEmail) {
        if (userController == null) return;

        isLoading.setValue(true);

        userController.updateUserEmail(newEmail, new UserController.UpdateCallback() {
            @Override
            public void onSuccess() {
                isLoading.setValue(false);
                email.setValue(newEmail);
                emailUpdateSuccess.setValue(true);
            }

            @Override
            public void onFailure(String errorMsg) {
                isLoading.setValue(false);
                errorMessage.setValue("Failed to update email: " + errorMsg);
                emailUpdateSuccess.setValue(false);
            }
        });
    }

    public void updatePassword(String currentPassword, String newPassword) {
        if (userController == null) return;

        isLoading.setValue(true);

        userController.updateUserPassword(currentPassword, newPassword, new UserController.UpdateCallback() {
            @Override
            public void onSuccess() {
                isLoading.setValue(false);
                passwordUpdateSuccess.setValue(true);
            }

            @Override
            public void onFailure(String errorMsg) {
                isLoading.setValue(false);
                errorMessage.setValue("Failed to update password: " + errorMsg);
                passwordUpdateSuccess.setValue(false);
            }
        });
    }

    public void logout() {
        if (userController == null) return;

        isLogoutLoading.setValue(true);

        userController.logoutUser(new UserController.LogoutCallback() {
            @Override
            public void onSuccess() {
                isLogoutLoading.setValue(false);
                logoutSuccess.setValue(true);
                sessionManager.logout();
            }

            @Override
            public void onFailure(String errorMsg) {
                isLogoutLoading.setValue(false);
                errorMessage.setValue("Logout failed: " + errorMsg);
                logoutSuccess.setValue(false);
            }
        });
    }

    public void clearErrorMessage() {
        errorMessage.setValue(null);
    }

    public void clearSuccessFlags() {
        emailUpdateSuccess.setValue(null);
        passwordUpdateSuccess.setValue(null);
        logoutSuccess.setValue(null);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
    }
}