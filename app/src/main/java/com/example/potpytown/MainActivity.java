package com.example.potpytown;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    public HomeFragment homeFragment;
    private GameFragment gameFragment;
    private RecordFragment recordFragment;
    private ProfileFragment profileFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // MainActivity에 하단 내비게이션 포함

        homeFragment = new HomeFragment();
        gameFragment = new GameFragment();
        recordFragment = new RecordFragment();
        profileFragment = new ProfileFragment();

        // BottomNavigationView 초기화
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        bottomNavigationView.setSelectedItemId(R.id.nav_home);

        // 기본 프래그먼트 로드 (예: HomeFragment)
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, homeFragment, "HOME")
                    .add(R.id.fragment_container, gameFragment, "GAME")
                    .add(R.id.fragment_container, recordFragment, "RECORD")
                    .add(R.id.fragment_container, profileFragment, "PROFILE")
                    .hide(gameFragment)
                    .hide(recordFragment)
                    .hide(profileFragment)
                    .commit();
        }

        // BottomNavigationView 리스너 설정
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;

            // 선택된 탭에 따라 프래그먼트 설정
            int itemId = item.getItemId();
            if (itemId == R.id.nav_game) {
                selectedFragment = gameFragment;
            } else if (itemId == R.id.nav_home) {
                selectedFragment = homeFragment;
            } else if (itemId == R.id.nav_record) {
                selectedFragment = recordFragment;
            } else if (itemId == R.id.nav_profile) {
                selectedFragment = profileFragment;
            }

            // 선택된 프래그먼트를 보이게 하고 다른 프래그먼트는 숨기기
            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .hide(homeFragment)
                        .hide(gameFragment)
                        .hide(recordFragment)
                        .hide(profileFragment)
                        .show(selectedFragment)
                        .commit();
            }
            return true;
        });
    }

    protected void hideBottomNavigationView() {
        if (bottomNavigationView != null) {
            bottomNavigationView.setVisibility(View.GONE);
        }
    }
}