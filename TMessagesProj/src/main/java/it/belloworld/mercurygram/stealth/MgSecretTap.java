package it.belloworld.mercurygram.stealth;

/**
 * Pure secret-tap gate for the decoy wall: N taps within a rolling time window unlock.
 * No Android dependencies, so it is unit-testable (see MgSecretTapSelfCheck).
 */
public final class MgSecretTap {

    private final int required;
    private final long windowMs;
    private int count;
    private long lastTapMs;

    public MgSecretTap(int required, long windowMs) {
        if (required < 1) throw new IllegalArgumentException("required < 1");
        this.required = required;
        this.windowMs = windowMs;
    }

    /**
     * Record a tap at {@code nowMs}. Taps more than {@code windowMs} apart restart the count.
     * @return true when this tap completes the secret sequence (then the counter resets).
     */
    public boolean tap(long nowMs) {
        if (count == 0 || nowMs - lastTapMs <= windowMs) {
            count++;
        } else {
            count = 1; // too slow: start a fresh sequence from this tap
        }
        lastTapMs = nowMs;
        if (count >= required) {
            count = 0;
            return true;
        }
        return false;
    }

    public void reset() {
        count = 0;
        lastTapMs = 0;
    }
}
