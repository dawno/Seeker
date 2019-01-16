package com.trybe.trybe;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.trybe.trybe.dto.MatchesDTO;
import com.trybe.trybe.helper.UserSessionManager;
import com.trybe.trybe.helper.Utils;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatFragment2 extends Fragment {

    private Gson gson;
    private static final String ARG_PARAM1 = "referrers";
    private List<MatchesDTO.Match> matches;

    private LinearLayout chatList;

    public ChatFragment2() {
    }

    public static ChatFragment2 newInstance(List<MatchesDTO.Match> matches) {
        ChatFragment2 fragment = new ChatFragment2();
        Gson gson = Utils.getGsonInstance();
        String json = gson.toJson(matches);

        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, json);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        gson = Utils.getGsonInstance();
        if (getArguments() != null) {
            String json = getArguments().getString(ARG_PARAM1);
            Type listType = new TypeToken<ArrayList<MatchesDTO.Match>>() {
            }.getType();
            matches = gson.fromJson(json, listType);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.frag_chat2, container, false);
        chatList = (LinearLayout) rootView.findViewById(R.id.chatList);
        initializerReferList(matches);

        return rootView;
    }

    private void initializerReferList(List<MatchesDTO.Match> matchList) {
        if (matchList == null || matchList.size() == 0)
            return;

        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        ArrayList<String> unreadUids = UserSessionManager.getInstance().getUnreadChatsUID();
        String currentJobPos = "";
        for (int i = 0; i < matchList.size(); i++) {
            final MatchesDTO.Match item = matchList.get(i);

            if (!item.JR_POSITION.equals(currentJobPos)) {
                TextView chatPost = (TextView) inflater.inflate(R.layout.tv_refer_chat_heading, null, false);
                currentJobPos = item.JR_POSITION;
                chatPost.setText(currentJobPos);
                chatPost.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
                chatList.addView(chatPost, -1, params);
            }

            View layoutChatItem = inflater.inflate(R.layout.item_chat, null, false);
            ImageView chatImage = (CircleImageView) layoutChatItem.findViewById(R.id.chatItemPic);
            TextView chatName = (TextView) layoutChatItem.findViewById(R.id.chatItemName);
            final ImageView chatUnread = (ImageView) layoutChatItem.findViewById(R.id.chatItemUnread);

            chatName.setText(item.UD_NAME);
            Utils.loadImageByPicasso(getActivity(), chatImage, item.UD_IMG);
            if (unreadUids.contains(item.UD_ID))
                chatUnread.setVisibility(View.VISIBLE);
            else
                chatUnread.setVisibility(View.GONE);

            layoutChatItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    chatUnread.setVisibility(View.GONE);
                    Intent i = new Intent(getActivity(), ChatMessageActivity.class);
                    i.putExtra(ChatMessageActivity.ARG_USER, gson.toJson(item.UD_ID));
                    i.putExtra(ChatMessageActivity.ARG_USER_MATCH, gson.toJson(item));
                    i.putExtra(ChatMessageActivity.ARG_USER_TYPE, ChatMessageActivity.TYPE_REFER);
                    startActivity(i);
                }
            });

            chatList.addView(layoutChatItem, -1, params);
        }
    }

}