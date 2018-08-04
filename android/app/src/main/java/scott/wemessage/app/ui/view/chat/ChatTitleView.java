package scott.wemessage.app.ui.view.chat;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.github.siyamed.shapeimageview.CircularImageView;

import java.util.ArrayList;
import java.util.List;

import scott.wemessage.R;
import scott.wemessage.app.models.chats.Chat;
import scott.wemessage.app.models.chats.GroupChat;
import scott.wemessage.app.models.chats.PeerChat;
import scott.wemessage.app.models.users.Handle;
import scott.wemessage.app.ui.view.font.FontTextView;
import scott.wemessage.app.utils.IOUtils;
import scott.wemessage.app.utils.view.DisplayUtils;
import scott.wemessage.commons.utils.StringUtils;

public class ChatTitleView extends LinearLayout {

    private final int SINGLE_IMAGE_SIZE = 52;
    private final int GROUP_IMAGE_SIZE = 64;
    private final int BIG_TEXT_SIZE = 18;
    private final int TEXT_SIZE = 14;
    private final int PARTICIPANT_TEXT_SIZE = 12;

    private CircularImageView imageView;
    private FontTextView titleTextView;
    private FontTextView participantsTextView;

    public ChatTitleView(Context context) {
        super(context);

        init(context);
    }

    public ChatTitleView(Context context, AttributeSet attrs) {
        super(context, attrs);

        init(context);
    }

    public ChatTitleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init(context);
    }

    public void setChat(Chat chat){
        if (imageView == null){
            init(getContext());
        }

        if (chat instanceof PeerChat){
            Handle handle = ((PeerChat) chat).getHandle();
            String url = IOUtils.getChatIconUri(chat, IOUtils.IconSize.NORMAL);
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) getLayoutParams();

            layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
            layoutParams.removeRule(RelativeLayout.END_OF);
            layoutParams.removeRule(RelativeLayout.START_OF);

            setLayoutParams(layoutParams);
            setOrientation(VERTICAL);

            if (url.equals(IOUtils.getDefaultContactUri(IOUtils.IconSize.NORMAL, false))) {
                imageView.setBorderWidth(4);
                imageView.setBorderColor(Color.WHITE);
            }
            imageView.setLayoutParams(new LayoutParams(DisplayUtils.convertDpToRoundedPixel(SINGLE_IMAGE_SIZE, getContext()),
                    DisplayUtils.convertDpToRoundedPixel(SINGLE_IMAGE_SIZE, getContext())));

            Glide.with(getContext()).load(url).into(imageView);

            LayoutParams textLayoutParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            textLayoutParams.topMargin = DisplayUtils.convertDpToRoundedPixel(8, getContext());

            titleTextView.setTextSize(TEXT_SIZE);
            titleTextView.setTypeface(titleTextView.getTypeface(), Typeface.NORMAL);
            titleTextView.setTextColor(Color.WHITE);
            titleTextView.setSingleLine();
            titleTextView.setEllipsize(TextUtils.TruncateAt.END);
            titleTextView.setText(handle.getDisplayName());
            titleTextView.setLayoutParams(textLayoutParams);

            participantsTextView.setVisibility(GONE);
        } else {
            GroupChat groupChat = (GroupChat) chat;
            String url = IOUtils.getChatIconUri(groupChat, IOUtils.IconSize.NORMAL);
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) getLayoutParams();

            layoutParams.removeRule(RelativeLayout.CENTER_IN_PARENT);
            layoutParams.addRule(RelativeLayout.END_OF, R.id.conversationBackButton);
            layoutParams.addRule(RelativeLayout.START_OF, R.id.conversationInfoButton);

            setLayoutParams(layoutParams);
            setOrientation(HORIZONTAL);

            if (url.equals(IOUtils.getDefaultChatUri(IOUtils.IconSize.NORMAL, false))) {
                imageView.setBorderWidth(4);
                imageView.setBorderColor(Color.WHITE);
            }
            imageView.setLayoutParams(new LayoutParams(DisplayUtils.convertDpToRoundedPixel(GROUP_IMAGE_SIZE, getContext()),
                    DisplayUtils.convertDpToRoundedPixel(GROUP_IMAGE_SIZE, getContext())));

            Glide.with(getContext()).load(url).into(imageView);

            removeView(titleTextView);
            removeView(participantsTextView);

            LinearLayout textLayout = new LinearLayout(getContext());
            LayoutParams textLayoutParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.BOTTOM);

            textLayoutParams.setMarginStart(DisplayUtils.convertDpToRoundedPixel(24, getContext()));
            textLayout.setOrientation(VERTICAL);
            textLayout.setLayoutParams(textLayoutParams);

            LayoutParams titleLayoutParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            titleLayoutParams.bottomMargin = DisplayUtils.convertDpToRoundedPixel(2, getContext());

            titleTextView.setTextSize(BIG_TEXT_SIZE);
            titleTextView.setTypeface(titleTextView.getTypeface(), Typeface.BOLD);
            titleTextView.setTextColor(Color.WHITE);
            titleTextView.setSingleLine();
            titleTextView.setEllipsize(TextUtils.TruncateAt.END);
            titleTextView.setText(groupChat.getUIDisplayName());
            titleTextView.setLayoutParams(titleLayoutParams);

            String participantsText;
            List<String> participantDisplayNames = new ArrayList<>();

            for (Handle h : groupChat.getParticipants()){
                participantDisplayNames.add(h.getDisplayName());
            }
            participantDisplayNames.remove(participantDisplayNames.size() - 1);

            participantsText = StringUtils.join(participantDisplayNames, ", ", 0) + getContext().getString(R.string.word_and) +
                    " " + groupChat.getParticipants().get(groupChat.getParticipants().size() - 1).getDisplayName();

            participantsTextView.setTextSize(PARTICIPANT_TEXT_SIZE);
            participantsTextView.setTextColor(Color.WHITE);
            participantsTextView.setMaxLines(2);
            participantsTextView.setEllipsize(TextUtils.TruncateAt.END);
            participantsTextView.setText(participantsText);

            if (titleTextView.getParent() == null) {
                textLayout.addView(titleTextView);
                textLayout.addView(participantsTextView);

                addView(textLayout);
            }
        }
    }

    private void init(Context context){
        imageView = new CircularImageView(context);
        titleTextView = new FontTextView(context);
        participantsTextView = new FontTextView(context);

        participantsTextView.setFont("orkney_light.ttf");

        addView(imageView);
        addView(titleTextView);
        addView(participantsTextView);
    }
}