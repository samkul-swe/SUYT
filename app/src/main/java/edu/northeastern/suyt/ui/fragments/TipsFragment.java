package edu.northeastern.suyt.ui.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.List;

import edu.northeastern.suyt.R;
import edu.northeastern.suyt.controller.RecyclingTipController;
import edu.northeastern.suyt.model.RecyclingTip;
import edu.northeastern.suyt.ui.adapters.TipAdapter;

public class TipsFragment extends Fragment implements View.OnClickListener {

    private RecyclerView recyclerView;
    private TipAdapter adapter;
    private RecyclingTipController tipController;
    private Button btnReduce, btnReuse, btnRecycle, btnAll;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tips, container, false);

        tipController = new RecyclingTipController();

        // Initialize RecyclerView
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize buttons
        btnReduce = view.findViewById(R.id.btn_reduce);
        btnReuse = view.findViewById(R.id.btn_reuse);
        btnRecycle = view.findViewById(R.id.btn_recycle);
        btnAll = view.findViewById(R.id.btn_all);

        // Set click listeners
        btnReduce.setOnClickListener((View.OnClickListener) this);
        btnReuse.setOnClickListener((View.OnClickListener) this);
        btnRecycle.setOnClickListener((View.OnClickListener) this);
        btnAll.setOnClickListener((View.OnClickListener) this);

        // Load all tips initially
        loadTips("All");

        return view;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_reduce) {
            loadTips("Reduce");
        } else if (v.getId() == R.id.btn_reuse) {
            loadTips("Reuse");
        } else if (v.getId() == R.id.btn_recycle) {
            loadTips("Recycle");
        } else if (v.getId() == R.id.btn_all) {
            loadTips("All");
        }
    }

    private void loadTips(String category) {
        List<RecyclingTip> tips;

        if (category.equals("All")) {
            tips = tipController.getAllTips();
        } else {
            tips = tipController.getTipsByCategory(category);
        }

        adapter = new TipAdapter(tips);
        recyclerView.setAdapter(adapter);
    }
}