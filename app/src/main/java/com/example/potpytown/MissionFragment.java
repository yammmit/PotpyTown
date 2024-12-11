package com.example.potpytown;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.window.OnBackInvokedDispatcher;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.List;
import java.util.ArrayList;
import com.example.potpytown.models.UserMission;



public class MissionFragment extends Fragment {

    private LinearLayout missionContainer;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mission, container, false);
        missionContainer = view.findViewById(R.id.mission_container);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

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

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            fetchMissions(userId);
        } else {
            Log.e("MissionFragment", "No logged-in user.");
        }
    }

    private void fetchMissions(String userId) {
        db.collection("user_mission")
                .whereEqualTo("user_id", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<UserMission> missions = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        UserMission mission = document.toObject(UserMission.class);
                        missions.add(mission);
                    }

                    if (!missions.isEmpty()) {
                        displayMissionCards(missions); // UI에 미션 표시
                    } else {
                        Log.d("MissionFragment", "No missions assigned to user.");
                    }
                })
                .addOnFailureListener(e -> Log.e("MissionFragment", "Error fetching missions: " + e.getMessage()));
    }

    private void displayMissionCards(List<UserMission> missions) {
        missionContainer.removeAllViews(); // 기존 뷰 제거

        for (UserMission mission : missions) {
            // 카드 레이아웃 동적으로 생성
            View card = LayoutInflater.from(getContext()).inflate(R.layout.card_missionitem, missionContainer, false);

            TextView missionContent = card.findViewById(R.id.mission_content);
            TextView rewardText = card.findViewById(R.id.reward_amount);
            TextView missionProgress = card.findViewById(R.id.mission_progress);
            TextView missionStatus = card.findViewById(R.id.mission_status);

            // 데이터 바인딩
            missionContent.setText(mission.getDescription());
            rewardText.setText("Reward: " + mission.getReceive_reward() + " points");
            missionProgress.setText("Progress: " + mission.getProgress() + "/" + mission.getMission_value());
            missionStatus.setText("Status: " + mission.getStatus());

            // 카드 클릭 이벤트 추가 (필요 시)
            card.setOnClickListener(v -> {
                Log.d("MissionFragment", "Clicked mission: " + mission.getDescription());
            });

            // 컨테이너에 추가
            missionContainer.addView(card);
        }
    }
}
