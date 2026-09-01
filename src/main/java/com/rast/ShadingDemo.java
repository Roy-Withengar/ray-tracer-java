package com.rast;
import com.demo.*;
import java.util.ArrayList;
import java.util.List;
import static com.demo.LightType.*;


public class ShadingDemo
{
    // ========== 预定义颜色常量 ==========
    public static final Color RED = new Color(255,0,0);

    public static final Color GREEN = new Color(0,255,0);

    public static final Color BLUE = new Color(0,0,255);

    public static final Color YELLOW = new Color(255,255,0);

    public static final Color PURPLE = new Color(255,0,255);

    public static final Color CYAN = new Color(0,255,255);

    public PixelDrawer drawer = PixelDrawer.getInstance(RenderConfig.CANVAS_WIDTH,RenderConfig.CANVAS_HEIGHT);
    /**
     * 深度缓冲区（Z-Buffer）
     * 存储每个屏幕像素当前最近物体的 1/Z（深度倒数）
     * invZ 越大 → 物体离相机越近
     * 初始值 0.0 代表无穷远（任何物体都比它近）
     * 一维数组布局：offset = x + CANVAS_WIDTH * y
     */
    ArrayList<Double> depthBuffer ;
    /** 直线插值工具，用于扫描线水平/垂直插值 */
    Line line = new Line();

    // ========== 模型数据成员（当前与渲染器耦合，规范应拆分到独立模型类） ==========
    /** 模型顶点集合（相机空间/世界空间三维坐标） */
    List<Point> vertexes = new ArrayList<>();
    /** 模型三角形集合（每个三角形存3个顶点索引+颜色+顶点法线） */
    List<Triangle> triangles = new ArrayList<>();
    /** 三角形法线集合（备用，当前未使用） */
    List<Point[]> triangleNormals = new ArrayList<>();
    /** 模型包围球中心点（用于粗裁剪快速剔除） */
    Point boundsCenter;
    /** 模型包围球半径（用于粗裁剪快速剔除） */
    Double boundRadius;
    /** 场景光源集合 */
    List<Light> lights = new ArrayList<>();

    /**
     * 构造函数：初始化像素绘制器和深度缓冲区
     * 【深度缓冲初始化公式】
     *   总元素数 = CANVAS_WIDTH × CANVAS_HEIGHT
     *   每个元素初始值 = 0.0（代表无穷远，invZ=0）
     * 注意：ArrayList 不支持稀疏数组，必须预先 add 全部元素，
     *       否则后续 get(offset) 会抛 IndexOutOfBoundsException。
     */
    public ShadingDemo()
    {
        drawer = PixelDrawer.getInstance(RenderConfig.CANVAS_WIDTH,RenderConfig.CANVAS_HEIGHT);
        int w = RenderConfig.CANVAS_WIDTH;
        int h = RenderConfig.CANVAS_HEIGHT;
        int total = w * h;
        depthBuffer = new ArrayList<>(total);
        // 全部预先填充初始值 0.0；invZ(1/Z)越大离相机越近，初始0代表无穷远
        for(int i=0;i<total;i++)
        {
            depthBuffer.add(0.0);
        }
    }

