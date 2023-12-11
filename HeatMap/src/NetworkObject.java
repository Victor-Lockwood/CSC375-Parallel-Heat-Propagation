/**
 * All the stuff to send down the pipe.
 */
public class NetworkObject {
    Cell[][] cells;

    final Grid readGrid;

    final Grid writeGrid;

    final double[] metalConstants;

    int offset = 0;

    public NetworkObject(Cell[][] cells, Grid readGrid, Grid writeGrid, double C1, double C2, double C3, int offset) {
        this.cells = cells;
        this.readGrid = readGrid;
        this.writeGrid = writeGrid;
        this.metalConstants = new double[]{C1, C2, C3};
        this.offset = offset;
    }
}
