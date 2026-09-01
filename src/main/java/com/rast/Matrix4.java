package com.rast;

import com.demo.Point;
import com.demo.Vector3D;

public class Matrix4
{
    private final double[][] matrix;

    /**
     * 私有构造，禁止直接 new，统一通过静态工厂方法创建
     */
    private Matrix4(double[][] matrix)
    {
        this.matrix = matrix;
    }

    /**
     * 生成单位矩阵（乘法单位元，相当于"无变换"）
     */
    public static Matrix4 identity()
    {
        double[][] m = new double[4][4];

        for (int i = 0; i < 4; i++)
        {
            m[i][i] = 1.0;
        }

        return new Matrix4(m);
    }

    /**
     * 矩阵乘法：当前矩阵 右乘 另一个矩阵（this * other）
     * 变换顺序：先应用 other 的变换，再应用 this 的变换
     * 例如：translate.multiply(rotate) = 先旋转，再平移
     */
    public Matrix4 multiply(Matrix4 other)
    {
        double[][] result = new double[4][4];

        for (int row = 0; row < 4; row++)
        {
            for (int col = 0; col < 4; col++)
            {
                double sum = 0;

                for (int k = 0; k < 4; k++)
                {
                    sum += this.matrix[row][k] * other.matrix[k][col];
                }

                result[row][col] = sum;
            }
        }

        return new Matrix4(result);
    }

    // 1. 绕Y轴旋转矩阵（角度转弧度）
    public static Matrix4 MakeOYRotationMatrix(double degrees)
    {
        double rad = degrees * Math.PI / 180.0;

        double cos = Math.cos(rad);

        double sin = Math.sin(rad);

        double[][] m =
          {
              {cos, 0, -sin, 0},
              {0, 1, 0, 0},
              {sin, 0, cos, 0},
              {0, 0, 0, 1}
          };

        return new Matrix4(m);
    }

    //行列式转置
    public Matrix4 transposed()
    {
        double[][] trans = new double[4][4];

        for (int i = 0; i < 4; i++)
        {
            for (int j = 0; j < 4; j++)
            {
                trans[i][j] = matrix[j][i];
            }
        }
        return new Matrix4(trans);
    }

    //平移矩阵
    public static Matrix4 makeTranslationMatrix(Point translation)
    {
        double[][] m =
        {
                {1, 0, 0, translation.getX()},
                {0, 1, 0, translation.getY()},
                {0, 0, 1, translation.getZ()},
                {0, 0, 0, 1}
        };
        return new Matrix4(m);
    }

    // 3. 缩放矩阵
    public static Matrix4 makeScalingMatrix(double scale)
    {
        double[][] m =
           {
             {scale, 0, 0, 0},
             {0, scale, 0, 0},
             {0, 0, scale, 0},
             {0, 0, 0, 1}
           };
        return new Matrix4(m);
    }


    public Point multiplyMV(Point point)
    {
        double[] vec = {point.getX(), point.getY(), point.getZ(), 1.0};

        double[] res = new double[4];
        for (int row = 0; row < 4; row++)
        {
            double sum = 0;
            for (int k = 0; k < 4; k++)
            {
                sum += matrix[row][k] * vec[k];
            }
            res[row] = sum;
        }

        double w = res[3];

        return new Point(res[0]/w, res[1]/w, res[2]/w);
    }


    public Vector4 multiplyMV(Vector4 vec4)
    {
        double[] vec = {vec4.getX(), vec4.getY(), vec4.getZ(), vec4.getW()};

        double[] res = new double[4];

        for (int row = 0; row < 4; row++)
        {
            double sum = 0;

            for (int k = 0; k < 4; k++)
            {
                sum += matrix[row][k] * vec[k];
            }

            res[row] = sum;
        }

        return new Vector4(res[0], res[1], res[2], res[3]);
    }

}
