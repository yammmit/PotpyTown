package com.example.potpytown.manager;

import android.util.Log;

import com.example.potpytown.models.Mission;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MissionManager {
    private FirebaseFirestore db;

    public MissionManager() {
        db = FirebaseFirestore.getInstance();
    }

    public void assignInitialMissions(String userId, int count, Callback<Void> callback) {
        db.collection("user_mission")
                .whereEqualTo("user_id", userId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.isEmpty()) {
                        // user_mission 컬렉션이 비어 있다면 미션 할당
                        fetchAndAssignMissions(userId, count, callback);
                    } else {
                        Log.d("MissionManager", "User already has missions.");
                        callback.onSuccess(null);
                    }
                })
                .addOnFailureListener(callback::onFailure);
    }

    private void fetchAndAssignMissions(String userId, int count, Callback<Void> callback) {
        db.collection("mission")
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<Mission> allMissions = new ArrayList<>();
                    for (QueryDocumentSnapshot document : snapshot) {
                        Mission mission = new Mission();
                        mission.setMissionId(document.getString("mission_id"));
                        mission.setMission_type(document.getString("mission_type"));
                        mission.setDescription(document.getString("description"));
                        mission.setMission_value(document.getLong("mission_value").intValue());
                        mission.setReceive_reward(document.getLong("receive_reward").intValue());
                        allMissions.add(mission);
                    }

                    // 나머지 로직 동일
                    Collections.shuffle(allMissions);
                    List<Mission> selectedMissions = allMissions.subList(0, Math.min(count, allMissions.size()));

                    for (Mission mission : selectedMissions) {
                        Map<String, Object> userMission = new HashMap<>();
                        userMission.put("user_id", userId);
                        userMission.put("mission_id", mission.getMissionId());
                        userMission.put("description", mission.getDescription());
                        userMission.put("mission_type", mission.getMission_type());
                        userMission.put("mission_value", mission.getMission_value());
                        userMission.put("receive_reward", mission.getReceive_reward());
                        userMission.put("progress", 0);
                        userMission.put("status", "진행 전");

                        db.collection("user_mission").add(userMission)
                                .addOnSuccessListener(doc -> Log.d("MissionManager", "Mission assigned: " + mission.getMissionId()))
                                .addOnFailureListener(callback::onFailure);
                    }

                    callback.onSuccess(null);
                })
                .addOnFailureListener(callback::onFailure);
    }



    public interface Callback<T> {
        void onSuccess(T result);
        void onFailure(Exception e);
    }
}
