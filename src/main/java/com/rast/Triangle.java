package com.rast;

import com.demo.Color;
import com.demo.PixelDrawer;
import com.demo.Point;

import java.util.ArrayList;
import java.util.List;

public record Triangle(double v0, double v1, double v2, Color color ,Point[] normals)
{


    // 兼容旧代码的工厂方法：不传法线，等价于无顶点法线模式
    public static Triangle create(double v0, double v1, double v2, Color color)
    {
        return new Triangle(v0, v1, v2, color, null);
    }

    public static void DrawFilledTriangle(Point2D p0,Point2D p1,Point2D p2,Color color,PixelDrawer drawer)
    {
        // Sort the points from bottom to top.
        if (p1.y() < p0.y())
        {
            var swap = p0;
            p0 = p1;
            p1 = swap;
        }
        if (p2.y() < p0.y())
        {
            var swap = p0;
            p0 = p2;
            p2 = swap;
        }
        if (p2.y() < p1.y())
        {
            var swap = p1;
            p1 = p2;
            p2 = swap;
        }

        Line line = new Line();

        List<Double> x01 = line.interpolate(p0.y(), p0.x(), p1.y(), p1.x());

        List<Double> h01 = line.interpolate(p0.y(), p0.h(), p1.y(), p1.h());

        List<Double> x12 = line.interpolate(p1.y(), p1.x(), p2.y(), p2.x());

        List<Double> h12 = line.interpolate(p1.y(), p1.h(), p2.y(), p2.h());

        List<Double> x02 = line.interpolate(p0.y(), p0.x(), p2.y(), p2.x());

        List<Double> h02 = line.interpolate(p0.y(), p0.h(), p2.y(), p2.h());

        List<Double> x012 = new ArrayList<>();

        List<Double> h012 = new ArrayList<>();

        x012.addAll(x01);

        x012.remove(x012.size() - 1);

        x012.addAll(x12);

        h012.addAll(h01);

        h012.remove(h012.size() - 1);

        h012.addAll(h12);

        List<Double> xLeft;

        List<Double> hLeft;

        List<Double> xRight;

        List<Double> hRight;

        int m = x02.size() / 2;

        if (x02.get(m) < x012.get(m))
        {
            xLeft = x02;

            hLeft = h02;

            xRight = x012;

            hRight = h012;
        }
        else
        {
            xLeft = x012;

            hLeft = h012;

            xRight = x02;

            hRight = h02;
        }

        for (int y = (int) p0.y(); y <= p2.y(); y++)
        {
            double xL = xLeft.get((int) (y - p0.y()));

            double xR = xRight.get((int) (y - p0.y()));

            List<Double> hSegment = line.interpolate(xL, hLeft.get((int) (y - p0.y())), xR, hRight.get((int) (y - p0.y())));

            for (double x = xL; x <= xR; x++)
            {
                drawer.writePixel((int) x,y,Color.multiply(hSegment.get((int) (x - xL)),color));
            }
        }


    }

}
