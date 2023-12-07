import java.util.concurrent.Phaser;

/**
 * I was feeling punchy when I named this.
 *
 * Just like regular bonus content, this may be unnecessary.
 */
public class BonusContent {

    /**
     * If it ain't broke don't fix it - grabbed over here, thanks DL:
     * https://gee.cs.oswego.edu/cgi-bin/viewcvs.cgi/jsr166/jsr166/src/test/loops/FJPhaserJacobi.java?revision=1.13&view=markup
     */
    static class MyPhaser extends Phaser {
 	        final int max;
 	        MyPhaser(int steps) { this.max = steps - 1; }
 	        public boolean onAdvance(int phase, int registeredParties) {
                 return phase >= max || registeredParties <= 0;
            }
    }
}
