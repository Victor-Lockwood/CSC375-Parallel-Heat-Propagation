import java.util.Arrays;
import java.util.concurrent.RecursiveAction;

public class Worker extends RecursiveAction {

    Grid.Cell[][] cells;

    //int threshold;

    public Worker(Grid.Cell[][] cells) {
        this.cells = cells;
        //this.threshold = threshold;
    }

    @Override
    protected void compute() {
        int numRows = cells.length;

        if(numRows > 5) {

            //Divide into quadrants and then spawn a new Worker for each quadrant til they reach a specified minimum number of rows tall
            Grid.Cell[][] firstHalf = Arrays.copyOfRange(cells, 0, numRows/2);

            Grid.Cell[][] firstQuarter = new Grid.Cell[firstHalf.length][firstHalf[0].length/2];
            Grid.Cell[][] secondQuarter = new Grid.Cell[firstHalf.length][firstHalf[0].length/2];

            for(int row = 0; row < firstHalf.length; row++) {
                firstQuarter[row] = Arrays.copyOfRange(firstHalf[row], 0, firstHalf[row].length/2);
                secondQuarter[row] = Arrays.copyOfRange(firstHalf[row], firstHalf[row].length/2, firstHalf[row].length);
            }


            Grid.Cell[][] secondHalf = Arrays.copyOfRange(cells, numRows/2, numRows);

            Grid.Cell[][] thirdQuarter = new Grid.Cell[secondHalf.length][secondHalf[0].length/2];
            Grid.Cell[][] fourthQuarter = new Grid.Cell[secondHalf.length][firstHalf[0].length/2];

            for(int row = 0; row < secondHalf.length; row++) {
                thirdQuarter[row] = Arrays.copyOfRange(secondHalf[row], 0, secondHalf[row].length/2);
                fourthQuarter[row] = Arrays.copyOfRange(secondHalf[row], secondHalf[row].length/2, secondHalf[row].length);
            }

            invokeAll(
                    new Worker(firstQuarter),
                    new Worker(secondQuarter),
                    new Worker(thirdQuarter),
                    new Worker(fourthQuarter)
            );

        } else {

            for(Grid.Cell[] cellLine: cells) {
                for(Grid.Cell cell: cellLine) {
                    if(cell == null) continue;
                    cell.calculateNewTemperature();
                }
            }
        }
    }
}
