package com.demo;

import lombok.Data;

@Data
public class Vector3D
{
    double x;

    double y;

    double z;

    public Vector3D(double x, double y, double z)
    {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public static Vector3D subtract(Point v1,Point v2)
    {
        return new Vector3D(v1.x-v2.x,v1.y-v2.y,v1.z-v2.z);
    }

    public static Vector3D vecSubtract(Vector3D v1,Vector3D v2)
    {
        return new Vector3D(v1.x-v2.x,v1.y-v2.y,v1.z-v2.z);
    }

    public static double dotProduct(Vector3D v1,Vector3D v2)
    {
        return v1.x * v2.x + v1.y * v2.y + v1.z * v2.z;
    }




    public static Vector3D normalize(Vector3D v)
    {
        double mag = Math.sqrt(dotProduct(v, v));

        return new Vector3D(v.x/mag, v.y/mag, v.z/mag);
    }


    public static double vectorLength(Vector3D vector3D)
    {
        return Math.sqrt(dotProduct(vector3D,vector3D));
    }


    public static Vector3D multiply(double k,Vector3D vector3D)
    {
        return new Vector3D(k * vector3D.x,k * vector3D.y,k*vector3D.z);
    }


    public static Vector3D add(Vector3D v1,Vector3D v2)
    {
        return new Vector3D(v1.x + v2.x,v1.y + v2.y,v1.z + v2.z);
    }

    public static Point vectorToPoint(Vector3D vector3D)
    {
        return new Point(vector3D.x,vector3D.y,vector3D.z);
    }

    public static Vector3D MultiplyMV(Matrix3 matrix3,Vector3D vector3D)
    {

        double x = vector3D.getX() * matrix3.m00() + vector3D.getY() * matrix3.m01() + vector3D.getZ() * matrix3.m02();
        double y = vector3D.getX() * matrix3.m10() + vector3D.getY() * matrix3.m11() + vector3D.getZ() * matrix3.m12();
        double z = vector3D.getX() * matrix3.m20() + vector3D.getY() * matrix3.m21() + vector3D.getZ() * matrix3.m22();

        return new Vector3D(x,y,z);

    }

    public static Vector3D cross(Point p1,Point p2)
    {
        return new Vector3D(p1.y * p2.z - p1.z * p2.y,
                p1.z * p2.x - p1.x * p2.z,
                p1.x * p2.y - p1.y * p2.x);

    }

    public static Vector3D cross(Vector3D v1,Vector3D v2)
    {
        return new Vector3D(v1.y * v2.z - v1.z * v2.y,
                v1.z * v2.x - v1.x * v2.z,
                v1.x * v2.y - v1.y * v2.x);

    }

    public static double magnitude(Vector3D v1)
    {
        return Math.sqrt(Vector3D.dotProduct(v1,v1));
    }


}
