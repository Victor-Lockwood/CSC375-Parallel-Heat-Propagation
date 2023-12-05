import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

public class Grid {

    public Cell[][] Cells;

    public int sector = -1;

    public Grid() {}

    /**
     * Make a grid with randomly populated cells.
     * @param numRows Number of blocks high.
     * @param numCols Number of blocks wide.
     */
    public Grid(int numRows, int numCols) {
        this.Cells = new Cell[numRows][numCols];

        for(int i = 0; i < numRows; i++) {
            for(int j = 0; j < numCols; j++) {
                Cell cell = new Cell(i, j);
                Cells[i][j] = cell;
            }
        }

        //Top-left cell
        this.Cells[0][0].temperature = Main.S;
        this.Cells[0][0].isHeatSource = true;

        //Bottom-right cell
        this.Cells[numRows - 1][numCols - 1].temperature = Main.T;
        this.Cells[numRows - 1][numCols - 1].isHeatSource = true;
    }

    /**
     * Create a copy of a grid.
     * @param grid The grid to be copied.
     */
    public Grid(Grid grid) {
        int numRows = grid.Cells.length;
        int numCols = grid.Cells[0].length;

        this.sector = grid.sector;
        this.Cells = new Cell[numRows][numCols];

        for(int i = 0; i < numRows; i++) {
            for(int j = 0; j < numCols; j++) {
                this.Cells[i][j] = new Cell(grid.Cells[i][j]);
            }
        }
    }

    /**
     * Calculate the temperature of the whole grid.
     * @param directCalculation
     */
    public static void calculateNewTemperature(boolean directCalculation, Grid grid) {

        if(directCalculation) {

            for(Cell[] cellLine: grid.Cells) {
                for(Cell cell: cellLine) {
                    cell.calculateNewTemperature();
                }
            }
        } else {
            divideAndConquer(grid.Cells);
        }
    }

    static void divideAndConquer(Cell[][] cells) {
        int numRows = cells.length;

        if(numRows > 5) {

            //Divide into quadrants and then call divideAndConquer on those quadrants til they reach a specified minimum number of rows
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


            divideAndConquer(firstQuarter);
            divideAndConquer(secondQuarter);

            divideAndConquer(thirdQuarter);
            divideAndConquer(fourthQuarter);
        } else {

            for(Cell[] cellLine: cells) {
                for(Cell cell: cellLine) {
                    if(cell == null) continue;
                    cell.calculateNewTemperature();
                }
            }
        }
    }

    /**
     * Individual pockets in the grid.
     */
    public class Cell {
        int rowNumber, colNumber;

        public int sector = -1;

        double percentM1, percentM2, percentM3;

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

            double metal1Variation = ThreadLocalRandom.current().nextDouble(.25);
            double metal2Variation = ThreadLocalRandom.current().nextDouble(.25);

            percentM1 = 0.33; //* metal1Variation;
            percentM2 = 0.33; //* metal2Variation;
            percentM3 = 1 - (percentM1 + percentM2);
        }

        /**
         * Copy a cell.
         * @param cell The cell to be copied.
         */
        public Cell(Cell cell) {
            this.rowNumber = cell.rowNumber;;
            this.colNumber = cell.colNumber;
            this.temperature = cell.temperature;
            this.sector = cell.sector;

            this.percentM1 = cell.percentM1;
            this.percentM2 = cell.percentM2;
            this.percentM3 = cell.percentM3;

            this.isHeatSource = cell.isHeatSource;
        }


        /**
         * Calculate a new temperature based on neighbors.
         */
        public void calculateNewTemperature() {
            //Don't update if these two cells are the ones where heat is applied.
            //TODO: These can vary randomly over time
            if(this.isHeatSource) return;


            //Figuring out the max X and Y on the other side made me want to cry.
            int numRows = Main.readGrid.Cells.length;
            int maxRowNum = numRows - 1;
            int numCols = Main.readGrid.Cells[0].length;
            int maxColNum = numCols - 1;

            //Gather up the neighbors from the read grid
            ArrayList<Cell> neighbors = new ArrayList<>();

            //Neighbor to the left
            if((this.rowNumber - 1) >= 0) {
                neighbors.add(Main.readGrid.Cells[this.rowNumber - 1][this.colNumber]);
            }

            //Neighbor to the right
            if((this.rowNumber + 1) <= maxRowNum) {
                neighbors.add(Main.readGrid.Cells[this.rowNumber + 1][this.colNumber]);
            }

            //Neighbor up above - remember, top left is (0,0) so y is the opposite direction than we intuitively expect
            if((this.colNumber - 1) >= 0) {
                neighbors.add(Main.readGrid.Cells[this.rowNumber][this.colNumber - 1]);
            }

            //Neighbor below
            if((this.colNumber + 1) <= maxColNum) {
                neighbors.add(Main.readGrid.Cells[this.rowNumber][this.colNumber + 1]);
            }

            //Loop through our metals and aggregate the temperature percentage calculation
            double resultTemp = 0;

            //Metal 1
            double agg1 = 0;
            for(Cell neighbor : neighbors) {
                agg1 += neighbor.temperature * neighbor.percentM1;
            }

            agg1 *= Main.C1;

            resultTemp = resultTemp + agg1/neighbors.size();

            //Metal 2
            double agg2 = 0;
            for(Cell neighbor : neighbors) {
                agg2 += neighbor.temperature * neighbor.percentM2;
            }

            agg2 *= Main.C2;

            resultTemp = resultTemp + agg2/neighbors.size();

            //Metal 3
            double agg3 = 0;
            for(Cell neighbor : neighbors) {
                agg3 += neighbor.temperature * neighbor.percentM3;
            }

            agg3 *= Main.C3;

            resultTemp = resultTemp + agg3/neighbors.size();

            //We update the temp of the corresponding cell in the WRITE GRID,
            //NOT our local temp.
            Main.writeGrid.Cells[this.rowNumber][this.colNumber].temperature = resultTemp;

            if(resultTemp > Main.highestTemp) Main.highestTemp = resultTemp;
        }
    }
}
