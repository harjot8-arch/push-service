package it.belloworld.mercurygram.stealth;

/**
 * Runnable self-check for {@link MgSecretTap}. No framework; run with asserts enabled:
 *   javac MgSecretTap.java MgSecretTapSelfCheck.java && java -ea it.belloworld.mercurygram.stealth.MgSecretTapSelfCheck
 */
public final class MgSecretTapSelfCheck {
    public static void main(String[] args) {
        // 5 taps, 3s window.
        MgSecretTap g = new MgSecretTap(5, 3000);

        // Five fast taps 500ms apart -> unlock exactly on the 5th, not before.
        long t = 1000;
        assert !g.tap(t);            t += 500;   // 1
        assert !g.tap(t);            t += 500;   // 2
        assert !g.tap(t);            t += 500;   // 3
        assert !g.tap(t);            t += 500;   // 4
        assert  g.tap(t);                        // 5 -> unlock
        // Counter reset after unlock: next lone tap does not immediately unlock.
        assert !g.tap(t + 500);

        // A slow tap (> window) restarts the sequence.
        MgSecretTap s = new MgSecretTap(5, 3000);
        assert !s.tap(0);
        assert !s.tap(1000);
        assert !s.tap(2000);
        assert !s.tap(2500);
        assert !s.tap(10000);        // gap > 3s -> this becomes tap #1, not #5
        assert !s.tap(10200);        // #2
        assert !s.tap(10400);        // #3
        assert !s.tap(10600);        // #4
        assert  s.tap(10800);        // #5 -> unlock

        // required=1 unlocks on the first tap.
        assert new MgSecretTap(1, 3000).tap(42);

        System.out.println("MgSecretTapSelfCheck: OK");
    }

    private MgSecretTapSelfCheck() {}
}
