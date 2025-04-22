package model;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

public class imageProcessing {
    private static int[][] image2gray(BufferedImage image){
        int width = image.getWidth();
        int height = image.getHeight();

        // 创建灰度矩阵
        int[][] grayMatrix = new int[height][width];

        // 如果图像已经是TYPE_BYTE_GRAY类型
        if (image.getType() == BufferedImage.TYPE_BYTE_GRAY) {
            byte[] pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
            // 直接转换为灰度矩阵
            for (int i = 0; i < pixels.length; i++) {
                int y = i / width;
                int x = i % width;
                grayMatrix[y][x] = pixels[i] & 0xFF; // 转换为无符号值
            }
        }else {
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int rgb = image.getRGB(x, y);
                    // 提取RGB分量
                    int r = (rgb >> 16) & 0xFF;
                    int g = (rgb >> 8) & 0xFF;
                    int b = rgb & 0xFF;
                    // 转换为灰度值
                    grayMatrix[y][x] = (int)(0.299 * r + 0.587 * g + 0.114 * b);
                }
            }
        }
        return grayMatrix;
    }
}
