package com.trybe.trybe;


import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.trybe.trybe.app.Config;
import com.trybe.trybe.dto.LoginDTO;
import com.trybe.trybe.dto.MatchesDTO;
import com.trybe.trybe.dto.UserDataWrapper;
import com.trybe.trybe.helper.UserSessionManager;
import com.trybe.trybe.helper.Utils;

import java.util.HashMap;
import java.util.Map;

public class ChatMessageAboutFragment extends Fragment {

    private Gson gson;
    private static final String ARG_PARAM1 = "aboutUser";
    private static final String ARG_PARAM2 = "aboutUserType";
    private static final String ARG_PARAM3 = "aboutUserMatch";
    private LoginDTO user;
    private MatchesDTO.Match matchObj;
    private String userType;
    private boolean fetchedUserType = false;

    private ImageButton resume;
    private TextView resumeTitle;
    private LinearLayout jdContainer;
    private TextView caPost, caCompany, caLocation, caSalary;

    public ChatMessageAboutFragment() {
    }

    public static ChatMessageAboutFragment newInstance(LoginDTO user, MatchesDTO.Match matchObj, String userType) {
        ChatMessageAboutFragment fragment = new ChatMessageAboutFragment();
        Gson gson = Utils.getGsonInstance();

        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, gson.toJson(user));
        args.putString(ARG_PARAM2, userType);
        args.putString(ARG_PARAM3, gson.toJson(matchObj));
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        gson = Utils.getGsonInstance();
        if (getArguments() != null) {
            String userJson = getArguments().getString(ARG_PARAM1);
            user = gson.fromJson(userJson, LoginDTO.class);
            userType = getArguments().getString(ARG_PARAM2);
            String matchJson = getArguments().getString(ARG_PARAM3);
            matchObj = gson.fromJson(matchJson, MatchesDTO.Match.class);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.frag_chat_message_about, container, false);
        TextView aboutName = (TextView) rootView.findViewById(R.id.chatAboutName);
        TextView abtSchool = (TextView) rootView.findViewById(R.id.chatAboutSchool);
        TextView abtWork = (TextView) rootView.findViewById(R.id.chatAboutWork);
        TextView abtSectors = (TextView) rootView.findViewById(R.id.chatAboutSectors);
        TextView abtSkills = (TextView) rootView.findViewById(R.id.chatAboutSkills);
        TextView abtProjects = (TextView) rootView.findViewById(R.id.chatAboutProjects);
        resume = (ImageButton) rootView.findViewById(R.id.chatResumeBtn);
        ImageButton email = (ImageButton) rootView.findViewById(R.id.chatEmailBtn);
        resumeTitle = (TextView) rootView.findViewById(R.id.chatResumeText);

        jdContainer = (LinearLayout) rootView.findViewById(R.id.jdContainer);
        caPost = (TextView) rootView.findViewById(R.id.caPost);
        caCompany = (TextView) rootView.findViewById(R.id.caCompany);
        caLocation = (TextView) rootView.findViewById(R.id.caLocation);
        caSalary = (TextView) rootView.findViewById(R.id.caSalary);

