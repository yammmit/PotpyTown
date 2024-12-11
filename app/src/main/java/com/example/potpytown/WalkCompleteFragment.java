package com.example.potpytown;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class WalkCompleteFragment extends Fragment {

    private TextView walkTime, walkDistance, walkCheckpoints, walkCompletedMissions;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_walkcomplete, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // View 연결
        walkTime = view.findViewById(R.id.walk_time);
        walkDistance = view.findViewById(R.id.walk_distance);
        walkCheckpoints = view.findViewById(R.id.walk_checkpoints);
        walkCompletedMissions = view.findViewById(R.id.walk_completed_missions);
        ImageView btnClose = view.findViewById(R.id.btn_close);
        ImageView btnWriteDiary = view.findViewById(R.id.btn_write_walk_diary);

        // Firebase 초기화
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // 데이터 로드
        loadWalkData();

        // 닫기 버튼
        btnClose.setOnClickListener(v -> {
            Fragment gameFragment = requireActivity().getSupportFragmentManager().findFragmentByTag("GAME");
            if (gameFragment != null) {
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, gameFragment)
                        .commit();
            }
        });

        // 산책일기 쓰기 버튼
        btnWriteDiary.setOnClickListener(v -> {
            // 산책일기 작성 화면으로 이동
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.fragment_container, new RecordFragment())
                    .addToBackStack(null)
                    .commit();
        });
    }

    private void loadWalkData() {
        String userId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : "unknown";

        // Firestore에서 가장 최근 산책 기록 가져오기
        db.collection("walkRecord")
                .whereEqualTo("user_id", userId)
                .orderBy("date", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot document = queryDocumentSnapshots.getDocuments().get(0);

                        // Firestore 데이터 읽기
                        long time = document.getLong("time");
                        double distance = document.getDouble("distance");
                        long checkpoints = document.getLong("checkpoints"); // Firestore 필드명 확인 필요
                        long completedMissions = document.getLong("completed_missions"); // Firestore 필드명 확인 필요

                        // 데이터 표시
                        walkTime.setText(formatTime(time));
                        walkDistance.setText(String.format("%.2f km", distance / 1000.0));
                        walkCheckpoints.setText(String.valueOf(checkpoints));
                        walkCompletedMissions.setText(String.valueOf(completedMissions));
                    } else {
                        Log.d("WalkCompleteFragment", "No walk records found.");
                        walkTime.setText("-");
                        walkDistance.setText("-");
                        walkCheckpoints.setText("-");
                        walkCompletedMissions.setText("-");
                    }
                })
                .addOnFailureListener(e -> Log.e("WalkCompleteFragment", "Error fetching walk data", e));
    }

    private String formatTime(long minutes) {
        int hours = (int) (minutes / 60);
        int mins = (int) (minutes % 60);
        return String.format("%02d:%02d:00", hours, mins);
    }
}
