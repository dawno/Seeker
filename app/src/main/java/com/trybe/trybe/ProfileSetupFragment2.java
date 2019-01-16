package com.trybe.trybe;

import android.Manifest;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.method.KeyListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.trybe.trybe.app.Config;
import com.trybe.trybe.dto.LoginDTO;
import com.trybe.trybe.helper.UserSessionManager;
import com.trybe.trybe.helper.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import cz.msebera.android.httpclient.Header;

/**
 * Created by Gopi on 31-Mar-16.
 */
public class ProfileSetupFragment2 extends Fragment {

    @Bind(R.id.addSectorBtn)
    ImageButton addSectorBtn;
    @Bind(R.id.editSectorBtn)
    ImageButton editSectorBtn;
    @Bind(R.id.saveSectorBtn)
    ImageButton saveSectorBtn;
    @Bind(R.id.addSkillBtn)
    ImageButton addSkillBtn;
    @Bind(R.id.editSkillBtn)
    ImageButton editSkillBtn;
    @Bind(R.id.saveSkillBtn)
    ImageButton saveSkillBtn;
    @Bind(R.id.editProjectBtn)
    ImageButton editProjectBtn;
    @Bind(R.id.saveProjectBtn)
    ImageButton saveProjectBtn;

    @Bind(R.id.addSector)
    EditText addSector;
    @Bind(R.id.addSkill)
    EditText addSkill;
    @Bind(R.id.addProject)
    EditText addProject;

    @Bind(R.id.sectorsContent)
    TextView sectorsContent;
    @Bind(R.id.skillsContent)
    TextView skillsContent;

    @Bind(R.id.uploadBtn)
    ImageButton uploadBtn;
    @Bind(R.id.uploadBtnText)
    TextView uploadBtnText;
    @Bind(R.id.nextBtn)
    ImageButton nextBtn;
    @Bind(R.id.backBtn)
     ImageButton backBtn;
    private static final int PICKFILE_REQUEST_CODE = 101;
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private boolean isSectorEditable = false;
    private boolean isSkillEditable = false;
    private boolean isProjectEditable = false;

    private String userSectors, userSkills, userProjects;

    private UserSessionManager session;
    private LoginDTO user;
    private MaterialDialog progressDialog;

    public ProfileSetupFragment2() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        session = UserSessionManager.getInstance();
        user = session.getUserProfile();
        progressDialog = new MaterialDialog.Builder(getActivity())
                .title("Saving Data")
                .content("Please wait")
                .progress(true, 0)
                .cancelable(false)
                .build();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.frag_profile_setup2, container, false);
        ButterKnife.bind(this, rootView);

        if (user.data.data_sector == null)
            user.data.data_sector = "";
        if (user.data.data_skills == null)
            user.data.data_skills = "";
        if (user.data.data_projects == null)
            user.data.data_projects = "";
        userSectors = user.data.data_sector;
        userSkills = user.data.data_skills;
        userProjects = user.data.data_projects;
        sectorsContent.setText(userSectors);
        skillsContent.setText(userSkills);
        addProject.setText(userProjects);

        initializeButtons();
        changeEditText(addSector, isSectorEditable);
        changeEditText(addSkill, isSkillEditable);
        changeEditText(addProject, isProjectEditable);
