import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

public class Grid implements Serializable {

    public Cell[][] Cells;

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
        this.Cells = new Cell[numRows][numCols];

        for(int i = 0; i < numRows; i++) {
            for(int j = 0; j < numCols; j++) {
                this.Cells[i][j] = new Cell(grid.Cells[i][j]);
            }
        }
    }

}
