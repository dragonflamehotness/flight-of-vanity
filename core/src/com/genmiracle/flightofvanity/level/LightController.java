package com.genmiracle.flightofvanity.level;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.genmiracle.flightofvanity.graphics.GameCanvas;
import com.genmiracle.flightofvanity.instance.Instance;
import com.genmiracle.flightofvanity.instance.mechanisms.Keyhole;
import com.genmiracle.flightofvanity.instance.mechanisms.Mechanism;
import com.genmiracle.flightofvanity.instance.model.Enemy;
import com.genmiracle.flightofvanity.instance.model.Mirror;
import com.genmiracle.flightofvanity.instance.model.Player;
import com.genmiracle.flightofvanity.physics.PhysicsController;
import com.genmiracle.flightofvanity.physics.raycasting.Ray2D;
import com.genmiracle.flightofvanity.util.Utilities;

import java.util.ArrayList;

public class LightController {
    private Vector2 position;
    /** Light position. */
    private Vector2 direction;
    /** Light Direction. */

    private Pixmap texturepixmap;
    private Pixmap transparentPixmap;
    private int sideIndex;

    private ArrayList<Vector3> lightPositions;

    private ArrayList<ArrayList<Vector3>> lines;
    private ArrayList<Boolean> linePlatforms;
    private ArrayList<ArrayList<Vector3>> transparentLines;
    private ArrayList<Boolean> transparentLinePlatforms;

    private Vector2 v2temp;
    private Vector2 v2temp2;

    private boolean isSilhouette;


    /**
     * Create a Light at the given position, direction.
     *
     * @param x The initial x-coordinate of the light position
     * @param y The initial y-coordinate of the light position
     * @param dx The initial x-coordinate of the light direction
     * @param dy The initial y-coordinate of the light direction
     */
    public LightController(float x, float y, float dx, float dy, int sideIndex, Pixmap texturepixmap) {
        position = new Vector2();
        direction = new Vector2();
        position.x = x;
        position.y = y;
        direction.x = dx;
        direction.y = dy;
        lightPositions = new ArrayList<>();
        linePlatforms = new ArrayList<>();
        lines = new ArrayList<>();
        transparentLinePlatforms = new ArrayList<>();
        transparentLines = new ArrayList<>();
        this.sideIndex = sideIndex;
        this.texturepixmap = texturepixmap;

        v2temp = new Vector2();
        v2temp2 = new Vector2();

        transparentPixmap = Utilities.getPixmapWithTint(texturepixmap, 0.5f, 0.5f, 0.5f, 1);
    }
    /**
     * Returns the position of the light
     *
     * @return the position of the light
     */
    public Vector2 getPosition(){
        return position;
    }
    /**
     * Returns the direction of the light
     *
     * @return the direction of the light
     */
    public Vector2 getDirection(){
        return direction;
    }
    /**
     * Returns the x-coordinate of the light position
     *
     * @return the x-coordinate of the light position
     */
    public float getX(){
        return position.x;
    }
    /**
     * Returns the y-coordinate of the light position
     *
     * @return the y-coordinate of the light position
     */
    public float getY(){
        return position.y;
    }
    /**
     * Returns the x-coordinate of the light direction
     *
     * @return the x-coordinate of the light direction
     */
    public float getDX(){
        return direction.x;
    }
    /**
     * Returns the y-coordinate of the light direction
     *
     * @return the y-coordinate of the light direction
     */
    public float getDY(){
        return direction.y;
    }
    /**
     * Sets the x-coordinate of the light position
     *
     * @param x The x-coordinate of the light position
     */
    public void setX(float x){
        position.x = x;
    }
    /**
     * Sets the y-coordinate of the light position
     *
     * @param y The y-coordinate of the light position
     */
    public void setY(float y){
        position.y = y;
    }
    /**
     * Sets the x-coordinate of the light direction
     *
     * @param dx The x-coordinate of the light direction
     */
    public void setDX(float dx ){
        direction.x = dx;
    }
    /**
     * Sets the y-coordinate of the light direction
     *
     * @param dy The y-coordinate of the light direction
     */
    public void setDY(float dy){
        direction.y = dy;
    }

