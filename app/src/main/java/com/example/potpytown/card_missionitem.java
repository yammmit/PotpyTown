package com.example.potpytown;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class card_missionitem extends Fragment {
    private String missionId;
    private String userId;

    public card_missionitem(String missionId, String userId) {
        this.missionId = missionId;
        this.userId = userId;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.card_missionitem, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ProgressBar progressBar = view.findViewById(R.id.progress_bar);
        TextView progressText = view.findViewById(R.id.progress_text);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Firestore 데이터 가져오기
        db.collection("mission").document(missionId)
                .get()
                .addOnSuccessListener(missionSnapshot -> {
                    if (missionSnapshot.exists()) {
                        String description = missionSnapshot.getString("description");
                        long missionValue = missionSnapshot.getLong("mission_value");

                        TextView missionContent = view.findViewById(R.id.mission_content);
                        missionContent.setText(description);

                        db.collection("user_mission")
                                .whereEqualTo("user_id", userId)
                                .whereEqualTo("mission_id", missionId)
                                .get()
                                .addOnSuccessListener(userMissionSnapshot -> {
                                    if (!userMissionSnapshot.isEmpty()) {
                                        DocumentSnapshot userMission = userMissionSnapshot.getDocuments().get(0);
                                        long progress = userMission.getLong("progress");

                                        progressBar.setMax((int) missionValue);
                                        progressBar.setProgress((int) progress);
                                        progressText.setText(progress + "/" + missionValue);
                                    }
                                });
                    }
                });
    }
}