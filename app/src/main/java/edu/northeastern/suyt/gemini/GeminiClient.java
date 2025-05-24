package edu.northeastern.suyt.gemini;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.ai.FirebaseAI;
import com.google.firebase.ai.GenerativeModel;
import com.google.firebase.ai.java.GenerativeModelFutures;
import com.google.firebase.ai.type.Content;
import com.google.firebase.ai.type.GenerateContentResponse;
import com.google.firebase.ai.type.GenerationConfig;
import com.google.firebase.ai.type.Schema;

public class GeminiClient {

    private GenerativeModelFutures model;

    public GeminiClient(Schema schema) {
        model = getModel(schema);
    }

    public GenerativeModelFutures getModel(Schema schema) {
        GenerationConfig.Builder configBuilder = new GenerationConfig.Builder();
        configBuilder.responseMimeType = "application/json";
        configBuilder.responseSchema = schema;

        GenerationConfig generationConfig = configBuilder.build();

        GenerativeModel gm = FirebaseAI.getInstance().generativeModel(
                "gemini-2.0-flash-lite",
                generationConfig);
        return GenerativeModelFutures.from(gm);
    }

    public ListenableFuture<GenerateContentResponse> generateResult(Content prompt) {
        return this.model.generateContent(prompt);
    }
}
