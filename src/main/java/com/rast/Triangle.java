package com.rast;

import com.demo.Color;
import com.demo.PixelDrawer;

import java.util.ArrayList;
import java.util.List;

public record Triangle(double v0, double v1, double v2, Color color)
{

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

        List<Integer> x01 = line.interpolate(p0.y(), p0.x(), p1.y(), p1.x());

        List<Double> h01 = line.interpolate(p0.y(), p0.h(), p1.y(), p1.h());

        List<Integer> x12 = line.interpolate(p1.y(), p1.x(), p2.y(), p2.x());

        List<Double> h12 = line.interpolate(p1.y(), p1.h(), p2.y(), p2.h());

        List<Integer> x02 = line.interpolate(p0.y(), p0.x(), p2.y(), p2.x());

        List<Double> h02 = line.interpolate(p0.y(), p0.h(), p2.y(), p2.h());

        List<Integer> x012 = new ArrayList<>();

        List<Double> h012 = new ArrayList<>();

        x012.addAll(x01);

        x012.remove(x012.size() - 1);

        x012.addAll(x12);

        h012.addAll(h01);

        h012.remove(h012.size() - 1);

        h012.addAll(h12);

        List<Integer> xLeft;

        List<Double> hLeft;

        List<Integer> xRight;

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

        for (int y = p0.y();y <= p2.y();y++)
        {
            var xL = xLeft.get(y - p0.y());

            var xR = xRight.get(y - p0.y());

            var hSegment = line.interpolate(xL, hLeft.get(y - p0.y()), xR, hRight.get(y - p0.y()));

            for (var x = xL; x <= xR; x++)
            {
                drawer.writePixel(x,y,Color.multiply(hSegment.get(x - xL),color));
            }
        }


    }

}
