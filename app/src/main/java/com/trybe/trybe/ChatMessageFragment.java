package com.trybe.trybe;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.trybe.trybe.adapter.ChatThreadAdapter;
import com.trybe.trybe.app.Config;
import com.trybe.trybe.dto.LoginDTO;
import com.trybe.trybe.gcm.NotificationUtils;
import com.trybe.trybe.helper.Utils;
import com.trybe.trybe.model.Message;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatMessageFragment extends Fragment {

    private static final String ARG_PARAM1 = "messageList";
    private static final String ARG_PARAM2 = "currentSenderUser";
    private static final String ARG_PARAM3 = "otherReceiverUser";
    private LoginDTO currentUser, otherUser;
    private Gson gson;
    private ArrayList<Message> messageArrayList;
    private BroadcastReceiver mRegistrationBroadcastReceiver;

    private RecyclerView recyclerView;
    private ChatThreadAdapter mAdapter;
    private EditText inputMessage;
    private Button btnSend;

    public ChatMessageFragment() {
    }

    public static ChatMessageFragment newInstance(LoginDTO currentUser, LoginDTO otherUser, List<Message> messageList) {
        ChatMessageFragment fragment = new ChatMessageFragment();
        Gson gson = Utils.getGsonInstance();

        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, gson.toJson(messageList));
        args.putString(ARG_PARAM2, gson.toJson(currentUser));
        args.putString(ARG_PARAM3, gson.toJson(otherUser));
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        gson = Utils.getGsonInstance();
        if (getArguments() != null) {
            String json = getArguments().getString(ARG_PARAM1);
            Type listType = new TypeToken<ArrayList<Message>>() {
            }.getType();
            messageArrayList = gson.fromJson(json, listType);
            currentUser = gson.fromJson(getArguments().getString(ARG_PARAM2), LoginDTO.class);
            otherUser = gson.fromJson(getArguments().getString(ARG_PARAM3), LoginDTO.class);
        }

        /*
        Message message = new Message("29", "Check", "2016-04-09 10:12:34");
        Message message1 = new Message("9999", "Hey! The app looks good", "2016-04-12 10:12:34");
        Message message2 = new Message("29", "Yes, it is!", "2016-04-13 20:12:34");
        Message message3 = new Message("29", "Took all of my time though :(", "2016-04-13 20:13:34");
        Message message4 = new Message("9999", "No worries. We will compensate you more :)", "2016-04-14 10:13:34");
        Message message5 = new Message("29", ":P", "2016-04-14 11:13:34");
        Message message6 = new Message("29",
                "test long message. blah blah blah blah blah blah blah blah blah blahblah", "2016-04-14 11:15:34");
        Message message7 = new Message("9999",
                "test long message from other user. blah blah blah blah blah blah blah blah blah blahblah", "2016-04-14 11:25:34");
        messageArrayList = new ArrayList<>();
        messageArrayList.add(message);
        messageArrayList.add(message1);
        messageArrayList.add(message2);
        messageArrayList.add(message3);
        messageArrayList.add(message4);
        messageArrayList.add(message5);
        messageArrayList.add(message6);
        messageArrayList.add(message7);*/
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.frag_chat_message, container, false);
        recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        inputMessage = (EditText) rootView.findViewById(R.id.message);
        btnSend = (Button) rootView.findViewById(R.id.btn_send);

        if (messageArrayList == null)
            messageArrayList = new ArrayList<>();
        mAdapter = new ChatThreadAdapter(getActivity(), messageArrayList,
                currentUser.user_def.UD_ID, currentUser.user_def.UD_NAME, otherUser.user_def.UD_NAME);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);
        if (mAdapter.getItemCount() > 1)
            recyclerView.getLayoutManager().scrollToPosition(mAdapter.getItemCount() - 1);

        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(Config.PUSH_NOTIFICATION)) {
                    // new push message is received
                    handlePushNotification(intent);
                }
            }
        };

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        Utils.registerGCMReceiver(getActivity(), mRegistrationBroadcastReceiver);
        NotificationUtils.clearNotifications();
    }

    @Override
    public void onPause() {
        Utils.unregisterGCMReceiver(getActivity(), mRegistrationBroadcastReceiver);
        super.onPause();
    }

    /**
     * Handling new push message, will add the message to
     * recycler view and scroll it to bottom
     */
    private void handlePushNotification(Intent intent) {
        Message message = (Message) intent.getSerializableExtra("data");
        if (message != null) {
            messageArrayList.add(message);
            mAdapter.notifyDataSetChanged();
            if (mAdapter.getItemCount() > 1) {
                recyclerView.getLayoutManager().smoothScrollToPosition(recyclerView, null, mAdapter.getItemCount() - 1);
            }
        }
    }

    /**
     * Posting a new message in chat room
     * will make an http call to our server. Our server again sends the message
     * to the other user device as push notification
     */
    private void sendMessage() {
        final String message = inputMessage.getText().toString().trim();
        if (TextUtils.isEmpty(message)) {
            Toast.makeText(getActivity(), "Enter a message", Toast.LENGTH_SHORT).show();
            return;
        }

        // inputMessage.setEnabled(false);
        final String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        inputMessage.setText("");
        final Message msg = new Message(currentUser.user_def.UD_ID, message, timestamp);
        messageArrayList.add(msg);
        mAdapter.notifyDataSetChanged();
        if (mAdapter.getItemCount() > 1) {
            recyclerView.getLayoutManager().smoothScrollToPosition(recyclerView, null, mAdapter.getItemCount() - 1);
        }

        Map<String, String> params = new HashMap<String, String>();
        params.put("UD_ID_S", currentUser.user_def.UD_ID);
        params.put("UD_ID_R", otherUser.user_def.UD_ID);
        params.put("MESSAGE", message);
        params.put("CREATE_DT", timestamp);
        Utils.sendVolleyJsonRequest(getActivity(), Config.Server.CHAT_REQUESTS_URL, params, 0, new Utils.VolleyRequestListener() {
            @Override
            public void onResponse(String response) {
                Log.d("test", "response: " + response);
                // inputMessage.setEnabled(true);
            }

            @Override
            public void onError(String error) {
                Log.d("test", "error: " + error);
                if (getActivity() != null) {
                    Toast.makeText(getActivity(), "Network error", Toast.LENGTH_SHORT).show();
                    messageArrayList.remove(msg);
                    mAdapter.notifyDataSetChanged();
                    // inputMessage.setEnabled(true);
                    // inputMessage.setText(message);
                }
            }
        });
        inputMessage.requestFocus();
    }

}