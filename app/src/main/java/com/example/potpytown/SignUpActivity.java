package com.example.potpytown;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class SignUpActivity extends AppCompatActivity {

    private static final String TAG = "SignUpActivity";

    private EditText editTextID, editTextEmail, editTextNickname, editTextName, editTextPhoneNumber, editTextPassword, editTextPasswordConfirm, editTextVerificationCode;
    private Button buttonRegister, buttonCheckID, buttonSendVerification;
    private LinearLayout verificationCodeLayout;
    private TextView idError, emailError, passwordConfirmError, verificationError;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private boolean isPhoneVerified = false;
    private boolean isIDChecked = false;
    private String mVerificationId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Initialize Firebase Auth and Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize UI
        initializeUI();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Enable back button
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // Set up listeners
        setupListeners();

        editTextEmail.addTextChangedListener(new TextWatcher() {
            private final Handler handler = new Handler();
            private Runnable emailCheckRunnable;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String email = s.toString().trim();

                // 기존 요청 취소
                if (emailCheckRunnable != null) {
                    handler.removeCallbacks(emailCheckRunnable);
                }

                // 500ms 이후 이메일 중복 확인
                emailCheckRunnable = () -> {
                    if (Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                        checkEmailAvailability(email); // 이메일 중복 확인
                    } else {
                        emailError.setText("올바른 이메일 형식이 아닙니다.");
                        emailError.setTextColor(getResources().getColor(R.color.red));
                        emailError.setVisibility(View.VISIBLE);
                    }
                };
                handler.postDelayed(emailCheckRunnable, 500);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void initializeUI() {
        editTextID = findViewById(R.id.editTextID);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextNickname = findViewById(R.id.editTextNickname);
        editTextName = findViewById(R.id.editTextName);
        editTextPhoneNumber = findViewById(R.id.editTextPhoneNumber);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextPasswordConfirm = findViewById(R.id.editTextPasswordConfirm);
        editTextVerificationCode = findViewById(R.id.editTextVerificationCode);
        buttonRegister = findViewById(R.id.buttonRegister);
        buttonCheckID = findViewById(R.id.buttonCheckID);
        buttonSendVerification = findViewById(R.id.buttonSendVerification);
        verificationCodeLayout = findViewById(R.id.verificationCodeLayout);
        idError = findViewById(R.id.idError);
        emailError = findViewById(R.id.emailError);
        passwordConfirmError = findViewById(R.id.passwordConfirmError);
        verificationError = findViewById(R.id.verificationError);

        verificationCodeLayout.setVisibility(View.GONE);
    }

    private void checkEmailAvailability(String email) {
        db.collection("users")
                .whereEqualTo("email", email) // 이메일이 동일한 문서를 찾음
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot snapshot = task.getResult();
                        if (!snapshot.isEmpty()) {
                            emailError.setText("중복된 이메일입니다.");
                            emailError.setTextColor(getResources().getColor(R.color.red));
                            emailError.setVisibility(View.VISIBLE);
                        } else {
                            emailError.setText("사용 가능한 이메일입니다.");
                            emailError.setTextColor(getResources().getColor(R.color.text_dark));
                            emailError.setVisibility(View.VISIBLE);
                        }
                    } else {
                        Toast.makeText(SignUpActivity.this, "이메일 확인 실패: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setupListeners() {
        buttonSendVerification.setOnClickListener(v -> {
            String phoneNumber = editTextPhoneNumber.getText().toString().trim();

            if (phoneNumber.isEmpty() || phoneNumber.length() != 11) {
                Toast.makeText(this, "올바른 휴대폰 번호를 입력하세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            // 인증번호 발송 로직
            sendVerificationCode(phoneNumber);

            // 버튼 색상 및 텍스트 변경
            buttonSendVerification.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.btn_gray)));
            buttonSendVerification.setTextColor(getResources().getColor(R.color.text_dark));
            buttonSendVerification.setEnabled(false); // 중복 클릭 방지
        });

        editTextVerificationCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 6) {
                    verifyCode(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        buttonCheckID.setOnClickListener(v -> checkIDAvailability());
        buttonRegister.setOnClickListener(v -> registerUser());
    }

    private boolean validatePhoneNumber(String phoneNumber) {
        if (phoneNumber.length() != 11 || !phoneNumber.matches("\\d+")) {
            verificationError.setText("올바르지 않은 형식의 휴대폰 번호입니다.");
            verificationError.setVisibility(View.VISIBLE);
            return false;
        }
        return true;
    }

    private void sendVerificationCode(String phoneNumber) {
        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(mAuth)
                .setPhoneNumber("+82" + phoneNumber.substring(1))
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                        signInWithPhoneAuthCredential(credential);
                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {
                        Toast.makeText(SignUpActivity.this, "인증 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();

                        // 인증번호 입력칸 유지
                        verificationCodeLayout.setVisibility(View.VISIBLE);

                        // 버튼 상태 복구
                        buttonSendVerification.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.green)));
                        buttonSendVerification.setTextColor(getResources().getColor(R.color.text_ivory));
                        buttonSendVerification.setEnabled(true);
                    }

                    @Override
                    public void onCodeSent(@NonNull String verificationId,
                                           @NonNull PhoneAuthProvider.ForceResendingToken token) {
                        mVerificationId = verificationId;
                        Toast.makeText(SignUpActivity.this, "인증번호가 발송되었습니다.", Toast.LENGTH_SHORT).show();

                        // 인증번호 입력칸 및 재발송 버튼 표시
                        verificationCodeLayout.setVisibility(View.VISIBLE);

                        // 버튼 상태 변경
                        buttonSendVerification.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.btn_gray)));
                        buttonSendVerification.setTextColor(getResources().getColor(R.color.text_dark));
                        buttonSendVerification.setEnabled(false);
                    }
                }).build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void verifyCode(String code) {
        if (mVerificationId != null) {
            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, code);
            signInWithPhoneAuthCredential(credential);
        } else {
            verificationError.setText("인증번호를 다시 요청해주세요.");
            verificationError.setVisibility(View.VISIBLE);
        }
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                isPhoneVerified = true;
                verificationError.setVisibility(View.GONE);

                // 인증 완료된 상태로 버튼 변경
                buttonSendVerification.setText("인증 완료");
                buttonSendVerification.setEnabled(false); // 중복 클릭 방지
                buttonSendVerification.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.btn_gray)));
                buttonSendVerification.setTextColor(getResources().getColor(R.color.text_dark));

                Toast.makeText(this, "인증이 완료되었습니다.", Toast.LENGTH_SHORT).show();
            } else {
                verificationError.setText("인증번호를 정확히 입력해주세요.");
                verificationError.setVisibility(View.VISIBLE);
            }
        });
    }

    private void checkIDAvailability() {
        String enteredID = editTextID.getText().toString().trim();
        if (enteredID.isEmpty() || !Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d).{6,}$").matcher(enteredID).matches()) {
            idError.setText("영문, 숫자를 포함한 6글자 이상이어야 합니다.");
            idError.setVisibility(View.VISIBLE);
            return;
        }
        idError.setVisibility(View.GONE);

        db.collection("users")
                .whereEqualTo("id", enteredID)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot snapshot = task.getResult();
                        if (!snapshot.isEmpty()) {
                            idError.setText("중복된 아이디입니다.");
                            idError.setTextColor(getResources().getColor(R.color.red));
                            idError.setVisibility(View.VISIBLE);
                            isIDChecked = false;
                        } else {
                            idError.setText("사용 가능한 아이디입니다.");
                            idError.setTextColor(getResources().getColor(R.color.text_dark));
                            idError.setVisibility(View.VISIBLE);
                            isIDChecked = true;
                        }
                    } else {
                        Toast.makeText(SignUpActivity.this, "데이터베이스 요청 실패: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void registerUser() {
        if (!isIDChecked || !isPhoneVerified) {
            Toast.makeText(this, "아이디와 휴대폰 인증을 완료해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailError.setText("올바른 이메일 형식이 아닙니다.");
            emailError.setVisibility(View.VISIBLE);
            return;
        }

        if (password.length() < 6 || !Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@#$%^&+=!]).{6,}$").matcher(password).matches()) {
            Toast.makeText(this, "비밀번호는 영문, 숫자, 특수문자를 포함하여 6글자 이상이어야 합니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            saveAdditionalUserInfo(user.getUid());
        } else {
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser newUser = mAuth.getCurrentUser();
                            if (newUser != null) {
                                saveAdditionalUserInfo(newUser.getUid());
                            }
                        } else {
                            Toast.makeText(this, "회원가입에 실패했습니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void saveAdditionalUserInfo(String userId) {
        String id = editTextID.getText().toString();
        String nickname = editTextNickname.getText().toString();
        String phoneNumber = editTextPhoneNumber.getText().toString();
        String name = editTextName.getText().toString();
        String email = editTextEmail.getText().toString();

        if (id.isEmpty() || nickname.isEmpty() || phoneNumber.isEmpty() || name.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "필수 정보를 모두 입력하세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> user = new HashMap<>();
        user.put("email", email);
        user.put("id", id);
        user.put("name", name);
        user.put("nickname", nickname);
        user.put("phoneNumber", phoneNumber);
        user.put("rewards", 0);

        db.collection("users")
                .document(userId)
                .set(user)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "회원가입이 완료되었습니다.", Toast.LENGTH_SHORT).show();
                    navigateToDogName();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "회원 정보를 저장하는 데 실패했습니다: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void navigateToDogName() {
        Intent intent = new Intent(SignUpActivity.this, DogNameActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}