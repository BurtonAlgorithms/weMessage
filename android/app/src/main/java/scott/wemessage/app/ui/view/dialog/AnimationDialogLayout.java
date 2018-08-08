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

package scott.wemessage.app.ui.view.dialog;

import android.content.Context;
import android.net.Uri;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.VideoView;

import scott.wemessage.R;

public class AnimationDialogLayout extends LinearLayout {

    public AnimationDialogLayout(Context context){
        super(context);
    }

    public AnimationDialogLayout(Context context, AttributeSet attributeSet){
        super(context, attributeSet);
    }

    public AnimationDialogLayout(Context context, AttributeSet attributeSet, int defStyle){
        super(context, attributeSet, defStyle);
    }

    public void setAnimationSource(int videoFile){
        Uri uri = Uri.parse("android.resource://" + getContext().getPackageName() + "/" + videoFile);
        VideoView videoView = findViewById(R.id.animationDialogVideoView);
        videoView.setVideoURI(uri);
    }

    public VideoView getVideoView(){
        return (VideoView) findViewById(R.id.animationDialogVideoView);
    }

    public void startAnimation(){
        VideoView videoView = findViewById(R.id.animationDialogVideoView);
        videoView.seekTo(0);
        videoView.start();
    }
}