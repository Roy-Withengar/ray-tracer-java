package com.rast;

import com.demo.Point;

import java.util.List;

public record  RastCamera(Point position,Matrix4 orientation)
{
    public static List<Plane> clippingPlanes ;

}
