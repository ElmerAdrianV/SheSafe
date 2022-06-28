package com.elmeradrianv.shesafe.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.elmeradrianv.shesafe.R;
import com.elmeradrianv.shesafe.adapters.ReportCardAdapter;
import com.elmeradrianv.shesafe.database.Report;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;


public class TableViewFragment extends Fragment {
    public static final String TAG = TableViewFragment.class.getSimpleName();
    private ReportCardAdapter adapter;

    public TableViewFragment() {
        // Required empty public constructor
    }

    public static TableViewFragment newInstance(String param1, String param2) {
        TableViewFragment fragment = new TableViewFragment();
        Bundle args = new Bundle();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_table_view, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        adapter = new ReportCardAdapter(getContext());
        RecyclerView rvReportCard = (RecyclerView) view.findViewById(R.id.rvReportCards);
        rvReportCard.setHasFixedSize(true);
        rvReportCard.setLayoutManager(new LinearLayoutManager(getContext()));
        rvReportCard.setAdapter(adapter);
        adapter.showReports();
    }


}