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
    private Stack<Command> undoStack = new Stack<>();

    private Image image;

    private List<Pixel> highlightedSeam = null;

    public void load(String filePath) throws IOException {
        File originalFile = new File(filePath);
        BufferedImage img = ImageIO.read(originalFile);
        image = new Image(img);
    }

    /**
     * Save the image
     *
     * @param filePath Where image is saved
     * @throws IOException
     */
    public void save(String filePath) throws IOException {
        BufferedImage img = image.toBufferedImage();
        ImageIO.write(img, "png", new File(filePath));
    }

    /**
     * Highlight greenest seam
     *
     * @throws IOException
     */
    public void highlightGreenest() throws IOException {
        executeCommand(new highlightGreenCommand()); //Execute command to highlight green
        save("target/highlightedGreen.png");//Export image
    }

    /**
     * Remove highlighted seam
     *
     * @throws IOException
     */
    public void removeHighlighted() throws IOException {
        executeCommand(new removeHighlightCommand()); //Execute command to remove highlight
        save("target/removedSeam.png"); //Export image
    }

    /**
     * Undo command
     *
     * @throws IOException
     */
    public void undo() throws IOException {
        if (!undoStack.isEmpty()) {//If the stack is not empty
            Command command = undoStack.pop(); //Pop action
            command.undo(); //Undo command
            save("target/undidSeam.png");//Export image
        } else {//If stack is empty
            System.out.println("Nothing to undo"); //Message
        }
    }

    /**
     * Executes command
     *
     * @param command given
     * @throws IOException
     */
    public void executeCommand(Command command) throws IOException {
        command.execute();//Executes given command
        undoStack.push(command);//push action
    }

    /**
     * Highlight the lowest energy seam
     *
     * @throws IOException
     */
    public void highlightLowestEnergySeam() throws IOException {
        executeCommand(new highlightLowestEnergySeamCommand()); //Execute command to highlight lowest energy seam
        save("target/highlightLowestEnergy.png");//Export image
    }

    interface Command {
        void execute();

        void undo();
    }

    /**
     * Command for highlighting the greenest seam
     */
    private class highlightGreenCommand implements Command {
        private List<Pixel> originalSeam;

        @Override
        public void execute() {
            originalSeam = image.getGreenestSeam();
            highlightedSeam = image.highlightSeam(originalSeam, new Color(0, 0, 255)); //Make originalSeam to highlight green at the greenest seam
        }

        @Override
        public void undo() {
            image.removeSeam(highlightedSeam);
            image.addSeam(originalSeam);
        }
    }

    /**
     * Command for removing highlight
     */
    private class removeHighlightCommand implements Command {
        private List<Pixel> removedSeam;

        @Override
        public void execute() {
            if (highlightedSeam != null) { //If a highlighted seam exists
                removedSeam = new ArrayList<>();// Create a copy for the seam that will be removed
                image.removeSeam(highlightedSeam); //remove seam from image
                highlightedSeam = null; //highlightedSeam does not exist after removal
            } else {
                System.out.println("No seam highlighted");
            }
        }

        @Override
        public void undo() {
            image.addSeam(removedSeam); //Add the removed seam back
        }
    }

    /**
     * Command for highlighting the lowest energy seam
     */
    private class highlightLowestEnergySeamCommand implements Command {
        private List<Pixel> originalSeam;

        @Override
        public void execute() {
            originalSeam = image.getLowestEnergySeam();
            highlightedSeam = image.highlightSeam(originalSeam, new Color(250, 0, 0));//Make the original seam equal to the new seam which highlights the lowest energy seam of image in the color red
        }

        @Override
        public void undo() {
            image.addSeam(originalSeam);
        }
    }
}