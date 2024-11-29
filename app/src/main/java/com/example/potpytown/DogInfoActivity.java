package com.example.potpytown;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class DogInfoActivity extends AppCompatActivity {
    private EditText editTextBirthDate;
    private AutoCompleteTextView editTextBreed;
    private RadioGroup genderGroup;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dog_info);

        // 전달받은 문서 ID
        String documentId = getIntent().getStringExtra("documentId");

        // Firestore 초기화
        db = FirebaseFirestore.getInstance();

        // View 초기화
        editTextBirthDate = findViewById(R.id.edit_birth_date);
        editTextBreed = findViewById(R.id.edit_breed);
        genderGroup = findViewById(R.id.genderGroup);
        Button nextButton = findViewById(R.id.btn_next);
        TextView skipButton = findViewById(R.id.btn_skip);

        // 다음 버튼 클릭 이벤트
        nextButton.setOnClickListener(v -> {
            if (validateInput()) {
                String gender = ((RadioButton) findViewById(genderGroup.getCheckedRadioButtonId())).getText().toString();
                String birthDate = editTextBirthDate.getText().toString().trim();
                String breed = editTextBreed.getText().toString().trim();

                // Firestore 문서 업데이트 및 페이지 이동
                updateDogInfo(documentId, gender, birthDate, breed);
            }
        });

        // 건너뛰기 버튼 클릭 이벤트
        skipButton.setOnClickListener(v -> showSkipDialog());
    }

    private boolean validateInput() {
        // 성별 체크
        if (genderGroup.getCheckedRadioButtonId() == -1) {
            showToast("성별을 선택해주세요.");
            return false;
        }

        // 생년월일 및 견종 체크
        if (editTextBirthDate.getText().toString().trim().isEmpty() || editTextBreed.getText().toString().trim().isEmpty()) {
            showToast("모든 정보를 입력해주세요.");
            return false;
        }

        return true;
    }

    private void updateDogInfo(String documentId, String gender, String birthDate, String breed) {
        // Firestore에 업데이트할 데이터
        Map<String, Object> updatedFields = new HashMap<>();
        updatedFields.put("gender", gender);
        updatedFields.put("birthDate", birthDate);
        updatedFields.put("breed", breed);

        // Firestore 문서 업데이트
        db.collection("dogs")
                .document(documentId) // 전달받은 문서 ID를 사용하여 업데이트
                .update(updatedFields)
                .addOnSuccessListener(aVoid -> {
                    showToast("정보가 업데이트되었습니다.");
                    navigateToNextPage(documentId); // 다음 페이지로 이동
                })
                .addOnFailureListener(e -> showToast("데이터 업데이트 실패: " + e.getMessage()));
    }

    private void navigateToNextPage(String documentId) {
        Intent intent = new Intent(this, DogHealthActivity.class); // DogHealthActivity로 이동
        intent.putExtra("documentId", documentId); // 문서 ID 전달
        startActivity(intent);
    }

    private void showSkipDialog() {
        new AlertDialog.Builder(this)
                .setTitle("반려견 등록 없이 둘러보시겠습니까?")
                .setPositiveButton("둘러보기", (dialog, which) -> {
                    Intent intent = new Intent(this, HomeFragment.class); // 홈 화면으로 이동
                    startActivity(intent);
                })
                .setNegativeButton("이어서 등록하기", null)
                .show();
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}