    public int getSide(){
        return sideIndex;
    }

    public void setSide(int sideIndex){
        this.sideIndex = sideIndex;
    }

    private boolean isEdgeSide(Vector2 pos){
        return pos.x == 0 || pos.x == 10 - 1 || pos.y == 0 || pos.y == 10 - 1;
    }

    /**
     * Casting the ray depending on what object it hits.
     * If it hits the mirror, it will handle the reflection.
     * If it hits the player, platform, or enemy, the r
     * If it hits nothing, the ray continues until it hits something.
     * @param pos the position of the ray
     * @param dir the direction of the ray
     * @param side the side of the ray
     */
    private void castRay(Vector2 pos, Vector2 dir, int side, int numReflection){
        Ray2D ray = new Ray2D(pos, dir, side);
        PhysicsController.Hitinfo hitinfo = PhysicsController.getInstance().raycast(ray, numReflection, isSilhouette);
        Instance obj = hitinfo.getHit();

        //hit nothing


        //hit player, platform, enemy
        if ( numReflection <= 0 || hitinfo.hasHitPlatform() || obj instanceof Enemy || obj instanceof Player || obj instanceof Mechanism) {
            for (ArrayList<Vector3> light: hitinfo.getLightPositions()){
                if (isSilhouette) {
                    transparentLines.add(obj instanceof Player ? (ArrayList<Vector3>) light.clone() : light);
                    transparentLinePlatforms.add(hitinfo.hasHitPlatform());
                } else {
                    lines.add(obj instanceof Player ? (ArrayList<Vector3>) light.clone() : light);
                    linePlatforms.add(hitinfo.hasHitPlatform());
                }
            }

            if (obj instanceof Keyhole && !isSilhouette) {
                Keyhole k = (Keyhole) obj;

                k.activate();
            }

            if (obj instanceof Player) {
                v2temp.set(hitinfo.getX(), hitinfo.getY());

                isSilhouette = true;
                castRay(v2temp, hitinfo.getLastRay().getDir(), obj.getSide(), numReflection - 1);
            }
        }
        //hit mirror
        else if (obj instanceof Mirror) {
            Mirror m = (Mirror) obj;
            for(ArrayList<Vector3> light: hitinfo.getLightPositions()){
                if (isSilhouette) {
                    transparentLines.add((ArrayList<Vector3>) light.clone());
                    transparentLinePlatforms.add(false);
                } else {
                    lines.add((ArrayList<Vector3>) light.clone());
                    linePlatforms.add(false);
                }
            }

            isSilhouette = isSilhouette || obj.getObstacle().isSensor();

            Vector3[] normalInfo = m.getReflectionRay(position.x, position.y);
            castRay( new Vector2(normalInfo[1].x, normalInfo[1].y), new Vector2(normalInfo[0].x, normalInfo[0].y), m.getSide(), numReflection-1);
        }
//        //hit side edge
        else {
//            lightPositions.add(new Vector3(pos.add(dir),side));
//            castRay(pos.add(dir),dir,hitinfo.getSide(),numReflection-1);
            for(ArrayList<Vector3> light: hitinfo.getLightPositions()){
                if (isSilhouette) {
                    transparentLines.add(light);
                    transparentLinePlatforms.add(false);
                } else {
                    lines.add(light);
                    linePlatforms.add(false);
                }
            }
        }
    }

    public void update(float dt) {
        reset();
        castRay(position, direction, sideIndex, 20);
    }


    private Vector3 physicsToScreenPos(Vector3 v, float scale){
        return new Vector3((int) Math.round(scale * (v.x + PhysicsController.SIZE / 2)),
                (int) Math.round(scale * (PhysicsController.SIZE / 2 - v.y)), v.z);
    }

