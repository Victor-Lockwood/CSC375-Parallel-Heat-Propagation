import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

public class Grid {

    public Cell[][] Cells;

    public int sector = -1;

    public Grid() {}

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

        this.sector = grid.sector;
        this.Cells = new Cell[height][width];

        for(int i = 0; i < height; i++) {
            for(int j = 0; j < width; j++) {
                this.Cells[i][j] = new Cell(grid.Cells[i][j]);
            }
        }
    }

    /**
     * Calculate the temperature of the whole grid.
     * @param includeEdges
     */
    //TODO: Edges/dividing up grid
    public static void calculateNewTemperature(boolean includeEdges, Grid grid) {

        if(includeEdges) {
            int height = grid.Cells.length;
            int width = grid.Cells[0].length;

            for(Cell[] cellLine: grid.Cells) {
                for(Cell cell: cellLine) {
                    cell.calculateNewTemperature();
                }
            }
        } else {
            //Calculate all cells except the ones along edges
            int height = grid.Cells.length;
            int width = grid.Cells[0].length;

            divideAndConquer(grid.Cells);
        }

        //This is here so I have a spot to put a breakpoint
        return;
    }

    static void divideAndConquer(Cell[][] cells) {
        int height = cells.length;
        int width = cells[0].length;


        if(height > 10) {

            Cell[][] firstHalf = Arrays.copyOfRange(cells, 0, height/2); //new Cell[height/2][width];
            Cell[][] secondHalf = Arrays.copyOfRange(cells, height/2, height); //new Cell[height/2][width];


            divideAndConquer(firstHalf);
            divideAndConquer(secondHalf);
        } else {
            //Left
            for(int i = 0; i < (height/2); i ++) {
                if(cells[i] == null) continue;
                for(int j = 0; j < (width/2) + 1; j++) {
                    cells[i][j].calculateNewTemperature();
                }
            }

            for(int i = ((height/2)); i < height; i ++) {
                if(cells[i] == null) continue;
                for(int j = 0; j < (width/2); j++) {
                    cells[i][j].calculateNewTemperature();
                }
            }

            //Right
            for(int i = (height/2); i < height; i ++) {
                if(cells[i] == null) continue;
                for(int j = ((width/2)); j < width; j++) {
                    cells[i][j].calculateNewTemperature();
                }
            }

            for(int i = 0; i < (height/2); i ++) {
                if(cells[i] == null) continue;
                for(int j = ((width/2) + 1); j < width; j++) {
                    cells[i][j].calculateNewTemperature();
                }
            }
        }
    }

    /**
     * Individual pockets in the grid.
     */
    public class Cell {
        int x, y;

        public int sector = -1;

        double percentM1, percentM2, percentM3;

        //Keep track of the heat source cell once we get down to dividing and conquering.
        boolean isHeatSource = false;

        volatile double temperature;

        public Cell(){
        }

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

            percentM1 = 0.33; //* metal1Variation;
            percentM2 = 0.33; //* metal2Variation;
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
            int height = Main.readGrid.Cells.length;
            int maxX = height - 1;
            int width = Main.readGrid.Cells[0].length;
            int maxY = width - 1;

            //Gather up the neighbors from the read grid
            ArrayList<Cell> neighbors = new ArrayList<>();

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
            Main.writeGrid.Cells[this.x][this.y].temperature = resultTemp;

            if(resultTemp > Main.highestTemp) Main.highestTemp = resultTemp;
        }
    }
}
