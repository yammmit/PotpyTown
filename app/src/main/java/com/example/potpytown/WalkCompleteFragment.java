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

public class WalkCompleteFragment extends Fragment {

    private TextView walkTime, walkDistance, walkCheckpoints, walkCompletedMissions;

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

        // 번들에서 데이터 가져오기
        Bundle bundle = getArguments();
        if (bundle != null) {
            String elapsedTime = bundle.getString("elapsedTime", "00:00:00");
            String distance = bundle.getString("distance", "0.0 km");

            // 데이터 표시
            walkTime.setText(elapsedTime);
            walkDistance.setText(distance);
        } else {
            Log.d("WalkCompleteFragment", "No bundle data found.");
            walkTime.setText("-");
            walkDistance.setText("-");
        }

        // 닫기 버튼 이벤트
        btnClose.setOnClickListener(v -> {
            Fragment gameFragment = requireActivity().getSupportFragmentManager().findFragmentByTag("GAME");
            if (gameFragment != null) {
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, gameFragment)
                        .commit();
            }
        });

        // 산책일기 쓰기 버튼 이벤트
        btnWriteDiary.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.fragment_container, new RecordFragment())
                    .addToBackStack(null)
                    .commit();
        });
    }
}