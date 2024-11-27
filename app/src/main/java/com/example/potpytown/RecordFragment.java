package com.example.potpytown;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.tabs.TabLayout;

public class RecordFragment extends Fragment {

    private RecordTab recordTab;
    private DiaryTab diaryTab;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Fragment 레이아웃을 inflate
        return inflater.inflate(R.layout.fragment_record, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TabLayout tabs = view.findViewById(R.id.tabs);

        // RecordTab 및 DiaryTab 인스턴스 생성
        recordTab = new RecordTab();
        diaryTab = new DiaryTab();

        // 기본 탭을 RecordTab으로 설정
        showSelectedTab(recordTab);

        // TabLayout에 탭 추가
        tabs.addTab(tabs.newTab().setText("산책기록"));
        tabs.addTab(tabs.newTab().setText("산책일기"));

        // 탭 클릭 이벤트 설정
        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Fragment selected = null;
                int position = tab.getPosition();
                Log.d("RecordFragment", "선택된 탭: " + position);

                if (position == 0) {
                    selected = recordTab;
                } else if (position == 1) {
                    selected = diaryTab;
                }

                // 선택된 탭에 해당하는 프래그먼트 표시
                showSelectedTab(selected);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // 처리 필요 없음
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // 처리 필요 없음
            }
        });
    }

    private void showSelectedTab(Fragment fragment) {
        FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.record_container, fragment); // container는 프래그먼트를 표시할 영역의 ID
        transaction.commit();
    }
}