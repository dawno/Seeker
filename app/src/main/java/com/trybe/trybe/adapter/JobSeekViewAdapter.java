package com.trybe.trybe.adapter;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.trybe.trybe.R;
import com.trybe.trybe.dto.JobReferralDTO;
import com.trybe.trybe.helper.Utils;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Gopi on 19-Feb-16.
 */
public class JobSeekViewAdapter extends ArrayAdapter<JobReferralDTO.JobReferral> {

    Context context;

    public JobSeekViewAdapter(Context context, int resourceId, List<JobReferralDTO.JobReferral> items) {
        super(context, resourceId, items);
        this.context = context;
        Log.d("adapter", items.size() + "");
        Log.d("adapter", items.get(0).JR_COMPANY);
    }

    private static class ViewHolder {
        TextView tvRefName, tvCurrentPost, tvCurrentCompany;
        TextView tvCompany, tvPost, tvLocation, tvSalary, tvRemark;
        ImageView profilePic;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        ViewHolder holder = null;
        JobReferralDTO.JobReferral item = getItem(position);
        Log.d("adapter", item.JR_COMPANY);

        LayoutInflater mInflater = (LayoutInflater) context
                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        if (view == null) {
            view = mInflater.inflate(R.layout.item_job_seek, parent, false);
            holder = new ViewHolder();
            holder.tvRefName = (TextView) view.findViewById(R.id.tvRefName);
            holder.tvCurrentPost = (TextView) view.findViewById(R.id.tvCurrentPost);
            holder.tvCurrentCompany = (TextView) view.findViewById(R.id.tvCurrentCompany);
            holder.tvCompany = (TextView) view.findViewById(R.id.tvCompany);
            holder.tvPost = (TextView) view.findViewById(R.id.tvPost);
            holder.tvLocation = (TextView) view.findViewById(R.id.tvLocation);
            holder.tvSalary = (TextView) view.findViewById(R.id.tvSalary);
            holder.tvRemark = (TextView) view.findViewById(R.id.tvRemark);
            holder.profilePic = (CircleImageView) view.findViewById(R.id.profilePic);
            view.setTag(holder);
        } else
            holder = (ViewHolder) view.getTag();

        holder.tvRefName.setText(Utils.hideName(item.UD_NAME));
        if (item.DW_POSITION != null)
            holder.tvCurrentPost.setText(item.DW_POSITION);
        else
            holder.tvCurrentPost.setVisibility(View.GONE);
        if (item.DW_NAME != null)
            holder.tvCurrentCompany.setText(item.DW_NAME);
        else
            holder.tvCurrentCompany.setVisibility(View.GONE);

        holder.tvCompany.setText(item.JR_COMPANY);
        if (item.JR_POSITION != null)
            holder.tvPost.setText(item.JR_POSITION);
        else
            holder.tvPost.setText("Job Position not specified");
        holder.tvLocation.setText(item.JR_LOCATION);
        holder.tvSalary.setText(item.JR_SALARY_MIN + " - " + item.JR_SALARY_MAX + " Lacs");
        if (item.JR_REMARK != null && !item.JR_REMARK.trim().equals(""))
            holder.tvRemark.setText(item.JR_REMARK);
        else
            holder.tvRemark.setVisibility(View.GONE);
        String imageUrl = item.UD_IMG;
        Utils.loadImageByPicasso(context, holder.profilePic, imageUrl);
        return view;
    }

}