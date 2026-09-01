package com.rast;

import com.demo.Point;
import com.demo.Vector3D;
import lombok.Data;

/**
 * 四维齐次坐标向量（对应 JS 中的 Vertex4）
 * 用于 4x4 矩阵变换的中间计算
 * 空间点：w=1
 * 方向向量：w=0
 */
@Data
public class Vector4
{
    private final double x;
    private final double y;
    private final double z;
    private final double w;

    /**
     * 直接构造四维向量
     */
    public Vector4(double x, double y, double z, double w)
    {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }

    /**
     * 从 3D 空间点构造，默认 w=1
     */
    public Vector4(Point point)
    {
        this.x = point.getX();
        this.y = point.getY();
        this.z = point.getZ();
        this.w = 1.0;
    }

    /**
     * 从 3D 方向向量构造，默认 w=0
     */
    public Vector4(Vector3D vector)
    {
        this.x = vector.getX();
        this.y = vector.getY();
        this.z = vector.getZ();
        this.w = 0.0;
    }

    /**
     * 齐次除法：除以 w 分量，将齐次坐标还原为 3D 点
     * 对应透视投影后的归一化操作
     */
    public Point perspectiveDivideToPoint()
    {
        return new Point(x / w, y / w, z / w);
    }

    /**
     * 提取前三个分量，转为 3D 方向向量（适用于 w=0 的方向量）
     */
    public Vector3D toVector3D()
    {
        return new Vector3D(x, y, z);
    }
}