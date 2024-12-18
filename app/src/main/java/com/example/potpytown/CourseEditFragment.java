package com.example.potpytown;

import static com.example.potpytown.BuildConfig.PLACES_API_KEY;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.PlacesClient;

import java.util.ArrayList;
import java.util.List;

public class CourseEditFragment extends Fragment {

    private EditText originLocation, waypointInput, destiLocation;
    private LinearLayout waypointContainer;
    private ImageView backButton;
    private PlacesClient placesClient;

    private PopupWindow popupWindow;
    private ArrayAdapter<String> popupAdapter;
    private List<String> searchResults = new ArrayList<>();

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

        // Places API 초기화
        if (!Places.isInitialized()) {
            Places.initialize(requireContext(), PLACES_API_KEY);
        }
        placesClient = Places.createClient(requireContext());

        // View 초기화
        originLocation = view.findViewById(R.id.originLocation);
        waypointInput = view.findViewById(R.id.waypoint_input);
        destiLocation = view.findViewById(R.id.destiLocation);
        waypointContainer = view.findViewById(R.id.waypoint_container);
        backButton = view.findViewById(R.id.btn_back);

        // Bundle에서 현재 위치 데이터 가져오기
        Bundle args = getArguments();
        if (args != null) {
            String currentLocation = args.getString("current_location", "");
            originLocation.setText(currentLocation); // 출발점에 기본값 설정
        }

        // PopupWindow 초기화
        initPopupWindow();

        // EditText 설정
        setUpAutoComplete(originLocation);
        setUpAutoComplete(waypointInput);
        setUpAutoComplete(destiLocation);

        // 뒤로 가기 버튼 이벤트
        backButton.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());
    }

    // PopupWindow 초기화
    private void initPopupWindow() {
        ListView listView = new ListView(requireContext());
        listView.setBackgroundColor(getResources().getColor(android.R.color.white)); // 배경색을 흰색으로 설정
        listView.setDividerHeight(1); // 리스트 항목 사이 구분선 설정
        listView.setPadding(8, 8, 8, 8); // 패딩 설정

        popupAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, searchResults);
        listView.setAdapter(popupAdapter);

        popupWindow = new PopupWindow(listView, dpToPx(304), ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.setFocusable(false);
        popupWindow.setOutsideTouchable(true);

        // 리스트 항목 클릭 이벤트
        listView.setOnItemClickListener((parent, view, position, id) -> {
            String selectedPlace = searchResults.get(position);
            handleSelectedPlace(selectedPlace);
            popupWindow.dismiss();
        });
    }

    // dp를 px로 변환하는 메서드
    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }


    // EditText 설정
    private void setUpAutoComplete(EditText editText) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                fetchAutocompleteResults(s.toString(), editText);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    // Places API 검색 결과 가져오기
    private void fetchAutocompleteResults(String query, EditText editText) {
        if (query.isEmpty()) {
            popupWindow.dismiss();
            return;
        }

        FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder()
                .setQuery(query)
                .setCountries("KR") // 대한민국으로 제한
                .build();

        placesClient.findAutocompletePredictions(request)
                .addOnSuccessListener(response -> {
                    searchResults.clear();
                    for (AutocompletePrediction prediction : response.getAutocompletePredictions()) {
                        searchResults.add(prediction.getFullText(null).toString());
                    }
                    popupAdapter.notifyDataSetChanged();

                    if (!searchResults.isEmpty()) {
                        showPopupBelowEditText(editText);
                    } else {
                        popupWindow.dismiss();
                    }
                })
                .addOnFailureListener(exception -> Log.e("CourseEditFragment", "검색 실패: " + exception.getMessage()));
    }

    // PopupWindow를 EditText 바로 아래에 표시
    private void showPopupBelowEditText(EditText editText) {
        popupWindow.showAsDropDown(editText, 0, 0);
    }

    // 선택된 장소 처리
    private void handleSelectedPlace(String selectedPlace) {
        if (originLocation.hasFocus()) {
            originLocation.setText(selectedPlace);
        } else if (waypointInput.hasFocus()) {
            waypointInput.setText(""); // 입력 필드 초기화
            addWaypointBox(selectedPlace);
        } else if (destiLocation.hasFocus()) {
            destiLocation.setText(selectedPlace);
        }
    }

    // 경유지 박스 추가
    private void addWaypointBox(String waypoint) {
        View waypointView = LayoutInflater.from(getContext()).inflate(R.layout.item_waypoint_box, waypointContainer, false);

        TextView waypointText = waypointView.findViewById(R.id.waypoint_text);
        ImageView btnDeleteWaypoint = waypointView.findViewById(R.id.btn_delete_waypoint);

        waypointText.setText(waypoint);
        waypointText.setTextSize(14);
        waypointText.setEllipsize(TextUtils.TruncateAt.END);
        waypointText.setMaxLines(1);
        waypointText.setSingleLine(true);
        btnDeleteWaypoint.setOnClickListener(v -> waypointContainer.removeView(waypointView));

        waypointContainer.addView(waypointView);

        // 간격 조정
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        layoutParams.topMargin = 18;
        waypointView.setLayoutParams(layoutParams);

    }
}