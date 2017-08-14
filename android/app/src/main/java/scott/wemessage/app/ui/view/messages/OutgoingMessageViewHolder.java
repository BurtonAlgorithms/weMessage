package scott.wemessage.app.ui.view.messages;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.stfalcon.chatkit.messages.MessageHolders;
import com.stfalcon.chatkit.utils.DateFormatter;

import scott.wemessage.R;
import scott.wemessage.app.AppLogger;
import scott.wemessage.app.messages.objects.Attachment;
import scott.wemessage.app.ui.view.messages.media.AttachmentAudioView;
import scott.wemessage.app.ui.view.messages.media.AttachmentImageView;
import scott.wemessage.app.ui.view.messages.media.AttachmentUndefinedView;
import scott.wemessage.app.ui.view.messages.media.AttachmentVideoView;
import scott.wemessage.app.ui.view.messages.media.AttachmentView;
import scott.wemessage.app.weMessage;
import scott.wemessage.commons.types.MimeType;
import scott.wemessage.commons.utils.StringUtils;

public class OutgoingMessageViewHolder extends MessageHolders.OutcomingTextMessageViewHolder<MessageView> {

    private LinearLayout attachmentsContainer;
    private ImageView errorBubble;
    private TextView errorMessageView;

    public OutgoingMessageViewHolder(View itemView) {
        super(itemView);

        attachmentsContainer = (LinearLayout) itemView.findViewById(R.id.attachmentsContainer);
        errorBubble = (ImageView) itemView.findViewById(R.id.errorBubble);
        errorMessageView = (TextView) itemView.findViewById(R.id.errorMessageView);
    }

    @Override
    public void onBind(MessageView message) {
        super.onBind(message);

        time.setText(DateFormatter.format(message.getCreatedAt(), "h:mm a"));

        for (int i = 0; i < attachmentsContainer.getChildCount(); i++) {
            View v = attachmentsContainer.getChildAt(i);

            if (v instanceof AttachmentAudioView) {
                ((AttachmentAudioView) v).unbind();
            }
        }

        attachmentsContainer.removeAllViews();

        int i = 1;
        for (Attachment attachment : message.getMessage().getAttachments()) {
            try {
                MimeType mimeType = MimeType.getTypeFromString(attachment.getFileType());
                LayoutInflater inflater = LayoutInflater.from(itemView.getContext());

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
            }catch(Exception ex){
                Intent broadcastIntent = new Intent(weMessage.BROADCAST_LOAD_ATTACHMENT_ERROR);
                LocalBroadcastManager.getInstance(itemView.getContext()).sendBroadcast(broadcastIntent);

                AppLogger.error("An error occurred while trying to load an attachment", ex);
            }
        }

        if (StringUtils.isEmpty(message.getText())){
            bubble.setVisibility(View.GONE);
        }else {
            bubble.setVisibility(View.VISIBLE);
        }

        if (message.hasErrored()){
            errorBubble.setVisibility(View.VISIBLE);
            errorMessageView.setVisibility(View.VISIBLE);

        }else {
            errorBubble.setVisibility(View.GONE);
            errorMessageView.setVisibility(View.GONE);
        }
    }

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
}