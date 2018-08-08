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

import android.content.Context;
import android.util.AttributeSet;

import com.afollestad.easyvideoplayer.EasyVideoPlayer;

public class VideoAttachmentPlayer extends EasyVideoPlayer {

    private OnClickCallback clickCallback;

    public VideoAttachmentPlayer(Context context) {
        super(context);
    }

    public VideoAttachmentPlayer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public VideoAttachmentPlayer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setClickCallback(OnClickCallback clickCallback){
        this.clickCallback = clickCallback;
    }

    @Override
    public void showControls() {
        super.showControls();

        if (clickCallback != null){
            clickCallback.onShowControls();
        }
    }

    @Override
    public void hideControls() {
        super.hideControls();

        if (clickCallback != null){
            clickCallback.onHideControls();
        }
    }

    public interface OnClickCallback {

        void onShowControls();

        void onHideControls();
    }
}