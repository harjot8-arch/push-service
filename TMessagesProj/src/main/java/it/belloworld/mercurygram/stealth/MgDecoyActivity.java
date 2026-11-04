package it.belloworld.mercurygram.stealth;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.telegram.messenger.R;
import org.telegram.ui.LaunchActivity;

/**
 * MG stealth: the fake "system service" wall shown when the Quick Settings tile is tapped.
 * A snoop sees a plausible, dead-end service screen. The real app opens only after the
 * secret tap sequence (default: 5 taps on the status line within 3s).
 */
public class MgDecoyActivity extends Activity {

    private static final int SECRET_TAPS = 5;
    private static final long SECRET_WINDOW_MS = 3000;

    private final MgSecretTap gate = new MgSecretTap(SECRET_TAPS, SECRET_WINDOW_MS);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Keep the decoy (and whatever opens behind it) out of screenshots / recents previews.
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE);

        try {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setBackgroundColor(Color.parseColor("#FAFAFA"));
        int pad = dp(24);
        root.setPadding(pad, pad, pad, pad);

        TextView title = new TextView(this);
        title.setText(R.string.MgDecoyTitle);
        title.setTextColor(Color.parseColor("#202124"));
        title.setTextSize(22);
        title.setGravity(Gravity.CENTER);

        TextView body = new TextView(this);
        body.setText(R.string.MgDecoyBody);
        body.setTextColor(Color.parseColor("#5F6368"));
        body.setTextSize(15);
        body.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams bodyLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        bodyLp.topMargin = dp(12);
        body.setLayoutParams(bodyLp);

        // The status line is the secret hit target.
        TextView status = new TextView(this);
        status.setText(R.string.MgDecoyStatus);
        status.setTextColor(Color.parseColor("#9AA0A6"));
        status.setTextSize(13);
        status.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams statusLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        statusLp.topMargin = dp(32);
        status.setLayoutParams(statusLp);
        status.setOnClickListener(this::onSecretTap);

        // Hidden control screen: long-press the title opens uninstall protection / in-app uninstall.
        title.setOnLongClickListener(v -> {
            try {
                startActivity(new Intent(this, MgProtectionActivity.class));
            } catch (Throwable ignored) {
            }
            return true;
        });

        root.addView(title);
        root.addView(body);
        root.addView(status);
        setContentView(root);
        } catch (Throwable t) {
            // Fail safe: show a blank neutral screen rather than crash a "service" app.
            android.widget.FrameLayout blank = new android.widget.FrameLayout(this);
            blank.setBackgroundColor(Color.parseColor("#FAFAFA"));
            setContentView(blank);
        }
    }

    private void onSecretTap(View v) {
        try {
            if (gate.tap(SystemClock.uptimeMillis())) {
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
