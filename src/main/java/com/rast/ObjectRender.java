package com.rast;

import com.demo.*;

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

    Point boundsCenter;

    Double boundRadius;



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

        cube.triangles = List.of(

                Triangle.create(0, 1, 2, RED),

                Triangle.create(0, 2, 3, RED),

                Triangle.create(4, 0, 3, GREEN),

                Triangle.create(4, 3, 7, GREEN),

                Triangle.create(5, 4, 7, BLUE),

                Triangle.create(5, 7, 6, BLUE),

                Triangle.create(1, 5, 6, YELLOW),

                Triangle.create(1, 6, 2, YELLOW),

                Triangle.create(4, 5, 1, PURPLE),

                Triangle.create(4, 1, 0, PURPLE),

                Triangle.create(2, 6, 7, CYAN),

                Triangle.create(2, 7, 3, CYAN)
        );

        cube.boundsCenter = new Point(0,0,0);

        cube.boundRadius = Math.sqrt(3);


        List<ObjectInstance> objectInstances = List.of(new ObjectInstance(cube,new Point(-1.5,0,7),Matrix4.identity(),0.75),
                new ObjectInstance(cube,new Point(1.25,2.5,7.5),Matrix4.MakeOYRotationMatrix(195),1));

        RastCamera rastCamera = new RastCamera(new Point(-3,1,2),Matrix4.MakeOYRotationMatrix(-30));

        double s2 = Math.sqrt(2) / 2.0;

        RastCamera.clippingPlanes = List.of(

                new Plane(new Vector3D(0,0,1),-1),

                new Plane(new Vector3D(s2,0,s2),0),

                new Plane(new Vector3D(-s2,0,s2),0),

                new Plane(new Vector3D(0,-s2,s2),0),

                new Plane(new Vector3D(0,s2,s2),0)
        );


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

        Matrix4 matrix4 = Matrix4.makeTranslationMatrix(Point.multiply(-1, rastCamera.position()));

        Matrix4 transposed = rastCamera.orientation().transposed();

        Matrix4 cameraMatrix = transposed.multiply(matrix4);

        for (ObjectInstance objectInstance:instances)
        {

            Matrix4 translationMatrix = Matrix4.makeTranslationMatrix(objectInstance.point());

            Matrix4 matrix = objectInstance.orientation().multiply(Matrix4.makeScalingMatrix(objectInstance.scale()));

            Matrix4 transform = translationMatrix.multiply(matrix);

            Matrix4 multiply = cameraMatrix.multiply(transform);

            transformAndClip(RastCamera.clippingPlanes,objectInstance,multiply);

            renderModel(objectInstance,multiply);
        }
    }


    public void renderModel(ObjectInstance objectInstance,Matrix4 transform)
    {
        List<Point2D> projected = new ArrayList<>();

        ObjectRender model = (ObjectRender) objectInstance.objectRender();

        for (int i = 0;i < model.vertexes.size();i++)
        {
            projected.add(Point2D.projectVertex(transform.multiplyMV(model.vertexes.get(i))));
        }

        for (int i =0;i < model.triangles.size();i++)
        {
            renderTriangle(model.triangles.get(i),projected);
        }

    }

    public ClippingModel transformAndClip(List<Plane> clippingPlanes,ObjectInstance model,Matrix4 transform)
    {
        Point center = transform.multiplyMV(boundsCenter);

        for (Plane p : clippingPlanes)
        {
            double distance = Vector3D.dotProduct(p.normal(), Point.pointToVector(center)) + p.distance();

            if (distance < -boundRadius)
            {
                return null;
            }
        }

        List<Point> transformVertexes = new ArrayList<>();

        for (Point vertex:vertexes)
        {
            transformVertexes.add(transform.multiplyMV(vertex));
        }

        List<Triangle> copyTriangle = triangles;

        for (Plane p : clippingPlanes)
        {
            List<Triangle> newTriangles = new ArrayList<>();

            for (Triangle triangle:copyTriangle)
            {
                clipTriangle(triangle,p,newTriangles,transformVertexes);
            }

            copyTriangle = newTriangles;
        }

        return new ClippingModel(transformVertexes,copyTriangle,center,boundRadius);

    }


    public void clipTriangle(Triangle triangle,Plane plane,List<Triangle> newTriangle,List<Point> vertexes)
    {
        Point v0 = vertexes.get((int) triangle.v0());

        Point v1 = vertexes.get((int) triangle.v0());

        Point v2 = vertexes.get((int) triangle.v0());

        boolean in0 = Vector3D.dotProduct(plane.normal(),Point.pointToVector(v0)) + plane.distance() > 0;

        boolean in1 = Vector3D.dotProduct(plane.normal(),Point.pointToVector(v1)) + plane.distance() > 0;

        boolean in2 = Vector3D.dotProduct(plane.normal(),Point.pointToVector(v2)) + plane.distance() > 0;


        int inCount = (in0 ? 1 : 0) + (in1 ? 1 : 0) + (in2 ? 1 : 0);

        if (inCount == 0)
        {
            // 全在外側，丢弃，不做处理
        }
        else if (inCount == 3)
        {
            // 全在内側，直接保留原三角形
            newTriangle.add(triangle);
        }
        else if (inCount == 1)
        {
            // 1个在内，2个在外，裁剪后生成1个新三角形
        }
        else
        {
            // 2个在内，1个在外，裁剪后生成2个新三角形
        }

    }




}
