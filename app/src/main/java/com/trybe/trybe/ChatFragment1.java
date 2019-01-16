package com.trybe.trybe;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
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

public class ChatFragment1 extends Fragment {

    private Gson gson;
    private static final String ARG_PARAM1 = "seekers";
    private List<MatchesDTO.Match> matches;

    private ListView chatList;

    public ChatFragment1() {
    }

    public static ChatFragment1 newInstance(List<MatchesDTO.Match> matches) {
        ChatFragment1 fragment = new ChatFragment1();
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
        View rootView = inflater.inflate(R.layout.frag_chat1, container, false);
        chatList = (ListView) rootView.findViewById(R.id.chatList);
        ChatAdapter1 adapter = new ChatAdapter1(getActivity(), R.layout.item_chat, matches);
        chatList.setAdapter(adapter);

        return rootView;
    }

    class ChatAdapter1 extends ArrayAdapter<MatchesDTO.Match> {

        Context context;

        public ChatAdapter1(Context context, int resourceId, List<MatchesDTO.Match> items) {
            super(context, resourceId, items);
            this.context = context;
        }

        private class ViewHolder {
            ImageView pic;
            TextView name;
            ImageView unread;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            ViewHolder initHolder;
            final MatchesDTO.Match item = getItem(position);

            LayoutInflater mInflater = (LayoutInflater) context
                    .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            if (view == null) {
                view = mInflater.inflate(R.layout.item_chat, parent, false);
                initHolder = new ViewHolder();
                initHolder.pic = (CircleImageView) view.findViewById(R.id.chatItemPic);
                initHolder.name = (TextView) view.findViewById(R.id.chatItemName);
                initHolder.unread = (ImageView) view.findViewById(R.id.chatItemUnread);
                view.setTag(initHolder);
            } else
                initHolder = (ViewHolder) view.getTag();

            final ViewHolder holder = initHolder;
            holder.name.setText(item.UD_NAME);
            final String imageUrl = item.UD_IMG;
            Utils.loadImageByPicasso(context, holder.pic, imageUrl);
            ArrayList<String> unreadUids = UserSessionManager.getInstance().getUnreadChatsUID();
            if (unreadUids.contains(item.UD_ID))
                holder.unread.setVisibility(View.VISIBLE);
            else
                holder.unread.setVisibility(View.GONE);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    holder.unread.setVisibility(View.GONE);
                    Intent i = new Intent(getActivity(), ChatMessageActivity.class);
                    i.putExtra(ChatMessageActivity.ARG_USER, item.UD_ID);
                    i.putExtra(ChatMessageActivity.ARG_USER_MATCH, gson.toJson(item));
                    i.putExtra(ChatMessageActivity.ARG_USER_TYPE, ChatMessageActivity.TYPE_SEEK);
                    startActivity(i);
                }
            });

            return view;
        }

    }

}