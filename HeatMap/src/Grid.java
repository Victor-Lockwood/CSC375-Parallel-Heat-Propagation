import java.util.concurrent.ThreadLocalRandom;

public class Grid {

    public final Cell[][] Cells;

    public Grid(int height, int width) {
        this.Cells = new Cell[height][width];

        for(int i = 0; i < height; i++) {
            for(int j = 0; j < width; j++) {
                Cell cell = new Cell();
                Cells[i][j] = cell;
            }
        }

        this.Cells[0][0].Color = 'O';
    }

    public class Cell {
        //Double temperature = Main.S;

        char Color;

        public Cell() {
            int choice = ThreadLocalRandom.current().nextInt(2);

            switch (choice) {
                case 0:
                    this.Color = 'A';
                    break;
                case 1:
                    this.Color = 'D';
                    break;
            }
        }
    }
}
