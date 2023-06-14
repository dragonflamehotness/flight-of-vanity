package com.genmiracle.flightofvanity.physics.obstacle;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;

public class SensorObstacle extends BoxObstacle {
    private Fixture sensor;
    private float angle;
    private int sensorCounter;

    /**
     * Creates a new box at the origin.
     * <p>
     * The size is expressed in physics units NOT pixels.  In order for
     * drawing to work properly, you MUST set the drawScale. The drawScale
     * converts the physics units to pixels.
     *
     * @param width  The object width in physics units
     * @param height The object width in physics units
     */
    public SensorObstacle(float width, float height) {
        super(width, height);
    }

    /**
     * Creates a new box object.
     *
     * The size is expressed in physics units NOT pixels.  In order for
     * drawing to work properly, you MUST set the drawScale. The drawScale
     * converts the physics units to pixels.
     *
     * @param x  		Initial x position of the box center
     * @param y  		Initial y position of the box center
     * @param width		The object width in physics units
     * @param height	The object width in physics units
     */
    public SensorObstacle(float x, float y, float width, float height) {
        super(x,y, width, height);
    }

    @Override
    protected void createFixtures() {
        super.createFixtures();

        PolygonShape ps = new PolygonShape();
        ps.setAsBox(getWidth() / 2 - 0.25f, getHeight() / 2 - 0.25f, new Vector2(0, 0), 0);

        FixtureDef fd = new FixtureDef();
        fd.shape = ps;
        fd.density = 1;
        fd.isSensor = true;

        sensor = getBody().createFixture(fd);
        sensor.setUserData(2);

        markDirty(false);

//        body.setFixedRotation(false);
//        body.setAngularDamping(Float.POSITIVE_INFINITY);
    }

    @Override
    protected void releaseFixtures() {
        super.releaseFixtures();

        if (sensor != null) {
            body.destroyFixture(sensor);
            sensor = null;
        }
    }

    @Override
    public void update(float delta) {
//        body.setAngularVelocity(0);
    }

    public void changeSensorCount(int change) {
        sensorCounter += change;
        if (sensorCounter < 0) {
            sensorCounter = 0;
        }
    }

    public void setSensorCount(int sensorCounter) {
        this.sensorCounter = sensorCounter;
    }

    public int getSensorCount() {
        return sensorCounter;
    }
}
