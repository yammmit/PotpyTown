package com.example.potpytown;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.window.OnBackInvokedDispatcher;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.GoogleMap;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class CourseWalkFragment extends Fragment {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private GoogleMap map;
    private EditText searchBox;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_walk, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Firebase 초기화
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();


        ImageView btnBack = view.findViewById(R.id.btn_back);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                // 프래그먼트 종료
                requireActivity().getSupportFragmentManager().popBackStack();
            });
        }

        ImageView btnFavorite = view.findViewById(R.id.btn_favorite);
        btnFavorite.setOnClickListener(v -> {
            // 즐겨찾기 목록
        });

        // Android 13 이상에서 뒤로 가기 처리
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requireActivity().getOnBackInvokedDispatcher().registerOnBackInvokedCallback(
                    OnBackInvokedDispatcher.PRIORITY_DEFAULT,
                    () -> requireActivity().getSupportFragmentManager().popBackStack()
            );
        }

        // 현재 로그인한 유저 확인
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            Log.d("WalkFragment", "User ID: " + userId);
        } else {
            Log.d("WalkFragment", "User ID not available");
        }

        // 검색창 초기화
        searchBox = view.findViewById(R.id.search_box);

        // 검색창에서 엔터 키 이벤트 처리
        searchBox.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                String searchText = searchBox.getText().toString();
                if (!searchText.isEmpty()) {
                    searchLocation(searchText); // 검색 메서드 호출
                } else {
                    Log.d("WalkFragment", "Search text is empty");
                }
                return true;
            }
            return false;
        });
    }

    private void searchLocation(String searchText) {
        if (map == null) {
            Log.d("WalkFragment", "Map is not ready");
            return;
        }
    }
}
