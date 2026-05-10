package com.eggame.entities;
import com.eggame.map.Farm;

public class Camera {
    private double x;
    private double y;
    private double viewportWidth;
    private double viewportHeight;

    public Camera(double viewportWidth, double viewportHeight) {
        this.viewportWidth = viewportWidth;
        this.viewportHeight = viewportHeight;
    }

    public void follow(double targetX, double targetY, Farm farm) {
        x = targetX - viewportWidth / 2;
        y = targetY - viewportHeight / 2;

        // Clamp to world bounds
        x = Math.max(0, Math.min(farm.getWidth() - viewportWidth, x));
        y = Math.max(0, Math.min(farm.getHeight() - viewportHeight, y));
    }

    public double getX() { return x; }
    public double getY() { return y; }

    public double worldToScreenX(double wx) { return wx - x; }
    public double worldToScreenY(double wy) { return wy - y; }
}
