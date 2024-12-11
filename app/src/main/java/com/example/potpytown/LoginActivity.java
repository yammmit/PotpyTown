package com.example.potpytown;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.potpytown.manager.MissionManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    private EditText editTextID, editTextPassword;
    private Button buttonLogin;
    private CheckBox checkBoxRemember;
    private TextView textForgotPassword, textSignUp;

    private FirebaseAuth mAuth;
    private MissionManager missionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        missionManager = new MissionManager(); // MissionManager 초기화

        // View 초기화
        editTextID = findViewById(R.id.editTextID);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        checkBoxRemember = findViewById(R.id.checkBoxRemember);
        textForgotPassword = findViewById(R.id.textForgotPassword);
        textSignUp = findViewById(R.id.textSignUp);

        // 로그인 버튼 클릭 이벤트
        buttonLogin.setOnClickListener(v -> loginUser());

        // 회원가입 클릭 이벤트
        textSignUp.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
            startActivity(intent);
        });

        // 비밀번호 찾기 클릭 이벤트
        textForgotPassword.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
            startActivity(intent);
        });
    }

    private void loginUser() {
        String email = editTextID.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            editTextID.setError("아이디를 입력하세요.");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            editTextPassword.setError("비밀번호를 입력하세요.");
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d("LoginActivity", "로그인 성공");
                        Toast.makeText(LoginActivity.this, "로그인 성공", Toast.LENGTH_SHORT).show();

                        // 로그인 성공 후 유저 미션 확인 및 할당
                        FirebaseUser currentUser = mAuth.getCurrentUser();
                        if (currentUser != null) {
                            String userId = currentUser.getUid();
                            missionManager.assignInitialMissions(userId, 6, new MissionManager.Callback<Void>() {
                                @Override
                                public void onSuccess(Void result) {
                                    Log.d("LoginActivity", "Initial missions assigned successfully.");
                                }

                                @Override
                                public void onFailure(Exception e) {
                                    Log.e("LoginActivity", "Error assigning initial missions.", e);
                                }
                            });
                        }

                        navigateToHome();
                    } else {
                        Log.e("LoginActivity", "로그인 실패", task.getException());
                        Toast.makeText(LoginActivity.this, "로그인 실패: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void navigateToHome() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
