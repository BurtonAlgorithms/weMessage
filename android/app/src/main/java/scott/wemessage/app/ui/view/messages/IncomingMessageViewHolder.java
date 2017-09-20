package scott.wemessage.app.ui.view.messages;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.text.emoji.EmojiCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.stfalcon.chatkit.messages.MessageHolders;
import com.stfalcon.chatkit.utils.DateFormatter;
import com.vdurmont.emoji.EmojiParser;

import java.io.File;

import scott.wemessage.R;
import scott.wemessage.app.AppLogger;
import scott.wemessage.app.messages.objects.Attachment;
import scott.wemessage.app.messages.objects.chats.Chat;
import scott.wemessage.app.ui.ConversationFragment;
import scott.wemessage.app.ui.view.messages.media.AttachmentAudioView;
import scott.wemessage.app.ui.view.messages.media.AttachmentImageView;
import scott.wemessage.app.ui.view.messages.media.AttachmentUndefinedView;
import scott.wemessage.app.ui.view.messages.media.AttachmentVideoView;
import scott.wemessage.app.ui.view.messages.media.AttachmentView;
import scott.wemessage.app.weMessage;
import scott.wemessage.commons.types.MimeType;
import scott.wemessage.commons.utils.StringUtils;

public class IncomingMessageViewHolder extends MessageHolders.IncomingTextMessageViewHolder<MessageView> implements MessageViewHolder {

    private String messageId;
    private boolean isSelectionMode = false;
    private boolean isSelected = false;

    private boolean areDefaultsSet = false;
    private int defaultPaddingTop, defaultPaddingLeft, defaultPaddingRight, defaultPaddingBottom;
    private float defaultTextSizePx;
    private Drawable defaultBackgroundDrawable;

    private LinearLayout attachmentsContainer;
    private TextView senderName;
    private ImageView selectedBubble;

    public IncomingMessageViewHolder(View itemView) {
        super(itemView);

        attachmentsContainer = (LinearLayout) itemView.findViewById(R.id.attachmentsContainer);
        senderName = (TextView) itemView.findViewById(R.id.senderName);
        selectedBubble = (ImageView) itemView.findViewById(R.id.selectedMessageBubble);
    }

    @Override
    public void onBind(MessageView message) {
        super.onBind(message);

        if (!areDefaultsSet){
            areDefaultsSet = true;

            if (bubble != null) {
                defaultBackgroundDrawable = bubble.getBackground();
                defaultPaddingTop = bubble.getPaddingTop();
                defaultPaddingLeft = bubble.getPaddingLeft();
                defaultPaddingRight = bubble.getPaddingRight();
                defaultPaddingBottom = bubble.getPaddingBottom();
            }

            if (text != null) {
                defaultTextSizePx = text.getTextSize();
            }
        }

        messageId = message.getId();

        if (text != null && weMessage.get().isEmojiCompatInitialized()){
            text.setText(EmojiCompat.get().process(message.getText()));
        }

        if (time != null) {
            time.setText(DateFormatter.format(message.getCreatedAt(), "h:mm a"));
        }

        for (int i = 0; i < attachmentsContainer.getChildCount(); i++) {
            View v = attachmentsContainer.getChildAt(i);

            if (v instanceof AttachmentAudioView) {
                ((AttachmentAudioView) v).unbind();
            }
        }

        attachmentsContainer.removeAllViews();

        int i = 1;
        for (Attachment attachment : message.getMessage().getAttachments()) {
            if (!StringUtils.isEmpty(attachment.getFileType())) {

                LayoutInflater inflater = LayoutInflater.from(itemView.getContext());
                File file = new File(attachment.getFileLocation().getFileLocation());

                if (file.exists() && file.canRead()) {
                    try {
                        MimeType mimeType = MimeType.getTypeFromString(attachment.getFileType());

                        switch (mimeType) {
                            case IMAGE:
                                AttachmentImageView attachmentImageView = (AttachmentImageView) inflater.inflate(R.layout.message_image, null);

                                attachmentImageView.bind(message, attachment, AttachmentView.MessageType.INCOMING, message.hasErrored());
                                attachmentsContainer.addView(attachmentImageView);

                                if (i++ != message.getMessage().getAttachments().size()) {
                                    attachmentImageView.setBottomPadding(16);
                                } else {
                                    if (!StringUtils.isEmpty(message.getText())) {
                                        attachmentImageView.setBottomPadding(16);
                                    }
                                }
                                break;
                            case AUDIO:
                                AttachmentAudioView attachmentAudioView = (AttachmentAudioView) inflater.inflate(R.layout.message_audio, null);

                                attachmentAudioView.bind(message, attachment, AttachmentView.MessageType.INCOMING, message.hasErrored());
                                attachmentsContainer.addView(attachmentAudioView);

                                if (i++ != message.getMessage().getAttachments().size()) {
                                    attachmentAudioView.setBottomPadding(16);
                                } else {
                                    if (!StringUtils.isEmpty(message.getText())) {
                                        attachmentAudioView.setBottomPadding(16);
                                    }
                                }
                                break;
                            case VIDEO:
                                AttachmentVideoView attachmentVideoView = (AttachmentVideoView) inflater.inflate(R.layout.message_video, null);

                                attachmentVideoView.bind(message, attachment, AttachmentView.MessageType.INCOMING, message.hasErrored());
                                attachmentsContainer.addView(attachmentVideoView);

                                if (i++ != message.getMessage().getAttachments().size()) {
                                    attachmentVideoView.setBottomPadding(16);
                                } else {
                                    if (!StringUtils.isEmpty(message.getText())) {
                                        attachmentVideoView.setBottomPadding(16);
                                    }
                                }
                                break;
                            case UNDEFINED:
                                AttachmentUndefinedView attachmentUndefinedView = (AttachmentUndefinedView) inflater.inflate(R.layout.message_undefined_attachment, null);

                                attachmentUndefinedView.bind(message, attachment, AttachmentView.MessageType.INCOMING, message.hasErrored());
                                attachmentsContainer.addView(attachmentUndefinedView);

                                if (i++ != message.getMessage().getAttachments().size()) {
                                    attachmentUndefinedView.setBottomPadding(16);
                                } else {
                                    if (!StringUtils.isEmpty(message.getText())) {
                                        attachmentUndefinedView.setBottomPadding(16);
                                    }
                                }
                                break;
                        }
                    } catch (Exception ex) {
                        Intent broadcastIntent = new Intent(weMessage.BROADCAST_LOAD_ATTACHMENT_ERROR);
                        LocalBroadcastManager.getInstance(itemView.getContext()).sendBroadcast(broadcastIntent);

                        AppLogger.error("An error occurred while trying to load an attachment", ex);
                    }
                }else {
                    AttachmentUndefinedView attachmentUndefinedView = (AttachmentUndefinedView) inflater.inflate(R.layout.message_undefined_attachment, null);

                    attachmentUndefinedView.bind(message, attachment, AttachmentView.MessageType.INCOMING, message.hasErrored());
                    attachmentUndefinedView.setLost(true);

                    attachmentsContainer.addView(attachmentUndefinedView);

                    if (i++ != message.getMessage().getAttachments().size()) {
                        attachmentUndefinedView.setBottomPadding(16);
                    } else {
                        if (!StringUtils.isEmpty(message.getText())) {
                            attachmentUndefinedView.setBottomPadding(16);
                        }
                    }
                }
            }
        }

        if (StringUtils.isEmpty(message.getText())){
            bubble.setVisibility(View.GONE);
        }else {
            bubble.setVisibility(View.VISIBLE);
        }

        if (message.getMessage().getChat().getChatType() == Chat.ChatType.GROUP) {
            senderName.setText(message.getUser().getName());
            senderName.setVisibility(View.VISIBLE);
        }else {
            senderName.setVisibility(View.GONE);
        }

        toggleEmojiView(isStringEmojis(message.getText()));
        toggleSelectionMode(getParentFragment().isInSelectionMode());
        setSelected(getParentFragment().getSelectedMessages().containsKey(message.getId()));
    }

