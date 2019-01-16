package com.trybe.trybe;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.trybe.trybe.app.Config;
import com.trybe.trybe.dto.LoginDTO;
import com.trybe.trybe.dto.UserDataWrapper;
import com.trybe.trybe.helper.UserSessionManager;
import com.trybe.trybe.helper.Utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;


public class ProfileSetupFragment1 extends Fragment {

    private LinearLayout schoolList, workList;
    private ImageButton nameEdit, schoolAdd, workAdd;
    private ImageView profilePic;
    private TextView userName;
    private UserSessionManager session;
    private LoginDTO user;

    public ProfileSetupFragment1() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        session = UserSessionManager.getInstance();
    }

    @Override
    public void onResume() {
        super.onResume();
        user = session.getUserProfile();
        Utils.loadImageByPicasso(getActivity(), profilePic, user.user_def.UD_IMG);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.frag_profile_setup1, container, false);

        user = session.getUserProfile();
        String userImageUrl = user.user_def.UD_IMG;
        userName = (TextView) rootView.findViewById(R.id.userName);
        userName.setText("Hi, " + user.user_def.UD_NAME);
        profilePic = (CircleImageView) rootView.findViewById(R.id.profilePic);
        Utils.loadImageByPicasso(getActivity(), profilePic, userImageUrl);

        profilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), ProfilePicActivity.class));
            }
        });

        nameEdit = (ImageButton) rootView.findViewById(R.id.editName);
        nameEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nameEditDialog(user.user_def.UD_NAME);
            }
        });

        schoolAdd = (ImageButton) rootView.findViewById(R.id.schoolAddBtn);
        workAdd = (ImageButton) rootView.findViewById(R.id.workAddBtn);
        initializeAddButtons();

        schoolList = (LinearLayout) rootView.findViewById(R.id.schoolList);
        workList = (LinearLayout) rootView.findViewById(R.id.workList);
        initializeSchools(user.data.data_school);
        initializeWork(user.data.data_work);

        ImageButton next = (ImageButton) rootView.findViewById(R.id.nextBtn);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.replace(R.id.frag_container, new ProfileSetupFragment2(), ProfileSetupFragment2.class.getName());
                ft.addToBackStack(null);
                ft.commit();
            }
        });

        return rootView;
    }

    private void nameEditDialog(String name) {
        MaterialDialog sName = new MaterialDialog.Builder(getActivity())
                .title("Name")
                .input("Enter your name", name, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(MaterialDialog dialog, CharSequence input) {
                        saveName(input.toString().trim());
                    }
                }).show();
        sName.getActionButton(DialogAction.POSITIVE).setText("Save");
    }

    private void saveName(final String name) {
        final MaterialDialog progressDialog = new MaterialDialog.Builder(getActivity())
                .title("Updating user name")
                .content("Please wait")
                .progress(true, 0)
                .cancelable(false)
                .show();

        Map<String, String> params = new HashMap<String, String>();
        params.put("TABLE", "user_def");
        params.put("ACT", "2");
        params.put("UD_ID", user.user_def.UD_ID);
        params.put("UD_NAME", name);
        Utils.sendVolleyJsonRequest(getActivity(), Config.Server.SERVER_REQUESTS_URL, params, new Utils.VolleyRequestListener() {
            @Override
            public void onResponse(String response) {
                progressDialog.dismiss();
                Log.d("test", response);
                user.user_def.UD_NAME = name;
                session.setUserProfile(user);
                userName.setText("Hi, " + user.user_def.UD_NAME);
                Toast.makeText(getActivity(), "Name updated successfully", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String error) {
                progressDialog.dismiss();
                Log.d("test", error);
                Toast.makeText(getActivity(), "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initializeAddButtons() {
        schoolAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.setCustomAnimations(R.animator.slide_in, R.animator.slide_out);
                ft.replace(R.id.frag_container, SchoolProfileFragment.newInstance(null, true), SchoolProfileFragment.class.getName());
                ft.addToBackStack(null);
                ft.commit();
            }
        });
        workAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.setCustomAnimations(R.animator.slide_in, R.animator.slide_out);
                ft.replace(R.id.frag_container, WorkProfileFragment.newInstance(null, true), WorkProfileFragment.class.getName());
                ft.addToBackStack(null);
                ft.commit();
            }
        });
    }

    private void initializeSchools(List<UserDataWrapper.DataSchool> schools) {
        if (schools == null || schools.size() == 0)
            return;

        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        for (int i = 0; i < schools.size(); i++) {
            View view = inflater.inflate(R.layout.item_school, null, false);
            TextView tvSchool = (TextView) view.findViewById(R.id.tvSchool);
            ImageButton editSchool = (ImageButton) view.findViewById(R.id.editSchool);

            final UserDataWrapper.DataSchool item = schools.get(i);
            String s = item.DS_BOARD + ", " + item.DS_NAME;
            tvSchool.setText(s);
            editSchool.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                    ft.setCustomAnimations(R.animator.slide_in, R.animator.slide_out);
                    ft.replace(R.id.frag_container, SchoolProfileFragment.newInstance(item, false), SchoolProfileFragment.class.getName());
                    ft.addToBackStack(null);
                    ft.commit();
                }
            });

            schoolList.addView(view, -1, params);
        }
    }

    private void initializeWork(List<UserDataWrapper.DataWork> work) {
        if (work == null || work.size() == 0)
            return;

        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        for (int i = 0; i < work.size(); i++) {
            View layoutWork = inflater.inflate(R.layout.item_school, null, false);
            TextView tvWork = (TextView) layoutWork.findViewById(R.id.tvSchool);
            ImageButton editWork = (ImageButton) layoutWork.findViewById(R.id.editSchool);

            final UserDataWrapper.DataWork item = work.get(i);
            String s = item.DW_POSITION + ", " + item.DW_NAME;
            tvWork.setText(s);
            editWork.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                    ft.setCustomAnimations(R.animator.slide_in, R.animator.slide_out);
                    ft.replace(R.id.frag_container, WorkProfileFragment.newInstance(item, false), WorkProfileFragment.class.getName());
                    ft.addToBackStack(null);
                    ft.commit();
                }
            });

            workList.addView(layoutWork, -1, params);
        }
    }
}