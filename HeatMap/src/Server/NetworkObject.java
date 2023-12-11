package Server;

import java.io.Serializable;

/**
 * All the stuff to send down the pipe.
 */
public class NetworkObject implements Serializable {
    ServerCell[][] cells;

    final ServerGrid readGrid;

    final ServerGrid writeGrid;

    final double[] metalConstants;

    double S, T;

    int offset = 0;


    public NetworkObject(ServerCell[][] cells, ServerGrid readGrid, ServerGrid writeGrid, double C1, double C2, double C3, int offset, double S, double T) {
        this.cells = cells;
        this.readGrid = readGrid;
        this.writeGrid = writeGrid;
        this.metalConstants = new double[]{C1, C2, C3};
        this.offset = offset;
        this.S = S;
        this.T = T;
    }
}
