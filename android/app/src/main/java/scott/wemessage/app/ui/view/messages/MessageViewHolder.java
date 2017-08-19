package scott.wemessage.app.ui.view.messages;

import scott.wemessage.app.messages.objects.Attachment;

public interface MessageViewHolder {

    void notifyAudioPlaybackStart(Attachment a);

    void notifyAudioPlaybackStop(String attachmentUuid);

    void setSelected(boolean value);

    void toggleSelectionMode(boolean value);

    String getMessageId();
}