package scott.wemessage.app.ui.view.messages;

import scott.wemessage.app.models.messages.Attachment;

public interface MessageViewHolder {

    int EMOJI_VIEW_TEXT_SIZE = 36;

    void notifyAudioPlaybackStart(Attachment a);

    void notifyAudioPlaybackStop(String attachmentUuid);

    void setSelected(boolean value);

    void toggleSelectionMode(boolean value);

    String getMessageId();
}