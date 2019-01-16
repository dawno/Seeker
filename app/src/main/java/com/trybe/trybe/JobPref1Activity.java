package com.trybe.trybe;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.trybe.trybe.dto.JobCatDTO;
import com.trybe.trybe.dto.LoginDTO;
import com.trybe.trybe.gcm.NotificationUtils;
import com.trybe.trybe.helper.UserSessionManager;
import com.trybe.trybe.helper.Utils;

import java.util.LinkedList;
import java.util.Queue;

import butterknife.Bind;
import butterknife.ButterKnife;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class JobPref1Activity extends AppCompatActivity {

    @Bind(R.id.outerJcContainer)
    LinearLayout outerJcContainer;

    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private Queue<Integer> clickedJobs;
    private UserSessionManager session;
    private LoginDTO user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_job_pref1);
        ButterKnife.bind(this);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        clickedJobs = new LinkedList<Integer>();
        session = UserSessionManager.getInstance();
        user = session.getUserProfile();
        // TODO check for login session in each activity

        JobCatDTO jobCats = session.getJobCategories();
        if (jobCats != null)
            initializeJobCats(jobCats);

        if (user.job_seek != null)
            initUserJobCats(user.job_seek.JS_JC_IDS);

        ImageButton next = (ImageButton) findViewById(R.id.nextBtn);
        ImageButton back = (ImageButton)findViewById(R.id.backBtn);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), ProfileSetupActivity.class));
                finish();
            }
        });
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int selected = clickedJobs.size();
                if (selected <= 0) {
                    // TODO change to snackbar if needed
                    Toast.makeText(getApplicationContext(), "Select at least one job area", Toast.LENGTH_SHORT).show();
                    return;
                }

                String jobIds = "";
                for (Integer jobId : clickedJobs) {
                    jobIds = jobIds + jobId + ",";
                }
                jobIds = jobIds.substring(0, jobIds.length() - 1);
                Log.d("test", jobIds);

                user.job_seek.JS_JC_IDS = jobIds;
                session.setUserProfile(user);

                startActivity(new Intent(getApplicationContext(), JobPref2Activity.class));
                finish();
            }
        });

        mRegistrationBroadcastReceiver = Utils.getDefaultGCMBroadcastReceiver(getApplicationContext());
    }

    @Override
    public void onResume() {
        super.onResume();
        Utils.registerGCMReceiver(JobPref1Activity.this, mRegistrationBroadcastReceiver);
        NotificationUtils.clearNotifications();
    }

    @Override
    public void onPause() {
        Utils.unregisterGCMReceiver(JobPref1Activity.this, mRegistrationBroadcastReceiver);
        super.onPause();
    }

    private void initializeJobCats(JobCatDTO jobCats) {
        if (jobCats == null || jobCats.RESULTS.size() == 0)
            return;

        LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        int j = 0;
        String jCatCurrent = "";
        LinearLayout jcCol1 = null, jcCol2 = null;
        for (int i = 0; i < jobCats.RESULTS.size(); i++) {
            JobCatDTO.JobCat jc = jobCats.RESULTS.get(i);

            if (!jc.JC_CAT.equals(jCatCurrent)) {
                j = 0;
                jCatCurrent = jc.JC_CAT;
                View innerJcContainer = inflater.inflate(R.layout.layout_job_cat_container, null, false);
                outerJcContainer.addView(innerJcContainer, -1, params);
                jcCol1 = (LinearLayout) innerJcContainer.findViewById(R.id.jcVerticalCol1);
                jcCol2 = (LinearLayout) innerJcContainer.findViewById(R.id.jcVerticalCol2);
                TextView tvCat = (TextView) innerJcContainer.findViewById(R.id.jcCat);
                tvCat.setText(jc.JC_CAT);
            }

            View layoutJc = inflater.inflate(R.layout.layout_job_cat, null, false);
            TextView tvJc = (TextView) ((ViewGroup) layoutJc).getChildAt(0);

            tvJc.setText(jc.JC_SUB_CAT);
            tvJc.setId(jc.JC_ID);
            tvJc.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    jobClick(v);
                }
            });

            if (j % 2 == 0) {
                if (jcCol1 != null)
                    jcCol1.addView(layoutJc, -1, params);
            } else {
                if (jcCol2 != null)
                    jcCol2.addView(layoutJc, -1, params);
            }
            j += 1;
        }
    }

    public void jobClick(View v) {
        if (v == null || !(v instanceof TextView))
            return;

        TextView tv = (TextView) v;
        int id = v.getId();

        if (clickedJobs.contains(id)) {
            // undo click
            tv.setTextColor(getResources().getColor(R.color.textGray));
            tv.setBackgroundResource(R.drawable.round_corner_jobview);
            clickedJobs.remove(id);
            return;
        }
        if (clickedJobs.size() >= 3) {
            // click on id
            tv.setTextColor(getResources().getColor(R.color.textDarkGreen));
            tv.setBackgroundResource(R.drawable.round_corner_jobview_yellow);

            int removed = clickedJobs.poll();
            // remove first added view from clicked items
            TextView rtv = (TextView) outerJcContainer.findViewById(removed);
            rtv.setTextColor(getResources().getColor(R.color.textGray));
            rtv.setBackgroundResource(R.drawable.round_corner_jobview);

            clickedJobs.add(id);
            return;
        }
        // click on id
        tv.setTextColor(getResources().getColor(R.color.textDarkGreen));
        tv.setBackgroundResource(R.drawable.round_corner_jobview_yellow);
        clickedJobs.add(id);

        //Log.d("test", clickedJobs.size() + "");
    }

    // takes comma separated ids
    private void initUserJobCats(String ids) {
        if (ids == null)
            return;
        for (String id : ids.split(",")) {
            id = id.trim();
            int id_int = Integer.parseInt(id);
            jobClick(outerJcContainer.findViewById(id_int));
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}