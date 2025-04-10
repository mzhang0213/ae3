package uk.ac.nulondon;


import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

/*APPLICATION SERVICE LAYER*/
public class ImageEditorOld {

    public interface Command {
        void execute();
    }

    private Image image;

    private List<Pixel> highlightedSeam = null;

    private Deque<Command> commands = new ArrayDeque<>();

    public Image getImage() {
        return image;
    }

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
        //TODO: implement via Command pattern
        highlightedSeam = image.highlightSeam(image.getGreenestSeam(),new Color(0,0,255));
    }

    public void removeHighlighted() throws IOException {
        //TODO: implement via Command pattern
        image.removeSeam(highlightedSeam);
    }

    public void undo() throws IOException {
        //TODO: implement via Command pattern
    }

    public void highlightLowestEnergySeam() throws IOException {
        //TODO: implement via Command pattern
        highlightedSeam = image.highlightSeam(image.getLowestEnergySeam(),new Color(255,0,0));
    }

    //TODO: implement Command class or interface and its subtypes
}
