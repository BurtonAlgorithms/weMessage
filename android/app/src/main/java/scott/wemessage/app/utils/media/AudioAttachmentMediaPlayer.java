/*
 *  weMessage - iMessage for Android
 *  Copyright (C) 2018 Roman Scott
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package scott.wemessage.app.utils.media;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;

import java.io.IOException;

import scott.wemessage.app.models.messages.Attachment;
import scott.wemessage.app.weMessage;

public class AudioAttachmentMediaPlayer extends MediaPlayer implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener {

    private AttachmentAudioCallbacks attachmentAudioCallbacks;
    private Attachment attachment;
    private boolean hasAudio = false;
    private int currentPos;

    public AudioAttachmentMediaPlayer(){
        setOnPreparedListener(this);
        setOnCompletionListener(this);
    }

    public Attachment getAttachment() {
        return attachment;
    }

    public boolean hasAudio(){
        return hasAudio;
    }

    public void setAttachment(Attachment attachment) {
        this.attachment = attachment;
    }

    public void startAudioPlayback(Uri uri) throws IOException {
        hasAudio = true;
        setAudioStreamType(AudioManager.STREAM_MUSIC);
        setDataSource(weMessage.get(), uri);
        prepareAsync();
    }

    public void resumeAudioPlayback(){
        seekTo(currentPos);
        start();
    }

    public void pauseAudioPlayback(){
        currentPos = getCurrentPosition();
        pause();
    }

    public void stopAudioPlayback(){
        stop();
        release();
    }

    public void forceRelease(){
        release();
    }

    public void setCallback(AttachmentAudioCallbacks attachmentAudioCallbacks){
        this.attachmentAudioCallbacks = attachmentAudioCallbacks;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        start();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        release();
    }

    @Override
    public void start() throws IllegalStateException {
        super.start();
        attachmentAudioCallbacks.onPlaybackStart(attachment);
    }

    @Override
    public void pause() throws IllegalStateException {
        super.pause();
    }

    @Override
    public void release() {
        if (attachment != null) {
            attachmentAudioCallbacks.onPlaybackStop(attachment.getUuid().toString());
        }
        hasAudio = false;
        attachment = null;

        super.release();
    }

    public interface AttachmentAudioCallbacks {

        void onPlaybackStart(Attachment a);

        void onPlaybackStop(String attachmentUuid);
    }
}