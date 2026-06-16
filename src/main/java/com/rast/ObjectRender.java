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

    public PixelDrawer drawer = PixelDrawer.getInstance(RenderConfig.CANVAS_WIDTH,RenderConfig.CANVAS_HEIGHT);

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
        ObjectRender cube = new ObjectRender();

        cube.vertexes = List.of(new Point[]
                {
                        new Point(1, 1, 1),

                        new Point(-1, 1, 1),

                        new Point(-1, -1, 1),

                        new Point(1, -1, 1),

                        new Point(1, 1, -1),

                        new Point(-1, 1, -1),

                        new Point(-1, -1, -1),

                        new Point(1, -1, -1)

                });

        cube.triangles = List.of(new Triangle[]
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
                });


        List<ObjectInstance> objectInstances = List.of(new ObjectInstance(cube,new Point(-1.5,0,7),Matrix4.identity(),0.75),
                new ObjectInstance(cube,new Point(1.25,2,7.5),Matrix4.MakeOYRotationMatrix(195),1));

        RastCamera rastCamera = new RastCamera(new Point(-3,-1,2),Matrix4.MakeOYRotationMatrix(-30));

        cube.renderScene(rastCamera,objectInstances);

        cube.drawer.show("cube");

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

    //实例化渲染
    public void renderScene(RastCamera rastCamera,List<ObjectInstance> instances)
    {
        Matrix4.makeTranslationMatrix(Point.multiply(-1,rastCamera.position()));

        for (ObjectInstance objectInstance:instances)
        {
            renderInstance(objectInstance);
        }
    }


    public void renderInstance(ObjectInstance objectInstance)
    {
        List<Point2D> projected = new ArrayList<>();

        ObjectRender model = objectInstance.objectRender();

        for (int i = 0;i < model.vertexes.size();i++)
        {
            projected.add(Point2D.projectVertex(Point.addPoint(objectInstance.point(),model.vertexes.get(i))));
        }

        for (int i =0;i < model.triangles.size();i++)
        {
            renderTriangle(model.triangles.get(i),projected);
        }

    }




}
