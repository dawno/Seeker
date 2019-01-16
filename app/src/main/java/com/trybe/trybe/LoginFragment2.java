package com.trybe.trybe;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

public class LoginFragment2 extends Fragment {

    public LoginFragment2() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.frag_login2, container, false);

        String text = "Make your <font color='#eb881d'>TRYBE</font> at your workplace :)";
        TextView tv = (TextView) rootView.findViewById(R.id.lTv1);
        tv.setText(Html.fromHtml(text));

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        return rootView;
    }

}
