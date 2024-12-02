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

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class GameFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_game, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Walk button click listener
        ImageView btnWalk = view.findViewById(R.id.btnWalk);
        btnWalk.setOnClickListener(v -> onWalkButtonClick());

        // Main home button click listener
        ImageView btnMainHome = view.findViewById(R.id.btnMainHome);
        btnMainHome.setOnClickListener(v -> onMainHomeButtonClick());

        // Mission button click listener
        ImageView btnMission = view.findViewById(R.id.btnMission);
        btnMission.setOnClickListener(v -> onMissionButtonClick());

        // My room button click listener
        ImageView btnMyRoom = view.findViewById(R.id.btnMyRoom);
        btnMyRoom.setOnClickListener(v -> onMyRoomButtonClick());
    }

    // Walk button click handler
    private void onWalkButtonClick() {
        // Implement the action for the walk button click
    }

    // Main home button click handler
    private void onMainHomeButtonClick() {
        if (getActivity() instanceof MainActivity) {
            MainActivity mainActivity = (MainActivity) getActivity();

            if (mainActivity.bottomNavigationView != null) {
                mainActivity.bottomNavigationView.setSelectedItemId(R.id.nav_home);
            }
        }
    }

    // Mission button click handler
    private void onMissionButtonClick() {
        // Implement the action for the mission button click
    }

    // My room button click handler
    private void onMyRoomButtonClick() {
        // Implement the action for the my room button click
    }
}
