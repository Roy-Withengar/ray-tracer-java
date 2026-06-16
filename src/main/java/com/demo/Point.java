package com.demo;

import lombok.Data;

/**
 * @author Withengar
 */
@Data
public class Point
{
    double x;
    double y;
    double z;

    public Point(double x, double y, double z)
    {
        this.x = x;
        this.y = y;
        this.z = z;
    }


    public static Vector3D pointToVector(Point point)
    {
        return new Vector3D(point.x,point.y,point.z);
    }

    public static Point addPoint(Point p1,Point p2)
    {
        return new Point(p1.x + p2.x,p1.y + p2.y,p1.z + p2.z);
    }

    public static Point multiply(int k,Point point)
    {
        return new Point(k * point.x,k * point.y,k * point.z);
    }



}
