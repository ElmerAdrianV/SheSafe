package com.elmeradrianv.shesafe;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.elmeradrianv.shesafe.fragments.MapFragment;
import com.elmeradrianv.shesafe.fragments.TableViewFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        final FragmentManager fragmentManager = getSupportFragmentManager();
        // define your fragments here
        final Fragment mapFragment = new MapFragment();
        final Fragment tableFragment = new TableViewFragment();
//        final Fragment fragment3 = new ThirdFragment();

        setupBottomNavigationView(bottomNavigationView, fragmentManager, mapFragment,tableFragment);

    }

    private void setupBottomNavigationView(BottomNavigationView bottomNavigationView, FragmentManager fragmentManager, Fragment mapFragment, Fragment tableFragment) {
        // handle navigation selection
        bottomNavigationView.setOnItemSelectedListener(
                new BottomNavigationView.OnItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        Fragment fragment = null;
                        switch (item.getItemId()) {
                            case R.id.action_map:
                                fragment = mapFragment;
                                break;
                            case R.id.action_profile:
                                //fragment = fragment2;
                                break;
                            case R.id.action_table:
                                fragment = tableFragment;
                            default:
                                break;
                        }
                        fragmentManager.beginTransaction().replace(R.id.flContainer, fragment).commit();
                        return true;
                    }
                });
        // Set default selection
        bottomNavigationView.setSelectedItemId(R.id.action_map);
    }


}