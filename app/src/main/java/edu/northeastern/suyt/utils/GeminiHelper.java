package edu.northeastern.suyt.utils;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.ai.type.Content;
import com.google.firebase.ai.type.GenerateContentResponse;
import com.google.firebase.ai.type.Schema;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import edu.northeastern.suyt.gemini.GeminiClient;

public class GeminiHelper {

    private final String TAG = "GeminiHelper";
    private ThreadPoolExecutor geminiExecutor;
    private boolean isGenerating = false;

    public GeminiHelper() {
        int numThreads = Runtime.getRuntime().availableProcessors();
        geminiExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(numThreads);
    }

    public void generateNewQuote(QuoteCallback callback) {
        if (isGenerating) {
            Log.w(TAG, "Quote generation already in progress");
            return;
        }

        isGenerating = true;

        Content prompt = new Content.Builder()
                .addText("Give me a witty quote about sustainability.")
                .build();
        Schema schema = Schema.str();

        ListenableFuture<GenerateContentResponse> response = new GeminiClient(schema).generateResult(prompt);
        Futures.addCallback(
                response,
                new FutureCallback<>() {
                    @Override
                    public void onSuccess(GenerateContentResponse result) {
                        isGenerating = false;

                        if (result == null || result.getText() == null) {
                            callback.onFailure("No quote generated");
                            return;
                        }
                        Log.d(TAG, "Quote generated: " + result.getText());
                        callback.onSuccess(result.getText());
                    }

                    @Override
                    public void onFailure(@NonNull Throwable t) {
                        isGenerating = false;

                        Log.e(TAG, "Error generating quote: " + t.getMessage());
                        callback.onFailure("Failed to generate quote");
                    }
                },
                geminiExecutor
        );
    }

    //CALLBACK INTERFACE
    public interface QuoteCallback {
        void onSuccess(String quote);
        void onFailure(String errorMessage);
    }
}
