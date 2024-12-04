package com.example.potpytown;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.window.OnBackInvokedDispatcher;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class WalkFragment extends Fragment {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_walk, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Firebase 초기화
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        ImageView btnBack = view.findViewById(R.id.btn_back);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                // 프래그먼트 종료
                requireActivity().getSupportFragmentManager().popBackStack();
            });
        }

        // Android 13 이상에서 뒤로 가기 처리
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requireActivity().getOnBackInvokedDispatcher().registerOnBackInvokedCallback(
                    OnBackInvokedDispatcher.PRIORITY_DEFAULT,
                    () -> requireActivity().getSupportFragmentManager().popBackStack()
            );
        }

        // 현재 로그인한 유저 확인
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            Log.d("user_id", "user_id : " + userId);
        } else {
            Log.d("user_id", "유저 아이디 확인 불가");
        }
    }
}
