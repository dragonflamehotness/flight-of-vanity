package com.genmiracle.flightofvanity.instance;

import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.genmiracle.flightofvanity.graphics.GameCanvas;
import com.genmiracle.flightofvanity.graphics.cube.Renderable;
import com.genmiracle.flightofvanity.graphics.cube.RenderableFilmStrip;
import com.genmiracle.flightofvanity.physics.PhysicsController;
import com.genmiracle.flightofvanity.physics.obstacle.Obstacle;

import java.util.HashMap;

public class Instance extends RenderableFilmStrip {
    private int side;
    /** The current side.*/
    private TextureRegion textureRegion;
    /** The texture for the object instance. */

    protected boolean isDanger;
    /** Whether this instance damages the player*/
    private Obstacle obstacle;
   /** The obstacle instances for physics. */
    private String name;
    /** The name of the instance. Either "mirror", "player", "enemy", or "platform"*/
//    private Controller controller;
    private Vector2 position;
    /** The position of the instance based on the obstacle. */

    private Vector2 positionCache;
    /** Object velocity vector */
    protected Vector2 velocity;
    /** Reference to texture origin */
    protected Vector2 origin;
    /** Radius of the object (used for collisions) */
    protected float radius;
    /** Whether or not the object should be removed at next timestep. */
    protected boolean destroyed;

    /** The orientation of this object, where 0 is default, and each increment represents a 90 degree rotation
     * clockwise */
    private int orientation;

    /** Whether this object is flipped along the y-axis */
    private boolean flipped;

    private float width;
    private float height;
    private boolean isVisible;
    private boolean isAnimated;

    protected HashMap<String, HashMap<String, Object>> savedFrames;

    private boolean isActive;
    /** Returns whether or not the instance object is active */
//    public Instance(){}

    /**
     * Creates an instance given the name, side, texture, instance of an object that is either enemy, player, or platform
     * @param textureRegion The texture for the object instance.
     * @param name The name of the instance. Either "mirror", "player", "enemy", or "platform"
     * @param isActive True if the instance object is active; otherwise false
     */
    public Instance(float x, float y, float width, float height, int side, int orientation, TextureRegion textureRegion, String name,
                    boolean isActive, boolean isVisible, boolean isAnimated) {
        this(x, y, width, height, side, orientation, textureRegion, 1, 1, 1, name,
                isActive, isVisible, isAnimated);
    }

    public Instance(float x, float y, float width, float height, int side, TextureRegion textureRegion,
                    int animRows, int animCols, int animSize, String name, boolean isActive, boolean isVisible,
                    boolean isAnimated) {
        this(x, y, width, height, side, 0, textureRegion, animRows, animCols, animSize, name, isActive, isVisible, isAnimated);
    }

    public Instance(float x, float y, float width, float height, int side, TextureRegion textureRegion,
                    int animRows, int animCols, int animSize, String name, String textureName, boolean isActive, boolean isVisible,
                    boolean isAnimated) {
        this(x, y, width, height, side, 0, textureRegion, animRows, animCols, animSize, name, textureName, isActive, isVisible, isAnimated);
    }

    public Instance(float x, float y, float width, float height, int side, int orientation, TextureRegion textureRegion,
                    int[] animRowYs, int[] animColXs, int animSize, String name, boolean isActive, boolean isVisible,
                    boolean isAnimated) {
        super("", textureRegion.getTexture(), animRowYs, animColXs, animSize);

        init(x, y, width, height, side, orientation, textureRegion, name, isActive, isVisible, isAnimated);
    }

    public Instance(float x, float y, float width, float height, int side, int orientation, TextureRegion textureRegion,
                    int animRows, int animCols, int animSize, String name, boolean isActive, boolean isVisible,
                    boolean isAnimated) {
        super("", textureRegion.getTexture(), animRows, animCols, animSize);

        init(x, y, width, height, side, orientation, textureRegion, name, isActive, isVisible, isAnimated);
    }
    public Instance(float x, float y, float width, float height, int side, int orientation, TextureRegion textureRegion,
                    int animRows, int animCols, int animSize, String name, String textureName, boolean isActive, boolean isVisible,
                    boolean isAnimated) {
        super(textureName, textureRegion.getTexture(), animRows, animCols, animSize);

        init(x, y, width, height, side, orientation, textureRegion, name, isActive, isVisible, isAnimated);
    }

    private void init(float x, float y, float width, float height, int side, int orientation, TextureRegion textureRegion,
                        String name, boolean isActive, boolean isVisible, boolean isAnimated) {
        position = new Vector2(x, y);
        this.width = width;
        this.height = height;
        this.side = side;
        this.orientation = orientation;
        this.textureRegion = textureRegion;
        this.name = name;
        this.isActive = isActive;
        this.isVisible = isVisible;
        this.isAnimated = isAnimated;
        origin = new Vector2(x + width / 2, y + height / 2);
        radius = width / 2;

        positionCache = new Vector2(position);
        savedFrames = new HashMap<>();
    }

    /**
     * Sets the side to be the given side
     * @param side side to be changed
     */
    public void setSide(int side){
        this.side = side;
    }

    /**
     * Returns whether the instance object is active
     * @return true if the instance object is active otherwise false
     */
    public boolean isActive(){
        return isActive;
    }

    /**
     * Sets whether the instance is active
     * @param isActive true if the instance is active, otherwise false
     */
    public void setActive(boolean isActive){
        this.isActive = isActive;
        if (obstacle != null) {
            obstacle.setActive(isActive);
        }
    }

    public boolean isVisible() {
        return isVisible;
    }

