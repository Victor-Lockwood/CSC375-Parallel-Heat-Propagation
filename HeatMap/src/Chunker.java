import Server.ServerCell;

public class Chunker {

    public static ServerCell[][] getServerChunk(int numServers, Grid readGrid, int offset, boolean isLast) {
        int numRows = readGrid.Cells.length;

        //Overlapped row above the limit
        int rowsPerServer = numRows / numServers + 1;

        //If you're not the first chunk, add one more to get the row from the previous chunk
        if(offset != 0) rowsPerServer += 2;

        ServerCell[][] serverChunk = new ServerCell[rowsPerServer][readGrid.Cells[0].length];

        int lastRow = serverChunk.length - 1;
        //Defer to the number of rows in the readGrid since rounding from integer division
        //might bump us over
        for(int rowNumber = 0; rowNumber < readGrid.Cells.length; rowNumber++) {
            if(rowNumber == rowsPerServer) break;

            if(rowNumber + offset >= readGrid.Cells.length) {
                break;
            }

            serverChunk[rowNumber] = Cell.cloneCellLine(readGrid.Cells[rowNumber + offset]);

            for(int colNum = 0; colNum < serverChunk[0].length; colNum++) {
                serverChunk[rowNumber][colNum].chunkedRowNumber = rowNumber;
                serverChunk[rowNumber][colNum].chunkedColNum = colNum;
            }
        }

        //Get the number for the last row of cells that isn't null
        for(int rowNumber = 0; rowNumber < serverChunk.length; rowNumber++) {
            if(serverChunk[rowNumber][0] == null) {
               lastRow = rowNumber - 1;
               break;
            }
        }

        //Set the cells in that row to do not calculate unless we're on the last chunk
        if(!isLast) {
            for(ServerCell cell: serverChunk[lastRow]) {
                cell.doNotCalculate = true;
            }
        }


        //First row other than the very first chunk needs to be set to do not calculate as well
        if(offset != 0) {
            for(ServerCell cell: serverChunk[0]) {
                cell.doNotCalculate = true;
            }
        }

        return serverChunk;
    }
}
