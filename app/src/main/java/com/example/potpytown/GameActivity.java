package com.example.potpytown;

import android.os.Bundle;

public class GameActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        hideBottomNavigationView();

        // 네비게이션 바 숨기기
        hideBottomNavigationView();
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_game;
    }
}
