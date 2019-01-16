package com.trybe.trybe.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.linkedin.platform.LISessionManager;
import com.trybe.trybe.app.BaseApplication;
import com.trybe.trybe.dto.JobCatDTO;
import com.trybe.trybe.dto.JobLocDTO;
import com.trybe.trybe.dto.LoginDTO;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class UserSessionManager {

    private static SharedPreferences mPrefs;
    private static Gson gson;
    private static UserSessionManager mSessionInstance;

    private static final String PREF_NAME = "UserSession";
    private static final String IS_LOGIN = "IsLoggedIn";
    public static final String KEY_LINKEDIN = "isLinkedIn";
    public static final String KEY_USAGE = "homeUsageScreen";
    public static final String USER_PROFILE = "trybe_user_profile";
    public static final String JOB_CATS = "trybe_job_categories";
    public static final String JOB_LOCS = "trybe_job_locations";
    public static final String GCM_TOKEN = "trybe_gcm_token";
    public static final String UNREAD_CHATS = "trybe_unread_chats";

    private UserSessionManager() {
        mPrefs = BaseApplication.getInstance().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = Utils.getGsonInstance();
    }

    public static synchronized UserSessionManager getInstance() {
        if (mSessionInstance == null)
            mSessionInstance = new UserSessionManager();
        return mSessionInstance;
    }

    public void showHomeUsageScreen(boolean trueToShow) {
        Editor editor = mPrefs.edit();
        if (trueToShow)
            editor.putBoolean(KEY_USAGE, true);
        else
            editor.putBoolean(KEY_USAGE, false);
        editor.apply();
    }

    public boolean showHomeUsageScreen() {
        return mPrefs.getBoolean(KEY_USAGE, false);
    }

    public void createUserLoginSession() {
        Editor editor = mPrefs.edit();
        editor.putBoolean(IS_LOGIN, true);
        editor.apply();
    }

    public void setLinkedInLogin() {
        Editor editor = mPrefs.edit();
        editor.putBoolean(KEY_LINKEDIN, true);
        editor.apply();
    }

    public void setUserProfile(LoginDTO user) {
        Editor editor = mPrefs.edit();
        String json = gson.toJson(user);
        editor.putString(USER_PROFILE, json);
        editor.apply();
    }

    public LoginDTO getUserProfile() {
        String json = mPrefs.getString(USER_PROFILE, "");
        if (json.trim().equals(""))
            return null;
        else
            return gson.fromJson(json, LoginDTO.class);
    }

    public String getLoginToken() {
        LoginDTO user = getUserProfile();
        if (user == null)
            return null;
        else
            return user.UC_TOKEN;
    }

    public void setJobCategories(JobCatDTO jobCats) {
        Editor editor = mPrefs.edit();
        String json = gson.toJson(jobCats);
        editor.putString(JOB_CATS, json);
        editor.apply();
    }

    public JobCatDTO getJobCategories() {
        String json = mPrefs.getString(JOB_CATS, "");
        if (json.trim().equals(""))
            return null;
        else
            return gson.fromJson(json, JobCatDTO.class);
    }

    public void setJobLocations(JobLocDTO jobLocs) {
        Editor editor = mPrefs.edit();
        String json = gson.toJson(jobLocs);
        editor.putString(JOB_LOCS, json);
        editor.apply();
    }

    public JobLocDTO getJobLocations() {
        String json = mPrefs.getString(JOB_LOCS, "");
        if (json.trim().equals(""))
            return null;
        else
            return gson.fromJson(json, JobLocDTO.class);
    }

    public boolean isLoggedIn() {
        return mPrefs.getBoolean(IS_LOGIN, false);
    }

    public boolean isLinkedInLogin() {
        return mPrefs.getBoolean(KEY_LINKEDIN, false);
    }

    public void logoutUser() {
        if (isLinkedInLogin())
            LISessionManager.getInstance(BaseApplication.getInstance()).clearSession();

        Editor editor = mPrefs.edit();
        editor.clear();
        editor.apply();
    }

    public void setUnreadChatsUID(ArrayList<String> userIds) {
        Editor editor = mPrefs.edit();
        String json = gson.toJson(userIds);
        editor.putString(UNREAD_CHATS, json);
        editor.apply();
    }

    public ArrayList<String> getUnreadChatsUID() {
        String json = mPrefs.getString(UNREAD_CHATS, "");
        if (json.trim().equals(""))
            return new ArrayList<String>();
        else {
            Type listType = new TypeToken<ArrayList<String>>() {
            }.getType();
            return gson.fromJson(json, listType);
        }
    }

    public void setGcmToken(String token) {
        Editor editor = mPrefs.edit();
        editor.putString(GCM_TOKEN, token);
        editor.apply();
    }

    public String getGcmToken() {
        return mPrefs.getString(GCM_TOKEN, null);
    }
}