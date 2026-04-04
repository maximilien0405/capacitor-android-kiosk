package ee.forgr.plugin.android_kiosk;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * Watchdog alarm tick: tries to start the launcher activity through
 * {@link KioskRelaunchHelper#startLaunchActivity} (which skips {@code startActivity} while the display
 * is off). Then {@link KioskWatchdogScheduler#scheduleNext} re-arms the next alarm unless the watchdog
 * is disabled—{@link #onReceive} returns early when the enabled pref is false, so {@code scheduleNext}
 * is never called. When enabled, the schedule advances even if the activity start was skipped or blocked.
 */
public class KioskWatchdogReceiver extends BroadcastReceiver {

    private static final String TAG = "KioskWatchdog";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (context == null) {
            return;
        }

        SharedPreferences prefs = context.getSharedPreferences(KioskPrefs.PREFS_NAME, Context.MODE_PRIVATE);
        if (!prefs.getBoolean(KioskWatchdogScheduler.KEY_WATCHDOG_ENABLED, false)) {
            Log.w(TAG, "watchdog disabled in prefs; not re-arming");
            return;
        }

        Intent launchIntent = KioskLaunchIntents.resolveLaunchIntent(context);
        if (launchIntent != null) {
            try {
                KioskRelaunchHelper.startLaunchActivity(context, launchIntent);
            } catch (Exception e) {
                Log.e(TAG, "watchdog startActivity failed: " + e.getMessage(), e);
            }
        } else {
            Log.w(TAG, "no launch intent");
        }

        KioskWatchdogScheduler.scheduleNext(context);
    }
}
