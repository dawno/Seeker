package com.trybe.trybe;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class LoginFragment3 extends Fragment {

    public LoginFragment3() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.frag_login3, container, false);
        String text1 = "Refer like minded people to your workplace and get some <font color='#eb881d'>Goodwill</font> and <font color='#eb881d'>Referral bonus</font> from your workplace";
        String text2 = "Get <font color='#eb881d'>Referred</font> to your favourite workplace by peers like you";
        TextView tvL1 = (TextView) rootView.findViewById(R.id.tvL1);
        TextView tvL2 = (TextView) rootView.findViewById(R.id.tvL2);
        tvL1.setText(Html.fromHtml(text1));
        tvL2.setText(Html.fromHtml(text2));

        return rootView;
    }

}