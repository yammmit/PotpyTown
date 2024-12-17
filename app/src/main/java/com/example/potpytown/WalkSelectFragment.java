package com.example.potpytown;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class WalkSelectFragment extends Fragment {

    private boolean isFreeWalkSelected = false; // 선택 상태 여부

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_walk_select, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // ImageView 버튼 초기화
        ImageView freeWalkButton = view.findViewById(R.id.free_walk_button);
        ImageView courseWalkButton = view.findViewById(R.id.course_walk_button);
        Button completeButton = view.findViewById(R.id.complete_button);
        ImageView backButton = view.findViewById(R.id.btn_back);

        // 뒤로가기 버튼 클릭 이벤트
        backButton.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        // 초기 상태: 비활성화 이미지 설정
        freeWalkButton.setImageResource(R.drawable.btn_freewalk);
        courseWalkButton.setImageResource(R.drawable.btn_coursewalk);

        // 자유산책 버튼 클릭
        freeWalkButton.setOnClickListener(v -> {
            // 활성화 상태로 변경
            freeWalkButton.setImageResource(R.drawable.btn_freewalk_active);
            courseWalkButton.setImageResource(R.drawable.btn_coursewalk);
            isFreeWalkSelected = true; // 자유산책 선택
        });

        // 코스산책 버튼 클릭
        courseWalkButton.setOnClickListener(v -> {
            // 활성화 상태로 변경
            courseWalkButton.setImageResource(R.drawable.btn_coursewalk_active);
            freeWalkButton.setImageResource(R.drawable.btn_freewalk);
            isFreeWalkSelected = false; // 코스산책 선택
        });

        // 선택완료 버튼 클릭
        completeButton.setOnClickListener(v -> {
            if (isFreeWalkSelected) {
                navigateToFragment(new FreeWalkFragment()); // 자유산책 프래그먼트로 이동
            } else {
                navigateToFragment(new CourseWalkSearchFragment()); // 코스산책 프래그먼트로 이동
            }
        });
    }

    // 프래그먼트 전환 메서드
    private void navigateToFragment(Fragment fragment) {
        FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.fragment_container, fragment); // fragment_container는 부모 레이아웃의 ID
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
