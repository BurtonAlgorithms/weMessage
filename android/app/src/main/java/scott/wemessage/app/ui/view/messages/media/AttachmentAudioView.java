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

package scott.wemessage.app.ui.view.messages.media;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.concurrent.TimeUnit;

import scott.wemessage.R;
import scott.wemessage.app.AppLogger;
import scott.wemessage.app.models.messages.Attachment;
import scott.wemessage.app.models.sms.messages.MmsMessage;
import scott.wemessage.app.ui.ConversationFragment;
import scott.wemessage.app.ui.view.messages.MessageView;
import scott.wemessage.app.utils.IOUtils;
import scott.wemessage.app.utils.media.AudioAttachmentMediaPlayer;
import scott.wemessage.app.weMessage;

public class AttachmentAudioView extends AttachmentView {

    private boolean isInit = false;

    private String attachmentUuid = "";
    private AudioCounterHandler currentAudioLoopHandler;

    private int currentPositionMillisecond = 0;
    private int durationMillisecond = 0;

    private ViewGroup attachmentAudioBubble;
    private ImageView playImage;
    private ImageView errorBubble;
    private TextView audioCounterView;
    private TextView durationView;

    private int defaultBubblePaddingLeft = getContext().getResources().getDimensionPixelSize(R.dimen.message_padding_left);
    private int defaultPaddingPaddingRight = getContext().getResources().getDimensionPixelSize(R.dimen.message_padding_right);
    private int defaultBubblePaddingTop = getContext().getResources().getDimensionPixelSize(R.dimen.message_padding_top);
    private int defaultBubblePaddingBottom = getContext().getResources().getDimensionPixelSize(R.dimen.message_padding_bottom);

    public AttachmentAudioView(Context context) {
        super(context);
    }

    public AttachmentAudioView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AttachmentAudioView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void bind(MessageView messageView, final Attachment attachment, final MessageType messageType, boolean isErrored) {
        init();
        applyStyle(messageType, messageView.getMessage() instanceof MmsMessage);
        attachmentAudioBubble.setSelected(isSelected());

        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) attachmentAudioBubble.getLayoutParams();

