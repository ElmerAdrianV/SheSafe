package com.elmeradrianv.shesafe.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.elmeradrianv.shesafe.R;
import com.elmeradrianv.shesafe.database.EmergencyContacts;
import com.elmeradrianv.shesafe.fragments.TableViewFragment;
import com.parse.ParseException;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

public class EmergencyContactsCardAdapter extends RecyclerView.Adapter<EmergencyContactsCardAdapter.ViewHolder> {
    public static final String TAG = "PostAdapter";
    List<EmergencyContacts> contacts;
    Context context;

    public EmergencyContactsCardAdapter(Context context) {
        this.contacts = new ArrayList<>();
        this.context = context;
    }

    public void clear() {
        contacts.clear();
        notifyDataSetChanged();
    }

    public void addAll(List<EmergencyContacts> list) {
        contacts.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public EmergencyContactsCardAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.contact_card_view, parent, false);
        return new EmergencyContactsCardAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EmergencyContactsCardAdapter.ViewHolder holder, int position) {
        //Get the data at position
        EmergencyContacts report = contacts.get(position);
        try {
            holder.bind(report);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }


    @Override
    public int getItemCount() {
        return contacts.size();
    }

    public void fetchContacts(int currentLimit) {
        ParseQuery<EmergencyContacts> query = ParseQuery.getQuery(EmergencyContacts.class);
        query.include(EmergencyContacts.USER_KEY);
        query.setLimit(currentLimit);
        query.setSkip(currentLimit - TableViewFragment.NUMBER_REPORTS_REQUEST); // skip the first 10 results
        query.addDescendingOrder("createdAt");
        query.findInBackground((contactsList, e) -> {
            if (e != null) {
                Log.e(TAG, "Issue with getting posts", e);
                return;
            }
            this.addAll(contactsList);
        });
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNickname;
        TextView tvPhoneNumber;
        ImageButton btnDelete;
        ImageButton btnCall;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNickname = itemView.findViewById(R.id.tvNickname);
            tvPhoneNumber= itemView.findViewById(R.id.tvPhoneNumber);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            btnCall=itemView.findViewById(R.id.btnCall);
        }

        public void bind(EmergencyContacts contact) throws ParseException {
            tvNickname.setText(contact.getNickname());
            tvPhoneNumber.setText(contact.getNumber().toString());
        }
    }

}