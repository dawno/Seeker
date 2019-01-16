package com.trybe.trybe;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
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
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.trybe.trybe.app.BaseApplication;
import com.trybe.trybe.app.Config;
import com.trybe.trybe.dto.LoginDTO;
import com.trybe.trybe.dto.RegisterDTO;
import com.trybe.trybe.helper.UserSessionManager;
import com.trybe.trybe.helper.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Gopi on 29-Mar-16.
 */
public class LoginFragment1 extends Fragment {

    private EditText mName, mEmail, mPhone, mPassword, mConfirmPassword, mFPauth, mFPnew, mFPconfirm;
    private TextView tvTerms;
    private Button mSignIn, mSignUp, mForgotPass;
    private ImageButton li_login, mFPsubmit;
    private LinearLayout landingScrollContainer, mForgotPassLayout;
    private UserSessionManager session;
    private Gson gson;
    private MaterialDialog progressDialog;
    private String storedEmail = "";

    URL image_value;
    String name,email_id,id,gender,email,work,education;
    public CallbackManager mCallbackManager;
    public FacebookCallback<LoginResult> mCallBack=new FacebookCallback<LoginResult>() {

        @Override
        public void onSuccess(LoginResult loginResult) {
            Log.e("Fb_Success","Fb_Success");
            /*AccessToken accessToken=loginResult.getAccessToken();
            Profile profile= Profile.getCurrentProfile();
            if(profile!=null){
                Log.e("RESUKT", profile.toString());

                // t.setText("Welcome " + profile.toString());

                GraphRequest request = GraphRequest.newMeRequest(
                        accessToken,
                        new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(
                                    JSONObject object,
                                    GraphResponse response) {
                                // Application code
                                Log.e("RESUKT", response.toString());
                            }
                        });
                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,name,link,work,education");
                request.setParameters(parameters);
                request.executeAsync();
            }*/

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
                                //work = object.getString("user_work_history").toString();
                                //education = object.getString("user_education_history");
                                sendFacebookRequest(id,name,image_value,gender,email_id);
                                SharedPreferences sharedPreferences = getActivity().getSharedPreferences("current_profile", Context.MODE_PRIVATE);
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

        }

        @Override
        public void onCancel() {
            Log.e("Fb_Cancel","Fb_Cancel");
        }

        @Override
        public void onError(FacebookException error) {
            Log.e("Fb_Error","Fb_Error");
            }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getActivity().getApplicationContext());
        mCallbackManager = CallbackManager.Factory.create();
    }



    public LoginFragment1() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.frag_login1, container, false);

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        mName = (EditText) rootView.findViewById(R.id.name);
        mEmail = (EditText) rootView.findViewById(R.id.email);
        mPhone = (EditText) rootView.findViewById(R.id.phone);
        mPassword = (EditText) rootView.findViewById(R.id.password);
        mConfirmPassword = (EditText) rootView.findViewById(R.id.confirmPassword);
        mPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        li_login = (ImageButton) rootView.findViewById(R.id.li_sign_in);
        li_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                login_linkedin();
            }
        });

        mSignIn = (Button) rootView.findViewById(R.id.email_sign_in_button);
        mSignUp = (Button) rootView.findViewById(R.id.email_sign_up_button);
        mSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mConfirmPassword.getVisibility() == View.VISIBLE) {
                    mConfirmPassword.setVisibility(View.GONE);
                    mName.setVisibility(View.GONE);
                    mPhone.setVisibility(View.GONE);
                }
                attemptLogin();
            }
        });
        mSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mConfirmPassword.getVisibility() == View.VISIBLE) {
                    attemptRegister();
                } else {
                    mName.setVisibility(View.VISIBLE);
                    mConfirmPassword.setVisibility(View.VISIBLE);
                    mPhone.setVisibility(View.VISIBLE);
                    mName.requestFocus();
                }
            }
        });

        mFPauth = (EditText) rootView.findViewById(R.id.authCode);
        mFPnew = (EditText) rootView.findViewById(R.id.newPass);
        mFPconfirm = (EditText) rootView.findViewById(R.id.confirmPass);
        mFPsubmit = (ImageButton) rootView.findViewById(R.id.forgotPassSubmit);
        mForgotPassLayout = (LinearLayout) rootView.findViewById(R.id.forgot_pass_layout);
        mForgotPass = (Button) rootView.findViewById(R.id.forgot_pass_btn);
        mForgotPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleForgotPass();
            }
        });

        landingScrollContainer = (LinearLayout) rootView.findViewById(R.id.scrollContainer);
        final int childCount = landingScrollContainer.getChildCount();
        ImageView lastImage = (ImageView) landingScrollContainer.getChildAt(childCount - 1);
        File lastImageFile = Utils.getLastImage();

        if (lastImageFile != null) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            options.inJustDecodeBounds = false;
            options.inSampleSize = 2;
            Bitmap bitmap = BitmapFactory.decodeFile(lastImageFile.getAbsolutePath(), options);
            Bitmap bw = Utils.convertIntoBlackAndWhiteImage(bitmap);
            lastImage.setImageBitmap(bw);
        } else
            landingScrollContainer.removeView(lastImage);

        landingScrollContainer.post(new Runnable() {
            public void run() {
                int childCount = landingScrollContainer.getChildCount();
                ObjectAnimator animator = ObjectAnimator.ofInt(landingScrollContainer, "scrollX", landingScrollContainer.getChildAt(childCount - 1).getLeft());
                animator.setStartDelay(100);
                animator.setDuration(2500);
                animator.start();
            }
        });

        tvTerms = (TextView) rootView.findViewById(R.id.tvTerms);
        String termsText = "By signing in, you agree to our <a href='" + Config.Server.T_AND_C_URL +
                "'><font color='#eb881d'>Terms and Conditions</font></a> " +
                "and our <a href='" + Config.Server.PRIVACY_POLICY_URL +
                "'><font color='#eb881d'>Privacy Policy</font></a>";
        tvTerms.setText(Html.fromHtml(termsText));
        tvTerms.setClickable(true);
        tvTerms.setMovementMethod(LinkMovementMethod.getInstance());

        session = UserSessionManager.getInstance();
        gson = Utils.getGsonInstance();
        progressDialog = new MaterialDialog.Builder(getActivity())
                .title("Logging in")
                .content("Please wait")
                .progress(true, 0)
                .cancelable(false)
                .build();


        return rootView;
    }
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        LoginButton loginbutton = (LoginButton) view.findViewById(R.id.login_button);
        loginbutton.setReadPermissions("public_profile","email","user_education_history","user_work_history");
        loginbutton.setFragment(this);
        loginbutton.registerCallback(mCallbackManager,mCallBack);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
        Log.e("Frag_onActivityResult","Frag_onActivityResult");
    }

    public void sendFacebookRequest(String id, String name, URL userImg,String gender,String email) {
        final MaterialDialog fbLoginDialog = new MaterialDialog.Builder(getActivity())
                .title("Logging in")
                .content("Please wait")
                .progress(true, 0)
                .cancelable(false)
                .show();

        Map<String, String> params = new HashMap<String, String>();
        params.put("UD_FB", id);
        params.put("UD_NAME", name);
        params.put("UD_IMG", userImg.toString());
        params.put("UD_GENDER", gender);
        params.put("UD_EMAIL", email);

        Utils.sendVolleyJsonRequest(getActivity(), false, Config.Server.SERVER_LOGIN_URL, params, new Utils.VolleyRequestListener() {
            @Override
            public void onResponse(String response) {
                Log.d("test", "response: " + response);
                fbLoginDialog.dismiss();

                LoginDTO userLogin = null;
                try {
                    userLogin = gson.fromJson(response, LoginDTO.class);
                } catch (JsonSyntaxException e) {
                    Log.d("test", "json err: " + e.getLocalizedMessage());
                }

                if (userLogin == null) {
                    Log.d("test", "userLogin is null");
                    Toast.makeText(getActivity().getApplicationContext(), "Server Error", Toast.LENGTH_SHORT).show();
                } else {
                    Log.d("test", "FB login success");
                    session.setUserProfile(userLogin);
                    session.setLinkedInLogin();
                    session.createUserLoginSession();
                    if (userLogin.REGISTERSTATUS != null && userLogin.REGISTERSTATUS)
                        session.showHomeUsageScreen(true);

                    if (userLogin.PROFILESTATUS) {
                        startActivity(new Intent(getActivity(), HomeActivity.class));
                    } else {
                        startActivity(new Intent(getActivity(), ProfileSetupActivity.class));
                    }
                    getActivity().finish();
                }

            }

            @Override
            public void onError(String error) {
                Log.d("test", "error: " + error);
                fbLoginDialog.dismiss();
                if (error == null || error.trim().equals(""))
                    error = "Server Error";
                    Toast.makeText(getActivity().getApplicationContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
    }



    // returns false to prevent app close
    public boolean onBackPressed() {
        if (mForgotPassLayout == null)
            return true;

        if (mForgotPassLayout.getVisibility() == View.VISIBLE) {
            mForgotPassLayout.setVisibility(View.GONE);
            return false;
        } else
            return true;
    }

    public void generateHashKey() {
        try {
            PackageInfo info = getActivity().getPackageManager().getPackageInfo("com.trybe.trybe", PackageManager.GET_SIGNATURES);
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

    public void login_linkedin() {
        LoginActivity parent = (LoginActivity) getActivity();
        parent.loginLinkedIn();
    }

    private void attemptLogin() {
        mEmail.setError(null);
        mPassword.setError(null);

        String email = mEmail.getText().toString().trim();
        String password = mPassword.getText().toString().trim();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password)) {
            mPassword.setError("Password cannot be empty");
            focusView = mPassword;
            cancel = true;
        } else if (!isPasswordValid(password)) {
            mPassword.setError(getString(R.string.error_invalid_password));
            focusView = mPassword;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmail.setError(getString(R.string.error_field_required));
            focusView = mEmail;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmail.setError(getString(R.string.error_invalid_email));
            focusView = mEmail;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            progressDialog.show();
            sendLoginRequest(email, password);

            // skip login
//            session.createUserLoginSession("Gopi Ramena", email, null);
//            LoginDTO user = new LoginDTO();
//            user.user_def.UD_NAME = "Gopi Ramena";
//            user.user_def.UD_EMAIL = email;
//            user.user_def.UD_IMG = "https://lh4.googleusercontent.com/-ACk9b_sI7Mg/AAAAAAAAAAI/AAAAAAAAAAA/AMW9IgeSF2SBcxzBXr-41_KuqaTcdqXHIw/s96-c-mo/photo.jpg";
//            session.setUserProfile(user);
//            startActivity(new Intent(getApplicationContext(), ProfileSetupActivity.class));
//            finish();
        }
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@") && email.contains(".");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return true;
    }

    private void sendLoginRequest(String email, String password) {
        // TODO final String encryptedPass = md5(password);
        final String encryptedPass = (password);
        Log.d("test", "attempting login: with " + email + " : " + password + " : " + encryptedPass);

        Map<String, String> params = new HashMap<String, String>();
        params.put("UD_EMAIL", email);
        params.put("UD_PASSWORD", encryptedPass);
        params.put("UC_DEVICE_ID", Utils.getDeviceId(getActivity()));
        Utils.sendVolleyJsonRequest(getActivity(), false, Config.Server.SERVER_LOGIN_URL, params, new Utils.VolleyRequestListener() {
            @Override
            public void onResponse(String response) {
                Log.d("test", "response: " + response);
                progressDialog.dismiss();

                LoginDTO userLogin = null;
                try {
                    userLogin = gson.fromJson(response, LoginDTO.class);
                } catch (JsonSyntaxException e) {
                    Log.d("test", "json err: " + e.getLocalizedMessage());
                }

                if (userLogin == null) {
                    Log.d("test", "userLogin is null");
                    Toast.makeText(getActivity(), "Server Error", Toast.LENGTH_SHORT).show();
                } else {
                    if (!userLogin.LOGINSTATUS) {
                        mPassword.setError(getString(R.string.error_incorrect_password));
                        mPassword.requestFocus();
                    } else {
                        session.setUserProfile(userLogin);
                        session.createUserLoginSession();
                        if (userLogin.PROFILESTATUS) {
                            startActivity(new Intent(getActivity(), HomeActivity.class));
                        } else {
                            startActivity(new Intent(getActivity(), ProfileSetupActivity.class));
                        }
                        getActivity().finish();
                    }
                }

            }

            @Override
            public void onError(String error) {
                progressDialog.dismiss();
                Log.d("test", "error: " + error);
                Toast.makeText(getActivity(), error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void attemptRegister() {
        mName.setError(null);
        mEmail.setError(null);
        mPhone.setError(null);
        mPassword.setError(null);
        mConfirmPassword.setError(null);

        String name = mName.getText().toString().trim();
        String email = mEmail.getText().toString().trim();
        String phone = mPhone.getText().toString().trim();
        String password = mPassword.getText().toString().trim();
        String confirmPass = mConfirmPassword.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (!password.equals(confirmPass)) {
            mConfirmPassword.setError("Passwords do not match");
            focusView = mConfirmPassword;
            cancel = true;
        }
        if (TextUtils.isEmpty(password)) {
            mPassword.setError("Password cannot be empty");
            focusView = mPassword;
            cancel = true;
        } else if (!isPasswordValid(password)) {
            mPassword.setError(getString(R.string.error_invalid_password));
            focusView = mPassword;
            cancel = true;
        }
        if (TextUtils.isEmpty(email)) {
            mEmail.setError(getString(R.string.error_field_required));
            focusView = mEmail;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmail.setError(getString(R.string.error_invalid_email));
            focusView = mEmail;
            cancel = true;
        }
        if (TextUtils.isEmpty(phone)) {
            mPhone.setError("Phone number cannot be empty");
            focusView = mPhone;
            cancel = true;
        }
        if (TextUtils.isEmpty(name)) {
            mName.setError("Name cannot be empty");
            focusView = mName;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            progressDialog.show();
            sendRegisterRequest(name, phone, email, password);
        }
    }

    private void sendRegisterRequest(String name, String phone, String email, String password) {
        // TODO final String encryptedPass = md5(password);
        final String encryptedPass = (password);
        Log.d("test", "attempting register");
        final RegisterDTO reg = new RegisterDTO();
        reg.USER.UD_NAME = name;
        reg.USER.UD_PHONE = phone;
        reg.USER.UD_EMAIL = email;
        reg.USER.UD_PASSWORD = encryptedPass;

        Map<String, String> params = new HashMap<String, String>();
        params.put("REGISTER", gson.toJson(reg));
        params.put("UC_DEVICE_ID", Utils.getDeviceId(getActivity()));
        Utils.sendVolleyJsonRequest(getActivity(), false, Config.Server.SERVER_REGISTER_URL, params, new Utils.VolleyRequestListener() {
            @Override
            public void onResponse(String response) {
                Log.d("test", "response: " + response);
                progressDialog.dismiss();

                JSONObject resultObj;
                try {
                    resultObj = new JSONObject(response);
                    if (resultObj.getBoolean("USER_DEF")) {
                        int userId = resultObj.getInt("USER_ID");
                        String userToken = resultObj.getString("UC_TOKEN");
                        Log.d("test register", userId + "");

                        LoginDTO newUser = getNewlyRegUser(Integer.toString(userId), reg.USER.UD_NAME, reg.USER.UD_EMAIL, reg.USER.UD_PHONE);
                        newUser.UC_TOKEN = userToken;
                        session.setUserProfile(newUser);
                        session.createUserLoginSession();
                        session.showHomeUsageScreen(true);
                        startActivity(new Intent(getActivity(), ProfileSetupActivity.class));
                        getActivity().finish();
                    } else {
                        mEmail.setError("Email already exists");
                        mEmail.requestFocus();
                        Toast.makeText(getActivity(), "User already exists", Toast.LENGTH_SHORT).show();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getActivity(), "Server Error", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(String error) {
                Log.d("test", "error: " + error);
                progressDialog.dismiss();
                if (error == null || error.trim().equals(""))
                    error = "Server Error";
                Toast.makeText(getActivity(), error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private LoginDTO getNewlyRegUser(String id, String name, String email, String phone) {
        LoginDTO user = new LoginDTO();
        user.LOGINSTATUS = true;
        user.PROFILESTATUS = false;
        user.PREFERENCE_STATUS = false;
        user.user_def.UD_ID = id;
        user.user_def.UD_NAME = name;
        user.user_def.UD_EMAIL = email;
        user.user_def.UD_PHONE = phone;

        return user;
    }

    private void handleForgotPass() {
        new MaterialDialog.Builder(getActivity())
                .title("Enter your Email ID")
                .input("Email ID", storedEmail, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(MaterialDialog dialog, CharSequence input) {
                        String forgottenEmail = input.toString();
                        if (!isEmailValid(forgottenEmail)) {
                            Toast.makeText(getActivity(), "Invalid email", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        storedEmail = forgottenEmail;
                        callSendMailApi(forgottenEmail);
                        mForgotPassLayout.setVisibility(View.VISIBLE);
                        handleForgotPassSubmit(forgottenEmail);
                    }
                })
                .positiveText("Send Mail")
                .negativeText("I have Auth Code")
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(MaterialDialog dialog, DialogAction which) {
                        String inputMail = dialog.getInputEditText().getText().toString();
                        if (!isEmailValid(inputMail)) {
                            Toast.makeText(getActivity(), "Invalid email", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        storedEmail = inputMail;
                        mForgotPassLayout.setVisibility(View.VISIBLE);
                        handleForgotPassSubmit(inputMail);
                    }
                })
                .show();
    }

    private void callSendMailApi(String email) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("UD_EMAIL", email);
        params.put("TYPE", "FORGOT");
        Utils.sendVolleyJsonRequest(getActivity(), false, Config.Server.SERVER_PASS_RESET, params, new Utils.VolleyRequestListener() {
            @Override
            public void onResponse(String response) {
                Log.d("PassReset", "response: " + response);
            }

            @Override
            public void onError(String error) {
                Log.d("PassReset", "error: " + error);
            }
        });
    }

    private void handleForgotPassSubmit(final String email) {

        mFPsubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String auth = mFPauth.getText().toString();
                String pass = mFPnew.getText().toString();
                String confirmPass = mFPconfirm.getText().toString();
                if (!pass.equals(confirmPass)) {
                    Toast.makeText(getActivity(), "Passwords do not match!", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("UD_EMAIL", email);
                    params.put("AUTH_CODE", auth);
                    params.put("PASSWORD_NEW", pass);
                    Utils.sendVolleyJsonRequest(getActivity(), false, Config.Server.SERVER_PASS_RESET, params, new Utils.VolleyRequestListener() {
                        @Override
                        public void onResponse(String response) {
                            Log.d("PassReset", "response: " + response);

                            try {
                                JSONObject json = new JSONObject(response);
                                if (json.getBoolean("STATUS")) {
                                    // success
                                    Toast.makeText(BaseApplication.getInstance(), "Password Change Successful. Login using the new password", Toast.LENGTH_SHORT).show();
                                    mForgotPassLayout.setVisibility(View.GONE);
                                } else {
                                    Toast.makeText(getActivity(), "Incorrect authentication code", Toast.LENGTH_SHORT).show();
                                }

                            } catch (Exception e) {
                                Log.d("PassReset", "json err: " + e.getLocalizedMessage());
                                Toast.makeText(getActivity(), "Server Error", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onError(String error) {
                            Log.d("PassReset", "error: " + error);
                            Toast.makeText(getActivity(), "Network Error", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    private static String md5(String s) {
        try {
            // Create MD5 Hash
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuilder hexString = new StringBuilder();
            for (byte aMessageDigest : messageDigest) {
                String h = Integer.toHexString(0xFF & aMessageDigest);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

}