    @Override
    public String getMessageId() {
        return messageId;
    }

    @Override
    public void notifyAudioPlaybackStart(Attachment a){
        final int childCount = attachmentsContainer.getChildCount();

        for (int i = 0; i < childCount; i++) {
            View v = attachmentsContainer.getChildAt(i);

            if (v instanceof AttachmentAudioView){
                AttachmentAudioView attachmentAudioView = (AttachmentAudioView) v;

                if (attachmentAudioView.getAttachmentUuid().equals(a.getUuid().toString())){
                    attachmentAudioView.notifyAudioStart(AttachmentView.MessageType.INCOMING);
                }
            }
        }
    }

    @Override
    public void notifyAudioPlaybackStop(String attachmentUuid){
        final int childCount = attachmentsContainer.getChildCount();

        for (int i = 0; i < childCount; i++) {
            View v = attachmentsContainer.getChildAt(i);

            if (v instanceof AttachmentAudioView){
                AttachmentAudioView attachmentAudioView = (AttachmentAudioView) v;

                if (attachmentAudioView.getAttachmentUuid().equals(attachmentUuid)){
                    attachmentAudioView.notifyAudioFinish(AttachmentView.MessageType.INCOMING);
                }
            }
        }
    }

    @Override
    public void setSelected(boolean value){
        if (value){
            isSelected = true;
            selectedBubble.setImageDrawable(getActivity().getDrawable(R.drawable.ic_checkmark_circle));
        } else {
            isSelected = false;
            selectedBubble.setImageDrawable(getActivity().getDrawable(R.drawable.circle_outline));
        }
    }

    @Override
    public void toggleSelectionMode(boolean value){
        if (value){
            isSelectionMode = true;
            selectedBubble.setVisibility(View.VISIBLE);
        } else {
            isSelectionMode = false;
            selectedBubble.setVisibility(View.GONE);
        }
    }

    private void toggleEmojiView(boolean value){
        if (value){
            if (bubble != null) {
                ViewCompat.setBackground(bubble, null);

                bubble.setPadding(0, 0, 0, 0);
            }

            if (text != null){
                text.setTextSize(EMOJI_VIEW_TEXT_SIZE);
            }
        }else {
            if (bubble != null) {
                ViewCompat.setBackground(bubble, defaultBackgroundDrawable);

                bubble.setPadding(defaultPaddingLeft, defaultPaddingTop, defaultPaddingRight, defaultPaddingBottom);
            }

            if (text != null){
                text.setTextSize(TypedValue.COMPLEX_UNIT_PX, defaultTextSizePx);
            }
        }
    }

    private boolean isStringEmojis(String text){
        return !StringUtils.isEmpty(text) && StringUtils.isEmpty(EmojiParser.removeAllEmojis(text).trim());
    }

    private ConversationFragment getParentFragment(){
        return ((ConversationFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.conversationFragmentContainer));
    }

    private AppCompatActivity getActivity() {
        Context context = itemView.getContext();
        while (context instanceof ContextWrapper) {
            if (context instanceof Activity) {
                return (AppCompatActivity) context;
            }
            context = ((ContextWrapper)context).getBaseContext();
        }
        return null;
    }
}