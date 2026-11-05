package org.telegram.messenger.svc;

/**
 * Service: process-lifetime unlock flag guarding the app (LaunchActivity).
 *
 * The app may be reached ONLY through the setup screen's tap sequence. Any other entry
 * (Settings "Open", an adb/am launch, a crafted intent, a stale task) hits a locked gate in
 * LaunchActivity.onCreate and is bounced back to the setup.
 *
 * - Reaching the setup screen LOCKS (so the wall is always the starting point).
 * - The tap sequence UNLOCKS, then launches the app.
 * - Backgrounding the app (not a config change) re-LOCKS.
 * - Process death resets to locked for free (static state is gone).
 *
 * Deliberately in-memory only: nothing persisted, no disk trace.
 */
public final class SessionState {

    private static volatile boolean unlocked = false;

    private SessionState() {}

    public static void unlock() {
        unlocked = true;
    }

    public static void lock() {
        unlocked = false;
    }

    public static boolean isUnlocked() {
        return unlocked;
    }
}
