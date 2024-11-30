package com.example.potpytown;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.HashMap;
import java.util.Map;

public class card_placeitem extends Fragment {

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String userId;
    private String placeId;
    private double latitude;
    private double longitude;
    private ImageButton favoriteButton;
    private boolean isFavorite = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        userId = UserManager.getInstance().getUserId();

        // 현재 인증된 사용자가 있다면 userId 가져오기
        if (mAuth.getCurrentUser() != null) {
            userId = mAuth.getCurrentUser().getUid();
        } else {
            // 사용자가 인증되지 않은 경우의 처리
            Log.e("card_placeitem", "User is not logged in.");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        // Fragment 레이아웃 인플레이트
        View view = inflater.inflate(R.layout.card_placeitem, container, false);

        // 레이아웃에서 필요한 뷰 초기화
        favoriteButton = view.findViewById(R.id.favorite_button);
        ImageView backgroundImage = view.findViewById(R.id.img_place);
        TextView placeName = view.findViewById(R.id.txt_placename);

        if (userId != null) {
            // Firestore 접근 시 userId를 사용
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("favorite_places")
                    .whereEqualTo("user_id", userId)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        // 성공적으로 즐겨찾기 데이터를 가져왔을 경우 처리
                    })
                    .addOnFailureListener(e -> {
                        Log.e("FirestoreError", "Error getting favorite places: ", e);
                    });
        }

        // 번들로 전달된 데이터 가져오기
        if (getArguments() != null) {
            placeId = getArguments().getString("placeId");
            placeName.setText(getArguments().getString("name"));
        }

        // 즐겨찾기 상태 초기화 (Firestore에서 즐겨찾기 여부 확인)
        if (db != null && userId != null) {
            db.collection("favorite_places")
                    .whereEqualTo("user_id", userId)
                    .whereEqualTo("place_id", placeId)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            favoriteButton.setImageResource(R.drawable.icon_favorite_active);
                            isFavorite = true;
                        } else {
                            favoriteButton.setImageResource(R.drawable.icon_favorite_inactive);
                            isFavorite = false;
                        }
                    });
        }

        // 즐겨찾기 버튼 클릭 리스너 설정
        favoriteButton.setOnClickListener(v -> {
            if (isFavorite) {
                // 이미 즐겨찾기인 경우 Firestore에서 삭제
                db.collection("favorite_places")
                        .whereEqualTo("user_id", userId)
                        .whereEqualTo("place_id", placeId)
                        .get()
                        .addOnSuccessListener(queryDocumentSnapshots -> {
                            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                                db.collection("favorite_places").document(document.getId()).delete()
                                        .addOnSuccessListener(aVoid -> {
                                            Log.d("Firestore", "즐겨찾기 삭제 완료");
                                            favoriteButton.setImageResource(R.drawable.icon_favorite_inactive); // 빈 별 이미지로 변경
                                            isFavorite = false; // 상태 업데이트
                                        })
                                        .addOnFailureListener(e -> Log.w("Firestore", "삭제 실패", e));
                            }
                        });
            } else {
                // 즐겨찾기가 아닌 경우 Firestore에 추가
                Map<String, Object> favoritePlace = new HashMap<>();
                favoritePlace.put("user_id", userId); // 현재 사용자 ID
                favoritePlace.put("place_id", placeId); // 구글맵 장소 ID
                favoritePlace.put("place_name", placeName.getText().toString()); // 장소 이름

                db.collection("favorite_places")
                        .add(favoritePlace)
                        .addOnSuccessListener(documentReference -> {
                            Log.d("Firestore", "즐겨찾기 추가 완료");
                            favoriteButton.setImageResource(R.drawable.icon_favorite_active); // 채워진 별 이미지로 변경
                            isFavorite = true; // 상태 업데이트
                        })
                        .addOnFailureListener(e -> Log.w("Firestore", "추가 실패", e));
            }
        });

        return view;
    }
}
