package uk.ac.nulondon;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
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

    double energy(Pixel above, Pixel current, Pixel below) {
        double horizontalEnergy = above.left.brightness() + current.left.brightness() * 2 + below.left.brightness() - above.right.brightness() - current.right.brightness() * 2 - below.right.brightness();
        double verticalEnergy = above.left.brightness() + above.brightness() * 2 + above.right.brightness() - below.left.brightness() - below.brightness() * 2 - below.right.brightness();
        return Math.sqrt(horizontalEnergy * horizontalEnergy + verticalEnergy * verticalEnergy);
    }

    public void calculateEnergy() {
        for (int i = 0; i < height; i++) {
            Pixel curr = rows.get(i);
            if (i == 0 || i == height - 1){
                for (int j = 0; j < width; j++){
                    curr.energy = curr.brightness();
                    curr = curr.right;
                }
                continue;
            }
            Pixel above = rows.get(i-1);
            Pixel below = rows.get(i+1);
            for (int j = 0; j < width; j++) {
                if (j == 0 || j == width - 1){
                    curr.energy = curr.brightness();
                }else{
                    curr.energy = energy(above, curr, below);
                }
                curr = curr.right;
                above = above.right;
                below = below.right;
            }
        }
    }

    public List<Pixel> highlightSeam(List<Pixel> seam, Color color) {
        //check pixel neighbors per row; guarantee identity with the same neighbors
        for (int i=0;i<height;i++){
            Pixel curr = seam.get(i);
            Pixel highlight = new Pixel(color);
            if (curr.right!=null) {
                highlight.right = curr.right;
                curr.right.left = highlight;
            }
            if (curr.left!=null) {
                highlight.left = curr.left;
                curr.left.right = highlight;
            }else{
                rows.set(i,highlight);
            }
        }
        return seam;
    }

    public void removeSeam(List<Pixel> seam) {
        width--;
        for (int i=0;i<height;i++){
            Pixel curr = seam.get(i);
            if (curr.left!=null&&curr.right!=null) {
                curr.right.left = curr.left;
                curr.left.right = curr.right;
            }else if (curr.right!=null){ //curr.left == null
                curr.right.left = null;
                rows.set(i,curr.right);
            }else if (curr.left!=null) { //curr.right == null
                curr.left.right = null;
            }
        }
    }

    public void addSeam(List<Pixel> seam) {
        height++;
        for (int i=0;i<height;i++){
            Pixel curr = seam.get(i);
            if (curr.right!=null){
                curr.right.left = curr;
            }
            if (curr.left!=null) {
                curr.left.right = curr;
            }else{
                rows.set(i,curr);
            }
        }
    }

    private List<Pixel> getSeamMaximizing(Function<Pixel, Double> valueGetter) {
        List<List<Pixel>> maximizedPixels = new ArrayList<>();
        List<List<Integer>> direction = new ArrayList<>();
        List<Pixel> ret = new ArrayList<>();
        Pixel curr = rows.get(0);

        calculateEnergy();

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


        //calc for future rows
        for (int i=1;i<height;i++){
            Pixel above = rows.get(i-1);
            curr = rows.get(i);
            for (int j=0;j<width;j++){
                //calc
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


        //find max seam in last row

        curr = rows.get(height-1);
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


        //find the actual seam

        for (int i=height-1;i>=1;i--){
            seamPos = direction.get(i).get(seamPos);
            ret.addFirst(maximizedPixels.get(i).get(seamPos));
        }

        return ret;
    }

    public List<Pixel> getGreenestSeam() {
        return getSeamMaximizing(Pixel::getGreen);
        /*Or, since we haven't lectured on lambda syntax in Java, this can be
        return getSeamMaximizing(new Function<Pixel, Double>() {
            @Override
            public Double apply(Pixel pixel) {
                return pixel.getGreen();
            }
        });*/
    }

    public List<Pixel> getLowestEnergySeam() {
        /*
        Maximizing negation of energy is the same as minimizing the energy.
         */
        return getSeamMaximizing(pixel -> -pixel.energy);

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
