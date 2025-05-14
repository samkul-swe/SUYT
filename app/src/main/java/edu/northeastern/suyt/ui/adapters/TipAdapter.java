package edu.northeastern.suyt.ui.adapters;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import edu.northeastern.suyt.R;
import edu.northeastern.suyt.model.RecyclingTip;

public class TipAdapter extends RecyclerView.Adapter<TipAdapter.TipViewHolder>  {

    private final List<RecyclingTip> tips;

    public TipAdapter(List<RecyclingTip> tips) {
        this.tips = tips;
    }

    @NonNull
    @Override
    public TipViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tip, parent, false);
        return new TipViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TipViewHolder holder, int position) {
        RecyclingTip tip = tips.get(position);

        holder.titleTextView.setText(tip.getTitle());
        holder.descriptionTextView.setText(tip.getDescription());

        // Set category color or icon based on category
        switch (tip.getCategory()) {
            case "Reduce":
                holder.categoryIndicator.setBackgroundResource(R.color.colorReduce);
                break;
            case "Reuse":
                holder.categoryIndicator.setBackgroundResource(R.color.colorReuse);
                break;
            case "Recycle":
                holder.categoryIndicator.setBackgroundResource(R.color.colorRecycle);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return tips.size();
    }

    static class TipViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        TextView descriptionTextView;
        View categoryIndicator;

        TipViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.title_text_view);
            descriptionTextView = itemView.findViewById(R.id.description_text_view);
            categoryIndicator = itemView.findViewById(R.id.category_indicator);
        }
    }
}