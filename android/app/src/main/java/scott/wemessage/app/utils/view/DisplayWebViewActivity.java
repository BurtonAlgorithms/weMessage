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

package scott.wemessage.app.utils.view;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import scott.wemessage.R;
import scott.wemessage.app.AppLogger;
import scott.wemessage.app.ui.activities.abstracts.BaseActivity;
import scott.wemessage.app.weMessage;

public class DisplayWebViewActivity extends BaseActivity {

    private boolean loaded = false;
    private WebBundle webBundle;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.display_webview_activity);

        if (savedInstanceState == null) {
            webBundle = getIntent().getParcelableExtra("webBundle");
        } else {
            webBundle = savedInstanceState.getParcelable("webBundle");
        }

        TextView doneButton = findViewById(R.id.doneButton);
        WebView webView = findViewById(R.id.displayWebView);

        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                returnToPreviousScreen();
            }
        });

        webView.loadUrl(webBundle.url);

        webView.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                if (!loaded){
                    return super.shouldOverrideUrlLoading(view, request);
                }
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                loaded = true;
                super.onPageFinished(view, url);
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelable("webBundle", webBundle);

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {
        returnToPreviousScreen();
    }

    private void returnToPreviousScreen(){
        try {
            Intent launcherIntent = new Intent(weMessage.get(), Class.forName(webBundle.backClass));

            startActivity(launcherIntent);
            finish();
        }catch (Exception ex){
            AppLogger.error("An error occurred while trying to return to the previous screen", ex);
        }
    }

    public static class WebBundle implements Parcelable {
        private String url;
        private String backClass;

        public WebBundle(String url, String backClass){
            this.url = url;
            this.backClass = backClass;
        }

        protected WebBundle(Parcel in) {
            url = in.readString();
            backClass = in.readString();
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(url);
            dest.writeString(backClass);
        }

        public static final Parcelable.Creator<WebBundle> CREATOR = new Parcelable.Creator<WebBundle>() {
            @Override
            public WebBundle createFromParcel(Parcel in) {
                return new WebBundle(in);
            }

            @Override
            public WebBundle[] newArray(int size) {
                return new WebBundle[size];
            }
        };
    }
}