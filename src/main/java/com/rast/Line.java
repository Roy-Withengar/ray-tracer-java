package com.rast;

import com.demo.Color;
import com.demo.PixelDrawer;
import com.demo.Point;

import java.util.ArrayList;
import java.util.List;

public class Line
{
    public void drawLine(Point2D p0,Point2D p1,Color color,PixelDrawer drawer)
    {
        drawLine((int) p0.x(), (int) p0.y(), (int) p1.x(), (int) p1.y(),color,drawer);
    }

    /**
     * a = (d1 - d0) / (i1 - i0)
     * d = d0
     * d = d + a
     * @param i0  自变量
     * @param d0  因变量起始点
     * @param i1  自变量终点
     * @param d1  因变量终点
     * @param <N>
     * @param <T>
     * @return 因变量结果数组
     */
    public <N extends Number,T extends Number> List<T> interpolate(N i0,T d0,N i1,T d1)
    {
        List<T> values = new ArrayList<>();

        double start = i0.doubleValue();
        double end = i1.doubleValue();
        double valStart = d0.doubleValue();
        double valEnd = d1.doubleValue();

        if (start == end)
        {
            values.add((T) d0);
            return values;
        }

        double step = (valEnd - valStart) / (end - start);

        double current = valStart;

        for (double i = start; i <= end; i += 1)
        {
            if (d0 instanceof Integer)
            {
                values.add((T) Integer.valueOf((int) current));
            }

            else if (d0 instanceof Double)
            {
                values.add((T) Double.valueOf(current));
            }

            current += step;
        }
        return values;

    }

    public void drawLine(int x0, int y0, int x1, int y1, Color color,PixelDrawer drawer)
    {
        float dx = x1 - x0;

        float dy = y1 - y0;

        if (Math.abs(dx) > Math.abs(dy))
        {
            if (dx<0)
            {
                int temp = x0; x0 = x1; x1 = temp;
                temp = y0; y0 = y1; y1 = temp;
            }

            List<Integer> ys = interpolate(x0, y0, x1, y1);

            int startX = Math.min(x0,x1);

            int endX = Math.max(x0,x1);

            for (int x = startX;x <= endX;x++)
            {
                int idx = x - startX;

                int y = ys.get(idx);

                drawer.writePixel(x,y,color);

            }
        }
        else
        {
            if (dy<0)
            {
                int temp = y0; y0 = y1; y1 = temp;
                temp = x0; x0 = x1; x1 = temp;
            }

            List<Integer> xs = interpolate(y0, x0, y1, x1);

            int startY = Math.min(y0,y1);

            int endY = Math.max(y0,y1);

            for (int y = startY;y <= endY;y++)
            {
                drawer.writePixel(xs.get(y-startY),y,color);
            }
        }

    }

}

