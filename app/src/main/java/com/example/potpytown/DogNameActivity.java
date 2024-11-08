package com.example.potpytown;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class DogNameActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dog_name);

        Button nextButton = findViewById(R.id.nextButton1);
        TextView skipButton = findViewById(R.id.skipButton1);

        nextButton.setOnClickListener(v -> {
            // 모든 정보가 입력되지 않았을 경우 팝업을 표시하는 로직 추가 필요
            Intent intent = new Intent(DogNameActivity.this, DogInfoActivity.class);
            startActivity(intent);
        });

        skipButton.setOnClickListener(v -> showSkipDialog());
    }

    private void showSkipDialog() {
        new AlertDialog.Builder(this)
                .setTitle("반려견 등록 없이 둘러보시겠습니까?")
                .setPositiveButton("둘러보기", (dialog, which) -> {
                    // 메인 화면으로 이동
                })
                .setNegativeButton("계속 등록하기", null)
                .show();
    }
}