        if (isErrored && messageType == MessageType.OUTGOING){
            errorBubble.setVisibility(VISIBLE);
            layoutParams.removeRule(RelativeLayout.ALIGN_PARENT_END);
            layoutParams.addRule(RelativeLayout.START_OF, R.id.errorBubble);
        }else {
            errorBubble.setVisibility(GONE);
            layoutParams.removeRule(RelativeLayout.START_OF);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_END);
        }

        if (messageType == MessageType.INCOMING) {
            layoutParams.removeRule(RelativeLayout.ALIGN_PARENT_END);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_START);
        }

        attachmentAudioBubble.setLayoutParams(layoutParams);

        new AsyncTask<Attachment, Void, AudioTrackMetadata>(){
            AudioAttachmentMediaPlayer audioAttachmentMediaPlayer;

            @Override
            protected void onPreExecute() {
                audioAttachmentMediaPlayer = getParentFragment().getAudioAttachmentMediaPlayer();
                super.onPreExecute();
            }

            @Override
            protected AudioTrackMetadata doInBackground(Attachment... params) {
                MediaMetadataRetriever mmr = new MediaMetadataRetriever();

                mmr.setDataSource(weMessage.get(), IOUtils.getUriFromFile(params[0].getFileLocation().getFile()));
                String durationStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);

                int millSecond = Integer.parseInt(durationStr);
                boolean isPlaying = audioAttachmentMediaPlayer.getAttachment() != null && audioAttachmentMediaPlayer.getAttachment().getUuid().toString().equals(params[0].getUuid().toString());

                mmr.release();

                AudioTrackMetadata audioTrackMetadata = new AudioTrackMetadata();
                audioTrackMetadata.attachmentUuid = params[0].getUuid().toString();
                audioTrackMetadata.durationMillisecond = millSecond;
                audioTrackMetadata.isPlaying = isPlaying;

                if (isPlaying){
                    audioTrackMetadata.currentPositionMillisecond = audioAttachmentMediaPlayer.getCurrentPosition();
                }

                return audioTrackMetadata;
            }

            @Override
            protected void onPostExecute(final AudioTrackMetadata audioTrackMetadata) {
                if (getContext() instanceof Activity && ((Activity) getContext()).isDestroyed()) return;

                if (messageType == MessageType.INCOMING) {
                    if (audioTrackMetadata.isPlaying) {
                        playImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_pause_button_black));
                    } else {
                        playImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_play_arrow_black));
                    }
                } else {
                    if (audioTrackMetadata.isPlaying) {
                        playImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_pause_button_white));
                    } else {
                        playImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_play_arrow_white));
                    }
                }

                attachmentUuid = audioTrackMetadata.attachmentUuid;
                currentPositionMillisecond = audioTrackMetadata.currentPositionMillisecond;
                durationMillisecond = audioTrackMetadata.durationMillisecond;

                String durationFormat = String.format(getResources().getConfiguration().locale, "%d:%02d", TimeUnit.MILLISECONDS.toMinutes(audioTrackMetadata.durationMillisecond),
                        TimeUnit.MILLISECONDS.toSeconds(audioTrackMetadata.durationMillisecond) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(audioTrackMetadata.durationMillisecond)));

                String countFormat = String.format(getResources().getConfiguration().locale, "%d:%02d",
                        TimeUnit.MILLISECONDS.toMinutes(currentPositionMillisecond),
                        TimeUnit.MILLISECONDS.toSeconds(currentPositionMillisecond)
                                - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(currentPositionMillisecond)));

                audioCounterView.setText(countFormat);
                durationView.setText(" / " + durationFormat);

                if (audioTrackMetadata.isPlaying){
                    startCountHandler();
                }
            }
        }.execute(attachment);

        attachmentAudioBubble.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    ConversationFragment conversationFragment = getParentFragment();
                    AudioAttachmentMediaPlayer audioAttachmentMediaPlayer = conversationFragment.getAudioAttachmentMediaPlayer();

                    if (playImage.getDrawable().getConstantState().equals(getResources().getDrawable(R.drawable.ic_play_arrow_black).getConstantState()) ||
                            playImage.getDrawable().getConstantState().equals(getResources().getDrawable(R.drawable.ic_play_arrow_white).getConstantState())) {

                        if (audioAttachmentMediaPlayer.hasAudio()) {
                            if (audioAttachmentMediaPlayer.getAttachment().getUuid().toString().equals(attachment.getUuid().toString())){

                                if (conversationFragment.resumeAudio(attachment)){
                                    if (messageType == MessageType.INCOMING){
                                        playImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_pause_button_black));
                                    }else {
                                        playImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_pause_button_white));
                                    }
                                }
                            }else {
                                conversationFragment.stopAudio();
                                conversationFragment.playAudio(attachment);
                            }
                        }else {
                            currentPositionMillisecond = 0;
                            conversationFragment.playAudio(attachment);
                        }

                    }else if (playImage.getDrawable().getConstantState().equals(getResources().getDrawable(R.drawable.ic_pause_button_black).getConstantState()) ||
                            playImage.getDrawable().getConstantState().equals(getResources().getDrawable(R.drawable.ic_pause_button_white).getConstantState())) {

                        if (conversationFragment.pauseAudio(attachment)){
                            if (messageType == MessageType.INCOMING){
                                playImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_play_arrow_black));
                            }else {
                                playImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_play_arrow_white));
                            }
                        }
                    }
                }catch (Exception ex){
                    Intent broadcastIntent = new Intent(weMessage.BROADCAST_PLAY_AUDIO_ATTACHMENT_ERROR);
                    LocalBroadcastManager.getInstance(getContext()).sendBroadcast(broadcastIntent);

                    AppLogger.error("An error occurred while trying to play an audio attachment", ex);
                }
            }
        });
    }

    public void unbind(){
        if (currentAudioLoopHandler != null){
            currentAudioLoopHandler.setStopped(true);
        }
    }

    public String getAttachmentUuid(){
        return attachmentUuid;
    }

    public void notifyAudioStart(MessageType messageType){
        if (messageType == MessageType.INCOMING) {
            playImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_pause_button_black));
        }else {
            playImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_pause_button_white));
        }
        startCountHandler();
    }

    public void notifyAudioFinish(MessageType messageType){
        if (messageType == MessageType.INCOMING) {
            playImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_play_arrow_black));
        }else {
            playImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_play_arrow_white));
        }
    }

    private void applyStyle(MessageType messageType, boolean isMms) {
        if (messageType == MessageType.INCOMING) {
            audioCounterView.setTextColor(Color.BLACK);
            durationView.setTextColor(Color.BLACK);
            attachmentAudioBubble.setPadding(defaultBubblePaddingLeft, defaultBubblePaddingTop, defaultPaddingPaddingRight, defaultBubblePaddingBottom);
            ViewCompat.setBackground(attachmentAudioBubble, getIncomingBubbleDrawable());
        }else {
            audioCounterView.setTextColor(Color.WHITE);
            durationView.setTextColor(Color.WHITE);
            attachmentAudioBubble.setPadding(defaultBubblePaddingLeft, defaultBubblePaddingTop, defaultPaddingPaddingRight, defaultBubblePaddingBottom);
            ViewCompat.setBackground(attachmentAudioBubble, getOutgoingBubbleDrawable(isMms));
        }
    }

    private void init(){
        if (!isInit){
            attachmentAudioBubble = findViewById(R.id.attachmentAudioBubble);
            playImage = findViewById(R.id.audioPlayImage);
            audioCounterView = findViewById(R.id.audioCounterView);
            durationView = findViewById(R.id.audioDurationView);
            errorBubble = findViewById(R.id.errorBubble);

            isInit = true;
        }
    }

    private Drawable getIncomingBubbleDrawable() {
        return getMessageSelector(getContext().getResources().getColor(R.color.incomingBubbleColor), getContext().getResources().getColor(R.color.incomingBubbleColorPressed),
                getContext().getResources().getColor(R.color.incomingBubbleColorPressed), R.drawable.shape_incoming_message);
    }

    private Drawable getOutgoingBubbleDrawable(boolean isMms) {
        if (isMms){
            return getMessageSelector(getContext().getResources().getColor(R.color.outgoingBubbleColorOrange), getContext().getResources().getColor(R.color.outgoingBubbleColorOrangePressed),
                    getContext().getResources().getColor(R.color.outgoingBubbleColorOrangePressed), R.drawable.shape_outcoming_message);
        }else {
            return getMessageSelector(getContext().getResources().getColor(R.color.outgoingBubbleColor), getContext().getResources().getColor(R.color.outgoingBubbleColorPressed),
                    getContext().getResources().getColor(R.color.outgoingBubbleColorPressed), R.drawable.shape_outcoming_message);
        }
    }

    private void startCountHandler() {
        if (currentAudioLoopHandler != null){
            currentAudioLoopHandler.setStopped(true);
            currentAudioLoopHandler = null;
        }
        currentAudioLoopHandler = new AudioCounterHandler();
        currentAudioLoopHandler.runTask();
    }

    private int round(double value, double multiple){
        return (int) (Math.round(value / multiple) * multiple);
    }

    private class AudioTrackMetadata {
        String attachmentUuid;
        int durationMillisecond;
        int currentPositionMillisecond = 0;
        boolean isPlaying;
    }

    private class AudioCounterHandler {
        private boolean isStopped = false;
        private boolean firstLoop = false;
        private Handler handler;

        AudioCounterHandler(){
            handler = new Handler();
        }

        void setStopped(boolean stopped) {
            isStopped = stopped;
        }

        void runTask(){
            firstLoop = false;

            handler.postDelayed(new Runnable() {
                public void run() {
                    try {
                        if (!isStopped) {
                            if (currentPositionMillisecond < durationMillisecond && getParentFragment().getAudioAttachmentMediaPlayer().isPlaying()) {
                                if (!firstLoop){
                                    firstLoop = true;
                                    currentPositionMillisecond = getParentFragment().getAudioAttachmentMediaPlayer().getCurrentPosition();
                                }else {
                                    currentPositionMillisecond += 1000;
                                }

                                int roundedMillis = round(currentPositionMillisecond, 1000);

                                String countFormat = String.format(getResources().getConfiguration().locale, "%d:%02d",
                                        TimeUnit.MILLISECONDS.toMinutes(roundedMillis),
                                        TimeUnit.MILLISECONDS.toSeconds(roundedMillis)
                                                - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(roundedMillis)));
                                audioCounterView.setText(countFormat);
                                handler.postDelayed(this, 1000);
                            }
                        }
                    }catch(Exception ex){ }
                }
            }, 1000);
        }
    }
}