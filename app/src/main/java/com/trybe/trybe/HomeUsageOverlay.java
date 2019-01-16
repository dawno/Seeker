package com.trybe.trybe;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class HomeUsageOverlay extends Fragment {

    public HomeUsageOverlay() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.frag_home_usage_overlay, container, false);
        rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("test", "removing usage screen");
                ((HomeActivity) getActivity()).removeUsageScreen();
            }
        });
        return rootView;
    }
}