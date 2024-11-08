package com.example.potpytown;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;

public class DogInfoActivity extends AppCompatActivity {
    private EditText editTextBirthDate;
    private AutoCompleteTextView editTextBreed;
    private String selectedGender = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dog_info);


        editTextBirthDate = findViewById(R.id.editTextBirthDate);
        editTextBreed = findViewById(R.id.edit_breed);
        RadioGroup genderGroup = findViewById(R.id.genderGroup);
        Button saveButton = findViewById(R.id.saveButton);



        // 레이아웃의 RadioGroup을 가져옵니다.
        RadioGroup genderGroup = findViewById(R.id.genderGroup);

        // 성별 선택 이벤트 처리
        genderGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioMale) {
                Toast.makeText(this, "남아가 선택되었습니다.", Toast.LENGTH_SHORT).show();
            } else if (checkedId == R.id.radioFemale) {
                Toast.makeText(this, "여아가 선택되었습니다.", Toast.LENGTH_SHORT).show();
            }
        });

        //견종 드롭다운
        // AutoCompleteTextView 인스턴스
        AutoCompleteTextView breedTextView = findViewById(R.id.edit_breed);

        // ArrayAdapter 설정
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.breed_list, // string-array 이름
                android.R.layout.simple_dropdown_item_1line // 드롭다운 항목 레이아웃
        );

        // Adapter 연결
        breedTextView.setAdapter(adapter);

        // 드롭다운 목록을 항상 표시
        breedTextView.setOnClickListener(v -> breedTextView.showDropDown());
    }


    private void showAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("입력 요청")
                .setMessage("모든 정보를 입력해주세요.")
                .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss(); // 팝업 닫기
                    }
                })
                .show();
    }
}
