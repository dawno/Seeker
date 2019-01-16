package com.trybe.trybe.gcm;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Html;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.trybe.trybe.ChatActivity;
import com.trybe.trybe.ChatMessageActivity;
import com.trybe.trybe.HomeActivity;
import com.trybe.trybe.app.Config;
import com.trybe.trybe.helper.UserSessionManager;
import com.trybe.trybe.helper.Utils;
import com.trybe.trybe.model.Message;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Gopi on 13-Apr-16.
 */
public class GcmPushReceiver extends GcmListenerService {

    private static final String TAG = GcmPushReceiver.class.getSimpleName();

    private NotificationUtils notificationUtils;

    /**
     * Called when message is received.
     *
     * @param from   SenderID of the sender.
     * @param bundle Data bundle containing message data as key/value pairs.
     *               For Set of keys use data.keySet().
     */
    @Override
    public void onMessageReceived(String from, Bundle bundle) {
        Log.d(TAG, bundle.toString());
        String title = bundle.getString("title");
        String flag = bundle.getString("flag");
        String data = bundle.getString("data");
        Log.d(TAG, "From: " + from);
        Log.d(TAG, "Title: " + title);
        Log.d(TAG, "flag: " + flag);
        Log.d(TAG, "data: " + data);

        if (flag == null) {
            Log.d(TAG, "flag not set");
            return;
        }

        if (flag.equals(Config.PUSH_TYPE_CHAT)) {
            if (!UserSessionManager.getInstance().isLoggedIn()) {
                Log.d(TAG, "user not logged in");
                return;
            }
            handleChatMessage(title, data);
        } else if (flag.equals(Config.PUSH_TYPE_NOTIF)) {
            handleNotification(flag, title, data);
        } else if (flag.equals(Config.PUSH_TYPE_UPDATE)) {
            handleNotification(flag, title, data);
        } else if (flag.equals(Config.PUSH_TYPE_MATCH)) {
            if (!UserSessionManager.getInstance().isLoggedIn()) {
                Log.d(TAG, "user not logged in");
                return;
            }
            handleMatchMessage(title, data);
        } else {
            Log.d(TAG, "Unknown push flag: " + flag);
        }
    }

    private void handleMatchMessage(String title, String data) {
        if (data == null)
            return;

        if (!NotificationUtils.isAppIsInBackground(getApplicationContext())) {
            // app is in foreground, broadcast the push message
            Log.d(TAG, "app in foreground");
            Intent pushNotification = new Intent(Config.PUSH_NOTIFICATION);
            pushNotification.putExtra("data", data);
            pushNotification.putExtra("title", title);
            LocalBroadcastManager.getInstance(this).sendBroadcast(pushNotification);
        }

        Intent resultIntent = new Intent(getApplicationContext(), ChatActivity.class);
        String ts = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        showNotificationMessage(getApplicationContext(), title, data, ts, resultIntent);

    }

    private void handleNotification(String flag, String title, String data) {
        if (data == null)
            return;

        Intent resultIntent = new Intent(getApplicationContext(), HomeActivity.class);
        resultIntent.putExtra(HomeActivity.NOTIFICATION_FLAG, flag);
        resultIntent.putExtra(HomeActivity.APP_NOTIFICATION_TITLE, title);
        resultIntent.putExtra(HomeActivity.APP_NOTIFICATION, data);

        String ts = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        showNotificationMessage(getApplicationContext(), title, Html.fromHtml(data).toString(), ts, resultIntent);
    }

    private void handleChatMessage(String title, String dataJson) {
        Gson gson = Utils.getGsonInstance();
        Message msg = null;
        try {
            msg = gson.fromJson(dataJson, Message.class);
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
        }
        if (msg == null)
            return;

        ArrayList<String> uids = UserSessionManager.getInstance().getUnreadChatsUID();
        if (!uids.contains(msg.UD_ID)) {
            uids.add(msg.UD_ID);
            UserSessionManager.getInstance().setUnreadChatsUID(uids);
        }

        if (!NotificationUtils.isAppIsInBackground(getApplicationContext())) {
            // app is in foreground, broadcast the push message
            Log.d(TAG, "app in foreground");
            Intent pushNotification = new Intent(Config.PUSH_NOTIFICATION);
            pushNotification.putExtra("data", msg);
            pushNotification.putExtra("title", title);
            LocalBroadcastManager.getInstance(this).sendBroadcast(pushNotification);

            // play notification sound
            NotificationUtils notificationUtils = new NotificationUtils();
            notificationUtils.playNotificationSound();
        } else {
            Log.d(TAG, "app in background");
            Intent resultIntent = new Intent(getApplicationContext(), ChatMessageActivity.class);
            resultIntent.putExtra(ChatMessageActivity.ARG_USER, msg.UD_ID);
            //resultIntent.putExtra(ChatMessageActivity.ARG_USER_TYPE, ChatMessageActivity.TYPE_REFER);

            showNotificationMessage(getApplicationContext(), title, msg.MESSAGE, msg.CREATE_DT, resultIntent);
        }
    }

    /**
     * Showing notification with text only
     */
    private void showNotificationMessage(Context context, String title, String message, String timeStamp, Intent intent) {
        notificationUtils = new NotificationUtils(context);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        notificationUtils.showNotificationMessage(title, message, timeStamp, intent);
    }

    /**
     * Showing notification with text and image
     */
    private void showNotificationMessageWithBigImage(Context context, String title, String message, String timeStamp, Intent intent, String imageUrl) {
        notificationUtils = new NotificationUtils(context);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        notificationUtils.showNotificationMessage(title, message, timeStamp, intent, imageUrl);
    }
}
