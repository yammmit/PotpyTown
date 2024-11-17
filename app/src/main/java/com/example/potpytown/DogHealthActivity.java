package com.example.potpytown;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DogHealthActivity extends AppCompatActivity {
    private List<String> selectedNotes = new ArrayList<>();
    private EditText editOtherNotes;
    private RadioGroup neuterGroup;
    private RadioButton selectedNeuterStatus;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dog_health);

        // Firestore 초기화
        db = FirebaseFirestore.getInstance();

        // View 초기화
        neuterGroup = findViewById(R.id.neuterGroup);

        Button btnHeart = findViewById(R.id.btn_heart);
        Button btnJoint = findViewById(R.id.btn_joint);
        Button btnFatness = findViewById(R.id.btn_fatness);
        Button btnKidneys = findViewById(R.id.btn_kidneys);
        Button btnEye = findViewById(R.id.btn_eye);
        Button btnLiver = findViewById(R.id.btn_liver);
        Button btnBladder = findViewById(R.id.btn_bladder);
        Button btnRespiratory = findViewById(R.id.btn_respiratory);
        Button btnNone = findViewById(R.id.btn_none);

        Button btnComplete = findViewById(R.id.btn_complete);
        TextView skipButton = findViewById(R.id.btn_skip);

        // 특이사항 버튼 클릭 이벤트
        btnHeart.setOnClickListener(v -> toggleSelection(btnHeart, "심장 질환"));
        btnJoint.setOnClickListener(v -> toggleSelection(btnJoint, "관절 질환"));
        btnFatness.setOnClickListener(v -> toggleSelection(btnFatness, "비만"));
        btnKidneys.setOnClickListener(v -> toggleSelection(btnKidneys, "신장 질환"));
        btnEye.setOnClickListener(v -> toggleSelection(btnEye, "안구 질환"));
        btnLiver.setOnClickListener(v -> toggleSelection(btnLiver, "간 질환"));
        btnBladder.setOnClickListener(v -> toggleSelection(btnBladder, "방광 질환"));
        btnRespiratory.setOnClickListener(v -> toggleSelection(btnRespiratory, "호흡기 질환"));
        btnNone.setOnClickListener(v -> toggleSelection(btnNone, "없음"));

        // 완료 버튼 클릭 이벤트
        btnComplete.setOnClickListener(v -> {
            // 중성화 여부 확인
            int selectedId = neuterGroup.getCheckedRadioButtonId();
            if (selectedId != -1) {
                selectedNeuterStatus = findViewById(selectedId);
            } else {
                Toast.makeText(this, "중성화 여부를 선택해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (selectedNotes.isEmpty()) {
                Toast.makeText(this, "특이사항을 최소 1개 선택해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Firestore에 데이터 저장
            saveDogHealthInfo(selectedNeuterStatus.getText().toString(), selectedNotes);
        });

        // 건너뛰기 버튼 클릭 이벤트
        skipButton.setOnClickListener(v -> showSkipDialog());
    }

    private void toggleSelection(Button button, String note) {
        if (selectedNotes.contains(note)) {
            selectedNotes.remove(note);
            button.setBackgroundResource(R.drawable.button_selector); // 선택 해제
        } else {
            selectedNotes.add(note);
            button.setBackgroundResource(R.drawable.button_selected); // 선택
        }
    }

    private void saveDogHealthInfo(String neuterStatus, List<String> notes) {
        // Firestore에 저장할 데이터
        Map<String, Object> dogHealthInfo = new HashMap<>();
        dogHealthInfo.put("neuterStatus", neuterStatus);
        dogHealthInfo.put("notes", notes);

        // Firestore에 데이터 추가
        db.collection("dogHealth")
                .add(dogHealthInfo)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "정보가 저장되었습니다.", Toast.LENGTH_SHORT).show();
                    navigateToNextPage();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "저장 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void navigateToNextPage() {
        Intent intent = new Intent(this, HomeActivity.class); // 다음 페이지 Activity로 변경
        startActivity(intent);
    }

    private void showSkipDialog() {
        new AlertDialog.Builder(this)
                .setTitle("반려견 등록 없이 둘러보시겠습니까?")
                .setPositiveButton("둘러보기", (dialog, which) -> {
                    Intent intent = new Intent(this, HomeActivity.class); // 홈 화면으로 이동
                    startActivity(intent);
                })
                .setNegativeButton("이어서 등록하기", null)
                .show();
    }
}
