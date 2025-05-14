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

import java.util.List;

import edu.northeastern.suyt.R;
import edu.northeastern.suyt.controller.RecyclingTipController;
import edu.northeastern.suyt.model.RecyclingTip;
import edu.northeastern.suyt.ui.adapters.TipAdapter;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerView;
    private TipAdapter adapter;
    private RecyclingTipController tipController;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        tipController = new RecyclingTipController();

        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Load and display tips
        List<RecyclingTip> tips = tipController.getAllTips();
        adapter = new TipAdapter(tips);
        recyclerView.setAdapter(adapter);

        return view;
    }
}