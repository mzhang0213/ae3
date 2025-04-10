package uk.ac.nulondon;


import javax.imageio.ImageIO;
import javax.xml.stream.events.Comment;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

/*APPLICATION SERVICE LAYER*/

public class ImageEditor {
    private Deque<Command> undoStack = new ArrayDeque<>();

    private Image image;

    private List<Pixel> highlightedSeam = null;

    public void load(String filePath) throws IOException {
        File originalFile = new File(filePath);
        BufferedImage img = ImageIO.read(originalFile);
        image = new Image(img);
    }

    public void save(String filePath) throws IOException {
        BufferedImage img = image.toBufferedImage();
        ImageIO.write(img, "png", new File(filePath));
    }

    public void highlightGreenest() throws IOException {
        executeCommand(new highlightGreenCommand());
        save("target/highlightedGreen.png");
    }

    public void removeHighlighted() throws IOException {
        executeCommand(new removeHighlightCommand());
        save("target/removedSeam.png");
    }

    public void undo() throws IOException {
        if (!undoStack.isEmpty()) {//if the stack is not empty
            Command command = undoStack.pop(); //pop action
            command.undo(); //undo
            save("target/undidSeam.png");//export
        } else {//if stack is empty
            System.out.println("Nothing to undo");
        }
    }

    public void executeCommand(Command command) throws IOException {
        command.execute();
        undoStack.push(command);//push action
    }

    public void highlightLowestEnergySeam() throws IOException {
        executeCommand(new highlightLowestEnergySeamCommand());
        save("target/highlightLowestEnergy.png");
    }

    interface Command {
        void execute();
        void undo();
    }
    private class highlightGreenCommand implements Command {
        private List<Pixel> originalSeam;

        @Override
        public void execute() {
            highlightedSeam = image.highlightSeam(image.getGreenestSeam(), new Color(0, 255, 0)); //Make originalSeam to highlight green at the greenest seam
        }
        @Override
        public void undo() {

        }
    }

    private class removeHighlightCommand implements Command {
        private List<Pixel> removedSeam;

        @Override
        public void execute() {
            if (highlightedSeam != null) { //If a highlighted seam exists
                removedSeam = new ArrayList<>();// Create a copy for the seam that will be removed
                image.removeSeam(highlightedSeam); //remove seam from image
                highlightedSeam = null; //highlightedSeam does not exist after removal
            }
        }

        @Override
        public void undo() {
            image.addSeam(removedSeam); //Add the removed seam back
        }
    }
    private class highlightLowestEnergySeamCommand implements Command {
        private List<Pixel> originalSeam;
        @Override
        public void execute() {
            highlightedSeam = image.highlightSeam(image.getLowestEnergySeam(), new Color(250, 0, 0));//Make the original seam equal to the new seam which highlights the lowest energy seam of image in the color red
        }
        @Override
        public void undo() {

        }
    }

}