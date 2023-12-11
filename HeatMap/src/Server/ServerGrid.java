package Server;

public class ServerGrid {

    public ServerCell[][] Cells;

    public ServerGrid() {}

    /**
     * Make a grid with randomly populated cells.
     * @param numRows Number of blocks high.
     * @param numCols Number of blocks wide.
     */
    public ServerGrid(int numRows, int numCols, double S, double T) {
        this.Cells = new ServerCell[numRows][numCols];

        for(int i = 0; i < numRows; i++) {
            for(int j = 0; j < numCols; j++) {
                ServerCell cell = new ServerCell(i, j);
                Cells[i][j] = cell;
            }
        }

        //Top-left cell
        this.Cells[0][0].temperature = S;
        this.Cells[0][0].isHeatSource = true;

        //Bottom-right cell
        this.Cells[numRows - 1][numCols - 1].temperature = T;
        this.Cells[numRows - 1][numCols - 1].isHeatSource = true;
    }

    /**
     * Create a copy of a grid.
     * @param grid The grid to be copied.
     */
    public ServerGrid(ServerGrid grid) {
        int numRows = grid.Cells.length;
        int numCols = grid.Cells[0].length;
        this.Cells = new ServerCell[numRows][numCols];

        for(int i = 0; i < numRows; i++) {
            for(int j = 0; j < numCols; j++) {
                this.Cells[i][j] = new ServerCell(grid.Cells[i][j]);
            }
        }
    }

}
