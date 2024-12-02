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
        // 초기 탭 선택 설정
        if (tabs.getTabCount() > 0) {
            tabs.getTabAt(0).select();
        }

        // 탭 선택 이벤트 리스너
        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Fragment selected = null;
                switch (tab.getPosition()) {
                    case 0:
                        selected = new RecordTab();
                        break;
                    case 1:
                        selected = new DiaryTab();
                        break;
                }
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

        // 초기 탭 설정
        if (tabs.getTabCount() > 0) {
            tabs.getTabAt(0).select();
            showSelectedTab(new RecordTab()); // 초기 프래그먼트 로드
        }

    }

    private void showSelectedTab(Fragment fragment) {
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.replace(R.id.record_container, fragment);
        transaction.commit();
    }

}