package com.genmiracle.flightofvanity.instance.model;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.genmiracle.flightofvanity.instance.Instance;
import com.genmiracle.flightofvanity.level.LightController;
import com.genmiracle.flightofvanity.physics.obstacle.SensorObstacle;
import com.genmiracle.flightofvanity.util.Utilities;

import java.util.HashMap;

public class Mirror extends Instance {

    private float angleOfRotation;
    /**
     * Angle of which it has rotated in degree.
     */

    private int orientation;

    private int sensorCount;

    private boolean isMovable;

    private boolean isRotatable;
    private float initangle;
    /**
     * Initial reflection angle of the 360 degree mirror.
     */

    private int type;
    /**
     * The type of mirror; Could be 45 degree, 90 degree, and 360 degree.
     */
    private int reflectionEdge;
    /**
     * The edge that the mirror reflect at.
     * For 90 degrees mirror: 0 - left, 1 - top, 2 - right, 3- bottom.
     * For 45 degrees mirror: 0 - bottom left, 1 - top left, 2 - top right, 3 - bottom right
     * For 360 degrees mirror: -1 - no reflection edge
     */

    private Vector3 position;
    /**
     * Mirror position.
     */
    private boolean isPickup;
    /**
     * True if mirror is pickup by player; otherwise false.
     */
//    private int side;
    /**
     * The side of the cube the mirror is at.
     */

    private float width;
    private float height;
    private boolean isRotated;
    /** True if mirror is rotated by the player; otherwise false. */

    /**
     * Create Mirror at the given position, type, and side.
     *
     * @param type            The type of mirror; 45 or 90 degree
     * @param reflectionEdge  The edge that the mirror reflect at.
     *                        For 90 degrees mirror: 0 - left, 1 - top, 2 - right, 3- bottom.
     *                        For 45 degrees mirror: 0 - bottom left, 1 - top left, 2 - top right, 3 - bottom right
     *                        For 360 degrees mirror: -1 - no reflection edge
     * @param side            The side of "cube" world
     * @param x               The initial x-coordinate of the mirror
     * @param y               The initial y-coordinate of the mirror
     * @param width           the width of the mirror
     * @param height          the height of the mirror
     */
    public Mirror(int type, int reflectionEdge, int side, float x, float y, float width, float height, boolean movable, TextureRegion tr, int facing) {
        super(x, y, width, height, side, facing, tr, "mirror", true, true, false);
        this.type = type;
        this.reflectionEdge = reflectionEdge;
        this.angleOfRotation = 0f;
//        this.side = side;
        this.orientation = 0;
        position = new Vector3(x, y, side);
        isPickup = false;
        isRotated = false;
        this.width = width;
        this.height = height;
        this.isMovable = movable;
        this.isRotatable = false;

        //set

    }
    /**
     * Create Mirror at the given position, type, angle of rotation, side.
     *
     *                        For 90 degrees mirror: 0 - left, 1 - top, 2 - right, 3- bottom.
     *                        For 45 degrees mirror: 0 - bottom left, 1 - top left, 2 - top right, 3 - bottom right
     *                        For 360 degrees mirror: -1 - no reflection edge
     * @param angleOfRotation The angle the mirror rotated in degree
     * @param side            The side of "cube" world
     * @param x               The initial x-coordinate of the mirror
     * @param y               The initial y-coordinate of the mirror
     * @param width           the width of the mirror
     * @param height          the height of the mirror
     * @param movable         if the mirror is movable or not
     */
    public Mirror(float angleOfRotation, int side, float x, float y, float width, float height, boolean movable, TextureRegion tr, int facing) {
        super(x, y, width, height, side, facing, tr, "mirror", true, true, false);
        this.type = 360;
        this.reflectionEdge = -1;
        this.orientation = 0;
        //only rotate if type degree is 360
        this.angleOfRotation = angleOfRotation;
//        this.side = side;
        position = new Vector3(x, y, side);
        isPickup = false;
        isRotated = false;
        this.width = width;
        this.height = height;
        this.isMovable = movable;
        this.isRotatable = true;

        //set

    }

    public boolean getMovable(){
        return isMovable;
    }

    public void setMovable(boolean movable){
        this.isMovable = movable;
    }

