package scott.wemessage.app.view.chat;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.Toolbar;
import android.text.InputFilter;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import scott.wemessage.R;
import scott.wemessage.app.messages.objects.Contact;
import scott.wemessage.app.messages.objects.chats.Chat;
import scott.wemessage.app.messages.objects.chats.GroupChat;
import scott.wemessage.app.messages.objects.chats.PeerChat;
import scott.wemessage.app.utils.AndroidIOUtils;
import scott.wemessage.app.utils.view.DisplayUtils;
import scott.wemessage.app.view.text.FontTextView;
import scott.wemessage.commons.utils.StringUtils;

public class ChatTitleView extends LinearLayout {

    private final int SINGLE_IMAGE_SIZE = 52;
    private final int GROUP_IMAGE_SIZE = 64;
    private final int BIG_TEXT_SIZE = 18;
    private final int TEXT_SIZE = 14;
    private final int PARTICIPANT_TEXT_SIZE = 12;

    private Chat chat;

    private CircleImageView imageView;
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

        this.chat = chat;

        if (chat instanceof PeerChat){
            Contact contact = ((PeerChat) chat).getContact();
            String url = AndroidIOUtils.getChatIconUri(chat);

            setOrientation(VERTICAL);
            setLayoutParams(new Toolbar.LayoutParams(Toolbar.LayoutParams.MATCH_PARENT, Toolbar.LayoutParams.WRAP_CONTENT, Gravity.CENTER));

            imageView.setLayoutParams(new LayoutParams(Math.round(DisplayUtils.convertDpToPixel(SINGLE_IMAGE_SIZE, getContext())),
                    Math.round(DisplayUtils.convertDpToPixel(SINGLE_IMAGE_SIZE, getContext())), Gravity.CENTER));

            Glide.with(getContext()).load(url).into(imageView);

            LayoutParams textLayoutParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            textLayoutParams.topMargin = Math.round(DisplayUtils.convertDpToPixel(8, getContext()));

            titleTextView.setTextSize(TEXT_SIZE);
            titleTextView.setFilters(new InputFilter[] { new InputFilter.LengthFilter(22) });
            titleTextView.setTextColor(Color.WHITE);
            titleTextView.setGravity(Gravity.CENTER);
            titleTextView.setSingleLine();
            titleTextView.setEllipsize(TextUtils.TruncateAt.END);
            titleTextView.setText(contact.getUIDisplayName());
            titleTextView.setLayoutParams(textLayoutParams);

            participantsTextView.setVisibility(GONE);
        }else {
            GroupChat groupChat = (GroupChat) chat;
            String url = AndroidIOUtils.getChatIconUri(groupChat);

            setOrientation(HORIZONTAL);
            setLayoutParams(new Toolbar.LayoutParams(Toolbar.LayoutParams.MATCH_PARENT, Toolbar.LayoutParams.WRAP_CONTENT, Gravity.START));

            imageView.setLayoutParams(new LayoutParams(Math.round(DisplayUtils.convertDpToPixel(GROUP_IMAGE_SIZE, getContext())),
                    Math.round(DisplayUtils.convertDpToPixel(GROUP_IMAGE_SIZE, getContext())), Gravity.CENTER));

            Glide.with(getContext()).load(url).into(imageView);

            removeView(titleTextView);
            removeView(participantsTextView);

            LinearLayout textLayout = new LinearLayout(getContext());
            LayoutParams textLayoutParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.BOTTOM);

            textLayoutParams.setMarginStart(Math.round(DisplayUtils.convertDpToPixel(8, getContext())));
            textLayoutParams.setMarginEnd(Math.round(DisplayUtils.convertDpToPixel(8, getContext())));
            textLayout.setOrientation(VERTICAL);
            textLayout.setLayoutParams(textLayoutParams);

            LayoutParams titleLayoutParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            titleLayoutParams.bottomMargin = Math.round(DisplayUtils.convertDpToPixel(2, getContext()));

            titleTextView.setTextSize(BIG_TEXT_SIZE);
            titleTextView.setFilters(new InputFilter[] { new InputFilter.LengthFilter(48) });
            titleTextView.setTextColor(Color.WHITE);
            titleTextView.setSingleLine();
            titleTextView.setEllipsize(TextUtils.TruncateAt.END);
            titleTextView.setText(groupChat.getUIDisplayName(false));
            titleTextView.setLayoutParams(titleLayoutParams);

            String participantsText;
            List<String> participantDisplayNames = new ArrayList<>();

            for (Contact c : groupChat.getParticipants()){
                participantDisplayNames.add(c.getUIDisplayName());
            }
            participantDisplayNames.remove(participantDisplayNames.size() - 1);

            participantsText = StringUtils.join(participantDisplayNames, ", ", 0) + getContext().getString(R.string.word_and) +
                    " " + groupChat.getParticipants().get(groupChat.getParticipants().size() - 1).getUIDisplayName();

            participantsTextView.setTextSize(PARTICIPANT_TEXT_SIZE);
            participantsTextView.setTextColor(Color.WHITE);
            participantsTextView.setMaxLines(2);
            participantsTextView.setEllipsize(TextUtils.TruncateAt.END);
            participantsTextView.setText(participantsText);

            if (titleTextView.getParent() == null) {
                textLayout.addView(titleTextView);
                textLayout.addView(participantsTextView);
            }

            addView(textLayout);
        }
    }

    //TODO: What font shall I use

    private void init(Context context){
        imageView = new CircleImageView(context);
        titleTextView = new FontTextView(context);
        participantsTextView = new FontTextView(context);

        addView(imageView);
        addView(titleTextView);
        addView(participantsTextView);
    }
}