import javax.swing.*;
import java.awt.*;

/**
 * For displaying the grid of cells.
 * Why reinvent the wheel? https://github.com/Victor-Lockwood/CSC-375/blob/main/GeneticAlgorithm/GeneticAlgorithm/src/Gui.java
 */
public class Gui extends JPanel {

    final int blockLength;
    final int blockOffset;
    final int startX;
    final int startY;



    public void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        graphics.setColor(Color.BLACK);

        graphics.setColor(Color.magenta);
        graphics.fillRect(startX, startY, blockLength, blockLength);

        graphics.setColor(Color.red);
        graphics.fillRect(startX + blockLength + blockOffset, startY, blockLength, blockLength);

        for(int i = 0; i < Main.grid.Cells.length; i++) {
            Grid.Cell[] cellLine = Main.grid.Cells[i];
            int currentY = startY + (blockLength * i);

            for(int j = 0; j < cellLine.length; j++) {
                int currentX = startX + (blockLength * j);

                switch (cellLine[j].Color) {
                    case 'A' -> graphics.setColor(Color.red);
                    case 'D' -> graphics.setColor(Color.magenta);
                    default -> graphics.setColor(Color.blue);
                }

                graphics.fillRect(currentX, currentY, blockLength, blockLength);
            }

        }
    }


    Gui(int blockLength, int blockOffset, int startX, int startY) {
        super(); //Guess we need this to get a bunch of the superclass stuff
        this.blockLength = blockLength;
        this.blockOffset = blockOffset;
        this.startX = startX;
        this.startY = startY;
    }
}
