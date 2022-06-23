package com.elmeradrianv.shesafe;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        final FragmentManager fragmentManager = getSupportFragmentManager();
//        // define your fragments here
//        final Fragment fragment1 = new FirstFragment();
//        final Fragment fragment2 = new SecondFragment();
//        final Fragment fragment3 = new ThirdFragment();

        setupBottomNavigationView(bottomNavigationView, fragmentManager);
        bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_profile:
                        // do something here
                        return true;
                    case R.id.action_map:
                        // do something here
                        return true;
                    case R.id.action_table:
                        // do something here
                        return true;
                    default:
                        return true;
                }
            }
        });
    }

    private void setupBottomNavigationView(BottomNavigationView bottomNavigationView, FragmentManager fragmentManager) {
        Fragment fragment;
        bottomNavigationView.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.action_profile:
                    // do something here
                    return true;
                case R.id.action_map:
                    // do something here
                    return true;
                case R.id.action_table:
                    // do something here
                    return true;
                default:
                    return true;
            }
//            fragmentManager.beginTransaction().replace(R.id.rlContainer, fragment).commit();
//            return true;
        });
        // Set default selection
        bottomNavigationView.setSelectedItemId(R.id.action_map);
    }


}