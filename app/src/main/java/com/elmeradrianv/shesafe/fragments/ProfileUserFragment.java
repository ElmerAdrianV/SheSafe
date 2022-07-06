package com.elmeradrianv.shesafe.fragments;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.elmeradrianv.shesafe.LoginActivity;
import com.elmeradrianv.shesafe.R;
import com.elmeradrianv.shesafe.adapters.EmergencyContactsCardAdapter;
import com.elmeradrianv.shesafe.auxiliar.MyPair;
import com.elmeradrianv.shesafe.database.EmergencyContacts;
import com.elmeradrianv.shesafe.database.User;
import com.parse.ParseFile;
import com.parse.ParseUser;

import java.io.File;
import java.util.HashMap;


public class ProfileUserFragment extends Fragment {
    public final static int PICK_PHOTO_CODE = 1046;
    public static final String TAG = ProfileUserFragment.class.getSimpleName();
    protected EmergencyContactsCardAdapter adapter;
    private ParseUser currentUser = ParseUser.getCurrentUser();
    private ParseFile profilePhoto;
    private ImageView ivProfileView;

    public ProfileUserFragment() {
        // Required empty public constructor
    }

    public static ProfileUserFragment newInstance() {
        return new ProfileUserFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile_user, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupRecycleView(view);
        profilePhoto = ParseUser.getCurrentUser().getParseFile(User.PROFILE_PHOTO_KEY);
        ivProfileView = view.findViewById(R.id.ivProfilePhoto);
        putProfileImage();
        TextView tvUsername = view.findViewById(R.id.tvUsername);
        view.findViewById(R.id.btnChangeProfilePhoto).setOnClickListener(v -> onPickPhoto());
        HashMap<String, MyPair<EditText, Boolean>> editUserFields = setupHashMapEditView(view);
        fillUserFields(editUserFields);
        HashMap<ImageButton, String> buttonsFieldsUser = setupHashMapButtons(view);
        setupButtonsListeners(buttonsFieldsUser, editUserFields);
        view.findViewById(R.id.btnSaveChanges).setOnClickListener(v -> setupSaveChanges(editUserFields));
        view.findViewById(R.id.btnLogout).setOnClickListener(v -> setupLogout());
        view.findViewById(R.id.btnAddContact).setOnClickListener(v -> setupAddContact(view));
        view.findViewById(R.id.btnAdd).setOnClickListener(v -> setupAdd(view));
    }

    private void setupAdd(View view) {
        String nickname = ((EditText) view.findViewById(R.id.etNickname)).getText().toString();
        String number = ((EditText) view.findViewById(R.id.etPhoneNumber)).getText().toString();
        EmergencyContacts contact = new EmergencyContacts();
        if (nickname.isEmpty() && number.isEmpty()) {
            Toast.makeText(getContext(), "Please, fill all the fields", Toast.LENGTH_SHORT).show();
        } else {
            contact.put(EmergencyContacts.USER_KEY, currentUser);
            contact.put(EmergencyContacts.NICKNAME_KEY, nickname);
            contact.put(EmergencyContacts.NUMBER_KEY, new Long(number));
            contact.saveInBackground(e -> {
                if (e != null) {
                    Log.e(TAG, "setupAdd: Issue adding contact ", e);
                }
                Toast.makeText(getContext(), "Contact saved!!", Toast.LENGTH_SHORT).show();
                CardView cvNewContact = view.findViewById(R.id.cvNewContact);
                ImageButton btnAddContact = view.findViewById(R.id.btnAddContact);
                cvNewContact.setVisibility(View.GONE);
                btnAddContact.setRotation(0);
                btnAddContact.setColorFilter(ContextCompat.getColor(getContext(), R.color.blue_cute), android.graphics.PorterDuff.Mode.MULTIPLY);
                adapter.addFirst(contact);
                ((RecyclerView) view.findViewById(R.id.rvEmergencyContacts)).smoothScrollToPosition(0);
            });
        }
    }

    private void setupAddContact(View view) {
        CardView cvNewContact = view.findViewById(R.id.cvNewContact);
        ImageButton btnAddContact = view.findViewById(R.id.btnAddContact);
        if (cvNewContact.getVisibility() == View.VISIBLE) {
            cvNewContact.setVisibility(View.GONE);
            btnAddContact.setRotation(0);
            btnAddContact.setColorFilter(ContextCompat.getColor(getContext(), R.color.blue_cute), android.graphics.PorterDuff.Mode.MULTIPLY);
        } else {
            cvNewContact.setVisibility(View.VISIBLE);
            btnAddContact.setRotation(45);
            btnAddContact.setColorFilter(ContextCompat.getColor(getContext(), R.color.red_ss), android.graphics.PorterDuff.Mode.MULTIPLY);
        }
    }

    private void putProfileImage() {
        int radius = 100000;
        Glide.with(getContext()).load(profilePhoto.getUrl())
                .fitCenter() // scale image to fill the entire ImageView
                .transform(new RoundedCorners(radius))
                .into(ivProfileView);
    }

