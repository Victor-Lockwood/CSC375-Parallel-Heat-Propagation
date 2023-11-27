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

        //Bottom-right cell
        this.Cells[height - 1][width - 1].temperature = Main.T;
    }

    public void calculateNewTemperature() {
        for(Cell[] cellLine: this.Cells) {
            for(Cell cell: cellLine) {
                cell.calculateNewTemperature();
            }
        }
    }

    /**
     * Individual pockets in the grid.
     */
    public class Cell {
        final int x, y;

        double percentM1, percentM2, percentM3;

        volatile double temperature;

        /**
         * Instantiate a new cell.
         * @param x X-coordinate in grid.
         * @param y
         */
        public Cell(int x, int y) {
            this.x = x;
            this.y = y;
            this.temperature = 0;
        }


        /**
         * Calculate a new temperature based on neighbors.
         */
        //TODO: Incomplete
        //TODO: Needs to follow rotating matrix pattern
        public void calculateNewTemperature() {
            //Don't update if these two cells are the ones where heat is applied.
            //TODO: These can vary randomly over time
            if((this.x == 0) && (this.y == 0)) return;
            if((this.x == Main.height - 1) && (this.y == Main.width -1)) return;

            this.temperature += 5;
        }
    }
}
