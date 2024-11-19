package com.example.potpytown;

import android.os.Bundle;

public class ProfileActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
    }
    @Override
    protected int getLayoutResId() {
        return R.layout.activity_profile;
    }
}
