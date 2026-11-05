package it.belloworld.mercurygram.stealth;

/**
 * Runnable self-check for {@link MgGate}. No framework; run with asserts enabled:
 *   javac MgGate.java MgGateSelfCheck.java && java -ea it.belloworld.mercurygram.stealth.MgGateSelfCheck
 *
 * Mirrors the real entry state machine:
 *   fresh -> locked; decoy shown -> locked; secret tap -> unlocked;
 *   real app opens while unlocked; background (lock) -> locked again.
 */
public final class MgGateSelfCheck {
    public static void main(String[] args) {
        // Fresh process: gate is locked, so a direct LaunchActivity start would bounce.
        MgGate.lock();
        assert !MgGate.isUnlocked() : "fresh state must be locked";

        // Reaching the decoy keeps it locked.
        MgGate.lock();
        assert !MgGate.isUnlocked();

        // Secret tap unlocks; real app start is now allowed.
        MgGate.unlock();
        assert MgGate.isUnlocked() : "secret tap must unlock";

        // Config change (rotation / recreate) does NOT lock -> user not kicked mid-use.
        // (modelled by simply not calling lock())
        assert MgGate.isUnlocked();

        // Real background re-locks.
        MgGate.lock();
        assert !MgGate.isUnlocked() : "background must re-lock";

        // Re-entry requires a fresh tap again.
        assert !MgGate.isUnlocked();
        MgGate.unlock();
        assert MgGate.isUnlocked();

        // Leave the static in a safe (locked) state.
        MgGate.lock();

        System.out.println("MgGateSelfCheck: OK");
    }

    private MgGateSelfCheck() {}
}
