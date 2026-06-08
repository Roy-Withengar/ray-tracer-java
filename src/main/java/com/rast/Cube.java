package com.rast;

import com.demo.Color;
import com.demo.PixelDrawer;
import com.demo.Point;

public class Cube
{
    static final int canvasWidth = 600;

    static final int canvasHeight = 600;

    static final int viewSize = 1;

    static final int projectionPlaneZ = 1;

    public static PixelDrawer drawer = PixelDrawer.getInstance(canvasWidth,canvasHeight);

    static final Color red = new Color(255,0,0);

    static final Color green = new Color(0,255,0);

    static final Color blue = new Color(0,0,255);

    static Point vA = new Point(-2,-0.5,5);

    static Point vB = new Point(-2,0.5,5);

    static Point vC = new Point(-1,0.5,5);

    static Point vD = new Point(-1,-0.5,5);

    static Point vAb = new Point(-2,-0.5,6);

    static Point vBb = new Point(-2,0.5,6);

    static Point vCb = new Point(-1,0.5,6);

    static Point vDb = new Point(-1,-0.5,6);



    public static void main(String[] args)
    {
        Line line = new Line();
        line.drawLine(projectVertex(vA), projectVertex(vB), blue,drawer);

        line.drawLine(projectVertex(vB), projectVertex(vC), blue,drawer);

        line.drawLine(projectVertex(vC), projectVertex(vD), blue,drawer);

        line.drawLine(projectVertex(vD), projectVertex(vA), blue,drawer);

        line.drawLine(projectVertex(vAb), projectVertex(vBb), red,drawer);

        line.drawLine(projectVertex(vBb), projectVertex(vCb), red,drawer);

        line.drawLine(projectVertex(vCb), projectVertex(vDb), red,drawer);

        line.drawLine(projectVertex(vDb), projectVertex(vAb), red,drawer);

        line.drawLine(projectVertex(vA), projectVertex(vAb), green,drawer);

        line.drawLine(projectVertex(vB), projectVertex(vBb), green,drawer);

        line.drawLine(projectVertex(vC), projectVertex(vCb), green,drawer);

        line.drawLine(projectVertex(vD), projectVertex(vDb), green,drawer);

        drawer.show("Cube");
    }

    public static Point2D viewportToCanvas(double x ,double y)
    {
        return new Point2D( (int) (x * canvasWidth / viewSize), (int) (y * canvasHeight / viewSize),-1);
    }

    public static Point2D projectVertex(Point point)
    {
        double projX = point.getX() * projectionPlaneZ / point.getZ();

        double projY = point.getY() * projectionPlaneZ / point.getZ();

        return viewportToCanvas(projX, projY);
    }
}




