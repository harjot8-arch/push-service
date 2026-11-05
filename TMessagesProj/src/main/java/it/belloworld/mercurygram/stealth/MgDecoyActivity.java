package it.belloworld.mercurygram.stealth;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.telegram.messenger.R;
import org.telegram.ui.LaunchActivity;

/**
 * MG stealth: the fake "system service" wall shown when the Quick Settings tile is tapped.
 * A snoop sees a plausible, dead-end service screen. The real app opens only after the
 * secret tap sequence (default: 5 taps on the status line within 3s).
 *
 * The screen is built entirely in code with literal fallbacks, so it can NEVER render as a
 * blank white window: if a string resource or anything else fails, hardcoded text is shown.
 */
public class MgDecoyActivity extends Activity {

    private static final int SECRET_TAPS = 5;
    private static final long SECRET_WINDOW_MS = 3000;

    // Literal fallbacks: used if a string resource fails to resolve for any reason.
    private static final String FB_TITLE = "Media Service";
    private static final String FB_BODY = "This service manages background media caching. No user action is required.";
    private static final String FB_STATUS = "Status: connected";

    private final MgSecretTap gate = new MgSecretTap(SECRET_TAPS, SECRET_WINDOW_MS);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Reaching the decoy always means "locked": the real app is gone until a fresh tap.
        MgGate.lock();

        // Build content FIRST so a failure here can't leave a blank window; FLAG_SECURE after.
        setContentView(buildView());
        try {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,
                    WindowManager.LayoutParams.FLAG_SECURE);
        } catch (Throwable ignored) {
        }
    }

    private View buildView() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setBackgroundColor(Color.parseColor("#FAFAFA"));
        int pad = dp(24);
        root.setPadding(pad, pad, pad, pad);

        TextView title = new TextView(this);
        title.setText(str(R.string.MgDecoyTitle, FB_TITLE));
        title.setTextColor(Color.parseColor("#202124"));
        title.setTextSize(22);
        title.setGravity(Gravity.CENTER);
        root.addView(title);

        TextView body = new TextView(this);
        body.setText(str(R.string.MgDecoyBody, FB_BODY));
        body.setTextColor(Color.parseColor("#5F6368"));
        body.setTextSize(15);
        body.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams bodyLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        bodyLp.topMargin = dp(12);
        root.addView(body, bodyLp);

        // The status line is the secret hit target.
        TextView status = new TextView(this);
        status.setText(str(R.string.MgDecoyStatus, FB_STATUS));
        status.setTextColor(Color.parseColor("#9AA0A6"));
        status.setTextSize(13);
        status.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams statusLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        statusLp.topMargin = dp(32);
        status.setOnClickListener(this::onSecretTap);
        root.addView(status, statusLp);

        return root;
    }

    /** Resolve a string resource, falling back to a literal if it is missing/empty/throws. */
    private String str(int resId, String fallback) {
        try {
            String v = getString(resId);
            if (v != null && v.length() > 0) return v;
        } catch (Throwable ignored) {
        }
        return fallback;
    }

    private void onSecretTap(View v) {
        try {
            if (gate.tap(SystemClock.uptimeMillis())) {
                MgGate.unlock();
                Intent intent = new Intent(this, LaunchActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        } catch (Throwable ignored) {
            // Never crash the decoy: a reveal risk. A failed unlock just does nothing.
        }
    }

    private int dp(int v) {
        return (int) (v * getResources().getDisplayMetrics().density + 0.5f);
    }
}
