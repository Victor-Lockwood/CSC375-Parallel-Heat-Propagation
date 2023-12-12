import Server.ServerCell;
import Server.ServerGrid;

import javax.swing.*;
import java.util.ArrayList;
import java.util.concurrent.*;

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

    public static boolean network = false;
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

    static final int NCPUS = Runtime.getRuntime().availableProcessors();

    /**
     * Specs: https://gee.cs.oswego.edu/dl/csc375/a3V2.html
     * @param args
     */
    public static void main(String[] args) throws InterruptedException, ExecutionException {
        setInputVars(args);

        readGrid = new Grid(height, width);
        writeGrid = new Grid(readGrid);

        setGui();

        if(!network) {
            ForkJoinPool fjp = ForkJoinPool.commonPool();

            for(int i = 0; i < threshold; i++) {

                Worker worker = new Worker(readGrid.Cells, readGrid, writeGrid, C1, C2, C3, S, T);
                fjp.invoke(worker);

                fjp.awaitQuiescence(2, TimeUnit.SECONDS);
                Grid swapGrid = readGrid;
                readGrid = writeGrid;
                writeGrid = swapGrid;

                //Too fast otherwise!
                Thread.sleep(10);
            }
        } else {
            int numServers = 3;
            int numRows = readGrid.Cells.length;


            ExecutorService executorService = Executors.newFixedThreadPool(NCPUS);

            for(int i = 0; i < threshold; i++) {
                ArrayList<ClientWorker> workers = new ArrayList<>();

                for(int serverNum = 0; serverNum < numServers; serverNum++) {
                    String hostName = "127.0.0.1"; //"moxie.cs.oswego.edu"; //
                    int portNumber = 26880 + serverNum;

                    int offset = serverNum * (numRows / numServers);
                    if(offset != 0) offset -= 1;

                    ServerCell[][] serverChunk = Chunker.getServerChunk(numServers, readGrid, offset);

                    ServerGrid localReadGrid = new ServerGrid();
                    localReadGrid.Cells = serverChunk;

                    ServerGrid localWriteGrid = new ServerGrid();
                    localWriteGrid.Cells = ServerCell.cloneCellBlockForWrite(serverChunk);

                    Server.NetworkObject networkObject = new Server.NetworkObject(serverChunk, localReadGrid, localWriteGrid, C1, C2, C3, offset, S, T);
                    workers.add(new ClientWorker(networkObject, portNumber, hostName));

                }

                CountDownLatch countDownLatch = new CountDownLatch(workers.size());

                for(ClientWorker worker: workers) {
                    worker.countDownLatch = countDownLatch;
                    executorService.submit(worker);
                }

                countDownLatch.await();


                for(ClientWorker worker: workers) {
                    if(worker.returnedGrid == null) continue;
                    for(ServerCell[] cellLine: worker.returnedGrid.Cells) {
                        if(cellLine == null) continue;
                        for(ServerCell cell: cellLine) {
                            if(cell == null) continue;
                            writeGrid.Cells[cell.rowNumber][cell.colNumber].temperature = cell.temperature;
                        }
                    }
                }

                Grid swapGrid = readGrid;
                readGrid = writeGrid;
                writeGrid = swapGrid;

                //Too fast otherwise!
                Thread.sleep(100);
            }

            return;
        }

        System.out.println("Threshold reached.");

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
            if(args.length > 8) network = Boolean.parseBoolean(args[8]);

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
        Gui gui = new Gui(10, 10, 0, 0);

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