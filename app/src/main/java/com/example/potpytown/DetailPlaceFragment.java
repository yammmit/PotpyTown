package com.example.potpytown;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.libraries.places.api.model.Place;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class DetailPlaceFragment extends Fragment {

    private Place place;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String userId;
    private String placeId;
    private ImageButton favoriteButton;
    private boolean isFavorite = false;

    public static DetailPlaceFragment newInstance(Place place) {
        DetailPlaceFragment fragment = new DetailPlaceFragment();
        String userId = UserManager.getInstance().getUserId();
        Bundle args = new Bundle();
        args.putParcelable("place", place);
        fragment.setArguments(args);
        return fragment;
    }

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
            Log.e("DetailPlaceFragment", "User is not logged in.");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_detailplace, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 레이아웃 요소 초기화
        ImageView imgPlacePhoto = view.findViewById(R.id.img_place);
        TextView txtPlaceName = view.findViewById(R.id.txt_place_name);
        TextView txtPlaceType = view.findViewById(R.id.txt_place_type);
        TextView txtPlaceAddress = view.findViewById(R.id.txt_place_address);
        TextView txtPlacePhone = view.findViewById(R.id.txt_place_phone);
        TextView txtPlaceWebsite = view.findViewById(R.id.txt_place_website);
        ImageView favoriteButton = view.findViewById(R.id.favorite_button);
        ImageView btnMap = view.findViewById(R.id.btn_map);
        ImageView btnClose = view.findViewById(R.id.btn_close);

        // 번들로 전달된 장소 데이터 가져오기
        if (getArguments() != null) {
            place = getArguments().getParcelable("place");
            if (place != null) {
                placeId = place.getId();
                // 장소 이름
                txtPlaceName.setText(place.getDisplayName());

                // 장소 유형
                String placeType = place.getTypes() != null && !place.getTypes().isEmpty() ? place.getTypes().get(0).name() : "정보 없음";
                txtPlaceType.setText(placeType);

                // 장소 주소
                txtPlaceAddress.setText(TextUtils.isEmpty(place.getAddress()) ? "주소 정보 없음" : place.getAddress());

                // 전화번호
                txtPlacePhone.setText(TextUtils.isEmpty(place.getPhoneNumber()) ? "전화번호 정보 없음" : place.getPhoneNumber());

                // 홈페이지 주소
                txtPlaceWebsite.setText(place.getWebsiteUri() != null ? place.getWebsiteUri().toString() : "홈페이지 정보 없음");

                // 장소 사진 (기본 설정 또는 API 연결 필요)
                if (place.getPhotoMetadatas() != null && !place.getPhotoMetadatas().isEmpty()) {
                    // 사진 로드 예제 (Glide 사용 가능)
                    // Glide.with(this).load(photoUrl).into(imgPlacePhoto);
                    imgPlacePhoto.setBackgroundResource(R.drawable.img_place); // 대체용
                } else {
                    imgPlacePhoto.setBackgroundResource(R.drawable.img_place); // 기본 이미지
                }
            }
        }

        // 즐겨찾기 상태 확인 및 버튼 설정
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

        // 즐겨찾기 버튼 클릭 리스너 설정 (card_placeitem과 동일한 로직으로 설정)
        favoriteButton.setOnClickListener(v -> {
            if (isFavorite) {
                // 이미 즐겨찾기인 경우 Firestore에서 삭제
                db.collection("favorite_places")
                        .whereEqualTo("user_id", userId)
                        .whereEqualTo("place_id", placeId)
                        .get()
                        .addOnSuccessListener(queryDocumentSnapshots -> {
                            for (DocumentSnapshot document : queryDocumentSnapshots) {
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
                favoritePlace.put("place_name", txtPlaceName.getText().toString()); // 장소 이름

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

        btnClose.setOnClickListener(v -> {
            // 프래그먼트 종료
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        // 지도 버튼 클릭 이벤트
        btnMap.setOnClickListener(v -> {
            if (place.getLatLng() != null) {
                String geoUri = "geo:" + place.getLatLng().latitude + "," + place.getLatLng().longitude + "?q=" + place.getDisplayName();
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(geoUri));
                intent.setPackage("com.google.android.apps.maps");
                startActivity(intent);
            } else {
                Toast.makeText(getContext(), "지도 정보를 사용할 수 없습니다.", Toast.LENGTH_SHORT).show();
            }
        });

        // 홈페이지 클릭 이벤트
        txtPlaceWebsite.setOnClickListener(v -> {
            if (place.getWebsiteUri() != null) {
                Intent intent = new Intent(Intent.ACTION_VIEW, place.getWebsiteUri());
                startActivity(intent);
            } else {
                Toast.makeText(getContext(), "홈페이지 정보를 사용할 수 없습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
