package scott.wemessage.app.ui.view.messages;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.text.emoji.EmojiCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.util.Patterns;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.stfalcon.chatkit.messages.MessageHolders;
import com.stfalcon.chatkit.utils.DateFormatter;
import com.vdurmont.emoji.EmojiParser;

import java.io.File;
import java.util.Date;

import scott.wemessage.R;
import scott.wemessage.app.AppLogger;
import scott.wemessage.app.models.messages.Attachment;
import scott.wemessage.app.models.sms.messages.MmsMessage;
import scott.wemessage.app.ui.ConversationFragment;
import scott.wemessage.app.ui.view.messages.media.AttachmentAudioView;
import scott.wemessage.app.ui.view.messages.media.AttachmentImageView;
import scott.wemessage.app.ui.view.messages.media.AttachmentUndefinedView;
import scott.wemessage.app.ui.view.messages.media.AttachmentVideoView;
import scott.wemessage.app.ui.view.messages.media.AttachmentView;
import scott.wemessage.app.utils.AndroidUtils;
import scott.wemessage.app.weMessage;
import scott.wemessage.commons.types.MimeType;
import scott.wemessage.commons.utils.DateUtils;
import scott.wemessage.commons.utils.StringUtils;

public class OutgoingMessageViewHolder extends MessageHolders.OutcomingTextMessageViewHolder<MessageView> implements MessageViewHolder {

    private String messageId;
    private boolean showDeliveryView = false;
    private boolean isSelectionMode = false;
    private boolean isSelected = false;

    private boolean areDefaultsSet = false;
    private int defaultTimeColor;
    private int defaultPaddingTop, defaultPaddingLeft, defaultPaddingRight, defaultPaddingBottom;
    private float defaultTextSizePx;
    private Drawable defaultBackgroundDrawable;

    private LinearLayout attachmentsContainer;
    private ImageView errorBubble;
    private TextView errorMessageView;
    private TextView deliveryMessageView;
    private TextView deliveryMessageTimeView;
    private ImageView selectedBubble;

    public OutgoingMessageViewHolder(View itemView) {
        super(itemView);

        attachmentsContainer = itemView.findViewById(R.id.attachmentsContainer);
        errorBubble = itemView.findViewById(R.id.errorBubble);
        errorMessageView = itemView.findViewById(R.id.errorMessageView);
        selectedBubble = itemView.findViewById(R.id.selectedMessageBubble);
        deliveryMessageView = itemView.findViewById(R.id.deliveryMessageView);
        deliveryMessageTimeView = itemView.findViewById(R.id.deliveryMessageTimeView);
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

            if (time != null){
                defaultTimeColor = time.getCurrentTextColor();
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

                                attachmentImageView.bind(message, attachment, AttachmentView.MessageType.OUTGOING, message.hasErrored());
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

                                attachmentAudioView.bind(message, attachment, AttachmentView.MessageType.OUTGOING, message.hasErrored());
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

                                attachmentVideoView.bind(message, attachment, AttachmentView.MessageType.OUTGOING, message.hasErrored());
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

                                attachmentUndefinedView.bind(message, attachment, AttachmentView.MessageType.OUTGOING, message.hasErrored());
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

                    attachmentUndefinedView.bind(message, attachment, AttachmentView.MessageType.OUTGOING, message.hasErrored());
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

        ViewCompat.setBackground(bubble, getOutgoingBubbleDrawable(message.getMessage() instanceof MmsMessage));

        if (StringUtils.isEmpty(message.getText())){
            bubble.setVisibility(View.GONE);
        }else {
            bubble.setVisibility(View.VISIBLE);
        }

        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) bubble.getLayoutParams();

        if (message.hasErrored()){
            errorBubble.setVisibility(View.VISIBLE);
            errorMessageView.setVisibility(View.VISIBLE);

            layoutParams.removeRule(RelativeLayout.ALIGN_PARENT_END);
            layoutParams.addRule(RelativeLayout.START_OF, R.id.errorBubble);
        }else {
            errorBubble.setVisibility(View.GONE);
            errorMessageView.setVisibility(View.GONE);

            layoutParams.removeRule(RelativeLayout.START_OF);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_END);
        }

        bubble.setLayoutParams(layoutParams);

        showDeliveryView = !message.hasErrored() && (message.getMessage().isDelivered() || message.getMessage().isRead());

        if (message.getMessage().isDelivered()){
            deliveryMessageView.setText(itemView.getContext().getString(R.string.word_delivered));
            deliveryMessageTimeView.setVisibility(View.GONE);
        }

