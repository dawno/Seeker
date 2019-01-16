package com.trybe.trybe;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.github.paolorotolo.appintro.AppIntro;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.linkedin.platform.APIHelper;
import com.linkedin.platform.AccessToken;
import com.linkedin.platform.LISession;
import com.linkedin.platform.LISessionManager;
import com.linkedin.platform.errors.LIApiError;
import com.linkedin.platform.errors.LIAuthError;
import com.linkedin.platform.listeners.ApiListener;
import com.linkedin.platform.listeners.ApiResponse;
import com.linkedin.platform.listeners.AuthListener;
import com.linkedin.platform.utils.Scope;
import com.trybe.trybe.app.Config;
import com.trybe.trybe.dto.LoginDTO;
import com.trybe.trybe.helper.UserSessionManager;
import com.trybe.trybe.helper.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;


public class LoginActivity extends AppIntro {

    private static final String LINKEDIN_API_URL = "https://api.linkedin.com/v1/people/~:(email-address,formatted-name,phone-numbers,public-profile-url,picture-url)";
    public String liAccessToken;
    private UserSessionManager session;
    private Gson gson;
    URL image_value;
    String name,email_id,id,gender,email,work,education;
    private CallbackManager callbackManager;

    @Override
    public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
        setContentView(R.layout.activity_login);
    }

    @Override
    public void init(Bundle savedInstanceState) {
        FacebookSdk.sdkInitialize(getApplicationContext());
        addSlide(new LoginFragment1());
        addSlide(new LoginFragment2());
        addSlide(new LoginFragment3());

        setZoomAnimation();
        setIndicatorColor(ContextCompat.getColor(this, R.color.colorPrimary), ContextCompat.getColor(this, R.color.white));
        setBarColor(ContextCompat.getColor(this, android.R.color.transparent));
        setSeparatorColor(ContextCompat.getColor(this, android.R.color.transparent));
        showSkipButton(false);
        setProgressButtonEnabled(false);
        setVibrate(false);

        session = UserSessionManager.getInstance();
        gson = Utils.getGsonInstance();

        String notifFlag = null, notifData = null, notifTitle = null;
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            notifFlag = extras.getString(HomeActivity.NOTIFICATION_FLAG);
            notifTitle = extras.getString(HomeActivity.APP_NOTIFICATION_TITLE);
            notifData = extras.getString(HomeActivity.APP_NOTIFICATION);
        }
        if (notifFlag == null) {

        } else if (notifFlag.equals(Config.PUSH_TYPE_NOTIF)) {
            new MaterialDialog.Builder(LoginActivity.this)
                    .title(notifTitle)
                    .content(Html.fromHtml(notifData))
                    .positiveText("OK")
                    .show();
        } else if (notifFlag.equals(Config.PUSH_TYPE_UPDATE)) {
            new MaterialDialog.Builder(LoginActivity.this)
                    .title(notifTitle)
                    .content(notifData)
                    .positiveText("Update")
                    .negativeText("Later")
                    .onNegative(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(MaterialDialog dialog, DialogAction which) {
                            dialog.dismiss();
                        }
                    })
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(MaterialDialog dialog, DialogAction which) {
                            final String appPackageName = getPackageName();
                            try {
                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                            } catch (android.content.ActivityNotFoundException anfe) {
                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                            }
                            dialog.dismiss();
                        }
                    })
                    .show();
        }

        Utils.fetchJobCategories(LoginActivity.this);
    }
    public void printHashKey(){
        // Add code to print out the key hash
        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    "trybe",
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {

        } catch (NoSuchAlgorithmException e) {

        }
    }

    @Override
    public void onBackPressed() {
        if (getPager().getCurrentItem() == 0) {
            LoginFragment1 loginFrag1 = (LoginFragment1) getSlides().get(0);
            if (loginFrag1.onBackPressed())
                super.onBackPressed();
        } else {
            getPager().setCurrentItem(0, true);
        }
    }

    // This method is used to make permissions to retrieve data from linkedin
    private static Scope buildScope() {
        return Scope.build(Scope.R_BASICPROFILE, Scope.R_EMAILADDRESS);
    }

    public void loginLinkedIn() {
        LISession liSession = LISessionManager.getInstance(getApplicationContext()).getSession();
        boolean accessTokenValid = liSession.isValid();

        if (accessTokenValid) {
            AccessToken accessToken = liSession.getAccessToken();
            liAccessToken = accessToken.getValue();
            LISessionManager.getInstance(getApplicationContext()).init(accessToken);
            Log.d("LI", liAccessToken);
            makeLinkedInApiRequest();
        } else {
            Log.d("LI", "requesting new access token");
            final Activity thisActivity = this;
            LISessionManager.getInstance(getApplicationContext()).init(thisActivity, buildScope(), new AuthListener() {
                @Override
                public void onAuthSuccess() {
                    Log.d("LI", "onAuthSuccess");
                    Log.d("LI", LISessionManager.getInstance(getApplicationContext()).getSession().getAccessToken().toString());
                    liAccessToken = LISessionManager.getInstance(getApplicationContext()).getSession().getAccessToken().getValue();
                    Log.d("LI", liAccessToken);
                }

                @Override
                public void onAuthError(LIAuthError error) {
                    Log.d("LI", "onAuthError");
                    if (error != null) {
                        Log.d("LI", error.toString());
                        Toast.makeText(LoginActivity.this, "LinkedIn authentication failed", Toast.LENGTH_SHORT).show();
                    }
                }
            }, true);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        LISessionManager.getInstance(getApplicationContext()).onActivityResult(this, requestCode, resultCode, data);
        if (LISessionManager.getInstance(getApplicationContext()).getSession().isValid())
            makeLinkedInApiRequest();
        Log.e("LogAct_onActivityResult","logAct_onActivityResult");
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void makeLinkedInApiRequest() {
        final MaterialDialog liDialog = new MaterialDialog.Builder(this)
                .title("Fetching data from LinkedIn")
                .content("Please wait")
                .progress(true, 0)
                .cancelable(false)
                .show();
        APIHelper apiHelper = APIHelper.getInstance(getApplicationContext());
        apiHelper.getRequest(this, LINKEDIN_API_URL, new ApiListener() {
            @Override
            public void onApiSuccess(ApiResponse result) {
                liDialog.dismiss();
                Log.d("LI", "onApiSuccess");
                JSONObject response = result.getResponseDataAsJson();
                Log.d("LI", response.toString());
                try {
                    String email = response.get("emailAddress").toString();
                    String name = response.get("formattedName").toString();
                    String image = response.get("pictureUrl").toString();
                    String liProfile = response.get("publicProfileUrl").toString();

                    sendLinkedInRequest(name, email, image);

                } catch (Exception e) {
                    Toast.makeText(LoginActivity.this, "LinkedIn API error", Toast.LENGTH_SHORT).show();
                    Log.d("LI", e.getLocalizedMessage());
                    return;
                }
            }

            @Override
            public void onApiError(LIApiError error) {
                liDialog.dismiss();
                if (error != null) {
                    Log.d("LI", error.getLocalizedMessage());
                    Toast.makeText(LoginActivity.this, "Network Error", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void sendLinkedInRequest(String name, String email, String userImg) {
        final MaterialDialog liLoginDialog = new MaterialDialog.Builder(this)
                .title("Logging in")
                .content("Please wait")
                .progress(true, 0)
                .cancelable(false)
                .show();

        Map<String, String> params = new HashMap<String, String>();
        params.put("UD_LINKEDIN", email);
        params.put("UD_EMAIL", email);
        params.put("UD_NAME", name);
        params.put("UD_IMG", userImg);
        Utils.sendVolleyJsonRequest(this, false, Config.Server.SERVER_LOGIN_URL, params, new Utils.VolleyRequestListener() {
            @Override
            public void onResponse(String response) {
                Log.d("test", "response: " + response);
                liLoginDialog.dismiss();

                LoginDTO userLogin = null;
                try {
                    userLogin = gson.fromJson(response, LoginDTO.class);
                } catch (JsonSyntaxException e) {
                    Log.d("test", "json err: " + e.getLocalizedMessage());
                }

                if (userLogin == null) {
                    Log.d("test", "userLogin is null");
                    Toast.makeText(getApplicationContext(), "Server Error", Toast.LENGTH_SHORT).show();
                } else {
                    Log.d("test", "LI login success");
                    session.setUserProfile(userLogin);
                    session.setLinkedInLogin();
                    session.createUserLoginSession();
                    if (userLogin.REGISTERSTATUS != null && userLogin.REGISTERSTATUS)
                        session.showHomeUsageScreen(true);

                    if (userLogin.PROFILESTATUS) {
                        startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                    } else {
                        startActivity(new Intent(LoginActivity.this, ProfileSetupActivity.class));
                    }
                    finish();
                }

            }

            @Override
            public void onError(String error) {
                Log.d("test", "error: " + error);
                liLoginDialog.dismiss();
                if (error == null || error.trim().equals(""))
                    error = "Server Error";
                Toast.makeText(getApplicationContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onSkipPressed() {
        // Do something when users tap on Skip button.
    }

    @Override
    public void onDonePressed() {
        // Do something when users tap on Done button.
    }

    @Override
    public void onSlideChanged() {
        // Do something when the slide changes.
    }

    @Override
    public void onNextPressed() {
        // Do something when users tap on Next button.
    }

    public void generateHashKey() {
        try {
            PackageInfo info = getPackageManager().getPackageInfo("com.trybe.trybe", PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());

                Log.d("KeyHash1", info.packageName);
                Log.d("LI2", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.d("LI", e.getMessage(), e);
        } catch (NoSuchAlgorithmException e) {
            Log.d("LI", e.getMessage(), e);
        }

    }
     FacebookCallback<LoginResult> callback = new FacebookCallback<LoginResult>() {
        @Override
        public void onSuccess(LoginResult loginResult) {
            // fb_login_inner_relative.setVisibility(View.GONE);
            //  loading_fb_login.setVisibility(View.VISIBLE);
            GraphRequest request = GraphRequest.newMeRequest(
                    loginResult.getAccessToken(),
                    new GraphRequest.GraphJSONObjectCallback() {
                        @Override
                        public void onCompleted(
                                JSONObject object,
                                GraphResponse response) {
                            object = response.getJSONObject();

                            try {
                                id = object.getString("id").toString();
                                try {
                                    image_value = new URL("http://graph.facebook.com/" + id + "/picture?type=large");
                                } catch (MalformedURLException e) {
                                    e.printStackTrace();
                                }
                                email_id = object.getString("email").toString();
                                gender = object.getString("gender").toString();
                                name = object.getString("name").toString();
                                work = object.getString("user_work_history").toString();
                                education = object.getString("user_education_history");
                                displayMessage(email_id, name, id, gender, image_value, work, education);


                                SharedPreferences sharedPreferences = getSharedPreferences("current_profile", Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString("id", id);
                                editor.putString("email_id", email_id);
                                editor.putString("gender", gender);
                                editor.putString("name", name);
                                editor.apply();
                                Log.e("id", id);
                                Log.e("email", email_id);
                                Log.e("gender", gender);
                                Log.e("name", name);


                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }
                    });
            Bundle parameters = new Bundle();
            parameters.putString("fields", "id,name,gender,email");
            request.setParameters(parameters);
            request.executeAsync();


            /**  Intent intent=new Intent(LoginActivity.this,Home.class);
             startActivity(intent); **/

        }

        @Override
        public void onCancel() {

        }

        @Override
        public void onError(FacebookException e) {

        }
    };

    public void displayMessage(final String email_id, final String name, final String id, final String gender, final URL image_value,final String work,final String education){



        RequestQueue request= Volley.newRequestQueue(getApplicationContext());
        StringRequest stringRequest=new StringRequest(Request.Method.POST, Config.Server.SERVER_LOGIN_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                //textView.setText(id);
                Intent intent=new Intent(LoginActivity.this,ProfileSetupActivity.class);
                startActivity(intent);

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                //textView.setText(email_id);
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                 String image=image_value.toString();
                params.put("UD_FB", email);
                params.put("UD_EMAIL", email);
                params.put("UD_NAME", name);
                params.put("UD_IMG", image);
                params.put("data_work",work);
                params.put("data_education",education);
                return params;
            }
        };

        request.add(stringRequest);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}