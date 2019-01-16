package com.trybe.trybe.helper;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.design.widget.NavigationView;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.squareup.picasso.Picasso;
import com.trybe.trybe.BuildConfig;
import com.trybe.trybe.ChangePassActivity;
import com.trybe.trybe.ChatMessageActivity;
import com.trybe.trybe.JobPref1Activity;
import com.trybe.trybe.LoginActivity;
import com.trybe.trybe.ProfileSetupActivity;
import com.trybe.trybe.R;
import com.trybe.trybe.ShareActivity;
import com.trybe.trybe.app.Config;
import com.trybe.trybe.dto.JobCatDTO;
import com.trybe.trybe.dto.JobLocDTO;
import com.trybe.trybe.gcm.NotificationUtils;
import com.trybe.trybe.model.Message;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import io.branch.referral.Branch;

/**
 * Created by gopi.ra on 18-Feb-16.
 */
public class Utils {

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    public interface VolleyRequestListener {

        void onResponse(String response);

        void onError(String error);
    }

    private static Gson gson;

    public static Gson getGsonInstance() {
        if (gson == null)
            gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
        return gson;
    }

    // converts name to asterisks
    public static String hideName(String name) {
        if (name == null)
            return "**** ******";

        String[] temp = name.split(" ", 0);
        int l1 = temp[0].length();
        int l2 = 0;
        if (temp.length > 1)
            l2 = temp[1].length();
        String hiddenName = "";
        for (int i = 0; i < l1; i++)
            hiddenName += "*";
        hiddenName += " ";
        for (int i = 0; i < l2; i++)
            hiddenName += "*";

        return hiddenName;
    }

