package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

public class IS_View extends JFrame {
    private JButton openButton;
    private JPanel imageShowPanel;
    private File currentImageFile;
    private BufferedImage originalImage;
    private BufferedImage scaledImage;
    private Point imageLocation = new Point(0,0);

    public IS_View() {
        setTitle("图片查看器");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // 创建组件
        openButton = new JButton("打开图片");
        imageShowPanel = new JPanel(){
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                middle();
                g.drawImage(scaledImage, imageLocation.x, imageLocation.y,null);
            }
        };

        // 设置布局
        setLayout(new BorderLayout());
        add(imageShowPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(openButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // 添加事件监听器
        openButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openImage();
            }
        });

        // 添加组件大小变化监听器
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                if (originalImage != null) {
                    resizeImage();
                }
            }
        });
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
                repaint();
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
    }

    private void middle(){
        if (scaledImage != null){
            int y = (imageShowPanel.getHeight() - scaledImage.getHeight()) / 2;
            imageLocation.setLocation(0,y);
        }
    }
}