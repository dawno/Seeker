package com.trybe.trybe;


import android.app.Fragment;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.gson.Gson;
import com.trybe.trybe.app.Config;
import com.trybe.trybe.dto.LoginDTO;
import com.trybe.trybe.dto.UserDataWrapper;
import com.trybe.trybe.helper.UserSessionManager;
import com.trybe.trybe.helper.Utils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;

public class WorkProfileFragment extends Fragment {

    @Bind(R.id.workCancel)
    ImageView workCancel;
    @Bind(R.id.workSave)
    TextView workSave;
    @Bind(R.id.workDelete)
    TextView workDelete;
    @Bind(R.id.workName)
    EditText workName;
    @Bind(R.id.workPos)
    EditText workPos;
    @Bind(R.id.from_year)
    View fromYear;
    @Bind(R.id.from_month)
    View fromMonth;
    @Bind(R.id.to_year)
    View toYear;
    @Bind(R.id.to_month)
    View toMonth;
    @Bind(R.id.toDateLayout)
    LinearLayout toDateLayout;
    @Bind(R.id.currentWorkCheckbox)
    CheckBox isCurrentWork;

    private static final String ARG_PARAM1 = "work_profile";
    private static final String ARG_PARAM2 = "is_new_work";
    private Gson gson;
    private UserSessionManager session;
    private LoginDTO user;
    private UserDataWrapper.DataWork work;
    private MaterialDialog progressDialog;

    public WorkProfileFragment() {
    }

