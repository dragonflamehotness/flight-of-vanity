package com.genmiracle.flightofvanity.physics.raycasting;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.RayCastCallback;
import com.genmiracle.flightofvanity.physics.PhysicsController;
import com.genmiracle.flightofvanity.physics.obstacle.Obstacle;

import java.util.HashMap;

public class LightRayCastCallback implements RayCastCallback {
    private HashMap<Body, Obstacle> bodyMap;
    private int originSide;
    private Obstacle hit;
    private Vector2 hitPos;
    private Vector2 normal;
    private float closestHit;
    private boolean isSilhouette;

    public LightRayCastCallback(HashMap<Body, Obstacle> bodyMap, int side) {
        this.bodyMap = bodyMap;
        originSide = side;

        hit = null;
        closestHit = Float.MAX_VALUE;

        hitPos = new Vector2();
        normal = new Vector2();
    }

    public void reset() {
        hit = null;
        closestHit = Float.MAX_VALUE;

        hitPos.set(-5000, -5000);
        normal.set(0, 0);
    }

    public void setSilhouette(boolean isSilhouette) {
        this.isSilhouette = isSilhouette;
    }

    public void setSide(int side) {
        originSide = side;
    }

    public int getSide() {
        return originSide;
    }

    public void clear() {
        hit = null;
    }

    public Obstacle getHit() {
        return hit;
    }

    public Vector2 getNormal() {
        return normal;
    }

    public Vector2 getHitPos() {
        return hitPos;
    }

    /**
     * Called for each fixture found in the query. You control how the ray cast proceeds by returning a float: return -1: ignore
     * this fixture and continue return 0: terminate the ray cast return fraction: clip the ray to this point return 1: don't clip
     * the ray and continue.
     * <p>
     * The {@link Vector2} instances passed to the callback will be reused for future calls so make a copy of them!
     *
     * @param fixture  the fixture hit by the ray
     * @param point    the point of initial intersection
     * @param normal   the normal vector at the point of intersection
     * @param fraction
     * @return -1 to filter, 0 to terminate, fraction to clip the ray for closest hit, 1 to continue
     **/
    @Override
    public float reportRayFixture(Fixture fixture, Vector2 point, Vector2 normal, float fraction) {
        Body b = fixture.getBody();
        if (fraction < closestHit &&
                (!fixture.isSensor() || fixture.getFilterData().categoryBits == PhysicsController.PhysicsConstants.MIRROR)
                && fixture.getFilterData().maskBits != 0 &&
                (!isSilhouette || fixture.getFilterData().categoryBits != PhysicsController.PhysicsConstants.PLAYER)) {
            closestHit = fraction;

            hitPos.set(point);
            this.normal.set(normal);
            if (bodyMap.containsKey(b)) {
                hit = bodyMap.get(b);
            } else {
                hit = null;
            }
        }
        return 1;
    }
}
