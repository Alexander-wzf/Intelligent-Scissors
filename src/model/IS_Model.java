package model;

import java.awt.*;
import java.awt.image.BufferedImage;

public class IS_Model {
    BufferedImage image;    // 对应view中scaledImage
    double[][] cost;
    Point[][] parentPoint;
    Point seedPoint;
    Point currentPoint;
    public void setImage(BufferedImage image) {
        this.image = image;
    }

    public double[][] getCost() {
        return imageProcessing.getC(image);
    }

    public void getParentPoint() {
        // todo
        cost = getCost();
    }

    public void setSeedPoint(Point seedPoint) {
        this.seedPoint = seedPoint;
    }

    public void setCurrentPoint(Point currentPoint) {
        this.currentPoint = currentPoint;
    }

    //    public static void main(String[] args) throws IOException {
//        File imageFile = new File("D:\\User\\22147\\Desktop\\img2.png");
//        BufferedImage image = ImageIO.read(imageFile);
//        Color color = new Color(image.getRGB(1,1));
//        int r = color.getRed();
//        int g = color.getGreen();
//        int b = color.getBlue();
//
//        int pixel = image.getRGB(1,1);
//        int red = (pixel >> 16) & 0xff;
//        int green = (pixel >> 8) & 0xff;
//        int blue = pixel & 0xff;
//
//        System.out.printf("r: %d g: %d b: %d \n",r,g,b);
//        System.out.printf("r: %d g: %d b: %d \n",red,green,blue);
//    }
}