    private void setupSaveChanges(HashMap<String, MyPair<EditText, Boolean>> editUserFields) {
        MyPair<EditText, Boolean> pair;
        for (String key : editUserFields.keySet()) {
            pair = editUserFields.get(key);
            if (pair.second) {
                //if the field was edited...
                if (pair.first.getText().toString().isEmpty())
                    Toast.makeText(getContext(), "Please don't let fields empty", Toast.LENGTH_SHORT).show();
                else
                    //and the field is not empty, we save the changes
                    currentUser.put(key, pair.first.getText().toString());
            }
        }
        currentUser.saveInBackground(e -> {
            if (e != null)
                Log.e(TAG, "done: Error saving user changes", e);
            else {
                Toast.makeText(getContext(), "Changes saved", Toast.LENGTH_SHORT).show();
                resetFields(editUserFields);
            }
        });
    }

    private void resetFields(HashMap<String, MyPair<EditText, Boolean>> editUserFields) {
        MyPair<EditText, Boolean> pair;
        for (String key : editUserFields.keySet()) {
            pair = editUserFields.get(key);
            if (pair.second) {
                pair.second = false;
                pair.first.setEnabled(false);
            }
        }
    }

    private void setupRecycleView(View view) {
        RecyclerView rvReportCard = view.findViewById(R.id.rvEmergencyContacts);
        rvReportCard.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        rvReportCard.setLayoutManager(linearLayoutManager);
        adapter = new EmergencyContactsCardAdapter(getContext());
        rvReportCard.setAdapter(adapter);
        adapter.fetchContacts(5);
        rvReportCard.setItemAnimator(null);
    }

    private void setupLogout() {
        ParseUser.logOut();
        Intent intent = new Intent(getContext(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // this makes sure the Back button won't work
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // same as above
        startActivity(intent);
    }

    private HashMap<String, MyPair<EditText, Boolean>> setupHashMapEditView(View view) {
        HashMap<String, MyPair<EditText, Boolean>> editUserFields = new HashMap<>();

        editUserFields.put(User.FIRST_NAME_KEY, new MyPair<>(view.findViewById(R.id.etFirstName), false));
        editUserFields.put(User.LAST_NAME_KEY, new MyPair<>(view.findViewById(R.id.etLastName), false));
        editUserFields.put(User.EMAIL_KEY, new MyPair<>(view.findViewById(R.id.etEmail), false));
        editUserFields.put(User.PERSONAL_DESCRIPTION_KEY, new MyPair<>(view.findViewById(R.id.etPersonalDescription), false));
        editUserFields.put(User.PASSWORD_KEY, new MyPair<>(view.findViewById(R.id.etPassword), false));
        editUserFields.put(User.EMERGENCY_MESSAGE_KEY, new MyPair<>(view.findViewById(R.id.etEmergencyMessage), false));

        return editUserFields;
    }

    private HashMap<ImageButton, String> setupHashMapButtons(View view) {
        HashMap<ImageButton, String> buttonsFieldsUser = new HashMap<>();
        buttonsFieldsUser.put(view.findViewById(R.id.btnEditFirstName), User.FIRST_NAME_KEY);
        buttonsFieldsUser.put(view.findViewById(R.id.btnEditLastName), User.LAST_NAME_KEY);
        buttonsFieldsUser.put(view.findViewById(R.id.btnEditEmail), User.EMAIL_KEY);
        buttonsFieldsUser.put(view.findViewById(R.id.btnEditPersonalDescription), User.PERSONAL_DESCRIPTION_KEY);
        buttonsFieldsUser.put(view.findViewById(R.id.btnEditPassword), User.PASSWORD_KEY);
        buttonsFieldsUser.put(view.findViewById(R.id.btnEditEmergencyMessage), User.EMERGENCY_MESSAGE_KEY);
        return buttonsFieldsUser;
    }

    private void setupButtonsListeners(HashMap<ImageButton, String> buttonsFieldsUser, HashMap<String, MyPair<EditText, Boolean>> editUserFields) {
        for (ImageButton btn : buttonsFieldsUser.keySet()) {
            btn.setOnClickListener(v -> {
                String key = buttonsFieldsUser.get(btn);
                MyPair<EditText, Boolean> pair = editUserFields.get(key);
                pair.first.setEnabled(true);
                pair.second = true;
            });
        }
    }

    private void fillUserFields(HashMap<String, MyPair<EditText, Boolean>> editUserFields) {
        for (String key : editUserFields.keySet())
            editUserFields.get(key).first.setText(currentUser.getString(key));
    }

    // Trigger gallery selection for a photo
    public void onPickPhoto() {
        // Create intent for picking a photo from the gallery
        Intent intent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        // If you call startActivityForResult() using an intent that no app can handle, your app will crash.
        // So as long as the result is not null, it's safe to use the intent.
        if (intent.resolveActivity(getContext().getPackageManager()) != null) {
            // Bring up gallery to select a photo
            startActivityForResult(intent, PICK_PHOTO_CODE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((data != null) && requestCode == PICK_PHOTO_CODE) {
            Uri photoUri = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};
            Cursor cursor = getContext().getContentResolver().query(photoUri, filePathColumn, null, null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String mCurrentPhotoPath = cursor.getString(columnIndex);
            cursor.close();
            File image = new File(mCurrentPhotoPath);
            profilePhoto = new ParseFile(image);
            saveImage();
        }
    }

    private void saveImage() {
        currentUser.put(User.PROFILE_PHOTO_KEY, profilePhoto);
        currentUser.saveInBackground(e -> {
            if (e != null) {
                Log.e(TAG, "saveImage: error in saveImage", e);
                Toast.makeText(getContext(), "Unable to change the image", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Image updated", Toast.LENGTH_SHORT).show();
                putProfileImage();
            }
        });

    }
}