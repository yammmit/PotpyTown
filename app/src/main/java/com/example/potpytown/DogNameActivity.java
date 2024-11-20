package com.example.potpytown;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class DogNameActivity extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1; // 이미지 선택 요청 코드
    private FirebaseFirestore db;
    private ImageView profileImageView;
    private Uri profileImageUri; // 선택된 프로필 이미지 URI
    private EditText dogNameEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dog_name);

        // Firestore 초기화
        db = FirebaseFirestore.getInstance();

        // 뷰 초기화
        profileImageView = findViewById(R.id.profile_image);
        dogNameEditText = findViewById(R.id.input_dog_name);
        Button nextButton = findViewById(R.id.btn_next);
        TextView skipButton = findViewById(R.id.btn_skip);

        // 프로필 이미지 클릭 이벤트
        profileImageView.setOnClickListener(v -> selectImage());

        // 다음 버튼 클릭 이벤트
        nextButton.setOnClickListener(v -> {
            String dogName = dogNameEditText.getText().toString().trim();

            // 입력 확인
            if (profileImageUri == null) {
                showToast("프로필 사진을 등록해주세요.");
                return;
            }
            if (dogName.isEmpty()) {
                showToast("이름을 입력해주세요.");
                return;
            }

            // Firestore에 저장
            saveDogInfo(dogName, profileImageUri.toString());
        });

        // 건너뛰기 버튼 클릭 이벤트
        skipButton.setOnClickListener(v -> showSkipDialog());
    }

    private void selectImage() {
        // 갤러리에서 이미지 선택
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            profileImageUri = data.getData(); // 선택한 이미지 URI
            profileImageView.setImageURI(profileImageUri); // 이미지뷰에 설정
        }
    }

    private void saveDogInfo(String dogName, String profileImageUri) {
        // Firestore에 저장할 데이터
        Map<String, Object> dogInfo = new HashMap<>();
        dogInfo.put("name", dogName);
        dogInfo.put("profileImage", profileImageUri);

        // Firestore에 데이터 추가
        db.collection("dogs")
                .add(dogInfo)
                .addOnSuccessListener(documentReference -> {
                    showToast("정보가 저장되었습니다.");
                    navigateToNextPage();
                })
                .addOnFailureListener(e -> showToast("저장 실패: " + e.getMessage()));
    }

    private void navigateToNextPage() {
        Intent intent = new Intent(this, DogInfoActivity.class); // 다음 페이지로 이동
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
