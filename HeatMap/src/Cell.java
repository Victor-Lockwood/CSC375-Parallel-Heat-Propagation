import Server.ServerCell;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Individual pockets in the grid.
 */
public class Cell implements Serializable {
    public int rowNumber, colNumber;

    public int chunkedRowNumber, chunkedColNum;

    /**
     * Flag we'll use to ignore edges
     */
    boolean doNotCalculate = false;

    double[] metalPercentages;

    //Keep track of the heat source cell once we get down to dividing and conquering.
    boolean isHeatSource = false;

    volatile double temperature;

    public Cell(){
    }

    /**
     * Instantiate a new cell.
     * @param rowNumber X-coordinate in grid.
     * @param colNumber Y-coordinate in grid.
     */
    public Cell(int rowNumber, int colNumber) {
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
    public Cell(Cell cell) {
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
    public static Cell[][] cloneCellBlock(Cell[][] cellsToClone) {
        Cell[][] clones = new Cell[cellsToClone.length][cellsToClone[0].length];

        for(int rowNum = 0; rowNum < cellsToClone.length; rowNum++) {
            for(int colNum = 0; colNum < cellsToClone[0].length; colNum++) {
                if(cellsToClone[rowNum][colNum] == null) continue;
                clones[rowNum][colNum] = new Cell(cellsToClone[rowNum][colNum]);
            }
        }

        return clones;
    }

    /**
     * Clone an array of cells.
     *
     * @param cellsToClone What it says on the tin.
     * @return
     */
    public static ServerCell[] cloneCellLine(Cell[] cellsToClone) {
        ServerCell[] clones = new ServerCell[cellsToClone.length];

        for(int colNum = 0; colNum < cellsToClone.length; colNum++) {
            clones[colNum] = new ServerCell();

            clones[colNum].rowNumber = cellsToClone[colNum].rowNumber;;
            clones[colNum].colNumber = cellsToClone[colNum].colNumber;
            clones[colNum].temperature = cellsToClone[colNum].temperature;

            clones[colNum].metalPercentages = Arrays.copyOf(cellsToClone[colNum].metalPercentages, cellsToClone[colNum].metalPercentages.length);

            clones[colNum].isHeatSource = cellsToClone[colNum].isHeatSource;
        }

        return clones;
    }

    public static Cell[][] cloneCellBlockForWrite(Cell[][] cellsToClone) {
        int cloneRows = 0;

        for(int rowNum = 0; rowNum < cellsToClone.length; rowNum++) {
            if(cellsToClone[rowNum][0] != null && !cellsToClone[rowNum][0].doNotCalculate) cloneRows++;
        }

        Cell[][] clones = new Cell[cloneRows + 1][cellsToClone[0].length];

        for(int rowNum = 0; rowNum < cellsToClone.length; rowNum++) {
            for(int colNum = 0; colNum < cellsToClone[0].length; colNum++) {
                if(cellsToClone[rowNum][colNum] == null || cellsToClone[rowNum][colNum].doNotCalculate) continue;
                clones[rowNum][colNum] = new Cell(cellsToClone[rowNum][colNum]);
            }
        }

        return clones;
    }

    /**
     * Calculate a new temperature based on provided neighbors.
     */
    public void calculateNewTemperature(ArrayList<Cell> neighbors, Grid writeGrid, double[] metalConstants, double S, double T) {
        //Don't update if these two cells are the ones where heat is applied.
        if(this.isHeatSource) {
            double heatSourceTemp = S;
            if(rowNumber == 0) heatSourceTemp = T;
            double fluctuation = ThreadLocalRandom.current().nextDouble(.25);
            boolean coinFlip = ThreadLocalRandom.current().nextBoolean();

            if(coinFlip) {
                writeGrid.Cells[this.rowNumber][this.colNumber].temperature = heatSourceTemp + fluctuation;
                return;
            } else {
                writeGrid.Cells[this.rowNumber][this.colNumber].temperature = heatSourceTemp - fluctuation;
                return;
            }

        }

        //Loop through our metals and aggregate the temperature percentage calculation
        double resultTemp = 0;

        for(int metalNumber = 0; metalNumber < metalConstants.length; metalNumber++) {
            double neighborResult = 0;

            for(Cell neighbor: neighbors) {
                neighborResult += neighbor.temperature * neighbor.metalPercentages[metalNumber];
            }

            resultTemp += metalConstants[metalNumber] * (neighborResult / neighbors.size());
        }

        //We update the temp of the corresponding cell in the WRITE GRID,
        //NOT our local temp.
        writeGrid.Cells[this.rowNumber][this.colNumber].temperature = resultTemp;

        if(resultTemp > Main.highestTemp) Main.highestTemp = resultTemp;
    }

    /**
     * Calculate a new temperature based on provided neighbors.  Used in a local write grid.
     */
    public void calculateNewTemperaturePartialWriteGrid(ArrayList<Cell> neighbors, Grid writeGrid, double[] metalConstants) {
        //Don't update if these two cells are the ones where heat is applied.
        if(this.isHeatSource) {
            double heatSourceTemp = Main.T;
            if(rowNumber == 0) heatSourceTemp = Main.S;
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

            for(Cell neighbor: neighbors) {
                neighborResult += neighbor.temperature * neighbor.metalPercentages[metalNumber];
            }

            resultTemp += metalConstants[metalNumber] * (neighborResult / neighbors.size());
        }

        //We update the temp of the corresponding cell in the LOCAL WRITE GRID,
        //NOT our local temp.
        writeGrid.Cells[this.chunkedRowNumber][this.chunkedColNum].temperature = resultTemp;
    }
}