        if (userType != null) {
            if (userType.equals(ChatMessageActivity.TYPE_SEEK)) {
                // hide resume button etc stuff
                resume.setVisibility(View.GONE);
                resumeTitle.setVisibility(View.GONE);
                if (matchObj != null) {
                    jdContainer.setVisibility(View.VISIBLE);
                    caPost.setText(matchObj.JR_POSITION);
                    caCompany.setText(matchObj.JR_COMPANY);
                    caLocation.setText(matchObj.JR_LOCATION);
                    caSalary.setText(matchObj.JR_SALARY_MIN + " - " + matchObj.JR_SALARY_MAX + " Lacs");
                }
            } else if (userType.equals(ChatMessageActivity.TYPE_REFER)) {
                // show resume button etc stuff
                resume.setVisibility(View.VISIBLE);
                resumeTitle.setVisibility(View.VISIBLE);
                jdContainer.setVisibility(View.GONE);

                if (user.user_def.UD_RESUME == null || user.user_def.UD_RESUME.trim().equals("")) {
                    resumeTitle.setText("Resume not provided");
                } else {
                    resumeTitle.setText("Download Resume");
                    resume.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            openResume(user.user_def.UD_RESUME);
                        }
                    });
                    resumeTitle.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            openResume(user.user_def.UD_RESUME);
                        }
                    });
                }
            }
        } else {
            if (!fetchedUserType) {
                resume.setVisibility(View.GONE);
                resumeTitle.setVisibility(View.GONE);
                checkIfSeekOrRefer();
                fetchedUserType = true;
            }
        }

        aboutName.setText("Email " + user.user_def.UD_NAME);
        String textSchool = "";
        if (user.data.data_school != null) {
            for (UserDataWrapper.DataSchool school : user.data.data_school) {
                textSchool = textSchool + school.DS_BOARD + ", " + school.DS_NAME + "\n";
            }
        } else {
            textSchool = "No school added";
        }
        abtSchool.setText(textSchool.trim());
        String textWork = "";
        if (user.data.data_work != null) {
            for (UserDataWrapper.DataWork work : user.data.data_work) {
                textWork = textWork + work.DW_POSITION + ", " + work.DW_NAME + "\n";
            }
        } else {
            textWork = "No work added";
        }
        abtWork.setText(textWork.trim());
        if (user.data.data_sector != null)
            abtSectors.setText(user.data.data_sector);
        if (user.data.data_skills != null)
            abtSkills.setText(user.data.data_skills);
        if (user.data.data_projects != null)
            abtProjects.setText(user.data.data_projects);

        email.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendEmail(user.user_def.UD_EMAIL);
            }
        });
        aboutName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendEmail(user.user_def.UD_EMAIL);
            }
        });

        return rootView;
    }

    private void sendEmail(String id) {
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("message/rfc822");
        String[] emails = new String[1];
        emails[0] = id;
        emailIntent.putExtra(Intent.EXTRA_EMAIL, emails);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Contact from Trybe");
        emailIntent.putExtra(Intent.EXTRA_TEXT, "");

        try {
            startActivity(Intent.createChooser(emailIntent, "Send mail..."));
        } catch (ActivityNotFoundException e) {
            Toast.makeText(getActivity(), "No email client installed", Toast.LENGTH_SHORT).show();
        }
    }

    private void openResume(String url) {
        if (url == null || url.trim().equals("")) {
            Toast.makeText(getActivity(), "Resume not provided", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        try {
            startActivity(i);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(getActivity(), "No browser installed", Toast.LENGTH_SHORT).show();
        }
    }

    private void checkIfSeekOrRefer() {
        Log.d("about frag", "fetching matches as do not know if seeker or referrer");
        Map<String, String> params = new HashMap<String, String>();
        params.put("UD_ID", UserSessionManager.getInstance().getUserProfile().user_def.UD_ID);
        Utils.sendVolleyJsonRequest(getActivity(), Config.Server.FETCH_MATCHES_URL, params, new Utils.VolleyRequestListener() {
            @Override
            public void onResponse(String response) {
                Log.d("about frag", "response: " + response);

                MatchesDTO matches = null;
                try {
                    matches = gson.fromJson(response, MatchesDTO.class);
                } catch (JsonSyntaxException e) {
                    Log.d("about frag", "json err: " + e.getLocalizedMessage());
                }

                if (matches == null) {
                    Log.d("about frag", "server error");
                } else {
                    Log.d("about frag", "seek: " + matches.SEEK.size() + ", refer: " + matches.REFER.size());
                    // if its seek resume is already hidden, so just check for refer
                    for (MatchesDTO.Match mUser : matches.REFER) {
                        if (mUser.UD_ID.equals(user.user_def.UD_ID)) {
                            resume.setVisibility(View.VISIBLE);
                            resumeTitle.setVisibility(View.VISIBLE);

                            if (user.user_def.UD_RESUME == null || user.user_def.UD_RESUME.trim().equals("")) {
                                resumeTitle.setText("Resume not provided");
                            } else {
                                resumeTitle.setText("Download Resume");
                                resume.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        openResume(user.user_def.UD_RESUME);
                                    }
                                });
                            }

                            jdContainer.setVisibility(View.VISIBLE);
                            caPost.setText(mUser.JR_POSITION);
                            caCompany.setText(mUser.JR_COMPANY);
                            caLocation.setText(mUser.JR_LOCATION);
                            caSalary.setText(mUser.JR_SALARY_MIN + " - " + mUser.JR_SALARY_MAX + " Lacs");
                        }
                    }
                }
            }

            @Override
            public void onError(String error) {
                Log.d("about frag", "Error: " + error);
            }
        });
    }
}