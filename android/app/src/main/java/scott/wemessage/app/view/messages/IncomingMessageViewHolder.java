package scott.wemessage.app.view.messages;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.stfalcon.chatkit.messages.MessageHolders;
import com.stfalcon.chatkit.utils.DateFormatter;

import scott.wemessage.R;
import scott.wemessage.app.messages.objects.Attachment;
import scott.wemessage.app.view.messages.content.AttachmentImageView;
import scott.wemessage.app.view.messages.content.AttachmentView;
import scott.wemessage.app.weMessage;
import scott.wemessage.commons.types.MimeType;
import scott.wemessage.commons.utils.StringUtils;

public class IncomingMessageViewHolder extends MessageHolders.IncomingTextMessageViewHolder<MessageView> {

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

        for (Attachment attachment : message.getMessage().getAttachments()){
            MimeType mimeType = MimeType.getTypeFromString(attachment.getFileType());

            switch (mimeType){
                case IMAGE:
                    LayoutInflater inflater = (LayoutInflater) weMessage.get().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    AttachmentImageView attachmentImageView = (AttachmentImageView) inflater.inflate(R.layout.message_image, null);

                    attachmentImageView.bind(attachment, AttachmentView.MessageType.INCOMING);
                    attachmentsContainer.addView(attachmentImageView);
                    break;
                case UNDEFINED:
                    //TODO: Do some kind of file type unknown, include extension at end
                    break;
            }
        }

        if (StringUtils.isEmpty(message.getText())){
            bubble.setVisibility(View.GONE);
        }else {
            bubble.setVisibility(View.VISIBLE);
        }
    }
}