    public void setVisible(boolean isVisible) {
        this.isVisible = isVisible;
    }

    /**
     * Set the orientation of this object relative to its side, where 0 is default and each increment represents a
     * 90-degree rotation clockwise.
     *
     * @param orientation new orientation of this {@code Instance}
     */
    public void setOrientation(int orientation) {
        obstacle.setOrientation(orientation);
    }

    /**
     * Returns the orientation of this object relative to its side, where 0 is default and each increment represents a
     * 90-degree rotation clockwise.
     *
     * @return orientation of this {@code Instance}
     */
    public int getOrientation() {
        return obstacle.getOrientation();
    }

    /**
     * Returns the obstacle of the instance
     * @return obstacle
     */
    public Obstacle getObstacle(){
        return obstacle;
    }

    public void setObstacle(Obstacle obs) {
        obstacle = obs;
        obs.setOrientation(orientation);
    }

    /**
     * Returns the texture of the instance
     * @return texture
     */
    public TextureRegion getTextureRegion(){
        return textureRegion;
    }

    /**
     * Sets the x-coordinate of the instance's velocity
     * @param x the x-coordinate
     */
    public void setVX(float x){
        obstacle.setVX(x);
    }

    /**
     * Sets the y-coordinate of the instance's velocity
     * @param y the y-coordinate
     */
    public void setVY(float y){
        obstacle.setVY(y);
    }

    /**
     * Returns the current linear velocity of this object along the x-axis
     *
     * @return linear velocity along the x-axis
     */
    public float getVX() {
        return obstacle.getVX();
    }

    /**
     * Returns the current linear velocity of this object along the y-axis
     *
     * @return linear velocity along the y-axis
     */
    public float getVY() {
        return obstacle.getVY();
    }


    /**
     * Sets the position of the instance
     * @param x the x-coordinate of the instance
     * @param y the y-coordinate of the instance
     */
    public void setPosition(float x, float y){
        position.set(x, y);
        positionCache.set(x, y);
    }

    public Vector2 getPosition() {
        return positionCache;
    }

    /**
     * Returns the current side
     * @return side
     */
    public int getSide(){
        return side;
    }

    /**
     * Returns the name of the instance
     * @return name
     */
    public String getName(){
        return name;
    }

    /**
     * Returns the x-coordinate of the instance's position
     * @return x-coordinate of the instance's position
     */
    public float getX(){
        return position.x;
    }

    /**
     * Returns the y-coordinate of the instance's position
     * @return y-coordinate of the instance's position
     */
    public float getY(){
        return position.y;
    }

    public void saveFrame(String frameName) {
        HashMap<String, Object> frame = new HashMap<>();

        frame.put("position", position);
        frame.put("orientation", orientation);
        frame.put("isActive", isActive);
        frame.put("isVisible", isVisible);

        frame.put("obsPositionX", obstacle.getPosition().x);
        frame.put("obsPositionY", obstacle.getPosition().y);
        frame.put("obsOrientation", obstacle.getOrientation());
        frame.put("obsSide", obstacle.getSide());
        frame.put("obsAngle", obstacle.getAngle());
        frame.put("obsSideOrientation", obstacle.getSideOrientation());
        frame.put("obsLinearVelocityX", obstacle.getLinearVelocity().x);
        frame.put("obsLinearVelocityY", obstacle.getLinearVelocity().y);
        frame.put("obsGravity", new Vector2(obstacle.getGravity()));

        frame.put("renderableOpacity", getOpacity());

        savedFrames.put(frameName, frame);
    }

    public void loadFrame(String frameName) {
        getObstacle().setActive(false);

        HashMap<String, Object> frame = savedFrames.get(frameName);

        position = (Vector2) frame.get("position");
        orientation = (int) frame.get("orientation");
        isActive = (boolean) frame.get("isActive");
        isVisible = (boolean) frame.get("isVisible");
        side = (int) frame.get("obsSide");

        obstacle.setPosition((float) frame.get("obsPositionX"), (float) frame.get("obsPositionY"));
        obstacle.setSide((int) frame.get("obsSide"));
        obstacle.setAngle((float) frame.get("obsAngle"));
        obstacle.setSideOrientation((int) frame.get("obsSideOrientation"));
        obstacle.setLinearVelocity((float) frame.get("obsLinearVelocityX"), (float) frame.get("obsLinearVelocityY"));
        obstacle.setOrientation((int) frame.get("obsOrientation"));
        obstacle.setGravity((Vector2) frame.get("obsGravity"));

        setOpacity((float) frame.get("renderableOpacity"));

        getObstacle().setActive(true);
    }

    public void update(float delta) {
        if (isAnimated) {
            updateAnimation(delta);
        }
    }

    public void render(Pixmap pm) {
        if (isVisible) {
            render(pm, position.x, position.y, getOrientation(), width);
        }
    }

    public void render(Pixmap pm, float x, float y, int sourceOrientation, int destOrientation) {
        if (isVisible) {
            render(pm, x, y, (destOrientation - sourceOrientation + getOrientation() + 4) % 4, width);
        }
    }

    public void render(GameCanvas canvas, int orientation){
        canvas.begin();
//        if (textureRegion != null) {
            canvas.draw(textureRegion, Color.WHITE, 0, 0, obstacle.getX() + 150, obstacle.getY() + 150, -orientation * 90, 1, 1);
//        }
        canvas.end();
    }

    public boolean getIsDanger(){
        return isDanger;
    }

    public float getWidth(){
        return width;
    }
    public float getHeight(){
        return height;
    }

    public void setIsDanger(boolean b) {
        isDanger = b;
    }
}