    public boolean getRotatable(){
        return isRotatable;
    }

    public void setRotatable(boolean rotatable){
        this.isRotatable =rotatable;
    }

    /**
     * Returns the reflection edge of the mirror
     *The edge that the mirror reflect at.
     *                        For 90 degrees mirror: 0 - left, 1 - top, 2 - right, 3- bottom.
     *                        For 45 degrees mirror: 0 - bottom left, 1 - top left, 2 - top right, 3 - bottom right
     *                        For 360 degrees mirror: -1 - no reflection edge
     *
     * @return reflection edge
     */
    public int getReflectionEdge(){
        return reflectionEdge;
    }

    /**
     * Returns the type of mirror
     *
     * @return degree of mirror (45, 90, 360)
     */
    public int getType() {
        return type;
    }

    /**
     * Returns the orientation of the mirror
     *
     * @return the orientation of the mirror
     */
    public float getAngleOfRotation() {
        if (type == 45 || type == 90) {
            return 0f;
        } else {
            return angleOfRotation;
        }

    }


    public void setReflectionAngle(int reflectionEdge){
        this.reflectionEdge = reflectionEdge;
    }

    /**
     * Sets the angle of rotation for the 360 degree mirror. 45 and 90 degrees mirror always have angle of zero
     *
     * @param value the orientation of the mirror
     */
    public void setAngleOfRotation(float value) {
        if (type == 45 || type == 90) {
            angleOfRotation = 0;
        } else {
            angleOfRotation = value;
            isRotated = true;
        }
    }

    public void setPosition(float x, float y) {
        super.setPosition(x, y);

        position.set(x, y, getSide());
    }

    public void setSide(int side) {
        super.setSide(side);
        position.z = side;
    }

    public void changeSensorCount(int change) {
        sensorCount += change;
        if (sensorCount < 0) {
            sensorCount = 0;
        }
    }

    public int getSensorCount() {
        return sensorCount;
    }

    /**
     * Returns whether the mirror is pickup by the player
     *
     * @return true if moved; otherwise false
     */
    public boolean isPickup() {
        return isPickup;
    }

    /**
     * Returns whether the mirror is rotated by the player
     *
     * @return true if moved; otherwise false
     */
    public boolean isRotated() {
        return isRotated;
    }

    private Vector3 midpoint(Vector3 p1, Vector3 p2){
        return new Vector3((p1.x+p2.x)/2,(p1.y+p2.y)/2, position.z);
    }

