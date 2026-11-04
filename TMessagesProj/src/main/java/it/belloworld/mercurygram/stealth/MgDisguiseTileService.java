package it.belloworld.mercurygram.stealth;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.service.quicksettings.TileService;

/**
 * MG stealth: the app has no launcher icon, so this Quick Settings tile is the only entry.
 * Tapping it opens the decoy wall (MgDecoyActivity); the real app is reached from there via
 * the secret tap sequence.
 */
public class MgDisguiseTileService extends TileService {

    @Override
    public void onClick() {
        super.onClick();
        // Never let the tile crash: a crash here would draw attention to a "service" app.
        try {
            Intent intent = new Intent(this, MgDecoyActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                // Android 14+: startActivityAndCollapse requires a PendingIntent.
                PendingIntent pi = PendingIntent.getActivity(
                        this, 0, intent,
                        PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
                startActivityAndCollapse(pi);
            } else {
                // Older: unlock first if needed, then launch.
                //noinspection deprecation
                unlockAndRun(() -> {
                    try {
                        //noinspection deprecation
                        startActivityAndCollapse(intent);
                    } catch (Throwable ignored) {
                    }
                });
            }
        } catch (Throwable ignored) {
            // Swallow: better to do nothing than to crash a disguised service.
        }
    }
}
