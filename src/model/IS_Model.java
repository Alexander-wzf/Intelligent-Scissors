package model;

import view.IS_View;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.PriorityQueue;

public class IS_Model {
    BufferedImage image;    // 对应view中scaledImage
    double[][] cost;        // cost矩阵
    Point[][] parentPoint;  // 当前点的父节点，用于画出路径
    Point seedPoint;
    boolean[][] visited;    // 当前点是否访问
    private final int[] dx = {-1, 0, 1, -1, 1, -1, 0, 1};
    private final int[] dy = {-1, -1, -1, 0, 0, 1, 1, 1};
    private final double[] directionCost = {Math.sqrt(2), 1, Math.sqrt(2), 1, 1, Math.sqrt(2), 1, Math.sqrt(2)};
    double[][] dist;        // 储存距离
    IS_View isView;

    public void setImage(BufferedImage image) {
        this.image = image;
    }

    private double[][] getCost() {
        return imageProcessing.getC(image);
    }

    public void setCost() {
        setImage(isView.getScaledImage());
        this.cost = imageProcessing.getC(image);
    }

    public Point[][] getParentPoint() {
        setSeedPoint(isView.getSeedPoint());

        // dijkstra 算法
        node sp = new node(seedPoint.getLocation(), 0);
        PriorityQueue<node> pq = new PriorityQueue<>();
        pq.offer(sp);

        int rows = cost.length;
        int cols = cost[0].length;
        visited = new boolean[rows][cols];
        parentPoint = new Point[rows][cols];
        dist = new double[rows][cols];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                dist[i][j] = Double.MAX_VALUE;
            }
        }

        dist[sp.self.y][sp.self.x] = 0;
        parentPoint[sp.self.y][sp.self.x] = new Point(sp.self.x, sp.self.y);

        while (!pq.isEmpty()){
            node currentNode = pq.poll();
            int x = currentNode.self.x;
            int y = currentNode.self.y;

            if (visited[y][x]) continue;
            visited[y][x] = true;

            for (int i = 0; i < 8; i++) {
                int nx = x + dx[i];
                int ny = y + dy[i];

                if (nx >= 0 && nx < cols && ny >= 0 && ny < rows ){
                    double currentDist = currentNode.dist + cost[ny][nx] * directionCost[i];

                    if (currentDist < dist[ny][nx]){
                        dist[ny][nx] = currentDist;
                        parentPoint[ny][nx] = new Point(x,y);
                        pq.offer(new node(new Point(nx, ny), currentDist));
                    }
                }
            }
        }
        return parentPoint;
    }

    public void setSeedPoint(Point seedPoint) {
        this.seedPoint = seedPoint;
    }

    private static class node implements Comparable<node>{
        Point self;
        double dist;

        public node(Point self, double dist) {
            this.self = self;
            this.dist = dist;
        }

        @Override
        public int compareTo(node o) {
            return Double.compare(this.dist, o.dist);
        }
    }

    public void setIsView(IS_View isView) {
        this.isView = isView;
    }

    public BufferedImage getImage() {
        return image;
    }

    public Point getSnappedPoint(Point p, int R){
        double min = cost[p.y][p.x];
        Point snappedPoint = new Point(p.x,p.y);

        for (int y = -R; y <= R; y++) {
            for (int x = -R; x <= R; x++) {
                if (p.y + y >= 0 && p.y + y < cost.length && p.x + x >= 0 && p.x + x < cost[0].length
                        && x*x + y*y <= R*R && cost[p.y + y][p.x + x] < min){
                    min = cost[p.y + y][p.x + x];
                    snappedPoint.setLocation(p.x + x,p.y + y);
                }
            }
        }

        return snappedPoint;
    }
}