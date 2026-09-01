package com.rast;

import com.demo.Point;

import java.util.List;

public record ClippingModel(List<Point> vertexes, List<Triangle> triangles,Point center,double boundsRadius)
{

}
