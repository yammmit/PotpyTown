package com.example.potpytown;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    public BottomNavigationView bottomNavigationView;
    public HomeFragment homeFragment;
    public GameFragment gameFragment;
    public RecordFragment recordFragment;
    public ProfileFragment profileFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // MainActivity에 하단 내비게이션 포함

        // BottomNavigationView 초기화
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            String email = currentUser.getEmail();

            db.collection("users")
                    .whereEqualTo("email", email)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            String userId = queryDocumentSnapshots.getDocuments().get(0).getId();
                            UserManager.getInstance().setUserId(userId);
                            Log.d("Firestore", "User ID 설정 완료: " + userId);
                        }
                    })
                    .addOnFailureListener(e -> Log.e("Firestore", "main activity 사용자 도큐먼트 가져오기 실패", e));
        }

        String userId = UserManager.getInstance().getUserId();
        if (userId != null) {
            Log.d("HomeFragment", "User ID: " + userId);
        }

        homeFragment = new HomeFragment();
        gameFragment = new GameFragment();
        recordFragment = new RecordFragment();
        profileFragment = new ProfileFragment();

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

        bottomNavigationView.setSelectedItemId(R.id.nav_home);

        // BottomNavigationView 리스너 설정
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;

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

                // 선택된 프래그먼트에 따라 내비게이션 바 보이기/숨기기 설정
                if (selectedFragment == gameFragment) {
                    bottomNavigationView.setVisibility(View.GONE); // GameFragment에서는 숨김
                    Log.d("navigation", "nav gone");
                } else {
                    bottomNavigationView.setVisibility(View.VISIBLE); // 다른 프래그먼트에서는 보임
                    Log.d("navigation", "nav visible");
                }
            }
            return true;
        });
    }
}