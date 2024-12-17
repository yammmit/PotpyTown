package com.example.potpytown;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class CourseEditFragment extends Fragment {

    private View darkOverlay;
    private View courseNameLayout;
    private EditText editCourseName;
    private ImageView btnSaveCourse;

    private String startLocation;
    private String endLocation;

    public CourseEditFragment(String startLocation, String endLocation) {
        this.startLocation = startLocation;
        this.endLocation = endLocation;
    }

    public CourseEditFragment() {
        // 기본 생성자 필요
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_courseedit, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 뷰 초기화
        darkOverlay = view.findViewById(R.id.dark_overlay);
        courseNameLayout = view.findViewById(R.id.course_name_layout);
        editCourseName = view.findViewById(R.id.edit_course_name);
        btnSaveCourse = view.findViewById(R.id.btn_save_course);

        // 초기 상태 숨김
        darkOverlay.setVisibility(View.GONE);
        courseNameLayout.setVisibility(View.GONE);

        // "다음으로" 버튼 클릭 시 조건 확인
        checkAndShowCourseNameLayout();

        // 완료 버튼 클릭 이벤트
        btnSaveCourse.setOnClickListener(v -> {
            String courseName = editCourseName.getText().toString();

            if (courseName.isEmpty()) {
                Toast.makeText(requireContext(), "코스 이름을 입력해주세요.", Toast.LENGTH_SHORT).show();
            } else {
                saveCourseToFirestore(courseName);
            }
        });
    }

    // 출발지 및 도착지 설정 여부 확인
    private void checkAndShowCourseNameLayout() {
        if (startLocation == null || startLocation.isEmpty()) {
            Toast.makeText(requireContext(), "출발지를 설정해주세요.", Toast.LENGTH_SHORT).show();
        } else if (endLocation == null || endLocation.isEmpty()) {
            Toast.makeText(requireContext(), "도착지를 설정해주세요.", Toast.LENGTH_SHORT).show();
        } else {
            darkOverlay.setVisibility(View.VISIBLE);
            courseNameLayout.setVisibility(View.VISIBLE);
        }
    }

    // Firestore에 저장
    private void saveCourseToFirestore(String courseName) {
        Toast.makeText(requireContext(), "코스가 저장되었습니다: " + courseName, Toast.LENGTH_SHORT).show();
        darkOverlay.setVisibility(View.GONE);
        courseNameLayout.setVisibility(View.GONE);
    }
}
