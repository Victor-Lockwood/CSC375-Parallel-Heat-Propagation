import java.util.ArrayList;

public class NetworkHandler {

    public static Cell[][] getServerChunk(int numServers, Grid readGrid, int offset) {
        int numRows = readGrid.Cells.length;

        //Overlapped row above the limit
        int rowsPerServer = numRows / numServers + 1;

        //If you're not the first chunk, add one more to get the row from the previous chunk
        if(offset != 0) rowsPerServer += 1;

        Cell[][] serverChunk = new Cell[rowsPerServer][readGrid.Cells[0].length];

        int lastRow = serverChunk.length - 1;
        boolean finalChunk = false;
        //Defer to the number of rows in the readGrid since rounding from integer division
        //might bump us over
        for(int rowNumber = 0; rowNumber < readGrid.Cells.length; rowNumber++) {
            if(rowNumber == rowsPerServer) break;

            int localOffset = offset;
            if(offset != 0) localOffset = offset - 1;

            if(rowNumber + offset >= readGrid.Cells.length) {
                finalChunk = true;
                break;
            }

            serverChunk[rowNumber] = Cell.cloneCellLine(readGrid.Cells[rowNumber + offset]);
        }

        //Get the number for the last row of cells that isn't null
        for(int rowNumber = 0; rowNumber < serverChunk.length; rowNumber++) {
            if(serverChunk[rowNumber][0] == null) {
               lastRow = rowNumber - 1;
               break;
            }
        }

        //Set the cells in that row to do not calculate unless we're on the last chunk
        if(!finalChunk) {
            for(Cell cell: serverChunk[lastRow]) {
                cell.doNotCalculate = true;
            }
        }


        //First row other than the very first chunk needs to be set to do not calculate as well
        if(offset != 0) {
            for(Cell cell: serverChunk[0]) {
                cell.doNotCalculate = true;
            }
        }

        return serverChunk;
    }
}
