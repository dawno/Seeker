package com.trybe.trybe.adapter;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.trybe.trybe.R;
import com.trybe.trybe.dto.JobSeekerDTO;
import com.trybe.trybe.dto.UserDataWrapper;
import com.trybe.trybe.helper.Utils;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by gopi.ra on 23-Feb-16.
 */
public class JobReferPagerAdapter extends PagerAdapter {

    private Context mContext;
    List<JobSeekerDTO.JobSeeker> mUserList;

    public JobReferPagerAdapter(Context context, List<JobSeekerDTO.JobSeeker> list) {
        mContext = context;
        mUserList = list;
    }

    public List<JobSeekerDTO.JobSeeker> getAdapterArrayList() {
        return this.mUserList;
    }

    @Override
    public Object instantiateItem(ViewGroup collection, int position) {
        JobSeekerDTO.JobSeeker item = mUserList.get(position);
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View layout = inflater.inflate(R.layout.item_job_refer, collection, false);

        ImageView profilePic = (CircleImageView) layout.findViewById(R.id.profilePic);
        TextView tvName = (TextView) layout.findViewById(R.id.tvName);
        TextView tvSchool = (TextView) layout.findViewById(R.id.tvSchool);
        TextView tvWork = (TextView) layout.findViewById(R.id.tvWork);
        TextView tvSectorsTitle = (TextView) layout.findViewById(R.id.tvSectorsTitle);
        TextView tvSectors = (TextView) layout.findViewById(R.id.tvSectors);
        TextView tvSkillsTitle = (TextView) layout.findViewById(R.id.tvSkillsTitle);
        TextView tvSkills = (TextView) layout.findViewById(R.id.tvSkills);
        TextView tvProjects = (TextView) layout.findViewById(R.id.tvProjects);

        tvName.setText(Utils.hideName(item.user_def.UD_NAME));
        if (item.data.data_school != null && item.data.data_school.size() > 0) {
            UserDataWrapper.DataSchool lastSchool = item.data.data_school.get(0);
            tvSchool.setText(lastSchool.DS_BOARD + ", " + lastSchool.DS_NAME);
        } else
            tvSchool.setText("Education not added");

        if (item.data.data_work != null && item.data.data_work.size() > 0) {
            UserDataWrapper.DataWork lastWork = item.data.data_work.get(0);
            tvWork.setText(lastWork.DW_POSITION + ", " + lastWork.DW_NAME);
        } else
            tvWork.setText("Work experience not added");
        if (item.data.data_sector != null && !item.data.data_sector.trim().equals(""))
            tvSectors.setText(item.data.data_sector);
        else {
            tvSectorsTitle.setVisibility(View.GONE);
            tvSectors.setVisibility(View.GONE);
        }
        if (item.data.data_skills != null && !item.data.data_skills.trim().equals(""))
            tvSkills.setText(item.data.data_skills);
        else {
            tvSkillsTitle.setVisibility(View.GONE);
            tvSkills.setVisibility(View.GONE);
        }
        if (item.data.data_projects != null && !item.data.data_projects.trim().equals(""))
            tvProjects.setText(item.data.data_projects);
        else
            tvProjects.setVisibility(View.GONE);
        String imageUrl = item.user_def.UD_IMG;
        Utils.loadImageByPicasso(mContext, profilePic, imageUrl);

        collection.addView(layout);

        return layout;
    }

    @Override
    public void destroyItem(ViewGroup collection, int position, Object view) {
        collection.removeView((View) view);
    }

    @Override
    public int getCount() {
        return mUserList.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }
}