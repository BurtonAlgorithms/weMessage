package scott.wemessage.app.utils.view;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import scott.wemessage.R;
import scott.wemessage.app.AppLogger;
import scott.wemessage.app.weMessage;

public class DisplayWebViewActivity extends AppCompatActivity {

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
                    loaded = true;
                    return super.shouldOverrideUrlLoading(view, request);
                }
                return true;
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