backBtn.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.frag_container, new ProfileSetupFragment1(), ProfileSetupFragment1.class.getName());
        ft.addToBackStack(null);
        ft.commit();

    }
});
        uploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
                    int permission = getActivity().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);

                    if (permission != PackageManager.PERMISSION_GRANTED) {
                        // We don't have permission so prompt the user
                        requestPermissions(
                                PERMISSIONS_STORAGE,
                                REQUEST_EXTERNAL_STORAGE
                        );
                    } else {
                        startFileChooserIntent();
                    }
                } else {
                    startFileChooserIntent();
                }
            }
        });
        uploadBtnText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
                    int permission = getActivity().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);

                    if (permission != PackageManager.PERMISSION_GRANTED) {
                        // We don't have permission so prompt the user
                        requestPermissions(
                                PERMISSIONS_STORAGE,
                                REQUEST_EXTERNAL_STORAGE
                        );
                    } else {
                        startFileChooserIntent();
                    }
                } else {
                    startFileChooserIntent();
                }
            }
        });

        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // if user doesn't click tick after entering project, still save it
                userProjects = addProject.getText().toString().trim();

                if (nothingChanged()) {
                    if (user.PREFERENCE_STATUS) {
                        FragmentTransaction ft = getFragmentManager().beginTransaction();
                        ft.replace(R.id.frag_container, new SetupCompletedFragment(), SetupCompletedFragment.class.getName());
                        ft.addToBackStack(null);
                        ft.commit();
                    } else {
                        startActivity(new Intent(getActivity(), JobPref1Activity.class));
                        getActivity().finish();
                    }
                    return;
                }

                progressDialog.show();

                Map<String, String> params = new HashMap<String, String>();
                params.put("UD_ID", user.user_def.UD_ID);
                params.put("DATA_SECTOR", userSectors);
                params.put("DATA_SKILLS", userSkills);
                params.put("DATA_PROJECTS", userProjects);
                Utils.sendVolleyJsonRequest(getActivity(), Config.Server.PROFILE_REQUESTS_URL, params, new Utils.VolleyRequestListener() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("test", "response: " + response);
                        progressDialog.dismiss();
                        user.PROFILESTATUS = true;
                        user.data.data_sector = userSectors;
                        user.data.data_skills = userSkills;
                        user.data.data_projects = userProjects;
                        session.setUserProfile(user);

                        if (user.PREFERENCE_STATUS) {
                            FragmentTransaction ft = getFragmentManager().beginTransaction();
                            ft.replace(R.id.frag_container, new SetupCompletedFragment(), SetupCompletedFragment.class.getName());
                            ft.addToBackStack(null);
                            ft.commit();
                        } else {
                            startActivity(new Intent(getActivity(), JobPref1Activity.class));
                            getActivity().finish();
                        }
                    }

                    @Override
                    public void onError(String error) {
                        Log.d("test", "error: " + error);
                        progressDialog.dismiss();
                        Toast.makeText(getActivity(), "Network Error", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        return rootView;
    }

    private boolean nothingChanged() {
        if (user.data.data_sector.trim().equals(userSectors.trim()) &&
                user.data.data_skills.trim().equals(userSkills.trim()) &&
                user.data.data_projects.trim().equals(userProjects.trim()))
            return true;
        else
            return false;
    }

    private void startFileChooserIntent() {
        Intent intent = new Intent();
        intent.setType("*/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        try {
            startActivityForResult(intent, PICKFILE_REQUEST_CODE);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(getActivity(), "File Manager not found. Install a file manager first.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case PICKFILE_REQUEST_CODE:
                if (resultCode == getActivity().RESULT_OK && data.getData() != null) {
                    // TODO check size, file extension & other file constraints

                    Uri uri = data.getData();
                    Log.d("test", "File Uri: " + uri.toString());
                    String path = Utils.getPath(getActivity(), uri);
                    Log.d("test", "File Path: " + uri.getPath());
                    Log.d("test", "FileUtils Path: " + path);

                    if (path != null) {
                        File file = new File(path);
                        Log.d("test", "File exists: " + (file.exists() ? "true" : "false"));
                        // Initiate the upload
                        uploadResume(file);
                    } else {
                        Toast.makeText(getActivity(), "Invalid file", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.d("test", resultCode + "");
                    Toast.makeText(getActivity(), "No file selected", Toast.LENGTH_SHORT).show();
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("test", "yay! perm granted");
                    startFileChooserIntent();
                } else {
                    Log.d("test", "perm denied");
                    Toast.makeText(getActivity(), "You need to provide permission to upload your resume", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    public void uploadResume(final File resumeFile) {
        if (!checkValidFile(resumeFile)) {
            return;
        }

        final MaterialDialog progressDialog = new MaterialDialog.Builder(getActivity())
                .title("Uploading Resume")
                .content("Please wait")
                .progress(true, 0)
                .cancelable(false)
                .show();
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        try {
            params.put("FileToUpload", resumeFile);
        } catch (FileNotFoundException e) {
            Log.d("test", "file not found");
            Toast.makeText(getActivity(), "IO Error", Toast.LENGTH_SHORT).show();
            return;
        }
        params.put("TYPE", "resume");
        params.put("UD_ID", user.user_def.UD_ID);
        client.post(getActivity(), Config.Server.FILE_UPLOAD_URL, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                progressDialog.dismiss();
                Log.d("test", response.toString());
                try {
                    String url = (String) response.get("FILEURL");
                    url.replaceAll("\\\\", "");
                    Log.d("test", "resume url: " + url);
                    user.user_def.UD_RESUME = url;
                    session.setUserProfile(user);
                    Toast.makeText(getActivity(), "Resume successfully uploaded!", Toast.LENGTH_SHORT).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getActivity(), "Server error", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(getActivity(), "Server error", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable t, JSONObject errorResponse) {
                progressDialog.dismiss();
                // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                Log.d("test", "error: " + t.getLocalizedMessage());
                if (errorResponse != null)
                    Log.d("test", errorResponse.toString());
                Toast.makeText(getActivity(), "Network error", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String res, Throwable t) {
                // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                progressDialog.dismiss();
                Log.d("test", "error: " + t.getLocalizedMessage());
                Log.d("test", res);
                Toast.makeText(getActivity(), "Resume upload error!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onProgress(long bytesWritten, long totalSize) {
                super.onProgress(bytesWritten, totalSize);
                double percentUpload = ((double) bytesWritten / totalSize) * 100;
                int percent = (int) percentUpload;
                Log.d("test", "Progress: " + percent + "%");
            }
        });
    }

    private boolean checkValidFile(File resume) {
        boolean check = true;

        long fileSize = resume.length() / 1024;
        if (fileSize >= 3 * 1024) {
            Toast.makeText(getActivity(), "Resume file size must be less than 3MB", Toast.LENGTH_SHORT).show();
            check = false;
        }

        String filePath = resume.getAbsolutePath();
        String extn = filePath.substring(filePath.lastIndexOf(".") + 1);
        String[] validExtns = {"pdf", "doc", "docx"};
        if (!Arrays.asList(validExtns).contains(extn)) {
            Toast.makeText(getActivity(), "Invalid file type", Toast.LENGTH_SHORT).show();
            check = false;
        }

        return check;
    }

    private void initializeButtons() {
        addSectorBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isSectorEditable)
                    return;

                isSectorEditable = true;
                changeEditText(addSector, true);
                requestFocus(addSector);
                saveSectorBtn.setVisibility(View.VISIBLE);
            }
        });
        addSkillBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isSkillEditable)
                    return;

                isSkillEditable = true;
                changeEditText(addSkill, true);
                requestFocus(addSkill);
                saveSkillBtn.setVisibility(View.VISIBLE);
            }
        });
        editProjectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isProjectEditable)
                    return;

                isProjectEditable = true;
                changeEditText(addProject, true);
                requestFocus(addProject);
                saveProjectBtn.setVisibility(View.VISIBLE);
            }
        });
        editSectorBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new MaterialDialog.Builder(getActivity())
                        .title("Add your sectors")
                        .input("Add your sectors", sectorsContent.getText().toString(), new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(MaterialDialog dialog, CharSequence input) {
                                userSectors = input.toString().trim();
                                sectorsContent.setText(userSectors);
                            }
                        }).show();
            }
        });
        editSkillBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new MaterialDialog.Builder(getActivity())
                        .title("Add your skills")
                        .input("Add your skills", skillsContent.getText().toString(), new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(MaterialDialog dialog, CharSequence input) {
                                userSkills = input.toString().trim();
                                skillsContent.setText(userSkills);
                            }
                        }).show();
            }
        });
        saveSectorBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String s = addSector.getText().toString().trim();

                if (!s.isEmpty()) {
                    if (!userSectors.isEmpty())
                        userSectors = userSectors + ", " + s;
                    else
                        userSectors = s;
                    sectorsContent.setText(userSectors);
                }

                isSectorEditable = false;
                addSector.setText("");
                changeEditText(addSector, false);
                saveSectorBtn.setVisibility(View.GONE);
                hideInputFocus();
            }
        });
        saveSkillBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String s = addSkill.getText().toString().trim();

                if (!s.isEmpty()) {
                    if (!userSkills.isEmpty())
                        userSkills = userSkills + ", " + s;
                    else
                        userSkills = s;
                    skillsContent.setText(userSkills);
                }

                isSkillEditable = false;
                addSkill.setText("");
                changeEditText(addSkill, false);
                saveSkillBtn.setVisibility(View.GONE);
                hideInputFocus();
            }
        });
        saveProjectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userProjects = addProject.getText().toString().trim();

                addProject.setText(userProjects);
                isProjectEditable = false;
                changeEditText(addProject, false);
                saveProjectBtn.setVisibility(View.GONE);
                hideInputFocus();
            }
        });
    }

    private void requestFocus(EditText et) {
        et.requestFocus();
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(et, InputMethodManager.SHOW_IMPLICIT);
    }

    private void hideInputFocus() {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    private void changeEditText(EditText et, boolean enable) {
        if (!enable) {
            et.setTag(et.getKeyListener());
            et.setKeyListener(null);
        } else {
            et.setKeyListener((KeyListener) et.getTag());
        }
    }
}