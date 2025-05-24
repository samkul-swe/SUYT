package edu.northeastern.suyt.model;

import com.google.firebase.ai.type.Schema;

import java.util.List;
import java.util.Map;

public class TrashTip {
    private String title;
    private String description;
    private String category; // e.g., "Reduce", "Reuse", "Recycle"

    public TrashTip() {}

    public TrashTip(String title, String description, String category) {
        this.title = title;
        this.description = description;
        this.category = category;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Schema getSchema() {
        return Schema.obj(
            Map.of("title", Schema.str(),
                "description", Schema.str(),
                "category",
                    Schema.enumeration(
                        List.of("reduce", "reuse", "recycle")
                    )
            )
        );
    }
}
