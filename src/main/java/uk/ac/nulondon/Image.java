package uk.ac.nulondon;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

public class Image {
    private final List<Pixel> rows;

    private int width;
    private int height;


    public Image(BufferedImage img) {
        width = img.getWidth();
        height = img.getHeight();
        rows = new ArrayList<>();
        Pixel current = null;
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                Pixel pixel = new Pixel(img.getRGB(col, row));
                if (col == 0) {
                    rows.add(pixel);
                } else {
                    current.right = pixel;
                    pixel.left = current;
                }
                current = pixel;
            }
        }
    }

    public BufferedImage toBufferedImage() {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int row = 0; row < height; row++) {
            Pixel pixel = rows.get(row);
            int col = 0;
            while (pixel != null) {
                image.setRGB(col++, row, pixel.color.getRGB());
                pixel = pixel.right;
            }
        }
        return image;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    /**
     * Calculate energy based on neighbours of the current pixel
     * @param above pixel on top of the current pixel
     * @param current pixel being calculated
     * @param below pixel on the bottom of the current pixel
     * @return energy at current pixel
     */
    double energy(Pixel above, Pixel current, Pixel below) {
        double horizontalEnergy = above.left.brightness() + current.left.brightness() * 2 + below.left.brightness() - above.right.brightness() - current.right.brightness() * 2 - below.right.brightness();
        double verticalEnergy = above.left.brightness() + above.brightness() * 2 + above.right.brightness() - below.left.brightness() - below.brightness() * 2 - below.right.brightness();
        return Math.sqrt(horizontalEnergy * horizontalEnergy + verticalEnergy * verticalEnergy); //Energy formula
    }

    /**
     * Calculate energy for all the pixels in the image
     */
    public void calculateEnergy() {
        for (int i = 0; i < height; i++) {//Iterate through image rows
            Pixel curr = rows.get(i); //Current pixel in seam
            if (i == 0 || i == height - 1){ //If edge pixel (top or bottom row)
                for (int j = 0; j < width; j++){ //Iterate through row
                    curr.energy = curr.brightness(); //Edge pixel energy is equal to the brightness of pixel
                    curr = curr.right; //Move to the right
                }
                continue;
            }
            Pixel above = rows.get(i-1);//Pixel has an above pixel
            Pixel below = rows.get(i+1); //Pixel has a below pixel
            for (int j = 0; j < width; j++) {//Iterate through row
                if (j == 0 || j == width - 1){ //If edge pixel (first or last pixel of row)
                    curr.energy = curr.brightness();//Edge pixel energy is equal to the brightness of pixel
                }else{
                    curr.energy = energy(above, curr, below); //Normal energy calculation if not an edge pixel
                }
                curr = curr.right;//Move to the right
                above = above.right; //Move to the right
                below = below.right; //Move to the right
            }
        }
    }

    /**
     * Highlights the seam
     * @param seam Sequence of pixels
     * @param color Color of highlight
     * @return Previous value of seam
     */
    public List<Pixel> highlightSeam(List<Pixel> seam, Color color) {
        for (int i=0;i<height;i++){ //Iterate through each image row
            Pixel curr = seam.get(i); //Current pixel in seam
            Pixel highlight = new Pixel(color); //Highlight color
            if (curr.right!=null) { //If right of current exist
                highlight.right = curr.right; //Right of highlight seam is equal to right of current seam
                curr.right.left = highlight; //Right neighbor goes back to highlight
            }
            if (curr.left!=null) { //If left of current exists
                highlight.left = curr.left; //Left of highlight seam is equal to left of current seam
                curr.left.right = highlight; //Left neighbor goes back to highlight
            }else{ //Current pixel has no neighbors
                rows.set(i,highlight);//Set i to highlight
            }
        }
        return seam; //Original seam
    }

    /**
     * Removes provided seam
     * @param seam Sequence of pixels
     */
    public void removeSeam(List<Pixel> seam) {
        for (int i=0;i<height;i++){ //Iterate through rows in image
            Pixel curr = seam.get(i); //Current pixel in seam
            if (curr.left!=null&&curr.right!=null) { //If current pixel has neighbors on right and left
                curr.right.left = curr.left; //Right neighbor's left is the new left neighbor
                curr.left.right = curr.right;//Left neighbor's right is the new right neighbor
            }else if (curr.right!=null){ //curr.left == null
                curr.right.left = null; //Right neighbor's left is nonexistent
                rows.set(i,curr.right);//Update i to right neighbor
            }else if (curr.left!=null) { //curr.right == null
                curr.left.right = null; //Left neighbor's right is nonexistent
            }
        }
        width--;//Adjust width of image
    }

    /**
     * Add the provided seam
     * @param seam Sequence of pixels
     */
    public void addSeam(List<Pixel> seam) {
        for (int i=0;i<height;i++){ //Iterate through image rows
            Pixel curr = seam.get(i); //Current pixel of seam
            if (curr.right!=null){ //If right of current exists
                curr.right.left = curr; //Move to right of current then go left and set as the current pixel
            }
            if (curr.left!=null) { //If left of current pixel exists
                curr.left.right = curr; //Left neighbor's right is the current
            }else{ //If current pixel has no neighbors
                rows.set(i,curr);//Set i to current pixel
            }
        }
        width++;//Adjust height of image
    }

    /**
     * Find the seam which maximizes total value extracted from the given pixel
     * @param valueGetter Calculates value of pixel energy
     * @return The seam with the maximum total value
     */
    private List<Pixel> getSeamMaximizing(Function<Pixel, Double> valueGetter) {
        List<List<Pixel>> maximizedPixels = new ArrayList<>(); //Stores best pixels
        List<List<Integer>> direction = new ArrayList<>(); //Stores the direction of movement
        List<Pixel> ret = new ArrayList<>();
        Pixel curr = rows.get(0);

        calculateEnergy(); //Calculate energy

        //init for first row
        if (height == 1){
            Pixel best = curr;
            double max = valueGetter.apply(curr);
            curr = curr.right;
            for (int j=1;j<width;j++){
                if (valueGetter.apply(curr) > max){
                    max = valueGetter.apply(curr);
                    best = curr;
                }
                curr = curr.right;
            }
            ret.add(best);
            return ret;
        }
        for (int j=0;j<width;j++){
            maximizedPixels.add(new ArrayList<>());
            direction.add(new ArrayList<>());
        }


        //Calculate for future rows
        for (int i=1;i<height;i++){ //iterate through image rows
            Pixel above = rows.get(i-1);//Above pixel
            curr = rows.get(i); //Current pixel
            for (int j=0;j<width;j++){ //Iterate through row
                //Calculate
                int dir = 0;
                double max = valueGetter.apply(above);
                if (j>0&&valueGetter.apply(above.left)>max) {
                    max = valueGetter.apply(above.left);
                    dir = -1;
                }
                if (j<width-1&&valueGetter.apply(above.right)>max){
                    max = valueGetter.apply(above.right);
                    dir = 1;
                }
                if (dir < 0){
                    maximizedPixels.get(i).add(above.left);
                    curr.energy = Math.abs(valueGetter.apply(curr) + valueGetter.apply(above.left));
                }else if (dir > 0){
                    maximizedPixels.get(i).add(above.right);
                    curr.energy = Math.abs(valueGetter.apply(curr) + valueGetter.apply(above.right));
                }else{
                    maximizedPixels.get(i).add(above);
                    curr.energy = Math.abs(valueGetter.apply(curr) + valueGetter.apply(above));
                }
                direction.get(i).add(j+dir);

                curr=curr.right;
                above=above.right;
            }
        }


        //Find max seam in last row
        curr = rows.get(height-1); //Current pixel in last row
        Pixel best = curr;
        int seamPos = 0;
        double max = valueGetter.apply(maximizedPixels.get(height-1).getFirst());
        curr=curr.right;

        for (int j=1;j<width;j++){
            if (valueGetter.apply(maximizedPixels.get(height-1).get(j)) > max){
                best = curr;
                seamPos = j;
                max = valueGetter.apply(maximizedPixels.get(height-1).get(j));
            }
            curr = curr.right;
        }
        ret.addFirst(best);


        //Find the actual seam
        for (int i=height-1;i>=1;i--){
            seamPos = direction.get(i).get(seamPos);
            ret.addFirst(maximizedPixels.get(i).get(seamPos));
        }

        return ret;
    }

    /**
     * Gets the greenest seam in the image
     * @return Greenest seam
     */
    public List<Pixel> getGreenestSeam() {
        return getSeamMaximizing(Pixel::getGreen); //Get greenest seam
        /*Or, since we haven't lectured on lambda syntax in Java, this can be
        return getSeamMaximizing(new Function<Pixel, Double>() {
            @Override
            public Double apply(Pixel pixel) {
                return pixel.getGreen();
            }
        });*/

    }

    /**
     * Gets the seam with the lowest energy
     * @return Seam with the lowest energy
     */
    public List<Pixel> getLowestEnergySeam() {
        /*
        Maximizing negation of energy is the same as minimizing the energy.
         */
        return getSeamMaximizing(pixel -> -pixel.energy); //Get seam with lowest-energy

        /*Or, since we haven't lectured on lambda syntax in Java, this can be
        return getSeamMaximizing(new Function<Pixel, Double>() {
            @Override
            public Double apply(Pixel pixel) {
                return -pixel.energy;
            }
        });
        */
    }
}