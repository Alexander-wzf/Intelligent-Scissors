package model;

import view.IS_View;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.PriorityQueue;

public class IS_Model {
    BufferedImage image;    // 对应view中scaledImage
    double[][] cost;
    Point[][] parentPoint;
    Point seedPoint;
    boolean[][] visited;
    private final int[] dx = {-1, 0, 1, -1, 1, -1, 0, 1};
    private final int[] dy = {-1, -1, -1, 0, 0, 1, 1, 1};
    private final double[] directionCost = {Math.sqrt(2), 1, Math.sqrt(2), 1, 1, Math.sqrt(2), 1, Math.sqrt(2)};
    double[][] dist;
    IS_View isView;

    public void setImage(BufferedImage image) {
        this.image = image;
    }

    private double[][] getCost() {
        return imageProcessing.getC(image);
    }

    public Point[][] getParentPoint() {
        setImage(isView.getScaledImage());
        setSeedPoint(isView.getSeedPoint());

        // todo dijkstra 算法
        image = getImage();
        cost = getCost();

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

                if (nx >= 0 && nx < cols && ny >= 0 && ny < rows && !visited[ny][nx]){
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
}