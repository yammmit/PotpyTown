package com.example.potpytown;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class card_placeitem extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.card_placeitem, container, false);
        ImageView backgroundImage = view.findViewById(R.id.img_place);
        TextView placeName = view.findViewById(R.id.txt_placename);

        placeName.setText(getArguments().getString("name"));
//        backgroundImage.setImageResource(getArguments().getInt("img"));

        placeName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("text Click","text Click");
                // HomeFragment에서 placeDetail_view를 찾고, 해당 뷰를 보이게 설정

                ((MainActivity) getActivity()).homeFragment.placeDetail_view.setVisibility(View.VISIBLE);


            }
        });

        return view;
    }
}
