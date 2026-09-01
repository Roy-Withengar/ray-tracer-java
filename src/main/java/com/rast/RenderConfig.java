package com.rast;

public class RenderConfig
{
    private RenderConfig() {}

    // 画布尺寸（全局唯一）
    public static final int CANVAS_WIDTH = 600;

    public static final int CANVAS_HEIGHT = 600;

    // 视口大小、投影平面Z（透视投影参数）
    public static final int VIEW_SIZE = 1;

    public static final int PROJECTION_PLANE_Z = 1;

    public static final boolean UseVertexNormals = true;

    // 着色模型常量，和 JS 变量名完全对齐
    public static final int SM_FLAT = 0;

    public static final int SM_GOURAUD = 1;

    public static final int SM_PHONG = 2;

    // 当前使用的着色模型
    public static final int SHADING_MODEL = SM_PHONG;

    // ========== 光照掩码 新增 ==========

    public static final int LM_DIFFUSE  = 1 << 0;

    public static final int LM_SPECULAR = 1 << 1;

    // 当前光照模式：漫反射 + 高光，等价 JS: LM_DIFFUSE | LM_SPECULAR
    public static int LIGHTING_MODEL = LM_DIFFUSE | LM_SPECULAR;
}