    public static boolean checkPlayServices(Activity activity) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(activity);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(activity, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                Log.d("Play", "This device is not supported. Google Play Services not installed!");
                Toast.makeText(activity.getApplicationContext(), "This device is not supported. Google Play Services not installed!", Toast.LENGTH_LONG).show();
                activity.finish();
            }
            return false;
        }
        return true;
    }

    public static BroadcastReceiver getDefaultGCMBroadcastReceiver(final Context appContext) {
        return new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(Config.PUSH_NOTIFICATION)) {
                    // new push message is received
                    Message msg = (Message) intent.getSerializableExtra("data");
                    Intent resultIntent = new Intent(appContext, ChatMessageActivity.class);
                    resultIntent.putExtra(ChatMessageActivity.ARG_USER, msg.UD_ID);
                    resultIntent.putExtra(ChatMessageActivity.ARG_USER_TYPE, ChatMessageActivity.TYPE_REFER);

                    NotificationUtils notificationUtils = new NotificationUtils(context);
                    notificationUtils.showNotificationMessage(intent.getStringExtra("title"), msg.MESSAGE, msg.CREATE_DT, resultIntent);
                }
            }
        };
    }

    public static BroadcastReceiver getDefaultGCMBroadcastReceiver(final ImageButton chatBtn) {
        return new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(Config.PUSH_NOTIFICATION)) {
                    // new push message is received
                    notifyChat(chatBtn);
                }
            }
        };
    }

    public static void registerGCMReceiver(Activity activity, BroadcastReceiver registrationBroadcastReceiver) {
        LocalBroadcastManager.getInstance(activity).registerReceiver(registrationBroadcastReceiver,
                new IntentFilter(Config.PUSH_NOTIFICATION));
    }

    public static void unregisterGCMReceiver(Activity activity, BroadcastReceiver registrationBroadcastReceiver) {
        LocalBroadcastManager.getInstance(activity).unregisterReceiver(registrationBroadcastReceiver);
    }

    public static void checkUnreadMessages(ImageButton chatButton) {
        checkUnreadMessages(chatButton, R.drawable.chaticon);
    }

    public static void checkUnreadMessages(ImageButton chatButton, int resource) {
        ArrayList<String> uids = UserSessionManager.getInstance().getUnreadChatsUID();
        if (uids == null || uids.size() == 0)
            chatButton.setImageResource(resource);
        else
            notifyChat(chatButton);
    }

    public static void markMessageAsRead(String userId, ImageButton chatButton) {
        ArrayList<String> uids = UserSessionManager.getInstance().getUnreadChatsUID();
        if (uids == null || uids.size() == 0) {
            return;
        } else {
            uids.remove(userId);
            UserSessionManager.getInstance().setUnreadChatsUID(uids);
        }
    }

    public static void notifyChat(ImageButton chatButton) {
        chatButton.setImageResource(R.drawable.chaticon_unread);
    }

    public static String getDeviceId(Context context) {
        String devId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        Log.d("DEVICE_ID", devId);
        return (devId == null) ? "null" : devId;
    }

    public static String getVersionName() {
        return BuildConfig.VERSION_NAME;
    }

    public static void loadImageByPicasso(Context context, ImageView imageView, String imageUrl) {
        loadImageByPicasso(context, imageView, imageUrl, 200, 200);
    }

    public static void loadImageByPicasso(Context context, ImageView imageView, String imageUrl, int width, int height) {
        if (imageUrl == null || imageUrl.trim().equals(""))
            Picasso.with(context)
                    .load(R.drawable.anon_user)
                    .into(imageView);
        else
            Picasso.with(context)
                    .load(imageUrl)
                    .resize(width, height)
                    .centerInside()
                    .placeholder(R.drawable.anon_user)
                    .error(android.R.drawable.alert_light_frame)
                    .into(imageView);
    }

    public static File getImagesFolder() {
        File storageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "Trybe");
        if (!storageDir.exists()) {
            if (!storageDir.mkdirs()) {
                return null;
            }
        }
        return storageDir;
    }

    public static File getLastImage() {
        File dir = getImagesFolder();
        if (dir == null)
            return null;

        // This will return null in Android 6.0 if runtime permissions not granted
        File[] files = dir.listFiles();
        if (files == null || files.length == 0) {
            return null;
        }

        File lastModifiedFile = files[0];
        Log.d("test", lastModifiedFile.getAbsolutePath());
        for (int i = 1; i < files.length; i++) {
            if (lastModifiedFile.lastModified() < files[i].lastModified()) {
                lastModifiedFile = files[i];
            }
        }
        return lastModifiedFile;
    }

    public static Bitmap convertIntoBlackAndWhiteImage(Bitmap orginalBitmap) {
        Bitmap blackAndWhiteBitmap = orginalBitmap.copy(
                Bitmap.Config.ARGB_8888, true);

        ColorMatrix colorMatrix = new ColorMatrix();
        colorMatrix.setSaturation(0);
        ColorMatrixColorFilter colorMatrixFilter = new ColorMatrixColorFilter(
                colorMatrix);
        Paint paint = new Paint();
        paint.setColorFilter(colorMatrixFilter);
        Canvas canvas = new Canvas(blackAndWhiteBitmap);
        canvas.drawBitmap(blackAndWhiteBitmap, 0, 0, paint);

        return blackAndWhiteBitmap;
    }

    public static void copyFile(File source, File target) {
        try {
            InputStream in = new FileInputStream(source);
            OutputStream out = new FileOutputStream(target);

            // Copy the bits from instream to outstream
            byte[] buf = new byte[4 * 1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("IO", "File copy error");
        }
    }

    public static void deleteFile(File file) {
        if (!file.exists())
            return;
        file.delete();
    }

    public static class NavigationItemSelectedListenerClass implements NavigationView.OnNavigationItemSelectedListener {

        Activity activity;
        UserSessionManager session;
        DrawerLayout drawer;

        public NavigationItemSelectedListenerClass(Activity activity, UserSessionManager session, DrawerLayout drawer) {
            this.activity = activity;
            this.session = session;
            this.drawer = drawer;
        }

        @Override
        public boolean onNavigationItemSelected(MenuItem item) {
            // Handle navigation view item clicks here.
            int id = item.getItemId();

            if (id == R.id.nav_profile) {
                Intent i = new Intent(activity.getApplicationContext(), ProfileSetupActivity.class);
                activity.startActivity(i);
            } else if (id == R.id.nav_pref) {
                Intent i = new Intent(activity.getApplicationContext(), JobPref1Activity.class);
                activity.startActivity(i);
            } else if (id == R.id.nav_change_pass) {
                Intent i = new Intent(activity.getApplicationContext(), ChangePassActivity.class);
                activity.startActivity(i);
            } else if (id == R.id.nav_logout) {
                Branch branch=Branch.getInstance();
                branch.logout();
                FacebookSdk.sdkInitialize(activity.getApplicationContext());
                LoginManager.getInstance().logOut();
                Map<String, String> params = new HashMap<String, String>();
                UserSessionManager sess = UserSessionManager.getInstance();
                params.put("UD_ID", sess.getUserProfile().user_def.UD_ID);
                params.put("UC_TOKEN", sess.getLoginToken());
                Utils.sendVolleyJsonRequest(activity.getApplicationContext(), Config.Server.SERVER_LOGOUT_URL, params, new VolleyRequestListener() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("Logout", "Response: " + response);
                    }

                    @Override
                    public void onError(String error) {
                        Log.d("Logout", "error: " + error);
                    }
                });
                Intent i = new Intent(activity.getApplicationContext(), LoginActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                session.logoutUser();
                activity.startActivity(i);
                activity.finish();
            } else if (id == R.id.nav_share) {
                Intent shareIntent = new Intent(activity.getApplicationContext(), ShareActivity.class);
                activity.startActivity(shareIntent);
            }

            drawer.closeDrawer(GravityCompat.START);
            return true;
        }
    }

    public static void fetchJobCategories(Context context) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("ACT", "4");
        params.put("DATA", "job_categories");
        Utils.sendVolleyJsonRequest(context, false, Config.Server.SERVER_REQUESTS_URL, params, new VolleyRequestListener() {
            @Override
            public void onResponse(String response) {
                Log.d("JOB_CAT_FETCH", "response: " + response);
                JobCatDTO jobCats = null;
                try {
                    jobCats = getGsonInstance().fromJson(response, JobCatDTO.class);
                } catch (JsonSyntaxException e) {
                    Log.d("JOB_CAT_FETCH", "json err: " + e.getLocalizedMessage());
                }
                if (jobCats == null || jobCats.RESULTS.size() == 0) {
                    Log.d("JOB_CAT_FETCH", "empty response from server");
                } else {
                    UserSessionManager.getInstance().setJobCategories(jobCats);
                }
            }

            @Override
            public void onError(String error) {
                Log.d("JOB_CAT_FETCH", "error: " + error);
            }
        });

        Map<String, String> paramsLoc = new HashMap<String, String>();
        paramsLoc.put("ACT", "4");
        paramsLoc.put("DATA", "locations");
        Utils.sendVolleyJsonRequest(context, false, Config.Server.SERVER_REQUESTS_URL, paramsLoc, new VolleyRequestListener() {
            @Override
            public void onResponse(String response) {
                Log.d("JOB_LOC_FETCH", "response: " + response);
                JobLocDTO jobLocs = null;
                try {
                    jobLocs = getGsonInstance().fromJson(response, JobLocDTO.class);
                } catch (JsonSyntaxException e) {
                    Log.d("JOB_LOC_FETCH", "json err: " + e.getLocalizedMessage());
                }
                if (jobLocs == null || jobLocs.RESULTS.size() == 0) {
                    Log.d("JOB_LOC_FETCH", "empty response from server");
                } else {
                    UserSessionManager.getInstance().setJobLocations(jobLocs);
                }
            }

            @Override
            public void onError(String error) {
                Log.d("JOB_CAT_FETCH", "error: " + error);
            }
        });
    }

    public static void sendVolleyJsonRequest(final Context context, boolean addToken, String url, final Map<String,
            String> postParams, int timeout, int numRetries, final VolleyRequestListener listener) {
        if (addToken) {
            postParams.put("UC_TOKEN", UserSessionManager.getInstance().getLoginToken());
        }
        Log.d("MyVolley", postParams.toString());

        StringRequest volleyRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // successful response from server, check for success or failure
                        Log.d("MyVolley", "success");
                        if (listener != null) {
                            if (response != null)
                                listener.onResponse(response.trim());
                            else
                                listener.onResponse(null);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // volley error
                        String errMessage = error.getLocalizedMessage();
                        Log.d("MyVolley", "error");
                        if (listener != null) {
                            if (errMessage != null)
                                listener.onError(errMessage);
                            else
                                listener.onError("Network Error (Null Specified)");
                        }
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                return postParams;
            }
        };

        Log.d("MyVolley", "sending volley req to " + url);
        volleyRequest.setRetryPolicy(new DefaultRetryPolicy(
                timeout,
                numRetries,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        MyVolley.getInstance(context).addToRequestQueue(volleyRequest);
    }

    public static void sendVolleyJsonRequest(final Context context, String url, final Map<String,
            String> postParams, int timeout, int numRetries, final VolleyRequestListener listener) {
        sendVolleyJsonRequest(context, true, url, postParams, timeout, numRetries, listener);
    }

    public static void sendVolleyJsonRequest(final Context context, String url, final Map<String, String> postParams, int numRetries, final VolleyRequestListener listener) {
        sendVolleyJsonRequest(context, url, postParams, DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 2, numRetries, listener);
    }

    public static void sendVolleyJsonRequest(final Context context, String url, final Map<String, String> postParams, final VolleyRequestListener listener) {
        sendVolleyJsonRequest(context, url, postParams, DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 2, 1, listener);
    }

    public static void sendVolleyJsonRequest(final Context context, boolean addToken, String url, final Map<String, String> postParams, final VolleyRequestListener listener) {
        sendVolleyJsonRequest(context, addToken, url, postParams, DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 2, 1, listener);
    }

    public static String getPath(final Context context, final Uri uri) {

        // DocumentProvider
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }
}