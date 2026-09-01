package com.rast;

import com.demo.PixelDrawer;
import com.demo.Point;

public record Point2D(double x, double y, double h)
{

    public static Point2D viewportToCanvas(double x ,double y)
    {
        double scaledX = x * RenderConfig.CANVAS_WIDTH / RenderConfig.VIEW_SIZE;

        double scaledY = y * RenderConfig.CANVAS_HEIGHT / RenderConfig.VIEW_SIZE;

        return new Point2D( scaledX, scaledY, 1.0);
    }

    public static Point2D projectVertex(Point point)
    {

        double projX = point.getX() * RenderConfig.PROJECTION_PLANE_Z / point.getZ();

        double projY = point.getY() * RenderConfig.PROJECTION_PLANE_Z / point.getZ();

        return viewportToCanvas(projX, projY);
    }

}
