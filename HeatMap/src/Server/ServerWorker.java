package Server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.RecursiveAction;

public class ServerWorker extends RecursiveAction {

    ServerCell[][] cells;

    final ServerGrid readGrid;

    final ServerGrid writeGrid;

    final double[] metalConstants;

    int offset = 0;

    double S, T;

    //int threshold;

    public ServerWorker(NetworkObject networkObject) {
        this.cells = networkObject.cells;
        this.readGrid = networkObject.readGrid;
        this.writeGrid = networkObject.writeGrid;
        this.metalConstants = networkObject.metalConstants;
        this.offset = networkObject.offset;
        this.S = networkObject.S;
        this.T = networkObject.T;
    }

    public ServerWorker(ServerCell[][] cells, ServerGrid readGrid, ServerGrid writeGrid, double C1, double C2, double C3, int offset, double S, double T) {
        this.cells = cells;
        this.readGrid = readGrid;
        this.writeGrid = writeGrid;
        this.metalConstants = new double[]{C1, C2, C3};
        this.offset = offset;
        this.S = S;
        this.T = T;
    }

    @Override
    protected void compute() {
        int numRows = cells.length;

        if(numRows > 5) {

            //Divide into quadrants and then spawn a new Worker for each quadrant til they reach a specified minimum number of rows tall
            //Add one extra for the edge.
            ServerCell[][] firstHalf = Arrays.copyOfRange(cells, 0, numRows/2);

            ServerCell[][] firstQuarter = new ServerCell[firstHalf.length][firstHalf[0].length/2];
            ServerCell[][] secondQuarter = new ServerCell[firstHalf.length][firstHalf[0].length/2];

            for(int row = 0; row < firstHalf.length; row++) {
                firstQuarter[row] = Arrays.copyOfRange(firstHalf[row], 0, firstHalf[row].length/2);
                secondQuarter[row] = Arrays.copyOfRange(firstHalf[row], firstHalf[row].length/2, firstHalf[row].length);
            }

            ServerCell[][] secondHalf = Arrays.copyOfRange(cells, numRows/2, numRows);

            ServerCell[][] thirdQuarter = new ServerCell[secondHalf.length][secondHalf[0].length/2];
            ServerCell[][] fourthQuarter = new ServerCell[secondHalf.length][firstHalf[0].length/2];

            for(int row = 0; row < secondHalf.length; row++) {
                thirdQuarter[row] = Arrays.copyOfRange(secondHalf[row], 0, secondHalf[row].length/2);
                fourthQuarter[row] = Arrays.copyOfRange(secondHalf[row], secondHalf[row].length/2, secondHalf[row].length);
            }

            invokeAll(
                    new ServerWorker(firstQuarter, this.readGrid, this.writeGrid, metalConstants[0], metalConstants[1], metalConstants[2], this.offset, S, T),
                    new ServerWorker(secondQuarter, this.readGrid, this.writeGrid, metalConstants[0], metalConstants[1], metalConstants[2], this.offset, S, T),
                    new ServerWorker(thirdQuarter, this.readGrid, this.writeGrid, metalConstants[0], metalConstants[1], metalConstants[2], this.offset, S, T),
                    new ServerWorker(fourthQuarter, this.readGrid, this.writeGrid, metalConstants[0], metalConstants[1], metalConstants[2], this.offset, S, T)
            );

        } else {
            //Figuring out the max X and Y on the other side made me want to cry.
            int numRowFull = this.readGrid.Cells.length;
            int maxRowNum = numRowFull - 1;

            int numCols = this.readGrid.Cells[0].length;
            int maxColNum = numCols - 1;

            for(int rowNum = 0; rowNum < cells.length; rowNum++) {
                for(int colNum = 0; colNum < cells[0].length; colNum++) {
                    ServerCell cell = cells[rowNum][colNum];
                    if(cell == null || cell.doNotCalculate) continue;

                    ArrayList<ServerCell> neighbors = new ArrayList<>();

                    //Neighbor above
                    if((cell.chunkedRowNumber - 1) >= 0) {
                        if(this.readGrid.Cells[cell.chunkedRowNumber - 1][cell.chunkedColNum] != null) {
                            neighbors.add(this.readGrid.Cells[cell.chunkedRowNumber - 1][cell.chunkedColNum]);
                        }
                    }

                    //Neighbor below
                    if((cell.chunkedRowNumber + 1) <= maxRowNum) {
                        if(this.readGrid.Cells[cell.chunkedRowNumber + 1][cell.chunkedColNum] != null)
                            neighbors.add(this.readGrid.Cells[cell.chunkedRowNumber + 1][cell.chunkedColNum]);
                    }

                    //Neighbor to the left
                    if((cell.chunkedColNum - 1) >= 0) {
                        if(this.readGrid.Cells[cell.chunkedRowNumber][cell.chunkedColNum - 1] != null)
                            neighbors.add(this.readGrid.Cells[cell.chunkedRowNumber][cell.chunkedColNum - 1]);
                    }

                    //Neighbor to the right
                    if((cell.chunkedColNum + 1) <= maxColNum) {
                        if(this.readGrid.Cells[cell.chunkedRowNumber][cell.chunkedColNum + 1] != null)
                            neighbors.add(this.readGrid.Cells[cell.chunkedRowNumber][cell.chunkedColNum + 1]);
                    }

                    cell.calculateNewTemperaturePartialWriteGrid(neighbors, this.writeGrid, this.metalConstants, S, T);
                }
            }
        }
    }
}
