import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Individual pockets in the grid.
 */
public class Cell {
    int rowNumber, colNumber;

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
     * Calculate a new temperature based on provided neighbors.
     */
    public void calculateNewTemperature(ArrayList<Cell> neighbors, Grid writeGrid, double[] metalConstants) {
        //Don't update if these two cells are the ones where heat is applied.
        if(this.isHeatSource) {
            double heatSourceTemp = Main.T;
            if(rowNumber == 0) heatSourceTemp = Main.S;
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
}