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

import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private EditText editTextID, editTextPassword;
    private Button buttonLogin;
    private CheckBox checkBoxRemember;
    private TextView textForgotPassword, textSignUp;

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Firebase Auth 초기화
        firebaseAuth = FirebaseAuth.getInstance();

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

        // 자동 로그인 체크박스
        /*checkBoxRemember.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                FirebaseUser currentUser = firebaseAuth.getCurrentUser();
                if (currentUser != null) {
                    navigateToHome();
                }

        });*/
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

        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d("LoginActivity", "로그인 성공");
                        Toast.makeText(LoginActivity.this, "로그인 성공", Toast.LENGTH_SHORT).show();
                        navigateToHome();
                    } else {
                        Log.e("LoginActivity", "로그인 실패", task.getException());
                        Toast.makeText(LoginActivity.this, "로그인 실패: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void navigateToHome() {
        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}