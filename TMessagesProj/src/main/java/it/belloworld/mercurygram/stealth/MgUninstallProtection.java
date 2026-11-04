package it.belloworld.mercurygram.stealth;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import org.telegram.messenger.R;

/**
 * MG stealth: helpers for device-admin uninstall protection and the in-app uninstall path.
 *
 * Honest ceiling (non-device-owner phone): an active admin blocks a *casual* uninstall only.
 * The user can still deactivate it from Settings > Security > Device admin apps and then
 * uninstall, and force-stop cannot be blocked at all. A hard block requires device owner
 * (adb dpm set-device-owner on a freshly reset device).
 */
public final class MgUninstallProtection {

    private MgUninstallProtection() {}

    public static ComponentName component(Context ctx) {
        return new ComponentName(ctx.getApplicationContext(), MgAdminReceiver.class);
    }

    private static DevicePolicyManager dpm(Context ctx) {
        return (DevicePolicyManager) ctx.getSystemService(Context.DEVICE_POLICY_SERVICE);
    }

    public static boolean isActive(Context ctx) {
        try {
            DevicePolicyManager dpm = dpm(ctx);
            return dpm != null && dpm.isAdminActive(component(ctx));
        } catch (Throwable ignored) {
            return false;
        }
    }

    /** Intent that opens the system "activate device admin?" screen. */
    public static Intent buildEnableIntent(Context ctx) {
        return new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
                .putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, component(ctx))
                .putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                        ctx.getString(R.string.MgAdminExplanation));
    }

    /**
     * Deactivate the admin (so Android allows removal) and fire the uninstall prompt.
     * This is the in-app "uninstall" path, since the app itself is protected from the outside.
     */
    public static void disableAndUninstall(Context ctx) {
        try {
            DevicePolicyManager dpm = dpm(ctx);
            if (dpm != null && dpm.isAdminActive(component(ctx))) {
                dpm.removeActiveAdmin(component(ctx));
            }
        } catch (Throwable ignored) {
            // Removing admin can be refused on some OEMs; still offer the uninstall prompt.
        }
        try {
            Intent uninstall = new Intent(Intent.ACTION_DELETE,
                    Uri.parse("package:" + ctx.getPackageName()));
            uninstall.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ctx.startActivity(uninstall);
        } catch (Throwable ignored) {
        }
    }
}