    public static Pixmap rotatePixmap(Pixmap pixmap, float degrees) {
        int width = pixmap.getWidth();
        int height = pixmap.getHeight();

        Pixmap rotatedPixmap = new Pixmap(width, height, pixmap.getFormat());

        // center of rotation is at the middle of the pixmap
        float centerX = width / 2f;
        float centerY = height / 2f;

        // convert degrees to radians
        float radians = degrees * MathUtils.degreesToRadians;

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                // translate pixel to origin
                float originX = x - centerX;
                float originY = y - centerY;

                // apply rotation
                float rotatedX = originX * MathUtils.cos(radians) - originY * MathUtils.sin(radians);
                float rotatedY = originX * MathUtils.sin(radians) + originY * MathUtils.cos(radians);

                // translate back to original position
                int newX = MathUtils.round(rotatedX + centerX);
                int newY = MathUtils.round(rotatedY + centerY);

                // check bounds
                if (newX >= 0 && newX < width && newY >= 0 && newY < height) {
                    int color =  pixmap.getPixel(newX, newY);
                    rotatedPixmap.drawPixel(x, y,color);
                }
            }
        }

        return rotatedPixmap;
    }
    private static void rotatePixmap(Pixmap dest, Pixmap source, int dx, int dy, float angle, boolean blending) {
        int srcWidth = source.getWidth();
        int srcHeight = source.getHeight();
        int destWidth = dest.getWidth();
        int destHeight = dest.getHeight();

        // calculate the center of the source pixmap
        int cx = srcWidth / 2;
        int cy = srcHeight / 2;

        // calculate the sine and cosine of the rotation angle
        float cos = MathUtils.cos(angle);
        float sin = MathUtils.sin(angle);

        // iterate over every pixel in the destination pixmap
        for (int y = 0; y < destHeight; y++) {
            for (int x = 0; x < destWidth; x++) {
                float delx = (x - dx);
                float dely = (y - dy);
                // calculate the position of the current pixel in the source pixmap
                int srcX = (int) (cos * delx - sin * dely + cx*1/3);
                int srcY = (int) (sin * delx + cos * dely + cy*2);

                // check if the current pixel is inside the source pixmap
                if (srcX >= 0 && srcX < srcWidth && srcY >= 0 && srcY < srcHeight) {

                    // set the color of the current pixel in the destination pixmap
                    int color = source.getPixel(srcX, srcY);
                    dest.drawPixel(x, y, color);

                }
            }
        }
    }




    /**
     * Renders the light ray given the side
     */
    public void render(Pixmap[] pms, float scale){
        int size = transparentLines.size();
        for (int l = 0; l < transparentLines.size(); l++) {
            ArrayList<Vector3> line = transparentLines.get(l);

            for (int i = 0; i < line.size() - 1; i++) {
                Vector3 start = line.get(i);
                Vector3 end = line.get(i + 1);
                Vector3 nStart = physicsToScreenPos(start, scale);
                Vector3 nEnd = physicsToScreenPos(end, scale);

                int j = Math.round(start.z);
                if (pms[j] != null) {

                    // Calculate the angle and length of the line segment
                    float angle = MathUtils.atan2(nEnd.y - nStart.y, nEnd.x - nStart.x);
                    float length = nStart.dst(nEnd);
//                        System.out.println("angle: "+ angle);

                    //in degree
                    float rotationAngle = MathUtils.radiansToDegrees * (angle+MathUtils.PI/2f);

                    Pixmap rotated;
                    //no rotate if light texture is vertical
                    if (Math.round((MathUtils.radiansToDegrees*angle)) == 90){
                        rotated = transparentPixmap;
                    } else {
                        rotated = Utilities.rotatePixmap(transparentPixmap, -angle - (float) (Math.PI / 2f));
                    }

                    // Draw the texture region repeatedly along the length of the line segment
                    for (float k = l > 0 ? -1 : transparentPixmap.getHeight() - 1; k < length + (transparentLinePlatforms.get(l) && l >= size - 1 ? 0 : transparentPixmap.getHeight()); k += transparentPixmap.getHeight() - 1) {
                        float x = nStart.x + k * MathUtils.cos(angle) - rotated.getWidth() / 2f;
                        float y = nStart.y + k * MathUtils.sin(angle) - rotated.getHeight() / 2f;
                        if (Math.abs(Math.round(MathUtils.radiansToDegrees * angle)) == 45){
//                                Utilities.drawPixmapRotated(pms[j],texturepixmap,(int)(x  - rotated.getWidth() / 2f),
//                                        (int)(y - rotated.getHeight() / 2f),pms[j].getWidth(),pms[j].getHeight(),
//                                        pms[j].getWidth()/2,pms[j].getHeight()/2,rotated.getWidth(),
//                                        rotated.getHeight(),angle, false );
//                            rotatePixmap(pms[j], texturepixmap,(int) x, (int) y, angle, true);

                            pms[j].drawPixmap(rotated, (int) x, (int) y);

//                                Utilities.drawPixmapCentered(pms[j], rotated, (int) nStart.x, (int) nStart.y,
//                                        rotated.getWidth(), rotated.getHeight(), 0, 0,
//                                        rotated.getWidth(), rotated.getHeight());
                        } else {
                            pms[j].drawPixmap(rotated, (int) x, (int) y);
                        }

                    }

                }
            }
        }
//        for(int j = 0; j< pms.length; j++) {
        //System.out.println(lines.size());
        size = lines.size();

            for (int l = 0; l < lines.size(); l++) {
                ArrayList<Vector3> line = lines.get(l);

                for (int i = 0; i < line.size() - 1; i++) {
                    Vector3 start = line.get(i);
                    Vector3 end = line.get(i + 1);
                    Vector3 nStart = physicsToScreenPos(start, scale);
                    Vector3 nEnd = physicsToScreenPos(end, scale);

                    int j = Math.round(start.z);
                    if (pms[j] != null) {

                        // Calculate the angle and length of the line segment
                        float angle = MathUtils.atan2(nEnd.y - nStart.y, nEnd.x - nStart.x);
                        float length = nStart.dst(nEnd);
//                        System.out.println("angle: "+ angle);

                        //in degree
                        float rotationAngle = MathUtils.radiansToDegrees * (angle+MathUtils.PI/2f);

                        Pixmap rotated;
                        //no rotate if light texture is vertical
                        if (Math.round((MathUtils.radiansToDegrees*angle)) == 90){
                            rotated = texturepixmap;
                        } else {
                            rotated = Utilities.rotatePixmap(texturepixmap, -angle - (float) (Math.PI / 2f));
                        }

                        // Draw the texture region repeatedly along the length of the line segment
                        for (float k = -texturepixmap.getHeight(); k < length + (linePlatforms.get(l) && l >= size - 1 ? 0 : texturepixmap.getHeight()); k += texturepixmap.getHeight() - 1) {
                            float x = nStart.x + k * MathUtils.cos(angle) - rotated.getWidth() / 2f;
                            float y = nStart.y + k * MathUtils.sin(angle) - rotated.getHeight() / 2f;
                            if (Math.abs(Math.round(MathUtils.radiansToDegrees * angle)) == 45){
//                                Utilities.drawPixmapRotated(pms[j],texturepixmap,(int)(x  - rotated.getWidth() / 2f),
//                                        (int)(y - rotated.getHeight() / 2f),pms[j].getWidth(),pms[j].getHeight(),
//                                        pms[j].getWidth()/2,pms[j].getHeight()/2,rotated.getWidth(),
//                                        rotated.getHeight(),angle, false );
//                            rotatePixmap(pms[j], texturepixmap,(int) x, (int) y, angle, true);

                                 pms[j].drawPixmap(rotated, (int) x, (int) y);

//                                Utilities.drawPixmapCentered(pms[j], rotated, (int) nStart.x, (int) nStart.y,
//                                        rotated.getWidth(), rotated.getHeight(), 0, 0,
//                                        rotated.getWidth(), rotated.getHeight());
                            } else {
                                pms[j].drawPixmap(rotated, (int) x, (int) y);
                            }

                        }

                    }
                }
            }
//        }

        //        for(int j = 0; j< pms.length; j++) {
        //System.out.println(lines.size());

//        }
    }

    public void reset(){
        isSilhouette = false;
        lines.clear();
        linePlatforms.clear();
        transparentLines.clear();
        transparentLinePlatforms.clear();
    }
}
