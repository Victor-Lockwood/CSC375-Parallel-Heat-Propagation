import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.RecursiveAction;

public class Worker extends RecursiveAction {

    Cell[][] cells;

    final Grid readGrid;

    final Grid writeGrid;

    final double[] metalConstants;

    final double T, S;

    //int threshold;

    public Worker(Cell[][] cells, Grid readGrid, Grid writeGrid, double C1, double C2, double C3, double S, double T) {
        this.cells = cells;
        this.readGrid = readGrid;
        this.writeGrid = writeGrid;
        this.metalConstants = new double[]{C1, C2, C3};
        this.T = T;
        this.S = S;
    }

    @Override
    protected void compute() {
        int numRows = cells.length;

        if(numRows > 5) {

            //Divide into quadrants and then spawn a new Worker for each quadrant til they reach a specified minimum number of rows tall
            //Add one extra for the edge.
            Cell[][] firstHalf = Arrays.copyOfRange(cells, 0, numRows/2);

            Cell[][] firstQuarter = new Cell[firstHalf.length][firstHalf[0].length/2];
            Cell[][] secondQuarter = new Cell[firstHalf.length][firstHalf[0].length/2];

            for(int row = 0; row < firstHalf.length; row++) {
                firstQuarter[row] = Arrays.copyOfRange(firstHalf[row], 0, firstHalf[row].length/2);
                secondQuarter[row] = Arrays.copyOfRange(firstHalf[row], firstHalf[row].length/2, firstHalf[row].length);
            }

            Cell[][] secondHalf = Arrays.copyOfRange(cells, numRows/2, numRows);

            Cell[][] thirdQuarter = new Cell[secondHalf.length][secondHalf[0].length/2];
            Cell[][] fourthQuarter = new Cell[secondHalf.length][firstHalf[0].length/2];

            for(int row = 0; row < secondHalf.length; row++) {
                thirdQuarter[row] = Arrays.copyOfRange(secondHalf[row], 0, secondHalf[row].length/2);
                fourthQuarter[row] = Arrays.copyOfRange(secondHalf[row], secondHalf[row].length/2, secondHalf[row].length);
            }

            invokeAll(
                    new Worker(firstQuarter, this.readGrid, this.writeGrid, metalConstants[0], metalConstants[1], metalConstants[2], S, T),
                    new Worker(secondQuarter, this.readGrid, this.writeGrid, metalConstants[0], metalConstants[1], metalConstants[2], S, T),
                    new Worker(thirdQuarter, this.readGrid, this.writeGrid, metalConstants[0], metalConstants[1], metalConstants[2], S, T),
                    new Worker(fourthQuarter, this.readGrid, this.writeGrid, metalConstants[0], metalConstants[1], metalConstants[2], S, T)
            );

        } else {
            //Figuring out the max X and Y on the other side made me want to cry.
            int numRowFull = this.readGrid.Cells.length;
            int maxRowNum = numRowFull - 1;

            int numCols = this.readGrid.Cells[0].length;
            int maxColNum = numCols - 1;

            for(int rowNum = 0; rowNum < cells.length; rowNum++) {
                for(int colNum = 0; colNum < cells[0].length; colNum++) {
                    Cell cell = cells[rowNum][colNum];
                    if(cell == null || cell.doNotCalculate) continue;

                    ArrayList<Cell> neighbors = new ArrayList<>();

                    //Neighbor above
                    if((cell.rowNumber - 1) >= 0) {
                        neighbors.add(this.readGrid.Cells[cell.rowNumber - 1][cell.colNumber]);
                    }

                    //Neighbor below
                    if((cell.rowNumber + 1) <= maxRowNum) {
                        neighbors.add(this.readGrid.Cells[cell.rowNumber + 1][cell.colNumber]);
                    }

                    //Neighbor to the left
                    if((cell.colNumber - 1) >= 0) {
                        neighbors.add(this.readGrid.Cells[cell.rowNumber][cell.colNumber - 1]);
                    }

                    //Neighbor to the right
                    if((cell.colNumber + 1) <= maxColNum) {
                        neighbors.add(Main.readGrid.Cells[cell.rowNumber][cell.colNumber + 1]);
                    }

                    cell.calculateNewTemperature(neighbors, this.writeGrid, this.metalConstants, S, T);
                }
            }
        }
    }
}
