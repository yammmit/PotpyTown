package com.example.potpytown;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public abstract class BaseActivity extends AppCompatActivity {

    protected BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("BaseActivity", "BaseActivity onCreate called");

        // activity_base.xml을 공통 레이아웃으로 설정
        setContentView(R.layout.activity_base);

        // 각 액티비티의 콘텐츠를 main_content에 삽입
        View mainContent = findViewById(R.id.main_content);
        getLayoutInflater().inflate(getLayoutResId(), (android.view.ViewGroup) mainContent);

        // 하단 내비게이션 바 설정
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        if (bottomNavigationView != null) {
            Log.d("BaseActivity", "BottomNavigationView initialized");
            setupBottomNavigationView();
        } else {
            Log.e("BaseActivity", "BottomNavigationView is null");
        }
    }

    // 각 액티비티에서 콘텐츠 레이아웃 리소스를 제공
    protected abstract int getLayoutResId();

    protected void setupBottomNavigationView() {
        if (bottomNavigationView == null) {
            Log.e("BaseActivity", "BottomNavigationView is null");
            return;
        }

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            Log.d("BaseActivity", "Item selected: " + itemId);

            // 액티비티 전환 로직
            if (itemId == R.id.nav_game && !(this instanceof GameActivity)) {
                startActivity(new Intent(this, GameActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.nav_home && !(this instanceof HomeActivity)) {
                startActivity(new Intent(this, HomeActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.nav_record && !(this instanceof RecordActivity)) {
                startActivity(new Intent(this, RecordActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.nav_profile && !(this instanceof ProfileActivity)) {
                startActivity(new Intent(this, ProfileActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });
        Log.d("BaseActivity", "BottomNavigationView listener set.");
    }

    protected void hideBottomNavigationView() {
        if (bottomNavigationView != null) {
            bottomNavigationView.setVisibility(View.GONE);
        }
    }
}