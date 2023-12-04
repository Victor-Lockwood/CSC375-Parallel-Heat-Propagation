import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

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

        for(int i = 0; i < Main.readGrid.Cells.length; i++) {
            Grid.Cell[] cellLine = Main.readGrid.Cells[i];
            int currentY = startY + (blockLength * i);

            for(int j = 0; j < cellLine.length; j++) {
                int currentX = startX + (blockLength * j);

                if(cellLine[j].temperature < 20) {
                    //Cold
                    graphics.setColor(Color.blue);
                } else if(cellLine[j].temperature < 40) {
                    //Warm
                    graphics.setColor(Color.yellow);
                } else if(cellLine[j].temperature < 60) {
                    //Hot
                    graphics.setColor(Color.orange);
                } else if(cellLine[j].temperature < 80) {
                    //Hotter
                    graphics.setColor(Color.red);
                } else {
                    //Hottest
                    graphics.setColor(Color.magenta);
                }

//                //Kelvin = Celsius + 273.15
//
//                //Ported code from here: https://gist.github.com/paulkaplan/5184275
//                double modifiedTemp = (cellLine[j].temperature + 273.15) / 100;
//
//                int red, blue, green;
//
//                if(modifiedTemp <= 66) {
//                    red = 255;
//                    green = (int)modifiedTemp;
//                    green = (int)(99.4708025861 * Math.log(green) - 161.1195681661);
//
//                    if(modifiedTemp <= 19) {
//                        blue = 0;
//                    } else {
//                        blue = (int)(modifiedTemp - 10);
//                        blue = (int)(138.5177312231 * Math.log(blue) - 305.0447927307);
//                    }
//                } else {
//                    red = (int)modifiedTemp - 60;
//                    red = (int)(329.698727446 * Math.pow(red, -0.1332047592));
//
//                    green = (int)modifiedTemp - 60;
//                    green = (int)(288.1221695283 * Math.pow(green, -0.0755148492 ));
//
//                    blue = 255;
//                }
//
//                if(red < 0) red = 0;
//                if(red > 255) red = 255;
//
//                if(green < 0) green = 0;
//                if(green > 255) green = 255;
//
//                if(blue < 0) blue = 0;
//                if(blue > 255) blue = 255;
//
//                Color tempColor = new Color(red, green, blue);
//                graphics.setColor(tempColor);

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
