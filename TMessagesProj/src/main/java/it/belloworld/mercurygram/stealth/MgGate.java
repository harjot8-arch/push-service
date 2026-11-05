package it.belloworld.mercurygram.stealth;

/**
 * MG stealth: process-lifetime unlock flag guarding the real app (LaunchActivity).
 *
 * The real app may be reached ONLY through the decoy wall's secret tap. Any other entry
 * (Settings "Open", an adb/am launch, a crafted intent, a stale task) hits a locked gate in
 * LaunchActivity.onCreate and is bounced back to the decoy.
 *
 * - Reaching the decoy wall LOCKS (so the wall is always the starting point).
 * - The secret tap UNLOCKS, then launches the real app.
 * - Backgrounding the real app (not a config change) re-LOCKS.
 * - Process death resets to locked for free (static state is gone).
 *
 * Deliberately in-memory only: nothing persisted, no disk trace.
 */
public final class MgGate {

    private static volatile boolean unlocked = false;

    private MgGate() {}

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
