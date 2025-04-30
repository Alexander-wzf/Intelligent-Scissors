package model;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

public class imageProcessing {
    private static final int[][] sobelX = {{-1, 0, 1}, {-2, 0, 2}, {-1, 0, 1}};
    private static final int[][] sobelY = {{-1, -2, -1}, {0, 0, 0}, {1, 2, 1}};

    /**
     * 将图像转为int值的灰度矩阵
     * @param image 待处理的图像
     * @return 灰度矩阵
     */
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

    /**
     * 用sobel算子算x方向梯度
     * @param grayMatrix int[][]灰度矩阵
     * @return 梯度矩阵
     */
    private static double[][] gradient(int[][] grayMatrix, int[][] kernel){
        int rows = grayMatrix.length;
        int cols = grayMatrix[0].length;
        double[][] D = new double[rows][cols];
        int[][] appendZero = new int[rows+2][cols+2];

        // 矩阵补零
        for (int i = 1; i < rows + 1; i++) {
            System.arraycopy(grayMatrix[i - 1], 0, appendZero[i], 1, cols + 1 - 1);
        }

        // 算卷积
        for (int i = 1; i < rows; i++) {
            for (int j = 1; j < cols; j++) {
                double d = 0;

                for (int k = 0; k < 2; k++) {
                    for (int l = 0; l < 2; l++) {
                        d = d + kernel[k][l]*appendZero[i+k-1][j+l-1];
                    }
                }

                D[i][j] = d;
            }
        }

        return D;
    }

    private static double[][] getIx(int[][] grayMatrix){
        return gradient(grayMatrix,sobelX);
    }
    private static double[][] getIy(int[][] grayMatrix){
        return gradient(grayMatrix,sobelY);
    }

    /**
     * 计算梯度大小
     * @param Ix x方向梯度
     * @param Iy y方向梯度
     * @return double[][] 梯度大小矩阵 G = sqrt(Ix^2 + Iy^2)
     */
    private static double[][] gradientMagnitude(double[][] Ix, double[][] Iy){
        int rows = Ix.length;
        int cols = Ix[0].length;

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                Ix[i][j] = Math.sqrt(Math.pow(Ix[i][j], 2) + Math.pow(Iy[i][j], 2));
            }
        }

        return Ix;
    }

    /**
     * 计算节点权重
     * @param magnitude 图像梯度大小
     * @return 权重矩阵 Weight = (G_max-G)/G_max
     */
    private static double[][] nodeWeights(double[][] magnitude){
        double gMax = Max(magnitude);
        int rows = magnitude.length;
        int cols = magnitude[0].length;


        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                magnitude[i][j] = (gMax - magnitude[i][j]) / gMax;
            }
        }

        return magnitude;
    }

    /**
     * 计算成本矩阵
     * @param magnitude 图像梯度大小
     * @return 成本矩阵
     */
    private static double[][] costMatrix(double[][] magnitude){
        // todo 完成对成本矩阵的计算
        int rows = magnitude.length;
        int cols = magnitude[0].length;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                magnitude[i][j] = 1 / (1 + magnitude[i][j]);
            }
        }
        return magnitude;
    }

    private static double Max(double[][] matrix){
        double max = matrix[0][0];

        for (double[] doubles : matrix) {
            for (double value : doubles) {
                if (value > max) {
                    max = value;
                }
            }
        }

        return max;
    }

    public static double[][] getC(BufferedImage image){
        int[][] grayMatrix = image2gray(image);
        return costMatrix(gradientMagnitude(getIx(grayMatrix),getIy(grayMatrix)));
    }


}
