public class Main {

    //<editor-fold desc="Scaffolding Variables">
    /**
     * Temp to heat top left corner in degrees Celsius.
     */
    public static double S;

    /**
     * Temp to heat bottom right corner in degrees Celsius.
     */
    public static double T;

    /**
     * Thermal constant for metal 1.
     */
    public static double C1;

    /**
     * Thermal constant for metal 2.
     */
    public static double C2;

    /**
     * Thermal constant for metal 3.
     */
    public static double C3;

    /**
     * Height of grid.
     */
    public static int height;

    /**
     * Width for grid.  Should be 4 * height.
     */
    public static int width;

    /**
     * When to stop iterating.
     */
    public static double threshold;

    public static boolean debug = false;
    //</editor-fold>

    /**
     * Specs: https://gee.cs.oswego.edu/dl/csc375/a3V2.html
     * @param args
     */
    public static void main(String[] args) {
        setInputVars(args);
    }

    /**
     * Set our main program variables based on input.
     * Broken out to keep things clean.
     * @param args Commandline arguments.
     */
    private static void setInputVars(String[] args) {
        if(args.length < 7) {
            System.out.println("Should have 7 integer arguments.");
            System.exit(1);
        }

        try {
            S = Double.parseDouble(args[0]);
            T = Double.parseDouble(args[1]);
            C1 = Double.parseDouble(args[2]);
            C2 = Double.parseDouble(args[3]);
            C3 = Double.parseDouble(args[4]);
            height = Integer.parseInt(args[5]);
            width = height * 4;
            threshold = Double.parseDouble(args[6]);

            if(args.length > 7) debug = Boolean.parseBoolean(args[7]);

        } catch (NumberFormatException e) {
            System.err.println("Argument" + args[0] + " must be a number.");
            System.exit(1);
        }

        if(debug) {
            printDebugStats();
        }
    }


    /**
     * Makes sure we've gotten our inputs correctly.
     */
    private static void printDebugStats() {
        System.out.println("S:  " + S);
        System.out.println("T:  " + T);
        System.out.println("C1: " + C1);
        System.out.println("C2: " + C2);
        System.out.println("C3: " + C3);
        System.out.println("Height: " + height);
        System.out.println("Width: " + width);
        System.out.println("Threshold: " + threshold);
    }
}