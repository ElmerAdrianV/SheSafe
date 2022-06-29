package com.elmeradrianv.shesafe.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.elmeradrianv.shesafe.R;
import com.elmeradrianv.shesafe.adapters.ReportCardAdapter;
import com.elmeradrianv.shesafe.auxiliar.EndlessRecyclerViewScrollListener;


public class TableViewFragment extends Fragment {
    public static final String TAG = TableViewFragment.class.getSimpleName();
    public static final int NUMBER_REPORTS_REQUEST = 20;
    protected ReportCardAdapter adapter;
    // Store a member variable for the listener
    private EndlessRecyclerViewScrollListener scrollListener;
    private int currentOffset = NUMBER_REPORTS_REQUEST;//Count number of posts in the timeline

    public TableViewFragment() {
        // Required empty public constructor
    }

    public static TableViewFragment newInstance() {
        TableViewFragment fragment = new TableViewFragment();
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

        SwipeRefreshLayout swipeContainer = view.findViewById(R.id.swipeContainer);
        createSwipeRefresh(swipeContainer);

        RecyclerView rvReportCard = view.findViewById(R.id.rvReportCards);

        rvReportCard.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        rvReportCard.setLayoutManager(linearLayoutManager);
        adapter = new ReportCardAdapter(getContext());
        rvReportCard.setAdapter(adapter);
        adapter.showReports(currentOffset);
        rvReportCard.setItemAnimator(null);
        // Retain an instance so that you can call `resetState()` for fresh searches
        scrollListener = new EndlessRecyclerViewScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                // Triggered only when new data needs to be appended to the list
                // Add whatever code is needed to append new items to the bottom of the list
                loadNextDataFromApi(page);
            }
        };
        // Adds the scroll listener to RecyclerView
        rvReportCard.addOnScrollListener(scrollListener);
    }

    private void fetchFeedAsync() {
        adapter.clear();
        adapter.showReports(NUMBER_REPORTS_REQUEST);
        currentOffset = NUMBER_REPORTS_REQUEST;
    }

    // Append the next page of data into the adapter
    // This method probably sends out a network request and appends new data items to your adapter.
    public void loadNextDataFromApi(int offset) {
        adapter.showReports(currentOffset + NUMBER_REPORTS_REQUEST);
        int itemCountAdded = adapter.getItemCount() - currentOffset - 1;
        adapter.notifyItemRangeInserted(currentOffset, itemCountAdded);
        currentOffset += NUMBER_REPORTS_REQUEST;
    }

    private void createSwipeRefresh(SwipeRefreshLayout swipeContainer) {
        swipeContainer.setOnRefreshListener(() -> {
            // Your code to refresh the list here.
            // Make sure you call swipeContainer.setRefreshing(false)
            // once the network request has completed successfully.
            fetchFeedAsync();
            swipeContainer.setRefreshing(false);
        });

        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
    }
}