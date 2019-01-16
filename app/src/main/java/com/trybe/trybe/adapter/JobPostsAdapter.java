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
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.trybe.trybe.R;
import com.trybe.trybe.app.Config;
import com.trybe.trybe.dto.JobCatDTO;
import com.trybe.trybe.dto.JobsPostedDTO;
import com.trybe.trybe.helper.UserSessionManager;
import com.trybe.trybe.helper.Utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Gopi on 19-Feb-16.
 */
public class JobPostsAdapter extends ArrayAdapter<JobsPostedDTO.JobPost> {

    Context context;
    List<JobsPostedDTO.JobPost> items;
    HashMap<String, Integer> map;
    String userId;

    public JobPostsAdapter(Context context, int resourceId, List<JobsPostedDTO.JobPost> items, HashMap<String, Integer> map, String userId) {
        super(context, resourceId, items);
        this.context = context;
        this.items = items;
        this.map = map;
        this.userId = userId;
    }

    private static class ViewHolder {
        ImageView iv;
        TextView jobNum;
        TextView jobName;
        ImageView jobInfo;
        ImageView jobDelete;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        ViewHolder holder = null;
        final JobsPostedDTO.JobPost item = getItem(position);

        LayoutInflater mInflater = (LayoutInflater) context
                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        if (view == null) {
            view = mInflater.inflate(R.layout.item_job_post, parent, false);
            holder = new ViewHolder();
            holder.jobNum = (TextView) view.findViewById(R.id.jobNum);
            holder.jobName = (TextView) view.findViewById(R.id.jobName);
            holder.iv = (ImageView) view.findViewById(R.id.jobIv);
            holder.jobInfo = (ImageView) view.findViewById(R.id.jobInfo);
            holder.jobDelete = (ImageView) view.findViewById(R.id.jobDelete);
            view.setTag(holder);
        } else
            holder = (ViewHolder) view.getTag();

        int color = (int) (double) (Math.random() * 8);
        holder.iv.getBackground().setLevel(color);
        String text = item.JR_POSITION + ", " + item.JR_COMPANY;
        holder.jobName.setText(text);
        Integer num = map.get(text);
        if (num == null)
            holder.jobNum.setText("0");
        else
            holder.jobNum.setText(num.toString());

        holder.jobInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String info = "Company: " + item.JR_COMPANY + "\nPosition:" + item.JR_POSITION + "\nLocation: " + item.JR_LOCATION
                        + "\nJob Category: " + getJobCat(item.JR_JC_ID) + "\nSalary: " + item.JR_SALARY_MIN + " - "
                        + item.JR_SALARY_MAX + " Lacs" + "\nRemarks: " + item.JR_REMARK;
                new MaterialDialog.Builder(context)
                        .title("Job Post Info")
                        .content(info)
                        .positiveText("OK")
                        .titleColor(context.getResources().getColor(R.color.colorPrimaryDark))
                        .show();
            }
        });
        holder.jobDelete.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        new MaterialDialog.Builder(context)
                                .title("Delete Job Post")
                                .content("Are you Sure")
                                .positiveText("Delete")
                                .negativeText("Cancel")
                                .onNegative(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(MaterialDialog dialog, DialogAction which) {
                                        dialog.dismiss();
                                    }
                                })
                                .onPositive(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(MaterialDialog dialog, DialogAction which) {
                                        dialog.dismiss();
                                        deleteJob(item);
                                    }
                                })
                                .show();
                    }
                }

        );

        return view;
    }

    private void deleteJob(final JobsPostedDTO.JobPost jobPost) {
        final MaterialDialog progressDialog = new MaterialDialog.Builder(context)
                .title("Deleting job")
                .content("Please wait")
                .progress(true, 0)
                .cancelable(false)
                .show();

        Map<String, String> params = new HashMap<String, String>();
        params.put("TABLE", "job_refer");
        params.put("ACT", "3");
        params.put("UD_ID", userId);
        params.put("JR_ID", jobPost.JR_ID + "");
        Utils.sendVolleyJsonRequest(context, Config.Server.SERVER_REQUESTS_URL, params, new Utils.VolleyRequestListener() {
            @Override
            public void onResponse(String response) {
                progressDialog.dismiss();
                Log.d("test", response);
                items.remove(jobPost);
                notifyDataSetChanged();
                Toast.makeText(context, "Job deleted successfully", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String error) {
                progressDialog.dismiss();
                Log.d("test", error);
                Toast.makeText(context, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getJobCat(String jcId) {
        List<JobCatDTO.JobCat> jobCats = UserSessionManager.getInstance().getJobCategories().RESULTS;
        if (jobCats == null)
            return "";

        String jCat = "Not Specified";
        for (JobCatDTO.JobCat jc : jobCats) {
            if (jcId.equals(jc.JC_ID + "")) {
                jCat = jc.JC_CAT + ", " + jc.JC_SUB_CAT;
                break;
            }
        }

        return jCat;
    }

}