        if (message.getMessage().isRead() && message.getMessage().getModernDateRead() != null){
            Date date = message.getMessage().getModernDateRead();

            deliveryMessageView.setText(itemView.getContext().getString(R.string.word_read));

            if (DateFormatter.isToday(date)){
                deliveryMessageTimeView.setText(DateFormatter.format(date, " h:mm a"));
            }else if (DateFormatter.isYesterday(date)){
                String dateString = " " + itemView.getContext().getString(R.string.word_yesterday) + DateFormatter.format(date, " h:mm a");
                deliveryMessageTimeView.setText(dateString);
            }else if (DateUtils.isSameWeek(date)){
                String dateString = " " + AndroidUtils.getDayFromDate(itemView.getContext(), date) + DateFormatter.format(date, " h:mm a");
                deliveryMessageTimeView.setText(dateString);
            }else {
                if (DateFormatter.isCurrentYear(date)){
                    deliveryMessageTimeView.setText(DateFormatter.format(date, " MMMM d"));
                }else {
                    deliveryMessageTimeView.setText(DateFormatter.format(date, " MMMM d, yyyy"));
                }
            }
            deliveryMessageTimeView.setVisibility(View.VISIBLE);
        }

        toggleEmojiView(isStringEmojis(message.getText()));
        toggleDeliveryVisibility(!StringUtils.isEmpty(getParentFragment().getLastMessageId()) && getParentFragment().getLastMessageId().equals(getMessageId()));
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
                    attachmentAudioView.notifyAudioStart(AttachmentView.MessageType.OUTGOING);
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
                    attachmentAudioView.notifyAudioFinish(AttachmentView.MessageType.OUTGOING);
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
    protected void configureLinksBehavior(TextView text) {
        text.setLinksClickable(false);

        text.setMovementMethod(new LinkMovementMethod(){
            @Override
            public boolean onTouchEvent(TextView widget, Spannable buffer, MotionEvent event) {
                if (isSelectionMode) return true;

                if (Patterns.WEB_URL.matcher(buffer.toString()).matches()) {
                    getParentFragment().launchWebView(buffer.toString());
                    return true;
                }
                itemView.onTouchEvent(event);
                return false;
            }
        });
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

    public void toggleDeliveryVisibility(boolean visible){
        if (visible){
            if (showDeliveryView){
                itemView.findViewById(R.id.deliveryMessageContainer).setVisibility(View.VISIBLE);
            }
        }else {
            itemView.findViewById(R.id.deliveryMessageContainer).setVisibility(View.GONE);
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

            if (time != null){
                time.setTextColor(getActivity().getResources().getColor(R.color.warm_grey_four));
            }
        }else {
            if (bubble != null) {
                ViewCompat.setBackground(bubble, defaultBackgroundDrawable);

                bubble.setPadding(defaultPaddingLeft, defaultPaddingTop, defaultPaddingRight, defaultPaddingBottom);
            }

            if (text != null){
                text.setTextSize(TypedValue.COMPLEX_UNIT_PX, defaultTextSizePx);
            }

            if (time != null){
                time.setTextColor(defaultTimeColor);
            }
        }
    }

    private Drawable getOutgoingBubbleDrawable(boolean isMms) {
        Context context = itemView.getContext();

        if (isMms){
            return getMessageSelector(context.getResources().getColor(R.color.outgoingBubbleColorOrange), context.getResources().getColor(R.color.outgoingBubbleColorOrangePressed),
                    context.getResources().getColor(R.color.outgoingBubbleColorOrangePressed), R.drawable.shape_outcoming_message);
        }else {
            return getMessageSelector(context.getResources().getColor(R.color.outgoingBubbleColor), context.getResources().getColor(R.color.outgoingBubbleColorPressed),
                    context.getResources().getColor(R.color.outgoingBubbleColorPressed), R.drawable.shape_outcoming_message);
        }
    }

    private boolean isStringEmojis(String text){
        return !StringUtils.isEmpty(text.trim()) && StringUtils.isEmpty(EmojiParser.removeAllEmojis(text).trim());
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
            context = ((ContextWrapper) context).getBaseContext();
        }
        return null;
    }

    private Drawable getMessageSelector(@ColorInt int normalColor, @ColorInt int selectedColor, @ColorInt int pressedColor, @DrawableRes int shape) {
        Drawable drawable = DrawableCompat.wrap(getVectorDrawable(shape)).mutate();
        DrawableCompat.setTintList(
                drawable,
                new ColorStateList(
                        new int[][]{
                                new int[]{android.R.attr.state_selected},
                                new int[]{android.R.attr.state_pressed},
                                new int[]{-android.R.attr.state_pressed, -android.R.attr.state_selected}
                        },
                        new int[]{selectedColor, pressedColor, normalColor}
                ));
        return drawable;
    }

    private Drawable getVectorDrawable(@DrawableRes int drawable) {
        return ContextCompat.getDrawable(itemView.getContext(), drawable);
    }
}