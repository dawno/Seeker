package com.trybe.trybe;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.trybe.trybe.adapter.JobPostsAdapter;
import com.trybe.trybe.dto.JobSeekerDTO;
import com.trybe.trybe.dto.JobsPostedDTO;
import com.trybe.trybe.dto.UserDef;
import com.trybe.trybe.helper.Utils;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ReferJobOverlay extends Fragment {

    private static final String ARG_PARAM1 = "jobs";
    private static final String ARG_PARAM2 = "seekers";
    private static final String ARG_PARAM3 = "user";

    private Gson gson;
    private JobPostsAdapter jobpostsAdapter;
    private ListView lvJobs;
    private View lvJobsContainer, noJobView, newJobPost;
    private List<JobsPostedDTO.JobPost> mJobsList;
    private List<JobSeekerDTO.JobSeeker> mSeekerList;
    private UserDef user;


    public ReferJobOverlay() {
    }

    public static ReferJobOverlay newInstance(List<JobsPostedDTO.JobPost> jobsList, List<JobSeekerDTO.JobSeeker> seekerList, UserDef user) {
        ReferJobOverlay fragment = new ReferJobOverlay();

        Gson gson = Utils.getGsonInstance();
        String jobsJson = gson.toJson(jobsList);
        String seekersJson = gson.toJson(seekerList);
        String userJson = gson.toJson(user);

        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, jobsJson);
        args.putString(ARG_PARAM2, seekersJson);
        args.putString(ARG_PARAM3, userJson);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        gson = Utils.getGsonInstance();
        if (getArguments() != null) {
            String jobsJson = getArguments().getString(ARG_PARAM1);
            String seekersJson = getArguments().getString(ARG_PARAM2);
            String userJson = getArguments().getString(ARG_PARAM3);

            Type listType = new TypeToken<ArrayList<JobsPostedDTO.JobPost>>() {
            }.getType();
            mJobsList = gson.fromJson(jobsJson, listType);
            listType = new TypeToken<ArrayList<JobSeekerDTO.JobSeeker>>() {
            }.getType();
            mSeekerList = gson.fromJson(seekersJson, listType);
            user = gson.fromJson(userJson, UserDef.class);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.frag_refer_job_overlay, container, false);

        noJobView = rootView.findViewById(R.id.noJobs);
        noJobView.setVisibility(View.GONE);
        lvJobsContainer = rootView.findViewById(R.id.jobLvContainer);
        lvJobs = (ListView) rootView.findViewById(R.id.jobLv);
        lvJobsContainer.setVisibility(View.VISIBLE);
        newJobPost = rootView.findViewById(R.id.newJobPost);
        init();

        lvJobs.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                ReferFragment frag = (ReferFragment) getActivity().getSupportFragmentManager().findFragmentByTag(ReferFragment.class.getName());
                TextView tv = (TextView) view.findViewById(R.id.jobName);
                frag.refreshSeekers(tv.getText().toString());

                ((ReferActivity) getActivity()).removeOverlay();
            }
        });
//        lvJobs.setLongClickable(true);
//        lvJobs.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
//            @Override
//            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
//                Toast.makeText(getActivity(), "long click", Toast.LENGTH_SHORT).show();
//                return true;
//            }
//        });

        newJobPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), JobPostActivity.class));
            }
        });

        return rootView;
    }

    private void init() {
        if (mJobsList.size() == 0) {
            noJobView.setVisibility(View.VISIBLE);
            lvJobsContainer.setVisibility(View.GONE);
            return;
        }

        HashMap<String, Integer> matchesNum = new HashMap<>();
        for (JobSeekerDTO.JobSeeker seeker : mSeekerList) {
            String s = seeker.JR_POSITION + ", " + seeker.JR_COMPANY;
            Integer existing = matchesNum.get(s);
            if (existing == null)
                matchesNum.put(s, 1);
            else
                matchesNum.put(s, existing + 1);
        }

        jobpostsAdapter = new JobPostsAdapter(getActivity(), R.layout.item_job_post, mJobsList, matchesNum, user.UD_ID);
        lvJobs.setAdapter(jobpostsAdapter);
    }

    public void refreshJobList(List<JobSeekerDTO.JobSeeker> seekerList) {
        mSeekerList = seekerList;
        init();
    }

}