    /**
     * Returns the reflection ray, i.e, the normal of the reflection edge,
     * and the starting position of the reflection ray
     * @param lightX the x-coordinate of the light
     * @param lightY the y-coordinate of the light
     * @return an array with [the x, y and side of the reflection ray] and
     * [the x, y, and side of the starting position]
     */
    public Vector3[] getReflectionRay(float lightX, float lightY) {
        Vector3 topleft = new Vector3(position.x - width / 2, position.y + height / 2, position.z);
        Vector3 topright = new Vector3(position.x + width / 2, position.y + height / 2, position.z);
        Vector3 bottomleft = new Vector3(position.x - width / 2, position.y - height / 2, position.z);
        Vector3 bottomright = new Vector3(position.x + width / 2, position.y - height / 2, position.z);

        Vector3[] result = new Vector3[2];
        if (type == 45) {
            // For 45 degrees mirror: 0 - bottom left, 1 - top left, 2 - top right, 3 - bottom right
            // Find the hypothenus and rotate it 90 degrees
            switch ((reflectionEdge + getOrientation()) % 4) {
                case 0:
                    float x6 = (bottomright.x - topleft.x);
                    float y6 = (bottomright.y - topleft.y);
                    result[0] = new Vector3(-y6, x6, position.z);
                    result[1] = new Vector3(position.x, position.y, position.z);
//                    result[1] = midpoint(bottomright, topleft);
                    //System.out.println(position.z);
                    return result;

                case 1:
                    float x7 = bottomleft.x - topright.x;
                    float y7 = bottomleft.y - topright.y;
                    result[0] = new Vector3(-y7, x7, position.z);
                    result[1] = new Vector3(position.x, position.y, position.z);
//                    result[1] = midpoint(bottomleft,topright);
                    return result;

                case 2:
                    float x8 = topleft.x - bottomright.x;
                    float y8 = topleft.y - bottomright.y;
                    result[0] =new Vector3(-y8, x8, position.z);
                    result[1] = new Vector3(position.x, position.y, position.z);
//                    result[1] = midpoint(topleft,bottomright);
                    return result;
                case 3:
                    float x9 = topright.x - bottomleft.x;
                    float y9 = topright.y - bottomleft.y;
                    result[0] = new Vector3(-y9, x9, position.z);
                    result[1] = new Vector3(position.x, position.y, position.z);
//                    result[1] = midpoint(bottomleft,topright);
                    return result;

                default:
                    return null;
            }
        } else if (type == 90) {
            switch ((reflectionEdge + getOrientation()) % 4) {
                case 0:
                    float x1 = topleft.x - bottomleft.x;
                    float y1 = topleft.y - bottomleft.y;
                    result[0] = new Vector3(-y1, x1, position.z);
                    result[1] = new Vector3(position.x, position.y, position.z);
//                    result[1] = midpoint(topleft,bottomleft);
                    return result;
                case 1:
                    float x2 = topright.x - topleft.x;
                    float y2 = topright.y - topleft.y;
                    result[0] = new Vector3(-y2, x2, position.z);
                    result[1] = new Vector3(position.x, position.y, position.z);
//                    result[1] = midpoint(topright,topleft);
                    return result;
                case 2:
                    float x3 = bottomright.x - topright.x;
                    float y3 = bottomright.y - topright.y ;
                    result[0]=new Vector3(-y3, x3, position.z);
                    result[1] = new Vector3(position.x, position.y, position.z);
//                    result[1] =midpoint(topright,bottomright);
                    return result;
                case 3:
                    float x4 =  bottomleft.x -  bottomright.x;
                    float y4 = bottomleft.y - bottomright.y ;
                    result[0] =  new Vector3(-y4, x4, position.z);
                    result[1] = new Vector3(position.x, position.y, position.z);
//                    result[1] = midpoint(bottomleft, bottomright);
                    return result;
                default:
                    return null;
            }
        } else {
            double thedaRadian = angleOfRotation * Math.PI / 180 + Math.PI;
            double x5 = Math.cos(thedaRadian);
            double y5 = Math.sin(thedaRadian);
            result[0] = new Vector3((float) x5, (float) y5, position.z);
            result[1] = new Vector3(position.x, position.y, position.z);
//            result[1] = new Vector3(lightX, lightY, getSide());
            return result;
        }

    }

    @Override
    public void render(Pixmap pm) {
        if (isVisible()) {
            if(type != 360)
                render(pm, position.x, position.y, (reflectionEdge + getOrientation() + (type == 45 ? 0 : 2)) % 4, width);
           else{
                renderRotated(pm, position.x, position.y, (float)(angleOfRotation*Math.PI/180),getWidth());
            }
        }
    }

    @Override
    public void render(Pixmap pm, float x, float y, int sourceOrientation, int destOrientation) {
        if (isVisible()) {
            if(type != 360)
                render(pm, x,y, (destOrientation - sourceOrientation + reflectionEdge + getOrientation() + (type == 45 ? 4 : 6)) % 4, width);
            else{
                renderRotated(pm, x, y, (float) (((destOrientation - sourceOrientation) * 90 + angleOfRotation) * Math.PI/180), getWidth());
            }
        }
    }

    public void saveFrame(String frameName) {
        super.saveFrame(frameName);

        HashMap<String, Object> frame = savedFrames.get(frameName);

        frame.put("isPickup", isPickup);
        frame.put("isRotated", isRotated);
        frame.put("sensorCount", ((SensorObstacle) getObstacle()).getSensorCount());

        frame.put("obsSensor", getObstacle().isSensor());
    }

    public void loadFrame(String frameName) {
        super.loadFrame(frameName);

        HashMap<String, Object> frame = savedFrames.get(frameName);

        isPickup = (boolean) frame.get("isPickup");
        isRotated = (boolean) frame.get("isRotated");

        getObstacle().setSensor((boolean) frame.get("obsSensor"));
        ((SensorObstacle) getObstacle()).setSensorCount((int) frame.get("sensorCount"));
    }
}
