package com.example.potpytown;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.tabs.TabLayout;

public class RecordFragment extends Fragment {

    private Toolbar toolbar;
    private RecordTab recordTab;
    private DiaryTab diaryTab;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // fragment_record.xml 레이아웃을 inflate합니다.
        return inflater.inflate(R.layout.fragment_record, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 툴바 초기화
        toolbar = view.findViewById(R.id.toolbar);
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setSupportActionBar(toolbar);
        }

        // 탭 초기화
        recordTab = new RecordTab();
        diaryTab = new DiaryTab();

        // 기본 탭 설정
        replaceFragment(recordTab);

        // TabLayout 초기화
        TabLayout tabs = view.findViewById(R.id.tabs);
        tabs.addTab(tabs.newTab().setText("산책기록"));
        tabs.addTab(tabs.newTab().setText("산책일기"));

        // 탭 선택 리스너 설정
        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                Log.d("RecordFragment", "선택된 탭 : " + position);

                Fragment selected = null;
                if (position == 0) {
                    selected = recordTab;
                } else if (position == 1) {
                    selected = diaryTab;
                }

                replaceFragment(selected);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // 탭이 선택 해제되었을 때 처리할 코드 (필요 시 구현)
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // 이미 선택된 탭이 다시 선택되었을 때 처리할 코드 (필요 시 구현)
            }
        });
    }

    /**
     * 프래그먼트를 교체하는 메서드
     *
     * @param fragment 교체할 프래그먼트
     */
    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getChildFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.container, fragment);
        transaction.commit();
    }
}