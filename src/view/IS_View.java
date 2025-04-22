package view;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.io.File;

public class IS_View extends JFrame {
    private JButton openButton;              // 图像选择按钮
    private JPanel imageShowPanel;           // 图像展示面板
    private File currentImageFile;           // 读取的文件
    private BufferedImage originalImage;     // 原图像
    private BufferedImage scaledImage;       // 缩放后的图像以适应窗口
    private Point imageLocation = new Point(0,0);             // 图像的位置
    private Point seedPoint;                 // 路径寻找初始位置
    private Point currentPoint;              // 当前鼠标位置

    public IS_View() {
        // 设置frame
        setTitle("Intelligent Scissors");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // 创建组件
        openButton = new JButton("打开图片");
        imageShowPanel = new JPanel(){
            @Override // 重写repaint方法
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (scaledImage != null){
                    middle();
                    g.drawImage(scaledImage, imageLocation.x, imageLocation.y,null);
                }
                if (seedPoint != null){
                    g.setColor(Color.RED);
                    g.fillOval(seedPoint.x - 5, seedPoint.y - 5, 10, 10);
                }
                if (currentPoint != null){
                    drawPath(g);
                }
            }
        };

        // 设置布局
        setLayout(new BorderLayout());
        add(imageShowPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(openButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // 添加事件监听器
        openButton.addActionListener(e -> openImage());

        // 添加鼠标监听器
        imageShowPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if (scaledImage != null && isInImage(e.getPoint())) { // 无图像或不在图像内时，不要seedPoint
                    seedPoint = e.getPoint();
                    repaint();
                }
            }
        });
        imageShowPanel.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                super.mouseMoved(e);
                if (scaledImage != null && seedPoint != null) { // 在无图像且无seedPoint时不需要currentPoint
                    if (isInImage(e.getPoint())) {              // 在图像内才要currentPoint
                        currentPoint = e.getPoint();
                        repaint();
                    } else
                        System.out.println("在图像区域外");
                }
            }
        });

        // 添加组件大小变化监听器
//        addComponentListener(new java.awt.event.ComponentAdapter() {
//            public void componentResized(java.awt.event.ComponentEvent evt) {
//                if (originalImage != null) {
//                    resizeImage();
//                }
//            }
//        });
    }

    private void openImage() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "图片文件", "jpg", "jpeg", "png", "gif", "bmp"));

        int returnValue = fileChooser.showOpenDialog(this);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            currentImageFile = fileChooser.getSelectedFile();
            try {
                originalImage = ImageIO.read(currentImageFile);
                resizeImage();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "无法打开图片: " + ex.getMessage(),
                        "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void resizeImage() {
        if (originalImage == null) return;
        // 获取容器大小
        int containerWidth = imageShowPanel.getWidth();
        int containerHeight = imageShowPanel.getHeight();

        if (containerWidth <= 0 || containerHeight <= 0) return;

        // 计算保持比例的缩放尺寸
        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();
        double ratio = (double) originalWidth / originalHeight;

        int newWidth, newHeight;

        if ((double) containerWidth / containerHeight > ratio) {
            newHeight = containerHeight;
            newWidth = (int) (newHeight * ratio);
        } else {
            newWidth = containerWidth;
            newHeight = (int) (newWidth / ratio);
        }

        // 缩放图片
        Image tempImage = originalImage.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
        scaledImage = new BufferedImage(newWidth,newHeight,BufferedImage.TYPE_INT_RGB);
        Graphics g = scaledImage .createGraphics();
        g.drawImage(tempImage, 0, 0, null);
        g.dispose();
        repaint();
    }

    private void middle(){
        if (scaledImage != null){
            int x = (imageShowPanel.getWidth() - scaledImage.getWidth()) / 2;
            int y = (imageShowPanel.getHeight() - scaledImage.getHeight()) / 2;
            imageLocation.setLocation(x,y);
        }
    }

    private boolean isInImage(Point point){
        return point.x >= imageLocation.x && point.x <= imageLocation.x + scaledImage.getWidth()
                && point.y >= imageLocation.y && point.y <= imageLocation.y + scaledImage.getHeight();
    }

    private void drawPath(Graphics g){
        g.drawLine(seedPoint.x,seedPoint.y,currentPoint.x,currentPoint.y);
    }
    // ================ Getter and Setter =================
    public BufferedImage getScaledImage() {
        return scaledImage;
    }
}