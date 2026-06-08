package com.rast;

import com.demo.Color;
import com.demo.PixelDrawer;
import com.demo.Point;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author Withengar
 * 任何由三角形构成的物体
 */
public class ObjectRender
{


    public static final Color RED = new Color(255,0,0);

    public static final Color GREEN = new Color(0,255,0);

    public static final Color BLUE = new Color(0,0,255);

    public static final Color YELLOW = new Color(255,255,0);

    public static final Color PURPLE = new Color(255,0,255);

    public static final Color CYAN = new Color(0,255,255);

    static  int canvasWidth = 600;

    static  int canvasHeight = 600;

    public PixelDrawer drawer = PixelDrawer.getInstance(canvasWidth,canvasHeight);

    Line line = new Line();
    /**
     * 顶点集合
     */
    List<Point> vertexes = new ArrayList<>();

    /**
     * 三角形集合
     */
    List<Triangle> triangles = new ArrayList<>();


    public static void main(String[] args)
    {
        ObjectRender objectRender = new ObjectRender();

        var points = new Point[]
                {

                        new Point(1,1,1),

                        new Point(-1,1,1),

                        new Point(-1, -1, 1),

                        new Point(1, -1, 1),

                        new Point(1, 1, -1),

                        new Point(-1, 1, -1),

                        new Point(-1, -1, -1),

                        new Point(1, -1, -1)

                };

        var triangles = new Triangle[]
                {
                        new Triangle(0, 1, 2, RED),

                        new Triangle(0, 2, 3, RED),

                        new Triangle(4, 0, 3, GREEN),

                        new Triangle(4, 3, 7, GREEN),

                        new Triangle(5, 4, 7, BLUE),

                        new Triangle(5, 7, 6, BLUE),

                        new Triangle(1, 5, 6, YELLOW),

                        new Triangle(1, 6, 2, YELLOW),

                        new Triangle(4, 5, 1, PURPLE),

                        new Triangle(4, 1, 0, PURPLE),

                        new Triangle(2, 6, 7, CYAN),

                        new Triangle(2, 7, 3, CYAN)
                };

        for (Point point:points)
        {

            point.setX(point.getX()-1.5);

            point.setZ(point.getZ()+5);

        }

        objectRender.objectRender(List.of(points), List.of(triangles));

        objectRender.drawer.show("cube");

    }


    public void objectRender(List<Point> vertexes,List<Triangle> triangles)
    {
        List<Point2D> projected = new ArrayList<>();

        for (int i = 0;i < vertexes.size();i++)
        {
            projected.add(Point2D.projectVertex(vertexes.get(i)));
        }

        for (int i =0;i < triangles.size();i++)
        {
            renderTriangle(triangles.get(i),projected);
        }
    }


    public void renderTriangle(Triangle triangle,List<Point2D> projected)
    {
        drawWireframeTriangle(

                projected.get((int) triangle.v0()),

                projected.get((int) triangle.v1()),

                projected.get((int) triangle.v2()),

                triangle.color(),

                drawer);
    }


    public void drawWireframeTriangle(Point2D p0,Point2D p1,Point2D p2,Color color,PixelDrawer drawer)
    {
        line.drawLine(p0,p1,color,drawer);

        line.drawLine(p1,p2,color,drawer);

        line.drawLine(p0,p2,color,drawer);
    }








}
