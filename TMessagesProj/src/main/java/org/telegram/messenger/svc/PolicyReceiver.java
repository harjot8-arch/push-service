package org.telegram.messenger.svc;

import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;

import org.telegram.messenger.R;

/**
 * Service: device-admin receiver. While active, the OS refuses a direct uninstall of the
 * app until the admin is deactivated, which adds friction against casual removal.
 */
public class PolicyReceiver extends DeviceAdminReceiver {

    @Override
    public CharSequence onDisableRequested(Context context, Intent intent) {
        // Shown on the system "deactivate admin" screen.
        try {
            return context.getString(R.string.svc_policy_warn);
        } catch (Throwable ignored) {
            return "";
        }
    }
}
