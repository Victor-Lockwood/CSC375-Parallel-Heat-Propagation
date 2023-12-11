package Server;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Individual pockets in the grid.
 */
public class ServerCell implements Serializable {
    public int rowNumber, colNumber;

    public int chunkedRowNumber, chunkedColNum;

    /**
     * Flag we'll use to ignore edges
     */
    public boolean doNotCalculate = false;

    public double[] metalPercentages;

    //Keep track of the heat source cell once we get down to dividing and conquering.
    public boolean isHeatSource = false;

    public volatile double temperature;

    public ServerCell(){
    }

    /**
     * Instantiate a new cell.
     * @param rowNumber X-coordinate in grid.
     * @param colNumber Y-coordinate in grid.
     */
    public ServerCell(int rowNumber, int colNumber) {
        this.rowNumber = rowNumber;
        this.colNumber = colNumber;
        this.temperature = 0;

        int metal1Variation = ThreadLocalRandom.current().nextInt(25);
        int variationBound = 25 - metal1Variation;
        int metal2Variation = ThreadLocalRandom.current().nextInt(variationBound);

        int metal1 = 33 + metal1Variation;
        int metal2 = 33 + metal2Variation;
        int metal3 = 100 - (metal1 + metal2);

        double metal1Percent = (double) metal1 / 100;
        double metal2Percent = (double) metal2 / 100;
        double metal3Percent = (double) metal3 / 100;

        this.metalPercentages = new double[]{metal1Percent, metal2Percent, metal3Percent};
    }

    /**
     * Copy a cell.
     * @param cell The cell to be copied.
     */
    public ServerCell(ServerCell cell) {
        this.rowNumber = cell.rowNumber;;
        this.colNumber = cell.colNumber;
        this.temperature = cell.temperature;

        this.metalPercentages = Arrays.copyOf(cell.metalPercentages, cell.metalPercentages.length);

        this.isHeatSource = cell.isHeatSource;
    }

    /**
     * Clone a matrix of cells.
     * @param cellsToClone  What it says on the tin.
     * @return
     */
    public static ServerCell[][] cloneCellBlock(ServerCell[][] cellsToClone) {
        ServerCell[][] clones = new ServerCell[cellsToClone.length][cellsToClone[0].length];

        for(int rowNum = 0; rowNum < cellsToClone.length; rowNum++) {
            for(int colNum = 0; colNum < cellsToClone[0].length; colNum++) {
                if(cellsToClone[rowNum][colNum] == null) continue;
                clones[rowNum][colNum] = new ServerCell(cellsToClone[rowNum][colNum]);
            }
        }

        return clones;
    }

    /**
     * Clone an array of cells.
     * @param cellsToClone  What it says on the tin.
     * @return
     */
    public static ServerCell[] cloneCellLine(ServerCell[] cellsToClone) {
        ServerCell[] clones = new ServerCell[cellsToClone.length];

        for(int colNum = 0; colNum < cellsToClone.length; colNum++) {
            clones[colNum] = new ServerCell(cellsToClone[colNum]);
        }

        return clones;
    }

    public static ServerCell[][] cloneCellBlockForWrite(ServerCell[][] cellsToClone) {
        int cloneRows = 0;

        for(int rowNum = 0; rowNum < cellsToClone.length; rowNum++) {
            if(cellsToClone[rowNum][0] != null && !cellsToClone[rowNum][0].doNotCalculate) cloneRows++;
        }

        ServerCell[][] clones = new ServerCell[cloneRows + 1][cellsToClone[0].length];

        for(int rowNum = 0; rowNum < cellsToClone.length; rowNum++) {
            for(int colNum = 0; colNum < cellsToClone[0].length; colNum++) {
                if(cellsToClone[rowNum][colNum] == null || cellsToClone[rowNum][colNum].doNotCalculate) continue;
                clones[rowNum][colNum] = new ServerCell(cellsToClone[rowNum][colNum]);
            }
        }

        return clones;
    }


    /**
     * Calculate a new temperature based on provided neighbors.  Used in a local write grid.
     */
    public void calculateNewTemperaturePartialWriteGrid(ArrayList<ServerCell> neighbors, ServerGrid writeGrid, double[] metalConstants, double S, double T) {
        //Don't update if these two cells are the ones where heat is applied.
        if(this.isHeatSource) {
            double heatSourceTemp = T;
            if(rowNumber == 0) heatSourceTemp = S;
            double fluctuation = ThreadLocalRandom.current().nextDouble(.25);
            boolean coinFlip = ThreadLocalRandom.current().nextBoolean();

            if(coinFlip) {
                writeGrid.Cells[this.chunkedRowNumber][this.chunkedColNum].temperature = heatSourceTemp + fluctuation;
                return;
            } else {
                writeGrid.Cells[this.chunkedRowNumber][this.chunkedColNum].temperature = heatSourceTemp - fluctuation;
                return;
            }

        }

        //Loop through our metals and aggregate the temperature percentage calculation
        double resultTemp = 0;

        for(int metalNumber = 0; metalNumber < metalConstants.length; metalNumber++) {
            double neighborResult = 0;

            for(ServerCell neighbor: neighbors) {
                neighborResult += neighbor.temperature * neighbor.metalPercentages[metalNumber];
            }

            resultTemp += metalConstants[metalNumber] * (neighborResult / neighbors.size());
        }

        //We update the temp of the corresponding cell in the LOCAL WRITE GRID,
        //NOT our local temp.
        writeGrid.Cells[this.chunkedRowNumber][this.chunkedColNum].temperature = resultTemp;
    }
}