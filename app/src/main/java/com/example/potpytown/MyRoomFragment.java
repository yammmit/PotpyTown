package com.example.potpytown;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Build;
import android.window.OnBackInvokedDispatcher;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;


public class MyRoomFragment extends Fragment {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_myroom, container, false);
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

        setPotpyImg();
    }

    public void setPotpyImg() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Log.d("setPotpyImg", "유저가 로그인하지 않았습니다.");
            return;
        }

        String userId = currentUser.getUid();
        db.collection("user_potpy")
                .whereEqualTo("user_id", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.size() != 0) {
                        StringBuilder pets = new StringBuilder();
                        // 유저가 보유한 캐릭터 ID 목록 생성
                        List<Integer> ownedPotpyIds = new ArrayList<>();
                        for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                            String potpyId = document.getString("potpy_id");
                            if (potpyId != null) {
                                ownedPotpyIds.add(Integer.parseInt(potpyId));
                                pets.append(potpyId);
                                pets.append(", ");
                            }
                        }
                        Log.d("Pets", pets.toString());

                        // 캐릭터 이미지 업데이트
                        for (int i = 1; i <= 9; i++) {
                            int imgId = getResources().getIdentifier("img_potpy" + i, "id", requireContext().getPackageName());
                            ImageView potpyImage = getView().findViewById(imgId);

                            if (potpyImage != null) {
                                if (ownedPotpyIds.contains(i)) {
                                    // 보유한 캐릭터: 기본 상태 유지
                                    potpyImage.clearColorFilter();
                                } else {
                                    // 보유하지 않은 캐릭터: 어둡게 처리
                                    potpyImage.setColorFilter(0x99000000); // 반투명 검정색 필터
                                }
                            }
                        }
                    } else {
                        Log.d("setPotpyImg", "유저의 보유 캐릭터 데이터가 없습니다.");
                    }
                })
                .addOnFailureListener(e -> Log.e("setPotpyImg", "Firebase 조회 실패", e));
    }
}
