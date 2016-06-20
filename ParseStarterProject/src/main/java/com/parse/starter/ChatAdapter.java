package com.parse.starter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Technovibe on 17-04-2015.
 */
public class ChatAdapter extends BaseAdapter {

    private final List<ChatMessage> chatMessages;
    private Activity context;

    public ChatAdapter(Activity context, List<ChatMessage> chatMessages) {
        this.context = context;
        this.chatMessages = chatMessages;
    }

    @Override
    public int getCount() {
        if (chatMessages != null) {
            return chatMessages.size();
        } else {
            return 0;
        }
    }

    @Override
    public ChatMessage getItem(int position) {
        if (chatMessages != null) {
            return chatMessages.get(position);
        } else {
            return null;
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        View rowView;
        ChatMessage chatMessage = getItem(position);
        LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if(chatMessage.getMessageType().equals(ChatMessage.TEXT_TYPE)) {
            rowView = vi.inflate(R.layout.list_item_chat_message, parent, false);
            holder = createViewHolder(rowView, false);
        }else{
            rowView = vi.inflate(R.layout.list_item_chat_picture, parent, false);
            holder = createViewHolder(rowView, true);
        }

        boolean myMsg = chatMessage.getIsme() ;//Just a dummy check to simulate whether it me or other sender

        if (chatMessage.getMessageType().equals(ChatMessage.TEXT_TYPE)){
            setAlignment(holder, myMsg, false);
            holder.txtMessage.setText(chatMessage.getMessage());
            holder.txtMessage.setTextColor(Color.WHITE);
        }else{
            setAlignment(holder, myMsg, true);
            /*holder.txtMessage.setVisibility(View.GONE);*/
            holder.txtPicture.setVisibility(View.VISIBLE);
            String imageName = (chatMessage.getIsme())? ImageChannel.file_self + chatMessage.getMessage() : ImageChannel.file_pre + chatMessage.getMessage();
            ImageChannel.displayImage(holder.txtPicture, imageName, context);
        }

        holder.txtInfo.setText(chatMessage.getDate());

        return rowView;
    }

    public void add(ChatMessage message) {
        chatMessages.add(message);
    }

    public void add(List<ChatMessage> messages) {
        chatMessages.addAll(messages);
    }

    private void setAlignment(ViewHolder holder, boolean isMe, boolean isPic) {
        if (isMe) {
            holder.contentWithBG.setBackgroundResource(R.drawable.in_message_bg);

            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) holder.contentWithBG.getLayoutParams();
            layoutParams.gravity = Gravity.RIGHT;
            holder.contentWithBG.setLayoutParams(layoutParams);

            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) holder.content.getLayoutParams();
            lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 0);
            lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            holder.content.setLayoutParams(lp);
            if(!isPic) {
                layoutParams = (LinearLayout.LayoutParams) holder.txtMessage.getLayoutParams();
                layoutParams.gravity = Gravity.RIGHT;
                holder.txtMessage.setLayoutParams(layoutParams);
            }

            layoutParams = (LinearLayout.LayoutParams) holder.txtInfo.getLayoutParams();
            layoutParams.gravity = Gravity.RIGHT;
            holder.txtInfo.setLayoutParams(layoutParams);
        } else {
            holder.contentWithBG.setBackgroundResource(R.drawable.out_message_bg);

            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) holder.contentWithBG.getLayoutParams();
            layoutParams.gravity = Gravity.LEFT;
            holder.contentWithBG.setLayoutParams(layoutParams);

            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) holder.content.getLayoutParams();
            lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0);
            lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            holder.content.setLayoutParams(lp);
            if(!isPic) {
                layoutParams = (LinearLayout.LayoutParams) holder.txtMessage.getLayoutParams();
                layoutParams.gravity = Gravity.LEFT;
                holder.txtMessage.setLayoutParams(layoutParams);
            }
            layoutParams = (LinearLayout.LayoutParams) holder.txtInfo.getLayoutParams();
            layoutParams.gravity = Gravity.LEFT;
            holder.txtInfo.setLayoutParams(layoutParams);
        }
    }

    private ViewHolder createViewHolder(View v, Boolean isPicture) {
        ViewHolder holder = new ViewHolder();
        if(isPicture){
            holder.txtPicture = (ImageView) v.findViewById(R.id.txtPicture);
            holder.txtMessage = null;
            holder.content = (LinearLayout) v.findViewById(R.id.picture_content);
            holder.contentWithBG = (LinearLayout) v.findViewById(R.id.picture_content_bg);
            holder.txtInfo = (TextView) v.findViewById(R.id.picInfo);
        }else {
            holder.txtMessage = (TextView) v.findViewById(R.id.txtMessage);
            holder.txtPicture = null;
            holder.content = (LinearLayout) v.findViewById(R.id.content);
            holder.contentWithBG = (LinearLayout) v.findViewById(R.id.contentWithBackground);
            holder.txtInfo = (TextView) v.findViewById(R.id.txtInfo);
        }
        return holder;
    }


    private static class ViewHolder {
        public TextView txtMessage;
        public ImageView txtPicture;
        public TextView txtInfo;
        public LinearLayout content;
        public LinearLayout contentWithBG;
    }
}
