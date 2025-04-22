package view;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class MyPanel extends JPanel {


    BufferedImage originalImage;
    public MyPanel(BufferedImage image){
        this.originalImage = image;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (originalImage != null) {
            // 计算缩放比例
            double widthRatio = (double)getWidth() / originalImage.getWidth(null);
            double heightRatio = (double)getHeight() / originalImage.getHeight(null);
            double ratio = Math.min(widthRatio, heightRatio);

            // 计算新尺寸
            int newWidth = (int)(originalImage.getWidth(null) * ratio);
            int newHeight = (int)(originalImage.getHeight(null) * ratio);

            // 创建缩放后的BufferedImage
            BufferedImage scaledImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = scaledImage.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
            g2d.dispose();

            // 居中绘制图像
            int x = (getWidth() - scaledImage.getWidth()) / 2;
            int y = (getHeight() - scaledImage.getHeight()) / 2;
            g.drawImage(scaledImage, x, y, null);
        }
    }
}
