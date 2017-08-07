package scott.wemessage.app.ui.view.messages;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.stfalcon.chatkit.messages.MessageHolders;
import com.stfalcon.chatkit.utils.DateFormatter;

import scott.wemessage.R;
import scott.wemessage.app.AppLogger;
import scott.wemessage.app.messages.objects.Attachment;
import scott.wemessage.app.ui.view.messages.media.AttachmentAudioView;
import scott.wemessage.app.ui.view.messages.media.AttachmentImageView;
import scott.wemessage.app.ui.view.messages.media.AttachmentView;
import scott.wemessage.app.weMessage;
import scott.wemessage.commons.types.MimeType;
import scott.wemessage.commons.utils.StringUtils;

public class IncomingMessageViewHolder extends MessageHolders.IncomingTextMessageViewHolder<MessageView>  {

    private LinearLayout attachmentsContainer;

    public IncomingMessageViewHolder(View itemView) {
        super(itemView);

        attachmentsContainer = (LinearLayout) itemView.findViewById(R.id.attachmentsContainer);
    }

    @Override
    public void onBind(MessageView message) {
        super.onBind(message);

        time.setText(DateFormatter.format(message.getCreatedAt(), "h:mm a"));
        attachmentsContainer.removeAllViews();

        int i = 1;
        for (Attachment attachment : message.getMessage().getAttachments()) {
            try {
                MimeType mimeType = MimeType.getTypeFromString(attachment.getFileType());
                LayoutInflater inflater = LayoutInflater.from(itemView.getContext());

                switch (mimeType) {
                    case IMAGE:
                        AttachmentImageView attachmentImageView = (AttachmentImageView) inflater.inflate(R.layout.message_image, null);

                        attachmentImageView.bind(message, attachment, AttachmentView.MessageType.INCOMING);
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

                        attachmentAudioView.bind(message, attachment, AttachmentView.MessageType.INCOMING);
                        attachmentsContainer.addView(attachmentAudioView);

                        if (i++ != message.getMessage().getAttachments().size()) {
                            attachmentAudioView.setBottomPadding(16);
                        } else {
                            if (!StringUtils.isEmpty(message.getText())) {
                                attachmentAudioView.setBottomPadding(16);
                            }
                        }
                        break;
                    case UNDEFINED:
                        //TODO: Do some kind of file type unknown, include extension at end
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
    }

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
}