package com.example.potpytown;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.google.android.material.tabs.TabLayout;

public class RecordActivity extends BaseActivity {

    Toolbar toolbar;

    RecordTab recordTab;
    DiaryTab diaryTab;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);
        setupBottomNavigationView();

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);

        recordTab = new RecordTab();
        diaryTab = new DiaryTab();


        getSupportFragmentManager().beginTransaction().replace(R.id.container, recordTab).commit();

        TabLayout tabs = findViewById(R.id.tabs);
        tabs.addTab(tabs.newTab().setText("산책기록"));
        tabs.addTab(tabs.newTab().setText("산책일기"));
        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                Log.d("RecordActivity", "선택된 탭 : " + position);

                Fragment selected = null;
                if (position == 0) {
                    selected = recordTab;
                } else if (position == 1) {
                    selected = diaryTab;
                }
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, selected).commit();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_profile;
    }

}