    /**
     * 程序入口：构建立方体模型、相机、光源、裁剪平面，启动渲染
     * 流程：
     *   1. 定义立方体6个面的法线方向
     *   2. 创建 ShadingDemo 实例（同时作为模型数据容器）
     *   3. 赋值立方体8个顶点、12个三角形（每面2个三角形）
     *   4. 赋值3个光源（环境光+方向光+点光源）
     *   5. 赋值包围球中心(0,0,0)、半径√3（立方体顶点到中心最远距离）
     *   6. 创建2个 ObjectInstance（同一立方体，不同位置/旋转/缩放）
     *   7. 创建相机（位置+朝向旋转矩阵）
     *   8. 设置5个裁剪平面（近平面+上下左右视锥平面）
     *   9. 调用 renderScene 渲染整场景
     *  10. 调用 drawer.show 显示画布
     *
     * @param args 命令行参数（未使用）
     */
    public static void main(String[] args)
    {
        Point normalFront  = new Point(0, 0, 1);    // 正面（Z轴正方向）
        Point normalRight  = new Point(1, 0, 0);    // 右面（X轴正方向）
        Point normalBack   = new Point(0, 0, -1);   // 背面（Z轴负方向）
        Point normalLeft   = new Point(-1, 0, 0);   // 左面（X轴负方向）
        Point normalTop    = new Point(0, 1, 0);    // 顶面（Y轴正方向）
        Point normalBottom = new Point(0, -1, 0);   // 底面（Y轴负方向）

        ShadingDemo instances = new ShadingDemo();
        instances.vertexes = List.of(new Point[]
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
        instances.triangles = List.of(new Triangle[]
                {
                        new Triangle(0, 1, 2, RED,    new Point[]{normalFront, normalFront, normalFront}),
                        new Triangle(0, 2, 3, RED,    new Point[]{normalFront, normalFront, normalFront}),
                        new Triangle(4, 0, 3, GREEN,  new Point[]{normalRight, normalRight, normalRight}),
                        new Triangle(4, 3, 7, GREEN,  new Point[]{normalRight, normalRight, normalRight}),
                        new Triangle(5, 4, 7, BLUE,   new Point[]{normalBack, normalBack, normalBack}),
                        new Triangle(5, 7, 6, BLUE,   new Point[]{normalBack, normalBack, normalBack}),
                        new Triangle(1, 5, 6, YELLOW, new Point[]{normalLeft, normalLeft, normalLeft}),
                        new Triangle(1, 6, 2, YELLOW, new Point[]{normalLeft, normalLeft, normalLeft}),
                        new Triangle(1, 0, 5, PURPLE, new Point[]{normalTop, normalTop, normalTop}),
                        new Triangle(5, 0, 4, PURPLE, new Point[]{normalTop, normalTop, normalTop}),
                        new Triangle(2, 6, 7, CYAN,   new Point[]{normalBottom, normalBottom, normalBottom}),
                        new Triangle(2, 7, 3, CYAN,   new Point[]{normalBottom, normalBottom, normalBottom})
                });
        instances.lights = List.of(new Light[]
            {
                 new Light(AMBIENT,0.2),
                 new Light(DIRECTIONAL,0.2,new Vector3D(-1,0,1)),
                 new Light(POINT,0.6,new Vector3D(-3,2,10))
            }


        );

        instances.boundsCenter = new Point(0,0,0);
        instances.boundRadius = Math.sqrt(3);

        List<ObjectInstance> objectInstances = List.of(new ObjectInstance(instances,new Point(-1.5,0,7),Matrix4.identity(),0.75),
                new ObjectInstance(instances,new Point(1.25,2.5,7.5),Matrix4.MakeOYRotationMatrix(195),1));

        RastCamera rastCamera = new RastCamera(new Point(-3,1,2),Matrix4.MakeOYRotationMatrix(-30));
        double s2 = Math.sqrt(2) / 2.0;
        RastCamera.clippingPlanes = List.of(
                new Plane(new Vector3D(0,0,1),-1),
                new Plane(new Vector3D(s2,0,s2),0),
                new Plane(new Vector3D(-s2,0,s2),0),
                new Plane(new Vector3D(0,-s2,s2),0),
                new Plane(new Vector3D(0,s2,s2),0)
        );

        instances.renderScene(rastCamera,objectInstances);
        instances.drawer.show("ShadingDemo");
    }

    /**
     * 光栅化单个三角形（扫描线算法核心）
     *
     * 【整体流程】
     *   1. sortedVertexIndexes：按投影Y坐标排序三个顶点（p0最上，p2最下）
     *   2. computeTriangleNormal：计算三角形面法线，背面剔除
     *   3. edgeInterpolate：对X坐标、1/Z做两条边的垂直插值
     *   4. 根据着色模式：
     *      - FLAT：三角形重心算1次光照，全三角共用
     *      - GOURAUD：3个顶点算光照，对亮度i做边插值
     *      - PHONG：对法线nx/ny/nz分别做边插值
     *   5. 取三角形中间行，判定左右边界（x02 vs x012）
     *   6. 逐扫描线(row)循环：
     *      - 取左右边界xl/xr、左右深度zl/zr
     *      - line.interpolate：水平插值得到zScan（每像素1/Z）
     *      - GOURAUD：水平插值得到iScan（每像素亮度）
     *      - PHONG：水平插值得到nxScan/nyScan/nzScan（每像素法线）
     *   7. 逐像素(x)循环：
     *      - updateDepthBufferIfCloser：深度测试，更近才绘制
     *      - 根据着色模式计算像素亮度intensity
     *      - drawer.putPixel：颜色×亮度，画像素
     *
     * 【关键公式】
     *   - 背面剔除：dot(normal, center) < 0 → 背面，丢弃
     *     （normal为三角形面法线，center为三角形中心指向相机方向）
     *   - 透视正确插值：使用 1/Z（invZ）而非Z做线性插值
     *     因为透视投影下 1/Z 在屏幕空间是线性的，Z 不是线性的
     *   - Phong反投影：见 unProjectVertex
     *
     * @param triangle   待光栅化的三角形（含3顶点索引、颜色、顶点法线）
     * @param vertexes   模型全部顶点列表（相机空间三维坐标）
     * @param projected  模型全部顶点的投影坐标列表（屏幕二维坐标）
     * @param camera     相机（含位置、朝向矩阵，用于光照计算）
     * @param lights     场景光源列表
     * @param orientation 模型朝向旋转矩阵（用于顶点法线变换）
     */
    public void renderTriangle(Triangle triangle,List<Point> vertexes,List<Point2D> projected,RastCamera camera,List<Light> lights,Matrix4 orientation)
    {
        Double flatIntensity = null;

        Point normal0,normal1,normal2;

        // ========== 背面剔除必须用【原始顶点】，不能用排序后的！ ==========
        Point origV0 = vertexes.get((int)triangle.v0());

        Point origV1 = vertexes.get((int)triangle.v1());

        Point origV2 = vertexes.get((int)triangle.v2());

        // 用原始顶点计算法线
        Vector3D normal = computeTriangleNormal(origV0, origV1, origV2);

        // 用原始顶点计算中心
        Vector3D c0 = Point.pointToVector(origV0);

        Vector3D c1 = Point.pointToVector(origV1);

        Vector3D c2 = Point.pointToVector(origV2);

        Vector3D centre = Vector3D.multiply(-1.0 / 3.0, Vector3D.add(Vector3D.add(c0,c1),c2));

        // 背面剔除
        if (Vector3D.dotProduct(normal,centre)<0)
        {
            return;
        }

        // ========== 下面才是排序后的顶点，只用于扫描线插值 ==========
        int[] indexes = sortedVertexIndexes(triangle, projected);

        int[] vertexIndexes = {(int) triangle.v0(), (int) triangle.v1(), (int) triangle.v2()};

        int idx0 = vertexIndexes[indexes[0]];

        int idx1 = vertexIndexes[indexes[1]];

        int idx2 = vertexIndexes[indexes[2]];

        Point v0 = vertexes.get(idx0);

        Point v1 = vertexes.get(idx1);

        Point v2 = vertexes.get(idx2);

        Point2D p0 = projected.get(idx0);

        Point2D p1 = projected.get(idx1);

        Point2D p2 = projected.get(idx2);

        // 步骤3：边插值（垂直方向，按Y扫描）
        // x01X012 = [v02边, v012边]，每条边存每一行对应的X坐标
        List<List<Double>> x01X012 = edgeInterpolate(p0.y(),p0.x(),p1.y(),p1.x(),p2.y(),p2.x());
        // iz02Iz012 = [v02边, v012边]，每条边存每一行对应的 1/Z
        List<List<Double>> iz02Iz012 = edgeInterpolate(p0.y(),  (1/v0.getZ()),p1.y(), (1/v1.getZ()),p2.y(), (1/v2.getZ()));

        // 步骤4：法线处理
        if (RenderConfig.UseVertexNormals)
        {
            // 顶点法线：用法线变换矩阵变换每个顶点法线
            // 【法线变换矩阵公式】 N = transpose(camera.orientation) × orientation
            // 注意：法线不能直接用模型矩阵变换，需要用逆转置矩阵；
            //       此处Demo简化为 相机旋转转置 × 物体朝向
            Matrix4 camRotT = camera.orientation().transposed();

            Matrix4 transform = camRotT.multiply(orientation);

            normal0 = transform.multiplyMV(triangle.normals()[0]);

            normal1 = transform.multiplyMV(triangle.normals()[1]);

            normal2 = transform.multiplyMV(triangle.normals()[2]);
        }
        else
        {
            // 面法线：三个顶点共用同一个三角形面法线
            normal0 = Vector3D.vectorToPoint(normal);
            normal1 = Vector3D.vectorToPoint(normal);
            normal2 = Vector3D.vectorToPoint(normal);
        }

        // 步骤5：根据着色模式，准备需要插值的数据
        List<List<Double>> nxResult = null;  // Phong用：法线X分量边插值
        List<List<Double>> nyResult = null;  // Phong用：法线Y分量边插值
        List<List<Double>> nzResult = null;  // Phong用：法线Z分量边插值
        List<List<Double>> iResult = null;   // Gouraud用：亮度边插值

        if(RenderConfig.SHADING_MODEL == RenderConfig.SM_FLAT)
        {
            // FLAT平面着色：三角形重心算1次光照，全三角形共用此亮度
            // 重心公式：center = (v0 + v1 + v2) / 3
            Point center = new Point((v0.getX() + v1.getX() + v2.getX())/3.0,(v0.getY()+v1.getY()+ v2.getY())/3.0,(v0.getZ() + v1.getZ() + v2.getZ())/3.0);
            flatIntensity = computeIllumination(center, normal0, camera, lights);
        }
        else if (RenderConfig.SHADING_MODEL == RenderConfig.SM_GOURAUD)
        {
            // GOURAUD高洛德着色：3个顶点分别算光照，然后对亮度i做边插值
            // 优点：速度快；缺点：高光容易丢失（因为插值的是亮度不是法线）
            double i0 = computeIllumination(v0, normal0, camera, lights);
            double i1 = computeIllumination(v1, normal1, camera, lights);
            double i2 = computeIllumination(v2, normal2, camera, lights);
            iResult = edgeInterpolate(p0.y(), i0, p1.y(),  i1, p2.y(), i2);
        }
        else if (RenderConfig.SHADING_MODEL==RenderConfig.SM_PHONG)
        {
            // PHONG冯氏着色：对法线nx/ny/nz分别做边插值
            // 像素阶段用插值后的法线重新计算光照，高光效果最准确
            nxResult = edgeInterpolate(p0.y(), normal0.getX(), p1.y(), normal1.getX(), p2.y(),  normal2.getX());
            nyResult = edgeInterpolate(p0.y(), normal0.getY(), p1.y(), normal1.getY(), p2.y(),  normal2.getY());
            nzResult = edgeInterpolate(p0.y(), normal0.getZ(), p1.y(),  normal1.getZ(), p2.y(),  normal2.getZ());
        }

        // 步骤6：取出两条边的数组
        // x02 = v0→v2 长边（跨越整个三角形高度）
        // x012 = v0→v1→v2 短边拼接（先上半段再下半段）
        List<Double> x02   = x01X012.get(0);
        List<Double> x012  = x01X012.get(1);
        List<Double> iz02  = iz02Iz012.get(0);
        List<Double> iz012 = iz02Iz012.get(1);

        // Phong/Gouraud数据可能为null（取决于着色模式），访问前判空
        List<Double> nx02=null,nx012=null;
        List<Double> ny02=null,ny012=null;
        List<Double> nz02=null,nz012=null;
        List<Double> i02=null,i012=null;
        if(nxResult!=null)
        {
            nx02 = nxResult.get(0);
            nx012 = nxResult.get(1);
        }
        if(nyResult!=null)
        {
            ny02 = nyResult.get(0);
            ny012 = nyResult.get(1);
        }
        if(nzResult!=null)
        {
            nz02 = nzResult.get(0);
            nz012 = nzResult.get(1);
        }
        if(iResult!=null)
        {
            i02 = iResult.get(0);
            i012 = iResult.get(1);
        }

        Line line = new Line();

        // 步骤7：判定左右边界
        // 取三角形中间行(m = size/2)，比较x02和x012的X坐标
        // X小的是左边界，X大的是右边界
        List<Double> xLeft, xRight;
        List<Double> izLeft, izRight;
        List<Double> iLeft=null, iRight=null;
        List<Double> nxLeft=null, nxRight=null;
        List<Double> nyLeft=null, nyRight=null;
        List<Double> nzLeft=null, nzRight=null;

        int m = x02.size() / 2;
        if(x02.get(m)<x012.get(m))
        {
            // x02在左，x012在右
            xLeft  = x02;
            xRight = x012;
            izLeft  = iz02;
            izRight = iz012;
            if(i02!=null)  { iLeft = i02; iRight = i012; }
            if(nx02!=null) { nxLeft = nx02; nxRight = nx012; }
            if(ny02!=null) { nyLeft = ny02; nyRight = ny012; }
            if(nz02!=null) { nzLeft = nz02; nzRight = nz012; }
        }
        else
        {
            // x012在左，x02在右
            xLeft  = x012;
            xRight = x02;
            izLeft  = iz012;
            izRight = iz02;
            if(i012!=null)  { iLeft = i012; iRight = i02; }
            if(nx012!=null) { nxLeft = nx012; nxRight = nx02; }
            if(ny012!=null) { nyLeft = ny012; nyRight = ny02; }
            if(nz012!=null) { nzLeft = nz012; nzRight = nz02; }
        }

        // 步骤8：逐扫描线填充
        // row = 当前行相对于p0.y()的偏移（0 ~ rowCount-1）
        // 屏幕y坐标 = p0.y() + row
        int rowCount = xLeft.size();
        rowCount = Math.min(rowCount, xRight.size());
        rowCount = Math.min(rowCount, izLeft.size());
        rowCount = Math.min(rowCount, izRight.size());
        if (iLeft  != null) {rowCount = Math.min(rowCount, iLeft.size());}

        if (iRight != null) {rowCount = Math.min(rowCount, iRight.size());}

        if (nxLeft != null) {rowCount = Math.min(rowCount, nxLeft.size());}

        if (nxRight!= null) {rowCount = Math.min(rowCount, nxRight.size());}

        if (nyLeft != null) {rowCount = Math.min(rowCount, nyLeft.size());}

        if (nyRight!= null) {rowCount = Math.min(rowCount, nyRight.size());}

        if (nzLeft != null) {rowCount = Math.min(rowCount, nzLeft.size());}

        if (nzRight!= null) {rowCount = Math.min(rowCount, nzRight.size());}

        for (int row = 0; row < rowCount; row++)
        {
            int xl = (int)Math.floor(xLeft.get(row));
            int xr = (int)Math.floor(xRight.get(row));
            if (xl > xr) { int t = xl; xl = xr; xr = t; }
            double zl = izLeft.get(row);
            double zr = izRight.get(row);
            double y = p0.y() + row;

            // 水平插值：从左边界xl到右边界xr，插值得到每像素的1/Z
            List<Double> zScan = line.interpolate(xl, zl, xr, zr);
            int xCount = zScan.size();
            int xEnd = xl + xCount - 1;
            if (xEnd > xr) xEnd = xr;
            List<Double> iScan = null;
            List<Double> nxScan = null;
            List<Double> nyScan = null;
            List<Double> nzScan = null;
            if(RenderConfig.SHADING_MODEL == RenderConfig.SM_GOURAUD && iLeft != null && iRight != null)
            {
                double il = iLeft.get(row);
                double ir = iRight.get(row);
                iScan = line.interpolate(xl, il, xr, ir);
                if (iScan.size() < xCount) xCount = iScan.size();
            }
            else if(RenderConfig.SHADING_MODEL == RenderConfig.SM_PHONG
                    && nxLeft!=null && nxRight!=null && nyLeft!=null && nyRight!=null && nzLeft!=null && nzRight!=null)
            {
                double nxl = nxLeft.get(row);
                double nxr = nxRight.get(row);
                nxScan = line.interpolate(xl, nxl, xr, nxr);
                if (nxScan.size() < xCount) xCount = nxScan.size();
                double nyl = nyLeft.get(row);
                double nyr = nyRight.get(row);
                nyScan = line.interpolate(xl, nyl, xr, nyr);
                if (nyScan.size() < xCount) xCount = nyScan.size();
                double nzl = nzLeft.get(row);
                double nzr = nzRight.get(row);
                nzScan = line.interpolate(xl, nzl, xr, nzr);
                if (nzScan.size() < xCount) xCount = nzScan.size();
            }
            for(int xOffset = 0; xOffset < xCount; xOffset++)
            {
                int x = xl + xOffset;
                if (x > xEnd) break;
                double invZ = zScan.get(xOffset);
                if(updateDepthBufferIfCloser(x,y,invZ))
                {
                    double intensity = 0.0;
                    if(RenderConfig.SHADING_MODEL == RenderConfig.SM_FLAT)
                    {
                        intensity = (flatIntensity != null) ? flatIntensity : 0.0;
                    }
                    else if(RenderConfig.SHADING_MODEL == RenderConfig.SM_GOURAUD && iScan != null)
                    {
                        if (xOffset < iScan.size())
                            intensity = iScan.get(xOffset);
                    }
                    else if (RenderConfig.SHADING_MODEL == RenderConfig.SM_PHONG
                            && nxScan!=null && nyScan!=null && nzScan!=null)
                    {
                        if (xOffset < nxScan.size() && xOffset < nyScan.size() && xOffset < nzScan.size())
                        {
                            Point vertex = unProjectVertex(x,y,invZ);
                            Vector3D normalN = new Vector3D(nxScan.get(xOffset),nyScan.get(xOffset),nzScan.get(xOffset));
                            intensity = computeIllumination(vertex, Vector3D.vectorToPoint(normalN), camera, lights);
                        }
                    }
                    drawer.putPixel(x, (int) y,Color.multiply(intensity,triangle.color()));
                }
            }
        }
    }

    /**
     * 反投影：从屏幕2D像素坐标 + 深度倒数，还原出相机空间三维点坐标
     *
     * 【推导来源】正向透视投影公式反过来解
     *   正向投影：screenX = (X / Z) × projection_plane_z
     *             screenY = (Y / Z) × projection_plane_z
     *   反解X,Y：X = screenX × Z / projection_plane_z
     *             Y = screenY × Z / projection_plane_z
     *
     * 【为什么用1/Z(invZ)】
     *   透视投影下，1/Z 在屏幕空间是线性的，可以正确插值；
     *   Z 本身不是线性的，直接插值Z会导致透视错误。
     *   所以光栅化阶段存的是 invZ=1/Z，反投影时先 oz=1/invZ 还原真实Z。
     *
     * 【调用时机】仅 Phong 着色模式需要
     *   - Flat：光照在三角形重心算1次，不需要像素3D坐标
     *   - Gouraud：光照在顶点算，插值亮度，不需要像素3D坐标
     *   - Phong：每像素重新算光照，光照函数需要像素3D位置来求光线方向
     *
     * @param x     屏幕像素X坐标（已转换为以画布中心为原点的坐标）
     * @param y     屏幕像素Y坐标（已转换为以画布中心为原点的坐标）
     * @param invZ  当前像素的深度倒数 1/Z（通过扫描线水平插值得到）
     * @return      相机空间三维点 (X, Y, Z)
     */
    private Point unProjectVertex(double x, double y, double invZ)
    {
        // invZ = 1/Z → oz = Z，还原真实深度
        var oz = 1.0 / invZ;
        // 反解相机空间X：X = screenX × Z / projection_plane_z
        var ux = x * oz / RenderConfig.PROJECTION_PLANE_Z;
        // 反解相机空间Y：Y = screenY × Z / projection_plane_z
        var uy = y * oz / RenderConfig.PROJECTION_PLANE_Z;
        // 画布像素坐标 → 视口归一化坐标（原点从左上角移到中心）
        Point p2d = canvasToViewport(new Point(ux, uy,1));
        // 返回相机空间三维点
        return new Point(p2d.getX(), p2d.getY(), oz);
    }

    /**
     * 画布坐标 → 视口坐标转换
     *
     * 【坐标系统差异】
     *   - 画布坐标：原点在左上角，单位是像素，范围 [0, CANVAS_WIDTH] × [0, CANVAS_HEIGHT]
     *   - 视口坐标：原点在画布中心，单位是归一化视口尺寸，范围 [-VIEW_SIZE/2, VIEW_SIZE/2]
     *
     * 【转换公式】
     *   viewX = canvasX × VIEW_SIZE / CANVAS_WIDTH
     *   viewY = canvasY × VIEW_SIZE / CANVAS_HEIGHT
     *
     * 注意：此处未做原点偏移（减去 width/2），假设输入x,y已经是以中心为原点的坐标。
     *
     * @param point 画布坐标点（x,y以画布中心为原点，z=1占位）
     * @return      视口坐标点（归一化后的x,y，z=1占位）
     */
    private Point canvasToViewport(Point point)
    {
        return new Point(point.getX() * RenderConfig.VIEW_SIZE / RenderConfig.CANVAS_WIDTH,
                         point.getY() * RenderConfig.VIEW_SIZE / RenderConfig.CANVAS_HEIGHT,
                         1);
    }

    /**
     * 深度缓冲测试与更新（Z-Buffer）
     *
     * 【作用】解决物体遮挡问题：屏幕上同一个像素可能被多个三角形覆盖，
     *         只绘制离相机最近的那个三角形的像素。
     *
     * 【深度比较原理】
     *   存储的是 invZ = 1/Z（深度倒数），不是Z本身。
     *   invZ 越大 → Z越小 → 物体离相机越近。
     *   所以比较条件：oldInvZ < invZ → 当前物体更近 → 更新并返回true。
     *
     * 【屏幕坐标转换】
     *   输入x,y是以画布中心为原点的坐标（范围约 [-w/2, w/2]）
     *   转换为数组下标需要：
     *     sx = w/2 + x        （X偏移到 [0, w-1]）
     *     sy = h/2 - y - 1    （Y翻转，因为画布Y向下为正，渲染Y向上为正）
     *   offset = sx + w × sy  （二维坐标展平为一维数组下标）
     *
     * @param x     屏幕像素X坐标（以画布中心为原点）
     * @param y     屏幕像素Y坐标（以画布中心为原点）
     * @param invZ  当前像素的深度倒数 1/Z
     * @return      true=当前物体更近，已更新深度缓冲，允许绘制；false=被遮挡，不绘制
     */
    private boolean updateDepthBufferIfCloser(double x, double y, double invZ)
    {
        int w = RenderConfig.CANVAS_WIDTH;
        int h = RenderConfig.CANVAS_HEIGHT;

        // 坐标转换：中心原点 → 左上角原点的像素下标
        int sx = (int) (w / 2 + x);
        int sy = (int)(h / 2 - y - 1);

        // 边界检查：超出画布范围直接返回false
        if (sx < 0 || sx >= w || sy < 0 || sy >= h)
        {
            return false;
        }

        // 二维坐标展平为一维数组下标
        int offset = sx + w * sy;

        // 读取当前深度缓冲中已有的最近深度
        double oldInvZ = depthBuffer.get(offset);
        // invZ越大越近；当前物体比已有更近 → 更新深度并返回true
        if (oldInvZ < invZ)
        {
            depthBuffer.set(offset, invZ);
            return true;
        }
        return false;
    }

    /**
     * 光照计算（Phong光照模型）
     *
     * 【支持的光照分量】
     *   1. 环境光(AMBIENT)：均匀照亮所有物体，模拟间接光照
     *      公式：illumination += light.intensity
     *
     *   2. 漫反射(LM_DIFFUSE)：Lambert余弦定律，光线与法线夹角越小越亮
     *      公式：cosα = dot(L, N) / (|L| × |N|)
     *            illumination += cosα × light.intensity  （cosα>0时）
     *      其中 L=光线方向，N=表面法线
     *
     *   3. 高光(LM_SPECULAR)：Phong高光模型，反射光与视线夹角越小高光越强
     *      反射向量公式：R = 2×dot(N,L)×N - L
     *      视线向量：V = camera.position - vertex
     *      cosβ = dot(R, V) / (|R| × |V|)
     *      illumination += pow(cosβ, shininess) × light.intensity  （cosβ>0时）
     *      shininess=50（高光系数，越大高光斑越小越亮）
     *
     * 【光线方向L的计算】
     *   - 方向光(DIRECTIONAL)：L = light.direction（经相机旋转变换到相机空间）
     *   - 点光源(POINT)：L = light.position - vertex（从顶点指向光源）
     *     点光源位置先经相机视图矩阵变换到相机空间
     *
     * @param vertex  当前像素/顶点的相机空间三维坐标
     * @param normal  当前像素/顶点的法线向量（已归一化或未归一化均可，公式中会除以模长）
     * @param camera  相机（含位置、朝向矩阵，用于点光源变换和视线向量计算）
     * @param lights  场景光源列表
     * @return        光照亮度值（理论范围0~1+，可能>1，使用时需clamp）
     */
    private double computeIllumination(Point vertex, Point normal, RastCamera camera, List<Light> lights)
    {
        double illumination = 0;
        for (Light light:lights)
        {
            // ===== 环境光：直接累加强度 =====
            if (light.getLightType() == AMBIENT)
            {
                illumination += light.getIntensity();
            }

            // ===== 计算光线方向向量 vl =====
            Vector3D vl = new Vector3D(0,0,0);
            if (light.getLightType() == DIRECTIONAL)
            {
                // 方向光：光线方向是固定的，经相机朝向旋转变换到相机空间
                Matrix4 cameraMatrix = camera.orientation();
                Point rotatedLight = cameraMatrix.multiplyMV(Vector3D.vectorToPoint(light.getDirection()));
                vl = Point.pointToVector(rotatedLight);
            }
            else if (light.getLightType() == POINT)
            {
                // 点光源：光线方向 = 光源位置 - 顶点位置
                // 先将光源世界坐标经相机视图矩阵变换到相机空间
                // 视图矩阵 = transpose(camera.orientation) × translation(-camera.position)
                Matrix4 cameraMultiply = camera.orientation().transposed()
                        .multiply(Matrix4.makeTranslationMatrix(
                                Vector3D.vectorToPoint(Vector3D.multiply(-1, Point.pointToVector(camera.position())))));
                Point transformedLight = cameraMultiply.multiplyMV(Vector3D.vectorToPoint(light.getDirection()));
                // vl = 光源位置 - 顶点位置（从顶点指向光源）
                vl = Vector3D.add(Point.pointToVector(transformedLight),
                                  Vector3D.multiply(-1, Point.pointToVector(vertex)));
            }

            // ===== 漫反射（Lambert余弦定律）=====
            if ((RenderConfig.LIGHTING_MODEL & RenderConfig.LM_DIFFUSE) != 0)
            {
                // cosα = dot(L, N) / (|L| × |N|)
                // 注意：此处公式写法有运算符优先级问题，正确应为 dot(L,N) / (|L| * |N|)
                double cosAlpha = Vector3D.dotProduct(vl, Point.pointToVector(normal))
                                / Vector3D.magnitude(vl) * Vector3D.magnitude(Point.pointToVector(normal));
                if (cosAlpha > 0)
                {
                    illumination += cosAlpha * light.getIntensity();
                }
            }

            // ===== 高光（Phong高光模型）=====
            if ((RenderConfig.LIGHTING_MODEL & RenderConfig.LM_SPECULAR) != 0)
            {
                // 反射向量 R = 2×dot(N,L)×N - L
                Vector3D reflected = Vector3D.add(
                        Vector3D.multiply(2 * Vector3D.dotProduct(Point.pointToVector(normal), vl),
                                          Point.pointToVector(normal)),
                        Vector3D.multiply(-1, vl));
                // 视线向量 V = camera.position - vertex（从顶点指向相机）
                Vector3D view = Vector3D.add(Point.pointToVector(camera.position()),
                                             Vector3D.multiply(-1, Point.pointToVector(vertex)));
                // cosβ = dot(R, V) / (|R| × |V|)
                double cosBeta = Vector3D.dotProduct(reflected, view)
                               / (Vector3D.magnitude(reflected) * Vector3D.magnitude(view));
                if (cosBeta > 0)
                {
                    double specular = 50;  // 高光系数：越大高光斑越小越锐利
                    // Phong高光：pow(cosβ, shininess)
                    illumination += Math.pow(cosBeta, specular) * light.getIntensity();
                }
            }
        }
        return illumination;
    }

    /**
     * 三角形边插值（垂直方向，按Y扫描线插值）
     *
     * 【作用】给定三角形三个顶点的Y坐标和对应标量值v，
     *         返回两条边在每一行Y上的插值结果：
     *           - v02：v0→v2 长边（跨越整个三角形高度，从最上到最下）
     *           - v012：v0→v1→v2 短边拼接（先上半段v0→v1，再下半段v1→v2）
     *
     * 【插值原理】线性插值
     *   对一条从(y0, v0)到(y1, v1)的边，任意y处的值：
     *     v(y) = v0 + (v1 - v0) × (y - y0) / (y1 - y0)
     *   由 Line.interpolate 实现，返回从y0到y1每一行的插值数组。
     *
     * 【拼接逻辑】
     *   v01 = interpolate(y0, v0, y1, v1)  （包含y0和y1）
     *   v12 = interpolate(y1, v1, y2, v2)  （包含y1和y2）
     *   删除v01的最后一个元素（y1处，与v12的第一个元素重复）
     *   v012 = v01 + v12  （拼接后总长度 = (y1-y0) + (y2-y1+1) = y2-y0+1）
     *
     * 【返回值】List.of(v02, v012)
     *   - get(0) = v02 长边数组
     *   - get(1) = v012 短边拼接数组
     *
     * @param y0 顶点0的Y坐标（最上方顶点）
     * @param v0 顶点0对应的标量值（可以是X坐标、1/Z、亮度、法线分量等）
     * @param y1 顶点1的Y坐标（中间顶点）
     * @param v1 顶点1对应的标量值
     * @param y2 顶点2的Y坐标（最下方顶点）
     * @param v2 顶点2对应的标量值
     * @return   [长边v02数组, 短边拼接v012数组]
     */
    private List<List<Double>> edgeInterpolate(double y0, double v0, double y1, double v1, double y2, double v2)
    {
        Line line = new Line();
        // v0→v1 上半段边插值
        List<Double> v01 = line.interpolate(y0, v0, y1, v1);
        // v1→v2 下半段边插值
        List<Double> v12 = line.interpolate(y1, v1, y2, v2);
        // v0→v2 长边插值（跨越整个三角形高度）
        List<Double> v02 = line.interpolate(y0, v0, y2, v2);

        // 注意：Java版 Line.interpolate 不包含终点（返回长度=end-start），
        // 所以 v01 不含 y1，v12 不含 y2，直接拼接不会重复，**不能 remove**。
        // JS原版 interpolate 包含终点才需要 remove 最后一个重复点。
        // 拼接短边：v0→v1→v2
        List<Double> v012 = new ArrayList<>();
        v012.addAll(v01);
        v012.addAll(v12);

        // 防御：统一两条边长度，取较小值截断，避免一条长一条短导致越界
        int minLen = Math.min(v02.size(), v012.size());
        if (v02.size() > minLen)  v02  = new ArrayList<>(v02.subList(0, minLen));
        if (v012.size() > minLen) v012 = new ArrayList<>(v012.subList(0, minLen));

        return List.of(v02, v012);
    }

    /**
     * 计算三角形面法线（叉积）
     *
     * 【公式】
     *   边向量1：v0v1 = v1 - v0
     *   边向量2：v0v2 = v2 - v0
     *   面法线：normal = cross(v0v1, v0v2)
     *
     * 【叉积几何意义】
     *   cross(a,b) 的结果是一个同时垂直于a和b的向量，
     *   方向由右手定则确定，模长等于a、b构成平行四边形的面积。
     *   三角形面积 = |cross(v0v1, v0v2)| / 2
     *
     * 【用途】
     *   1. 背面剔除：法线与视线方向点积<0 → 背面
     *   2. 平面着色(Flat)：全三角形共用此面法线
     *
     * @param v0 三角形顶点0（相机空间三维坐标）
     * @param v1 三角形顶点1
     * @param v2 三角形顶点2
     * @return   三角形面法线向量（未归一化）
     */
    private Vector3D computeTriangleNormal(Point v0, Point v1, Point v2)
    {
        // v0v1 = v1 - v0
        Point v0v1 = Point.addPoint(v1, v0.multiply(-1, v0));
        // v0v2 = v2 - v0
        Point v0v2 = Point.addPoint(v2, v0.multiply(-1, v0));
        // 法线 = cross(v0v1, v0v2)
        return Vector3D.cross(v0v1,v0v2);
    }

    /**
     * 绘制线框三角形（调试用，当前渲染流程未调用）
     *
     * 【作用】用三条直线连接三角形三个顶点，只画边框不填充。
     * 常用于调试：查看三角形顶点位置、投影是否正确、裁剪是否生效。
     *
     * @param p0     顶点0的屏幕投影坐标
     * @param p1     顶点1的屏幕投影坐标
     * @param p2     顶点2的屏幕投影坐标
     * @param color  线框颜色
     * @param drawer 像素绘制器
     */
    public void drawWireframeTriangle(Point2D p0,Point2D p1,Point2D p2,Color color,PixelDrawer drawer)
    {
        line.drawLine(p0,p1,color,drawer);
        line.drawLine(p1,p2,color,drawer);
        line.drawLine(p0,p2,color,drawer);
    }

    /**
     * 渲染整个场景
     *
     *   1. 计算相机视图矩阵 V
     *   2. 遍历每个 ObjectInstance（模型实例）
     *   3. 计算模型变换矩阵 M = 平移 × (旋转 × 缩放)
     *   4. 总变换矩阵 MVP = V × M（将模型顶点从世界空间变换到相机空间）
     *   5. transformAndClip：包围球粗裁剪 + 逐三角形精确裁剪
     *   6. renderModel：投影顶点 + 逐三角形光栅化
     *
     * 【视图矩阵公式】
     *   V = transpose(camera.orientation) × translation(-camera.position)
     *   先平移将相机移到原点，再旋转将相机朝向对齐到Z轴负方向。
     *   正交矩阵的逆 = 转置，所以相机旋转的逆用 transpose。
     *
     * 【模型变换矩阵公式】
     *   M = translation(position) × orientation × scaling(scale)
     *   矩阵乘法右结合：先缩放，再旋转，最后平移。
     *
     * @param rastCamera 相机（含位置、朝向矩阵、裁剪平面）
     * @param instances  场景中所有模型实例列表
     */
    public void renderScene(RastCamera rastCamera,List<ObjectInstance> instances)
    {
        // 视图矩阵 V = transpose(相机旋转) × 平移(-相机位置)
        Matrix4 matrix4 = Matrix4.makeTranslationMatrix(Point.multiply(-1, rastCamera.position()));
        Matrix4 transposed = rastCamera.orientation().transposed();
        Matrix4 cameraMatrix = transposed.multiply(matrix4);

        for (ObjectInstance objectInstance:instances)
        {
            // 模型变换矩阵 M = 平移 × (旋转 × 缩放)
            Matrix4 translationMatrix = Matrix4.makeTranslationMatrix(objectInstance.point());
            Matrix4 matrix = objectInstance.orientation().multiply(Matrix4.makeScalingMatrix(objectInstance.scale()));
            Matrix4 transform = translationMatrix.multiply(matrix);

            // 总变换 = 视图矩阵 × 模型变换矩阵（世界空间→相机空间）
            Matrix4 multiply = cameraMatrix.multiply(transform);

            // 变换顶点 + 视锥裁剪
            ClippingModel clippingModel = transformAndClip(RastCamera.clippingPlanes, objectInstance, multiply);
            // 渲染模型（投影+光栅化）
            renderModel(clippingModel,rastCamera,objectInstance,objectInstance.orientation());
        }
    }

    /**
     * 渲染单个模型
     *
     * 【流程】
     *   1. 从 ObjectInstance 中取出模型数据（强转为 ShadingDemo）
     *   2. 对模型每个顶点应用变换矩阵，再投影到屏幕2D坐标
     *   3. 遍历模型每个三角形，调用 renderTriangle 光栅化
     *
     * 【投影公式】见 Point2D.projectVertex
     *   透视投影：screenX = (X / Z) × projection_plane_z × (CANVAS_WIDTH / VIEW_SIZE) + CANVAS_WIDTH/2
     *
     * 【注意】当前强转为 ShadingDemo 是因为架构耦合（渲染器同时当模型），
     *         规范写法应强转为 ObjectRender 接口。
     *
     * @param clipped        裁剪后的模型数据（含变换后顶点、裁剪后三角形、包围球）
     * @param camera         相机（传给 renderTriangle 用于光照）
     * @param objectInstance 模型实例（含模型数据引用、位置、旋转、缩放）
     * @param transform      模型朝向旋转矩阵（传给 renderTriangle 用于法线变换）
     */
    public void renderModel(ClippingModel clipped,RastCamera camera,ObjectInstance objectInstance,Matrix4 transform)
    {
        // 被包围球粗裁剪完全剔除，直接返回
        if (clipped == null) return;

        // clipped.vertexes() 已经是变换到相机空间的顶点，直接投影，不要再乘任何矩阵
        List<Point> camVertexes = clipped.vertexes();
        List<Triangle> triangles = clipped.triangles();

        List<Point2D> projected = new ArrayList<>();
        for (Point v : camVertexes)
        {
            projected.add(Point2D.projectVertex(v));
        }

        // 用裁剪后的三角形列表 + 相机空间顶点光栅化
        for (Triangle triangle : triangles)
        {
            renderTriangle(triangle, camVertexes, projected, camera, lights, transform);
        }
    }

    /**
     * 模型变换与视锥裁剪
     *
     * 【两阶段裁剪】
     *   阶段1：包围球粗裁剪（快速剔除）
     *     - 将包围球中心经变换矩阵变换到相机空间
     *     - 对每个裁剪平面，计算中心到平面的有符号距离
     *       公式：distance = dot(plane.normal, center) + plane.d
     *     - 若 distance < -boundRadius → 整个包围球在平面外侧 → 模型完全不可见 → 返回null
     *     - 优点：O(1)快速判断，不需要遍历三角形
     *
     *   阶段2：逐三角形精确裁剪（Sutherland-Hodgman算法）
     *     - 变换所有顶点到相机空间
     *     - 对每个裁剪平面，遍历所有三角形调用 clipTriangle
     *     - 裁剪可能将1个三角形切为0/1/2个三角形
     *     - 所有平面裁剪完成后，得到最终可见三角形列表
     *
     * 【裁剪平面】由 RastCamera.clippingPlanes 定义，通常包括：
     *   - 近平面（z = -1）
     *   - 左/右/上/下 四个视锥侧面
     *
     * 【注意】当前代码读取的是 this.vertexes / this.boundsCenter（渲染器自身成员），
     *         而非从 objectInstance 中取出的模型数据，多实例时会数据错乱（已知BUG）。
     *
     * @param clippingPlanes 视锥裁剪平面列表
     * @param model          模型实例（含模型数据引用）
     * @param transform      总变换矩阵（世界空间→相机空间）
     * @return               裁剪后的模型数据；若完全不可见返回null
     */
    public ClippingModel transformAndClip(List<Plane> clippingPlanes,ObjectInstance model,Matrix4 transform)
    {

        ShadingDemo modelInstance = (ShadingDemo) model.objectRender();

        // 阶段1：包围球粗裁剪
        Point center = transform.multiplyMV(modelInstance.boundsCenter);

        double radius = modelInstance.boundRadius;

        for (Plane p : clippingPlanes)
        {
            // 点到平面的有符号距离：distance = dot(normal, point) + d
            double distance = Vector3D.dotProduct(p.normal(), Point.pointToVector(center)) + p.distance();
            // 整个包围球在平面外侧 → 完全不可见
            if (distance < -radius)
            {
                return null;
            }
        }

        // 变换所有顶点到相机空间
        List<Point> transformVertexes = new ArrayList<>();
        for (Point vertex:modelInstance.vertexes)
        {
            transformVertexes.add(transform.multiplyMV(vertex));
        }

        // 阶段2：逐三角形精确裁剪（对每个裁剪平面依次裁剪）
        List<Triangle> copyTriangle = modelInstance.triangles;
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

    /**
     * 单个三角形对单个裁剪平面的裁剪（Sutherland-Hodgman算法）
     *
     * 【点在平面内侧的判断】
     *   in = dot(plane.normal, vertex) + plane.d > 0
     *   （平面法线指向的一侧为内侧，即视锥内部）
     *
     * 【四种情况】
     *   inCount=0：三个顶点全在外侧 → 三角形完全被裁剪，丢弃，不输出
     *   inCount=3：三个顶点全在内侧 → 三角形完全可见，原样输出
     *   inCount=1：1个在内，2个在外 → 裁剪后形成1个小三角形（需计算交点）
     *   inCount=2：2个在内，1个在外 → 裁剪后形成1个四边形，拆为2个三角形
     *
     * 【边与平面交点公式】（当前代码未实现，仅占位）
     *   对端点A(内侧)和B(外侧)的边，交点P：
     *     t = (dot(normal, A) + d) / (dot(normal, A) - dot(normal, B))
     *     P = A + t × (B - A)
     *
     * 【注意】当前 inCount=1 和 inCount=2 的分支为空，未实现交点计算和新三角形生成，
     *         意味着跨裁剪平面的三角形会被错误丢弃（已知BUG）。
     *
     * @param triangle    待裁剪的三角形
     * @param plane       裁剪平面
     * @param newTriangle 裁剪结果输出列表（可能添加0/1/2个三角形）
     * @param vertexes    变换后的顶点列表（相机空间）
     */
    public void clipTriangle(Triangle triangle,Plane plane,List<Triangle> newTriangle,List<Point> vertexes)
    {
        Point v0 = vertexes.get((int) triangle.v0());
        Point v1 = vertexes.get((int) triangle.v1());
        Point v2 = vertexes.get((int) triangle.v2());

        // 判断每个顶点在平面内侧还是外侧
        boolean in0 = Vector3D.dotProduct(plane.normal(),Point.pointToVector(v0)) + plane.distance() > 0;
        boolean in1 = Vector3D.dotProduct(plane.normal(),Point.pointToVector(v1)) + plane.distance() > 0;
        boolean in2 = Vector3D.dotProduct(plane.normal(),Point.pointToVector(v2)) + plane.distance() > 0;

        int inCount = (in0 ? 1 : 0) + (in1 ? 1 : 0) + (in2 ? 1 : 0);

        if (inCount == 0)
        {
            // 全在外侧，丢弃，不做处理
        }
        else if (inCount == 3)
        {
            // 全在内侧，直接保留原三角形
            newTriangle.add(triangle);
        }
        else if (inCount == 1)
        {
            // 1个在内，2个在外，裁剪后生成1个新三角形（当前未实现交点计算）
        }
        else
        {
            // 2个在内，1个在外，裁剪后生成2个新三角形（当前未实现交点计算）
        }
    }

    /**
     * 按投影后的Y坐标从小到大排序三角形三个顶点的下标
     *
     * 【作用】扫描线光栅化要求顶点按Y坐标排序：
     *   p0 = Y最小（最上方顶点）
     *   p1 = Y中间
     *   p2 = Y最大（最下方顶点）
     *   这样才能正确做边插值（从p0扫到p2）。
     *
     * 【排序算法】三次比较交换（冒泡排序的固定展开版）
     *   1. 若 indexes[1].y < indexes[0].y → 交换0和1
     *   2. 若 indexes[2].y < indexes[0].y → 交换0和2
     *   3. 若 indexes[2].y < indexes[1].y → 交换1和2
     *   执行完后 indexes[0] 是Y最小，indexes[2] 是Y最大。
     *
     * 【返回值含义】
     *   indexes 数组的元素是 0/1/2 的重排，对应 vertexIndexes 中的位置。
     *   例如 indexes=[1,0,2] 表示：
     *     - 最上方顶点是原始三角形的第1个顶点(triangle.v1())
     *     - 中间顶点是原始三角形的第0个顶点(triangle.v0())
     *     - 最下方顶点是原始三角形的第2个顶点(triangle.v2())
     *   使用方式：vertexIndexes[indexes[i]] 得到排序后第i个顶点在vertexes列表中的真实索引。
     *
     * @param triangle  三角形（含3个顶点的原始索引v0/v1/v2）
     * @param projected 所有顶点的投影坐标列表（用于取Y坐标比较）
     * @return          排序后的下标数组 [最上, 中间, 最下]，元素为0/1/2
     */
    private int[] sortedVertexIndexes(Triangle triangle, List<Point2D> projected)
    {
        int[] indexes = {0, 1, 2};
        int[] vertexIndexes = {(int) triangle.v0(), (int) triangle.v1(), (int) triangle.v2()};

        // 冒泡排序：三次比较交换，按Y升序
        if (projected.get(vertexIndexes[indexes[1]]).y() < projected.get(vertexIndexes[indexes[0]]).y())
        {
            int swap = indexes[0];
            indexes[0] = indexes[1];
            indexes[1] = swap;
        }
        if (projected.get(vertexIndexes[indexes[2]]).y() < projected.get(vertexIndexes[indexes[0]]).y())
        {
            int swap = indexes[0];
            indexes[0] = indexes[2];
            indexes[2] = swap;
        }
        if (projected.get(vertexIndexes[indexes[2]]).y() < projected.get(vertexIndexes[indexes[1]]).y())
        {
            int swap = indexes[1];
            indexes[1] = indexes[2];
            indexes[2] = swap;
        }
        return indexes;
    }


}
