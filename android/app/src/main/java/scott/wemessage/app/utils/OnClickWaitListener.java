package scott.wemessage.app.utils;

import android.os.SystemClock;
import android.view.View;

import java.util.Map;
import java.util.WeakHashMap;

public abstract class OnClickWaitListener implements View.OnClickListener {

    private final long minimumInterval;
    private Map<View, Long> lastClickMap;

    public abstract void onWaitClick(View v);

    public OnClickWaitListener(long minimumIntervalMillisec) {
        this.minimumInterval = minimumIntervalMillisec;
        this.lastClickMap = new WeakHashMap<View, Long>();
    }

    @Override
    public void onClick(View clickedView) {
        Long previousClickTimestamp = lastClickMap.get(clickedView);
        long currentTimestamp = SystemClock.uptimeMillis();

        lastClickMap.put(clickedView, currentTimestamp);

        if(previousClickTimestamp == null || (currentTimestamp - previousClickTimestamp.longValue() > minimumInterval)) {
            onWaitClick(clickedView);
        }
    }
}