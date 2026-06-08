package com.rast;

import com.demo.PixelDrawer;
import com.demo.Point;

public record Point2D(int x, int y, double h)
{
    static  int canvasWidth = 600;

    static  int canvasHeight = 600;

    static  int viewSize = 2;

    static  int projectionPlaneZ = 1;



    public static Point2D viewportToCanvas(double x ,double y)
    {
        double scaledX = x * canvasWidth / viewSize;

        double scaledY = y * canvasHeight / viewSize;

        return new Point2D((int) scaledX, (int) scaledY, 1.0);
    }

    public static Point2D projectVertex(Point point)
    {

        double projX = point.getX() * projectionPlaneZ / point.getZ();

        double projY = point.getY() * projectionPlaneZ / point.getZ();

        return viewportToCanvas(projX, projY);
    }

}
