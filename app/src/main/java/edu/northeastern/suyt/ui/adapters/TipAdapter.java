package edu.northeastern.suyt.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import edu.northeastern.suyt.R;
import edu.northeastern.suyt.model.TrashTip;

public class TipAdapter extends RecyclerView.Adapter<TipAdapter.TipViewHolder>  {

    private final List<TrashTip> tips;

    public TipAdapter(List<TrashTip> tips) {
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
        TrashTip tip = tips.get(position);

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