    public static WorkProfileFragment newInstance(UserDataWrapper.DataWork work, boolean isNew) {
        WorkProfileFragment fragment = new WorkProfileFragment();
        Gson gson = Utils.getGsonInstance();
        String json = gson.toJson(work);

        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, json);
        args.putBoolean(ARG_PARAM2, isNew);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        gson = Utils.getGsonInstance();
        session = UserSessionManager.getInstance();
        user = session.getUserProfile();
        if (getArguments() != null) {
            boolean isNew = getArguments().getBoolean(ARG_PARAM2);
            if (isNew)
                work = null;
            else {
                String json = getArguments().getString(ARG_PARAM1);
                work = gson.fromJson(json, UserDataWrapper.DataWork.class);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.frag_work_profile, container, false);
        ButterKnife.bind(this, rootView);

        progressDialog = new MaterialDialog.Builder(getActivity())
                .title("Saving Data")
                .content("Please wait")
                .progress(true, 0)
                .cancelable(false)
                .build();

        workCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getFragmentManager().popBackStack();
            }
        });
        if (work == null) {
            workDelete.setVisibility(View.GONE);
        } else {
            workDelete.setVisibility(View.VISIBLE);
            workName.setText(work.DW_NAME);
            workPos.setText(work.DW_POSITION);
            setDateText(fromYear, fromMonth, work.DW_START_DT);
            setDateText(toYear, toMonth, work.DW_END_DT);
            if (work.DW_END_DT == null) {
                isCurrentWork.setChecked(true);
                toDateLayout.setVisibility(View.GONE);
            }
        }

        handleDateOnClicks();

        isCurrentWork.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean checked = ((CheckBox) view).isChecked();
                if (checked) {
                    toDateLayout.setVisibility(View.GONE);
                } else {
                    toDateLayout.setVisibility(View.VISIBLE);
                }
            }
        });

        workSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkIfEmptyWork()) {
                    Toast.makeText(getActivity(), "Work company, position or start/end dates cannot be empty", Toast.LENGTH_SHORT).show();
                    return;
                }

                progressDialog.show();
                UserDataWrapper.DataWork newWork = new UserDataWrapper.DataWork();
                newWork.DW_NAME = workName.getText().toString();
                newWork.DW_POSITION = workPos.getText().toString();

                String startYear = getDateText(fromYear);
                String startMonth = getDateText(fromMonth);
                newWork.DW_START_DT = getDate(startYear, startMonth);

                if (isCurrentWork.isChecked()) {
                    newWork.DW_END_DT = null;
                } else {
                    String endYear = getDateText(toYear);
                    String endMonth = getDateText(toMonth);
                    newWork.DW_END_DT = getDate(endYear, endMonth);
                }

                if (work == null) {
                    addNewWork(newWork);
                } else {
                    updateWork(newWork);
                }
            }
        });

        if (work != null) {
            workDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    progressDialog.show();
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("ACT", "3");
                    params.put("TABLE", "data_work");
                    params.put("DW_ID", work.DW_ID + "");
                    params.put("UD_ID", user.user_def.UD_ID);
                    Utils.sendVolleyJsonRequest(getActivity(), Config.Server.SERVER_REQUESTS_URL, params, new Utils.VolleyRequestListener() {
                        @Override
                        public void onResponse(String response) {
                            Log.d("WORK", "response: " + response);
                            progressDialog.dismiss();
                            for (UserDataWrapper.DataWork s : user.data.data_work) {
                                if (s.DW_ID == work.DW_ID) {
                                    user.data.data_work.remove(s);
                                    session.setUserProfile(user);
                                    break;
                                }
                            }
                            getFragmentManager().popBackStack();
                        }

                        @Override
                        public void onError(String error) {
                            Log.d("WORK", "error: " + error);
                            progressDialog.dismiss();
                            Toast.makeText(getActivity(), "Server Error", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        }

        return rootView;
    }

    private void addNewWork(final UserDataWrapper.DataWork newWork) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("ACT", "1");
        params.put("TABLE", "data_work");
        params.put("UD_ID", user.user_def.UD_ID);
        params.put("DATA", gson.toJson(newWork));
        Utils.sendVolleyJsonRequest(getActivity(), Config.Server.SERVER_REQUESTS_URL, params, new Utils.VolleyRequestListener() {
            @Override
            public void onResponse(String response) {
                Log.d("WORK", "response: " + response);
                progressDialog.dismiss();
                if (user.data.data_work == null)
                    user.data.data_work = new ArrayList<UserDataWrapper.DataWork>();
                user.data.data_work.add(newWork);
                session.setUserProfile(user);
                getFragmentManager().popBackStack();
            }

            @Override
            public void onError(String error) {
                Log.d("WORK", "error: " + error);
                progressDialog.dismiss();
                Toast.makeText(getActivity(), "Server Error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateWork(final UserDataWrapper.DataWork updatedWork) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("ACT", "2");
        params.put("TABLE", "data_work");
        params.put("DW_ID", work.DW_ID + "");
        params.put("D", gson.toJson(updatedWork));
        Utils.sendVolleyJsonRequest(getActivity(), Config.Server.SERVER_REQUESTS_URL, params, new Utils.VolleyRequestListener() {
            @Override
            public void onResponse(String response) {
                Log.d("WORK", "response: " + response);
                progressDialog.dismiss();
                for (UserDataWrapper.DataWork s : user.data.data_work) {
                    if (s.DW_ID == work.DW_ID) {
                        user.data.data_work.remove(s);
                        updatedWork.DW_ID = s.DW_ID;
                        user.data.data_work.add(updatedWork);
                        session.setUserProfile(user);
                        break;
                    }
                }
                getFragmentManager().popBackStack();
            }

            @Override
            public void onError(String error) {
                Log.d("WORK", "error: " + error);
                progressDialog.dismiss();
                Toast.makeText(getActivity(), "Server Error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean checkIfEmptyWork() {
        if (workName.getText().toString().trim().equals("") || workPos.getText().toString().trim().equals(""))
            return true;

        String startYear = getDateText(fromYear);
        String startMonth = getDateText(fromMonth);
        if (startYear.equals("Year") || startMonth.equals("Month"))
            return true;
        if (!isCurrentWork.isChecked()) {
            String endYear = getDateText(toYear);
            String endMonth = getDateText(toMonth);
            if (endYear.equals("Year") || endMonth.equals("Month"))
                return true;
        }

        return false;
    }

    private void setDateText(View year, View month, Date date) {
        if (date == null)
            return;

        String yearText = (String) DateFormat.format("yyyy", date);
        String monthText = (String) DateFormat.format("MM", date);
        setDateText(year, yearText);
        setDateText(month, monthText);
    }

    private void setDateText(View v, String text) {
        View tv = ((ViewGroup) v).getChildAt(0);
        if (tv instanceof TextView) {
            ((TextView) tv).setText(text);
        } else
            throw new IllegalStateException("Layout changed to incompatible type. First child of date container must be a textview");
    }

    private String getDateText(View v) {
        View tv = ((ViewGroup) v).getChildAt(0);
        if (tv instanceof TextView) {
            return ((TextView) tv).getText().toString();
        } else
            throw new IllegalStateException("Layout changed to incompatible type. First child of date container must be a textview");
    }

    private Date getDate(String year, String month) {
        try {
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.YEAR, Integer.parseInt(year));
            cal.set(Calendar.MONTH, Integer.parseInt(month) - 1);
            cal.set(Calendar.DAY_OF_MONTH, 1);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            return cal.getTime();
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void handleDateOnClicks() {
        fromYear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new MaterialDialog.Builder(getActivity())
                        .title("Select year")
                        .items(R.array.date_year)
                        .itemsCallbackSingleChoice(-1, new MaterialDialog.ListCallbackSingleChoice() {
                            @Override
                            public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                                setDateText(fromYear, (String) text);
                                return true;
                            }
                        })
                        .positiveText("SELECT")
                        .show();
            }
        });

        fromMonth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new MaterialDialog.Builder(getActivity())
                        .title("Select month")
                        .items(R.array.date_month)
                        .itemsCallbackSingleChoice(-1, new MaterialDialog.ListCallbackSingleChoice() {
                            @Override
                            public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                                setDateText(fromMonth, (String) text);
                                return true;
                            }
                        })
                        .positiveText("SELECT")
                        .show();
            }
        });

        toYear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new MaterialDialog.Builder(getActivity())
                        .title("Select year")
                        .items(R.array.date_year)
                        .itemsCallbackSingleChoice(-1, new MaterialDialog.ListCallbackSingleChoice() {
                            @Override
                            public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                                setDateText(toYear, (String) text);
                                return true;
                            }
                        })
                        .positiveText("SELECT")
                        .show();
            }
        });

        toMonth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new MaterialDialog.Builder(getActivity())
                        .title("Select month")
                        .items(R.array.date_month)
                        .itemsCallbackSingleChoice(-1, new MaterialDialog.ListCallbackSingleChoice() {
                            @Override
                            public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                                setDateText(toMonth, (String) text);
                                return true;
                            }
                        })
                        .positiveText("SELECT")
                        .show();
            }
        });
    }
}