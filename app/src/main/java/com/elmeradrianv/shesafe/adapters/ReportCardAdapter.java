package com.elmeradrianv.shesafe.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.elmeradrianv.shesafe.R;
import com.elmeradrianv.shesafe.database.Report;
import com.parse.ParseException;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

public class ReportCardAdapter extends RecyclerView.Adapter<ReportCardAdapter.ViewHolder> {
    public static final String TAG = "PostAdapter";
    List<Report> reports;
    Context context;

    public ReportCardAdapter(Context context) {
        this.reports = new ArrayList<>();
        this.context = context;
    }

    public void clear() {
        reports.clear();
        notifyDataSetChanged();
    }

    public void addAll(List<Report> list) {
        reports.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ReportCardAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.report_card_view, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReportCardAdapter.ViewHolder holder, int position) {
        //Get the data at position
        Report report = reports.get(position);
        try {
            holder.bind(report);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }


    @Override
    public int getItemCount() {
        return reports.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDescription;
        TextView tvTypeOfCrime;
        TextView tvDate;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvTypeOfCrime = itemView.findViewById(R.id.tvTypeOfCrime);
            tvDate = itemView.findViewById(R.id.tvDate);
        }

        public void bind(Report report) throws ParseException {
            tvDescription.setText(report.getDescription());
            tvDate.setText(report.getDate().toString());
            tvTypeOfCrime.setText(report.getTypeOfCrime().getTag());
        }
    }
    public void showReports() {
        List<Report> reports = new ArrayList<>();
        ParseQuery<Report> query = ParseQuery.getQuery(Report.class);
        query.include(Report.TYPE_OF_CRIME_KEY);
        query.setLimit(50);
        query.addDescendingOrder("createdAt");
        query.findInBackground((reportList, e) -> {
            if (e != null) {
                Log.e(TAG, "Issue with getting posts", e);
                return;
            }
            this.addAll(reportList);
        });
    }

}