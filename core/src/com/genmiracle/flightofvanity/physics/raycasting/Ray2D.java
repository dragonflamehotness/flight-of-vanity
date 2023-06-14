package com.genmiracle.flightofvanity.physics.raycasting;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.genmiracle.flightofvanity.instance.Instance;

public class Ray2D {
    private Vector2 origin;
    private Vector2 dir;
    private Vector2 v2temp;
    private Vector2 v2temp2;

    private int side;

    public Ray2D(Vector2 origin, Vector2 dir, int side) {
        this.origin = new Vector2(origin);
        this.dir = new Vector2(dir);
        this.side = side;

        v2temp = new Vector2();
        v2temp2 = new Vector2();
    }

    public Vector2 getIntersectToLineSeg(float x1, float y1, float x2, float y2) {
        float ax = x1 - origin.x;
        float ay = y1 - origin.y;

        v2temp.set(ax, ay);
        v2temp2.set(x2 - x1, y2 - y1);
        float b = v2temp.crs(dir);
        float c = dir.crs(v2temp2);

        float u = b / c;
        float t = v2temp.crs(v2temp2) / c;
        if (t >= 0.01 && u >= -0.01 && u <= 1.01) {
            v2temp.set(x2 - x1, y2 - y1);
            v2temp.scl(u);
            v2temp.add(x1, y1);
            return v2temp;
        }
        return null;
    }

    public Vector2 getDir() {
        return dir;
    }

    public void setDir(Vector2 dir) {
        this.dir.set(dir);
    }

    public Vector2 getOrigin() {
        return origin;
    }

    public void setOrigin(Vector2 origin) {
        this.origin.set(origin);
    }

    public int getSide() {
        return side;
    }

    public void setSide(int side) {
        this.side = side;
    }
}
