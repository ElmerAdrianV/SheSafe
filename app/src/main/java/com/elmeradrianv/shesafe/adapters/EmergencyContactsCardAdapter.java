package com.elmeradrianv.shesafe.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.elmeradrianv.shesafe.R;
import com.elmeradrianv.shesafe.database.EmergencyContacts;
import com.elmeradrianv.shesafe.database.User;
import com.elmeradrianv.shesafe.fragments.TableViewFragment;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

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
    public void addFirst(EmergencyContacts contact) {
        contacts.add(0,contact);
        notifyItemInserted(0);
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
        query.whereEqualTo("user", ParseUser.getCurrentUser());
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
            tvPhoneNumber = itemView.findViewById(R.id.tvPhoneNumber);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            btnCall = itemView.findViewById(R.id.btnCall);
        }

        public void bind(EmergencyContacts contact) throws ParseException {
            tvNickname.setText(contact.getNickname());
            tvPhoneNumber.setText(contact.getNumber().toString());
            btnDelete.setOnClickListener(v -> deleteEmergencyContact(contact));
            btnCall.setOnClickListener(v -> makeACall());
        }

        private void deleteEmergencyContact(EmergencyContacts contact) {
            contact.deleteInBackground(e -> {
                if (e != null)
                    Log.e(TAG, "deleteEmergencyContact: issue deleting emergency contact", e);
                else {
                    contacts.remove(getAdapterPosition());
                    Toast.makeText(context, tvNickname.getText().toString() + " deleted", Toast.LENGTH_SHORT).show();
                    notifyDataSetChanged();
                }
            });
        }

        private void makeACall() {
            context.startActivity(new Intent(Intent.ACTION_DIAL)
                                    .setData(Uri.parse("tel:" + tvPhoneNumber.getText())));
        }
    }

}