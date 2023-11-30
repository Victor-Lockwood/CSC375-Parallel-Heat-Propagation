import javax.swing.*;

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
    public static int threshold;

    public static boolean debug = false;
    //</editor-fold>

    /**
     * Grid to read from.
     */
    static volatile Grid readGrid;

    /**
     * Grid to write to.
     */
    static volatile Grid writeGrid;

    static volatile double highestTemp = 0;

    /**
     * Specs: https://gee.cs.oswego.edu/dl/csc375/a3V2.html
     * @param args
     */
    public static void main(String[] args) throws InterruptedException {
        setInputVars(args);

        readGrid = new Grid(height, width);
        writeGrid = new Grid(readGrid);

        setGui();

        for(int i = 0; i < threshold; i++) {
            Thread.sleep(1000);
            readGrid.calculateNewTemperature();

            Grid swapGrid = readGrid;
            readGrid = writeGrid;
            writeGrid = swapGrid;
        }

        return;
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
            threshold = Integer.parseInt(args[6]);

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


    /**
     * Sets up the GUI and gets it rolling.
     */
    private static void setGui() {
        Gui gui = new Gui(60, 10, 0, 0);

        //Thank you to Scarlett Weeks for the original.
        //Pulled and tweaked from my project 1 code.
        SwingUtilities.invokeLater(() -> {
            JFrame window = new JFrame();
            window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            //Roll with the block offset here, it's annoying but this gets the job done.
            int fullWidth = (gui.blockLength * width) + gui.blockOffset * 6;
            int fullHeight = (gui.blockLength * height) + gui.blockOffset * 6;

            window.setBounds(0, 0, fullWidth, fullHeight);

            window.getContentPane().add(gui);
            window.setVisible(true);
        });

        //The actual thing that makes sure the display updates.
        new Timer(1,
                event->gui.repaint()
        ).start();
    }
}