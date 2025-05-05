package view;

import model.IS_Model;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class IS_View extends JFrame {
    private JButton openButton;              // 图像选择按钮
    private JButton screenShotButton;        // 截图按钮
    private JButton clearButton;             // 清除路径
    private JPanel imageShowPanel;           // 图像展示面板
    private JCheckBox snapCB;                // 光标吸附复选框
    private JCheckBox pathCoolingCB;         // 路径冷却复选框
    File currentImageFile;                   // 读取的文件
    private BufferedImage originalImage;     // 原图像
    private BufferedImage scaledImage;       // 缩放后的图像以适应窗口
    Point imageLocation = new Point(0,0);     // 图像的位置
    private Point seedPoint;                 // 路径寻找初始位置
    private Point currentPoint;              // 当前鼠标位置
    Point[][] parentPoint;                   // 父亲点：dijkstra算法中的指向
    IS_Model isModel;
    List<List<Point>> completedPaths = new ArrayList<>();    // 已经完成的路径
    List<Point> currentPath = new ArrayList<>();             // 记录当前路径
    boolean cursorSnap = false;           // 光标吸附
    int snapR = 7;                        // 吸附半径，默认为5
    boolean pathCooling = false;          // 路径冷却
    int pathSize;
    public IS_View() {
        // 设置frame
        setTitle("Intelligent Scissors");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // 创建组件
        createComponents();

        // 设置布局
        setLayout(new BorderLayout());
        add(imageShowPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(openButton);
        buttonPanel.add(screenShotButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(snapCB);
        buttonPanel.add(pathCoolingCB);
        add(buttonPanel, BorderLayout.SOUTH);

        // 添加监听器
        createListener();

        // 输出窗口信息
        setVisible(true);
        setResizable(false);
        System.out.println("窗口大小："+getWidth()+"*"+getHeight());
        System.out.println("图片层大小："+imageShowPanel.getWidth()+"*"+imageShowPanel.getHeight());
        System.out.println("按钮层大小："+buttonPanel.getWidth()+"*"+buttonPanel.getHeight());
    }

    private void createComponents(){
        openButton = new JButton("打开图片");
        screenShotButton = new JButton("截图");
        clearButton = new JButton("清除");

        imageShowPanel = new JPanel(){
            @Override // 重写repaint方法
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (scaledImage != null){
                    middle();
                    g.drawImage(scaledImage, imageLocation.x, imageLocation.y,this);
                    openButton.setText("换一张图片");
                }
                if (seedPoint != null){
                    g.setColor(Color.RED);
                    g.fillOval(seedPoint.x - 5, seedPoint.y - 5, 10, 10);
                }
                if (parentPoint != null && currentPoint != null && seedPoint != null){
                    drawPath(g);
                }
            }
        };

        snapCB = new JCheckBox("光标吸附");

        pathCoolingCB = new JCheckBox("路径冷却");
    }

    private void createListener(){
        // 按钮事件监听器
        openButton.addActionListener(e -> {
            seedPoint = null;
            currentPoint = null;
            completedPaths.clear();
            currentPath.clear();
            pathSize = 0;
            openImage();
            if (scaledImage != null) isModel.setCost();
        });

        screenShotButton.addActionListener(e -> screenShot());

        clearButton.addActionListener(e -> {
            seedPoint = null;
            currentPoint = null;
            currentPath.clear();
            completedPaths.clear();
            repaint();
            System.out.println("已清除");
        });

        // 添加鼠标监听器
        imageShowPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if (scaledImage != null && isInImage(e.getPoint())) { // 无图像或不在图像内时，不要seedPoint
                    if (cursorSnap){
                        seedPoint = SnappedPoint(e.getPoint());
                    } else {
                        seedPoint = e.getPoint();
                    }
                    setParentPoint(isModel.getParentPoint());

                    if (!currentPath.isEmpty()){
                        pathSize += currentPath.size();
                        completedPaths.add(new ArrayList<>(currentPath));
                    }
                    repaint();
                } else {
//                    System.out.println("不在图像区域内");
                }
            }
        });
        imageShowPanel.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                super.mouseMoved(e);
                if (scaledImage != null && seedPoint != null) { // 在无图像且无seedPoint时不需要currentPoint
                    if (isInImage(e.getPoint())) {              // 在图像内才要currentPoint
                        if (cursorSnap){
                            currentPoint = SnappedPoint(e.getPoint());
                        } else {
                            currentPoint = e.getPoint();
                        }
                        repaint();
                    } else {
//                        System.out.println("在图像区域外");
                    }
                }
            }
        });

        // 复选框监听器
        snapCB.addActionListener(e -> cursorSnap = snapCB.isSelected());
        pathCoolingCB.addActionListener(e -> pathCooling = pathCoolingCB.isSelected());
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

        System.out.println("原始图片大小："+originalImage.getWidth()+"*"+originalImage.getHeight());
        System.out.println("缩放后图片大小："+scaledImage.getWidth()+"*"+scaledImage.getHeight()+"\n");

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
        return point.x >= imageLocation.x && point.x < imageLocation.x + scaledImage.getWidth()
                && point.y >= imageLocation.y && point.y < imageLocation.y + scaledImage.getHeight();
    }

    private void drawPath(Graphics g){
        g.setColor(Color.green);

        // 先画出已保存的路线
        for (List<Point> path : completedPaths) {
            drawSinglePath(g, path);
        }

        // 再画出新的路线
        currentPath.clear();

        int x = currentPoint.x - imageLocation.x;
        int y = currentPoint.y - imageLocation.y;
        currentPath.add(new Point(currentPoint.x,currentPoint.y));

        // // 直到x和y都和图像中seedPoint重合，循环停止
        while (x != seedPoint.x - imageLocation.x || y != seedPoint.y - imageLocation.y){
            int px = parentPoint[y][x].x;
            int py = parentPoint[y][x].y;

            g.drawLine(x + imageLocation.x,y + imageLocation.y,px + imageLocation.x,py + imageLocation.y);
            x = px;
            y = py;
            currentPath.add(new Point(px + imageLocation.x,py + imageLocation.y));
        }
    }

    private void drawSinglePath(Graphics g, List<Point> path){
        for (int i = 1; i < path.size(); i++) {
            Point p1 = path.get(i-1);
            Point p2 = path.get(i);
            g.drawLine(p1.x, p1.y, p2.x, p2.y);
        }
    }

    private Point SnappedPoint(Point p){
        p.setLocation(p.x - imageLocation.x, p.y - imageLocation.y);
        p = isModel.getSnappedPoint(p, snapR);
        p.setLocation(p.x + imageLocation.x, p.y + imageLocation.y);
        return p;
    }

    private void screenShot(){
        // 把所有路径整合到一个list里
        if (completedPaths.isEmpty()) return;
        System.out.println("路径长度："+pathSize);
        ArrayList<Point> combinePath = new ArrayList<>(pathSize);
        for (int i = completedPaths.size() - 1; i >= 0; i--) {
            combinePath.addAll(completedPaths.get(i));
        }
        System.out.println("点集长度："+combinePath.size());

        // 生成闭合路径
        Path2D.Double path = new Path2D.Double();
        path.moveTo(combinePath.get(0).x, combinePath.get(0).y);
        for (int i = 1; i < combinePath.size(); i++) {
            path.lineTo(combinePath.get(i).x, combinePath.get(i).y);
        }
        path.closePath();

        // 闭合区域
        Area area = new Area(path);
        int[] coordinate = findCoordinate(area);
        int width = coordinate[3] - coordinate[2];
        int height = coordinate[1] - coordinate[0];

        // 画出内部区域
        BufferedImage screenShotImage = new BufferedImage(width + 10,height + 10,BufferedImage.TYPE_INT_RGB); // 10是间隔距离

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                if (area.contains(new Point(j + imageLocation.x + coordinate[2],i + imageLocation.y + coordinate[0]))){
                    screenShotImage.setRGB(j + 5,i + 5,scaledImage.getRGB(j + coordinate[2],i + coordinate[0]));
                    // 5是间隔距离
                }
            }
        }

        JFrame showScreenShotFrame = new JFrame("截图");
        JPanel ssPanel = new JPanel(){
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(screenShotImage,0,0,showScreenShotFrame);
            }
        };
        showScreenShotFrame.add(ssPanel);
        showScreenShotFrame.setVisible(true);
        showScreenShotFrame.setLocationRelativeTo(this);
        showScreenShotFrame.setSize(screenShotImage.getWidth()+showScreenShotFrame.getInsets().left+showScreenShotFrame.getInsets().right,
                screenShotImage.getHeight()+showScreenShotFrame.getInsets().bottom+showScreenShotFrame.getInsets().top);
    }

    private int[] findCoordinate(Area area){
        int[] coordinate = new int[4]; // y1 y2 x1 x2
        // 找y1, y2
        loop:
        for (int i = 0; i < scaledImage.getHeight(); i++) {
            for (int j = 0; j < scaledImage.getWidth(); j++) {
                if (area.contains(new Point(j + imageLocation.x,i + imageLocation.y))) {
                    coordinate[0] = i;
                    break loop;
                }
            }
        }
        loop:
        for (int i = scaledImage.getHeight() - 1; i >= 0; i--) {
            for (int j = 0; j < scaledImage.getWidth(); j++) {
                if (area.contains(new Point(j + imageLocation.x,i + imageLocation.y))) {
                    coordinate[1] = i;
                    break loop;
                }
            }
        }
        // 找x1, x2
        loop:
        for (int i = 0; i < scaledImage.getWidth(); i++) {// 列
            for (int j = 0; j < scaledImage.getHeight(); j++) {// 行
                if (area.contains(new Point(i + imageLocation.x,j + imageLocation.y))) {
                    coordinate[2] = i;
                    break loop;
                }
            }
        }
        loop:
        for (int i = scaledImage.getWidth() - 1; i >= 0; i--) {// 列
            for (int j = 0; j < scaledImage.getHeight(); j++) {// 行
                if (area.contains(new Point(i + imageLocation.x,j + imageLocation.y))) {
                    coordinate[3] = i;
                    break loop;
                }
            }
        }
        return coordinate;
    }

    public BufferedImage getScaledImage() {
        return scaledImage;
    }

    // 这个seedPoint是在整个panel上的坐标，要换成在图像上的坐标
    public Point getSeedPoint() {
        Point spInImage = new Point();
        spInImage.x = seedPoint.x - imageLocation.x;
        spInImage.y = seedPoint.y - imageLocation.y;
        return spInImage;
    }

    public void setParentPoint(Point[][] parentPoint) {
        this.parentPoint = parentPoint;
    }

    public void setIsModel(IS_Model isModel) {
        this.isModel = isModel;
    }
}