package scott.wemessage.app.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;

import scott.wemessage.R;
import scott.wemessage.app.utils.OnClickWaitListener;
import scott.wemessage.app.utils.view.DisplayWebViewActivity;
import scott.wemessage.app.weMessage;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        Toolbar toolbar = findViewById(R.id.aboutToolbar);
        ImageView homeButton = toolbar.findViewById(R.id.aboutSettingsButton);

        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToSettings();
            }
        });

        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        findViewById(R.id.aboutFAQ).setOnClickListener(new OnClickWaitListener(1000L) {
            @Override
            public void onWaitClick(View v) {
                launchWebView("https://wemessageapp.com/faq");
            }
        });

        findViewById(R.id.aboutPrivacy).setOnClickListener(new OnClickWaitListener(1000L) {
            @Override
            public void onWaitClick(View v) {
                launchWebView("https://wemessageapp.com/privacy");
            }
        });

        findViewById(R.id.aboutTerms).setOnClickListener(new OnClickWaitListener(1000L) {
            @Override
            public void onWaitClick(View v) {
                launchWebView("https://wemessageapp.com/terms");
            }
        });
    }

    @Override
    public void onBackPressed() {
        goToSettings();
    }

    private void launchWebView(String url){
        Intent launcherIntent = new Intent(weMessage.get(), DisplayWebViewActivity.class);
        launcherIntent.putExtra("webBundle", new DisplayWebViewActivity.WebBundle(url, AboutActivity.class.getName()));
        startActivity(launcherIntent);
        finish();
    }

    private void goToSettings(){
        Intent launcherIntent = new Intent(weMessage.get(), SettingsActivity.class);

        startActivity(launcherIntent);
        finish();
    }
}