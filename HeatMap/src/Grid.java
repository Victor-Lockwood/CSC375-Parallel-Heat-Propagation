import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class Grid {

    public final Cell[][] Cells;

    /**
     * Make a grid with randomly populated cells.
     * @param height Number of blocks high.
     * @param width Number of blocks wide.
     */
    public Grid(int height, int width) {
        this.Cells = new Cell[height][width];

        for(int i = 0; i < height; i++) {
            for(int j = 0; j < width; j++) {
                Cell cell = new Cell(i, j);
                Cells[i][j] = cell;
            }
        }

        //Top-left cell
        this.Cells[0][0].temperature = Main.S;
        this.Cells[0][0].isHeatSource = true;

        //Bottom-right cell
        this.Cells[height - 1][width - 1].temperature = Main.T;
        this.Cells[height - 1][width - 1].isHeatSource = true;
    }

    /**
     * Create a copy of a grid.
     * @param grid The grid to be copied.
     */
    public Grid(Grid grid) {
        int height = grid.Cells.length;
        int width = grid.Cells[0].length;

        this.Cells = new Cell[height][width];

        for(int i = 0; i < height; i++) {
            for(int j = 0; j < width; j++) {
                this.Cells[i][j] = new Cell(grid.Cells[i][j]);
            }
        }
    }

    public void calculateNewTemperature() {
        for(Cell[] cellLine: this.Cells) {
            for(Cell cell: cellLine) {
                cell.calculateNewTemperature();
            }
        }

        //This is here so I have a spot to put a breakpoint
        return;
    }

    /**
     * Individual pockets in the grid.
     */
    public class Cell {
        final int x, y;

        double percentM1, percentM2, percentM3;

        //Keep track of the heat source cell once we get down to dividing and conquering.
        boolean isHeatSource = false;

        volatile double temperature;

        /**
         * Instantiate a new cell.
         * @param x X-coordinate in grid.
         * @param y Y-coordinate in grid.
         */
        public Cell(int x, int y) {
            this.x = x;
            this.y = y;
            this.temperature = 0;

            double metal1Variation = ThreadLocalRandom.current().nextDouble(.25);
            double metal2Variation = ThreadLocalRandom.current().nextDouble(.25);

            percentM1 = 0.33 * metal1Variation;
            percentM2 = 0.33 * metal2Variation;
            percentM3 = 1 - (percentM1 + percentM2);
        }

        /**
         * Copy a cell.
         * @param cell The cell to be copied.
         */
        public Cell(Cell cell) {
            this.x = cell.x;;
            this.y = cell.y;
            this.temperature = cell.temperature;

            this.percentM1 = cell.percentM1;
            this.percentM2 = cell.percentM2;
            this.percentM3 = cell.percentM3;

            this.isHeatSource = cell.isHeatSource;
        }


        /**
         * Calculate a new temperature based on neighbors.
         */
        //TODO: Incomplete
        //TODO: Needs to follow rotating matrix pattern
        public void calculateNewTemperature() {
            //Don't update if these two cells are the ones where heat is applied.
            //TODO: These can vary randomly over time
            if(this.isHeatSource) return;

            //Gather up the neighbors from the read grid
            ArrayList<Cell> neighbors = new ArrayList<>();

            int height = Main.readGrid.Cells.length;
            int width = Main.readGrid.Cells[0].length;

            //Roll with it, this is right
            int maxX = height - 1;
            int maxY = width - 1;

            //Neighbor to the left
            if((this.x - 1) >= 0) {
                neighbors.add(Main.readGrid.Cells[this.x - 1][this.y]);
            }

            //Neighbor to the right
            if((this.x + 1) <= maxX) {
                neighbors.add(Main.readGrid.Cells[this.x + 1][this.y]);
            }

            //Neighbor up above - remember, top left is (0,0) so y is the opposite direction than we intuitively expect
            if((this.y - 1) >= 0) {
                neighbors.add(Main.readGrid.Cells[this.x][this.y - 1]);
            }

            //Neighbor below
            if((this.y + 1) <= maxY) {
                neighbors.add(Main.readGrid.Cells[this.x][this.y + 1]);
            }

            //Loop through our neighbors and aggregate the temperature percentage calculation
            double aggregatedTemp = 0;

            for(Cell neighbor: neighbors) {
                aggregatedTemp += neighbor.temperature * neighbor.percentM1;
                aggregatedTemp += neighbor.temperature * neighbor.percentM2;
                aggregatedTemp += neighbor.temperature * neighbor.percentM3;
            }

            //Take that aggregate and for each of the three metals, multiply it by the constant, divide it by the number of neighbors, and
            //set temperature to the sum of the three results
            double resultTemp = Main.C1 * aggregatedTemp / neighbors.size();
            resultTemp = resultTemp + (Main.C2 * aggregatedTemp / neighbors.size());
            resultTemp = resultTemp + (Main.C3 * aggregatedTemp / neighbors.size());

            //We update the temp of the corresponding cell in the WRITE GRID,
            //NOT our local temp.
            Main.writeGrid.Cells[this.x][this.y].temperature = resultTemp;

            if(resultTemp > Main.highestTemp) Main.highestTemp = resultTemp;
        }
    }
}
