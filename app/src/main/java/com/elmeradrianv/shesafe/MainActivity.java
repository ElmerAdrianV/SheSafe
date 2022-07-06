package com.elmeradrianv.shesafe;

import android.os.Bundle;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.elmeradrianv.shesafe.fragments.MapFragment;
import com.elmeradrianv.shesafe.fragments.ProfileUserFragment;
import com.elmeradrianv.shesafe.fragments.TableViewFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupBottomNavigationView();
        ((ImageButton)findViewById(R.id.btnSos)).setOnClickListener(v->setupSOSButton());
    }

    private void setupSOSButton() {

    }

    private void setupBottomNavigationView() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        // handle navigation selection
        bottomNavigationView.setOnItemSelectedListener(
                item -> {
                    Fragment fragment = null;
                    switch (item.getItemId()) {
                        case R.id.action_map:
                            fragment = new MapFragment();
                            break;
                        case R.id.action_profile:
                            fragment = new ProfileUserFragment();
                            break;
                        case R.id.action_table:
                            fragment = new TableViewFragment();
                        default:
                            break;
                    }
                    getSupportFragmentManager().beginTransaction().replace(R.id.flContainer, fragment).commit();
                    return true;
                });
        // Set default selection
        bottomNavigationView.setSelectedItemId(R.id.action_map);
    }
}