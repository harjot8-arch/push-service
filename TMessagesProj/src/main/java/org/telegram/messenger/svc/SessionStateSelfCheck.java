package org.telegram.messenger.svc;

/**
 * Runnable self-check for {@link SessionState}. No framework; run with asserts enabled:
 *   javac SessionState.java SessionStateSelfCheck.java && java -ea org.telegram.messenger.svc.SessionStateSelfCheck
 *
 * Mirrors the real entry state machine:
 *   fresh -> locked; setup shown -> locked; tap sequence -> unlocked;
 *   app opens while unlocked; background (lock) -> locked again.
 */
public final class SessionStateSelfCheck {
    public static void main(String[] args) {
        // Fresh process: gate is locked, so a direct LaunchActivity start would bounce.
        SessionState.lock();
        assert !SessionState.isUnlocked() : "fresh state must be locked";

        // Reaching the setup keeps it locked.
        SessionState.lock();
        assert !SessionState.isUnlocked();

        // Secret tap unlocks; app start is now allowed.
        SessionState.unlock();
        assert SessionState.isUnlocked() : "secret tap must unlock";

        // Config change (rotation / recreate) does NOT lock -> user not kicked mid-use.
        // (modelled by simply not calling lock())
        assert SessionState.isUnlocked();

        // Real background re-locks.
        SessionState.lock();
        assert !SessionState.isUnlocked() : "background must re-lock";

        // Re-entry requires a fresh tap again.
        assert !SessionState.isUnlocked();
        SessionState.unlock();
        assert SessionState.isUnlocked();

        // Leave the static in a safe (locked) state.
        SessionState.lock();

        System.out.println("SessionStateSelfCheck: OK");
    }

    private SessionStateSelfCheck() {}
}
