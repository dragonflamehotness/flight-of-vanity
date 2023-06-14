package com.genmiracle.flightofvanity.level;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.utils.Json;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Iterator;

import com.badlogic.gdx.*;
import com.badlogic.gdx.audio.*;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.assets.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.graphics.g2d.freetype.*;
import com.genmiracle.flightofvanity.InputController;
import com.genmiracle.flightofvanity.assets.AssetDirectory;
import com.genmiracle.flightofvanity.audio.AudioController;
import com.genmiracle.flightofvanity.graphics.GameCanvas;
import com.genmiracle.flightofvanity.graphics.cube.CubeController;
import com.genmiracle.flightofvanity.graphics.cube.RenderableFilmStrip;
import com.genmiracle.flightofvanity.instance.Instance;
import com.genmiracle.flightofvanity.instance.InstanceController;
import com.genmiracle.flightofvanity.instance.mechanisms.ExitDoor;
import com.genmiracle.flightofvanity.instance.mechanisms.Keyhole;
import com.genmiracle.flightofvanity.instance.model.Enemy;
//import com.genmiracle.flightofvanity.instance.model.EnemyController;
import com.genmiracle.flightofvanity.instance.model.EnemyController;
import com.genmiracle.flightofvanity.instance.model.Player;
import com.genmiracle.flightofvanity.physics.PhysicsController;
import com.genmiracle.flightofvanity.util.ScreenListener;
import com.genmiracle.flightofvanity.instance.PlayerController;
//import jdk.jfr.internal.PlatformRecorder;

public class WorldController {
    protected BitmapFont displayFont;
    /** The texture for background and platforms */
    protected TextureRegion platformTile;
    protected TextureRegion background;
    // protected Texture sideTextureOutput[6];
    Texture[] sideTextureOutput = new Texture[6];
    protected TextureRegion sideTextureRegion;
    TextureRegion[] sideTextureRegionOutput = new TextureRegion[6];

    /** Exit code for quitting the game */
    public static final int EXIT_QUIT = 0;
    /** Exit code for advancing to next level */
    public static final int EXIT_NEXT = 1;
    /** Exit code for jumping back to previous level */
    public static final int EXIT_PREV = 2;
    /** How many frames after winning/losing do we continue? */
    public static final int EXIT_COUNT = 120;

    /** Width of the game world in Box2d units */
    protected static final float DEFAULT_WIDTH = 32.0f;
    /** Height of the game world in Box2d units */
    protected static final float DEFAULT_HEIGHT = 18.0f;
    /** The default value of gravity (going down) */
    protected static final float DEFAULT_GRAVITY = -4.9f;

    /** The length of a single side of the cube (in pixels) */
    public static final int SIDE_LENGTH = 960;

    /** Reference to the game canvas */
    protected GameCanvas canvas;
    // /** All the objects in the world. */
    // protected PooledList<Obstacle> objects = new PooledList<Obstacle>();
    // /** Queue for adding objects */
    // protected PooledList<Obstacle> addQueue = new PooledList<Obstacle>();
    // /** Listener that will update the player mode when we are done */
    private ScreenListener listener;

    /** The Box2D world */
    protected World currentWorld;
    /** The boundary of the world */ // might need it later
    protected Rectangle bounds;
    /** The world scale */ // might need it later
    protected Vector2 scale;

    /** Whether or not this is an active controller */
    private boolean active;
    /** Whether we have completed this level */ // might need it later
    private boolean complete;
    /** Whether we have failed at this world (and need a reset) */ // might need it later
    private boolean failed;
    /** Whether or not debug mode is active */ // might need it later
    private boolean debug;
    /** Countdown active for winning or losing */ // might need it later
    private int countdown;
    private boolean drew;

    /**
     * Current cube layout:
     * 4
     * 0 1 2 3
     * 5
     *
     * The current side the player is on.
     */
    private int currentSide;

    /**
     * The current orientation the current side's up direction is facing.
     * 0 = up, 1 = right, 2 = down, 3 = left
     */
    private int currentOrientation;

    // Physics objects for the game
    /** Physics constants for initialization */
    private JsonValue constants;
    // CONTROLLERS

    private InstanceController ic;
    private LightController lc;
    private PlayerController pc;
    private AudioController ac;
    private EnemyController ec;

    private Player p;

    protected TextureRegion player;
    protected TextureRegion enemy;
    protected TextureRegion[] mirrors;
    protected AssetDirectory internal;
    ArrayList<ArrayList<Float>> testing;
    private ExitDoor door;
    private boolean playedUnlock;

    public void init() {
        Texture lightT = internal.getEntry("light",Texture.class);
        lightT.getTextureData().prepare();
        Pixmap lightp = lightT.getTextureData().consumePixmap();
        ic = new InstanceController();
        pc =  new PlayerController(InputController.getInstance());
        lc = new LightController(0,0,1,1,0, lightp);
    }

    /**
     * Loads the world stored within the given json.
     *
     */
    public void loadWorld(JsonValue base) {
        long time = System.currentTimeMillis();

        if (internal == null) {
            Gdx.app.error("WorldController", "Internal directory not loaded", new NullPointerException());
            return;
        }

        if (currentWorld != null) {
            currentWorld.dispose();
        }

        currentWorld = new World();
        currentWorld.setAssetDirectory(internal);
//        internal = new AssetDirectory("assets.json");
//        internal.loadAssets();
//        internal.finishLoading();

        ic.clear();

        ec = new EnemyController();

        JsonValue sideComponents = base.get("sides");
        player = new TextureRegion(internal.getEntry("vanity", Texture.class));
        enemy = new TextureRegion(internal.getEntry("ghost", Texture.class));
        ic.setPlayerTexture(player);
        pc.setInstanceController(ic);
        ac.setPlayerController(pc);

        System.out.println(System.currentTimeMillis() - time);
//        ic.createEnemy(10,10,0,true,enemy);
        for (int i = 0; i < sideComponents.size; i++) {
            for (JsonValue j : sideComponents.get(i).get("instances")) {
                float x = j.getFloat("x");
                float y = j.getFloat("y");
                String type = j.getString("type");
                String texture = j.getString("texture");

                if (type.equals("mirror")) {
                    int mtype = j.get("properties").getInt("type");
                    Boolean movable = j.get("properties").getBoolean("movable");
                    Boolean rotatable = j.get("properties").getBoolean("rotatable");
                    int orientation = j.get("properties").getInt("orientation");
                    int reflectAngle = j.get("properties").getInt("reflectionAngle");
                    String mirrorAsset = "";

                    if (mtype == 45) {
                        mirrorAsset = "mirror45";
                    } else if (mtype == 90) {
                        mirrorAsset = "mirror90";
                    } else {
                        mirrorAsset = "mirror360";
                    }
                    TextureRegion tr = new TextureRegion(internal.getEntry(mirrorAsset, Texture.class));
                    if (mtype == 45 || mtype == 90) {
                        Instance mirror = ic.createNonRotableMirror(mtype, reflectAngle, i, x, y, true, true, movable, tr);
                        ic.addInstance(mirror);
                    } else{
                        Instance mirror = ic.createRotableMirror(0, i, x, y, movable, true,tr);
                        ic.addInstance(mirror);
                    }
                    System.out.println("Mirror: " + (System.currentTimeMillis() - time));
                }
                else if (type.equals("vanity")) {
                    p = ic.createPlayer(x, y, i, true, true);
                    System.out.println("Vanity: " + (System.currentTimeMillis() - time));
                }
                else if(type.equals("light")){
                    int orientation = j.get("properties").getInt("orientation");
                    lc.setX(x);
                    lc.setY(y);
                    if(orientation == 0){
                        lc.setDX(0f);
                        lc.setDY(1f);
                    }
                    else if(orientation == 1){
                        lc.setDX(1f);
                        lc.setDY(0f);
                    }
                    else if(orientation == 2){
                        lc.setDX(0f);
                        lc.setDY(-1f);
                    }
                    else{
                        lc.setDX(-1f);
                        lc.setDY(0f);
                    }
                    lc.setSide(i);
                    TextureRegion tr = new TextureRegion(internal.getEntry("lightsource",Texture.class));
                    Instance lightSource = ic.createLightSource(x,y,8f,8f, orientation, tr,i,type,true,true,false);
                    ic.addInstance(lightSource);
                    System.out.println("Light: " + (System.currentTimeMillis() - time));
                }
                else if(type.equals("spike")){
                    int orientation = j.get("properties").getInt("orientation");
                    TextureRegion tr = new TextureRegion(internal.getEntry("spike",Texture.class));
                    Instance spike = ic.createSpike(x, y, 5f, 5f, orientation, tr, i, type, true, true, false);

//                    Instance spike = ic.createSpike(x, y, i, true, tr);
                    ic.addInstance(spike);
                    spike.getObstacle().setBodyType(BodyDef.BodyType.StaticBody);
                    System.out.println("Spike: " + (System.currentTimeMillis() - time));
                }
                else if(type.equals("enemy")){
                    TextureRegion tr = new TextureRegion(internal.getEntry("ghost", Texture.class));
                    Instance ghost = ic.createEnemy(x, y, i, true, tr);
                    ic.addInstance(ghost);
                    ghost.getObstacle().setBodyType(BodyDef.BodyType.DynamicBody);

                    ec.addEnemy((Enemy) ghost);
//                    ghost.addAnimation(type, 0, 13, RenderableFilmStrip.TYPE.LOOPING);
//                    ghost.setAnimation(type);
//                    ghost.setFrameRate(3f);
                    System.out.println("Enemy: " + (System.currentTimeMillis() - time));
                }
                else if(type.equals("hint")){
                    TextureRegion tr = new TextureRegion(internal.getEntry(texture, Texture.class));
                    Instance hint = ic.createInstance(x, y, 5f, 5f, 0, tr, i, type, true, true, false);
//                    ic.getPhysicsController().removeObstacle(hint.getObstacle());
                    ic.addInstance(hint);
                    hint.getObstacle().setBodyType(BodyDef.BodyType.StaticBody);
//                    hint.getObstacle().setSensor(true);
//                    hint.setVisible(true);
                    System.out.println("Hint: " + (System.currentTimeMillis() - time));
                }
                else if(type.equals("buttons")){
                    TextureRegion tr = new TextureRegion(internal.getEntry(texture, Texture.class));
                    Instance button = null;

                    if(texture.equals("left")) {
                        button = ic.createAnimateInstance(x, y, 5f, 6.363636364f, i, tr, 1,
                                14, 14, type, true, true, true);
                    }
                    else if(texture.equals("right")){
                        button = ic.createAnimateInstance(x, y, 5f, 6.363636364f, i, tr, 1,
                                14, 14, type, true, true, true);
                    }
                    else if(texture.equals("tab")){
                        button = ic.createAnimateInstance(x, y, 15.9090909f, 6.363636364f, i, tr,
                                1, 14, 14, type, true, true, true);
                    }
                    else if(texture.equals("e")){
                        button = ic.createAnimateInstance(x, y, 5f, 6.363636364f, i, tr, 1,
                                14, 14, type, true, true, true);
                    }
                    else if(texture.equals("q")){
                        button = ic.createAnimateInstance(x, y, 8.63636364f, 6.363636364f, i, tr, 1,
                                14, 14, type, true, true, true);
                    }
                    else if(texture.equals("space")){
                        button = ic.createAnimateInstance(x, y, 15.9090909f, 6.363636364f, i, tr, 1,
                                14, 14, type, true, true, true);
                    }
                    else if(texture.equals("r")){
                        button = ic.createAnimateInstance(x, y, 5f, 6.363636364f, i, tr, 1,
                                14, 14, type, true, true, true);
                    }
                    else if(texture.equals("warning")){
                        button = ic.createAnimateInstance(x, y, 3.63636364f, 6.363636364f, i, tr, 1,
                                14, 14, type, true, true, true);
                    }

                    ic.addInstance(button);
                    button.getObstacle().setBodyType(BodyDef.BodyType.StaticBody);
                    button.addAnimation(type, 0, 13, RenderableFilmStrip.TYPE.LOOPING);
                    button.setAnimation(type);
                    button.setFrameRate(3f);
                    System.out.println("Button: " + (System.currentTimeMillis() - time));
                }
            }
        }
        System.out.println(System.currentTimeMillis() - time);

        JsonValue objectives = base.get("objectives");
        JsonValue keyhole = objectives.get("keyhole");

        float x = keyhole.getFloat("x");
        float y = keyhole.getFloat("y");
        int side = keyhole.getInt("side");
        int orientation = keyhole.getInt("orientation");

        TextureRegion tr = new TextureRegion(internal.getEntry("keyhole", Texture.class));
        Keyhole kh = new Keyhole(x, y, side, orientation, tr,true, true);

        ic.createInstance(kh, true);

        JsonValue exitdoor = objectives.get("exit_door");

        x = exitdoor.getFloat("x");
        y = exitdoor.getFloat("y");
        side = exitdoor.getInt("side");
        orientation = exitdoor.getInt("orientation");

        tr = new TextureRegion(internal.getEntry("door", Texture.class));
        ExitDoor door = new ExitDoor(x, y, side, orientation, tr, true, true);

        ic.createInstance(door, true);

        this.door = door;

        kh.addConnected(door);
        System.out.println(System.currentTimeMillis() - time);

        pc.setPlayer(p);
        currentWorld.setSides(base, SIDE_LENGTH, SIDE_LENGTH);
        System.out.println(System.currentTimeMillis() - time);


        for (int i = 0; i < 6; i++) {
            Texture backgroundTexture = internal.getEntry("side" + Integer.toString(i) + "Background", Texture.class);
            currentWorld.setBackgroundTextureOfSide(i, backgroundTexture);
        }

        ic.getPhysicsController().combinePlatforms();
        System.out.println(System.currentTimeMillis() - time);
    }

    public InstanceController getInstanceController() {
        ic.setPlayerTexture(player);
        return ic;

    }

    public ExitDoor getDoor(){
        return door;
    }

    public void setAssetDirectory(AssetDirectory dir) {
        internal = dir;
    }

    /**
     * Returns true if the level is completed.
     *
     * If true, the level will advance after a countdown
     *
     * @return true if the level is completed.
     */
    public boolean isComplete() {
        return complete;
    }

    /**
     * Sets whether the level is completed.
     *
     * If true, the level will advance after a countdown
     *
     * @param value whether the level is completed.
     */
    public void setComplete(boolean value) {
        if (value) {
            countdown = EXIT_COUNT;
        }
        complete = value;
    }

    /**
     * Returns true if the level is failed.
     *
     * If true, the level will reset after a countdown
     *
     * @return true if the level is failed.
     */
    public boolean isFailure() {
        return failed;
    }

    /**
     * Sets whether the level is failed.
     *
     * If true, the level will reset after a countdown
     *
     * @param value whether the level is failed.
     */
    public void setFailure(boolean value) {
        if (value) {
            countdown = EXIT_COUNT;
        }
        failed = value;
    }

    /**
     * Returns true if this is the active screen
     *
     * @return true if this is the active screen
     */
    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * Returns the canvas associated with this controller
     *
     * The canvas is shared across all controllers
     *
     * @return the canvas associated with this controller
     */
    public GameCanvas getCanvas() {
        return canvas;
    }

    /**
     * Sets the canvas associated with this controller
     *
     * The canvas is shared across all controllers. Setting this value will compute
     * the drawing scale from the canvas size.
     *
     * @param canvas the canvas associated with this controller
     */
    public void setCanvas(GameCanvas canvas) {
        this.canvas = canvas;
        this.scale.x = canvas.getWidth() / bounds.getWidth();
        this.scale.y = canvas.getHeight() / bounds.getHeight();
    }

    public void setCurrentOrientation(int currentOrientation) {
        this.currentOrientation = currentOrientation;
    }

    public int getCurrentOrientation() {
        return currentOrientation;
    }

    public void setCurrentSide(int currentSide) {
        // current side is where-ever the player position is
        // currentSide =
        this.currentSide = currentSide;
    }

    public int getCurrentSide() {
        return currentSide;
    }

    /**
     * Creates a new game world with the default values.
     *
     * The game world is scaled so that the screen coordinates do not agree
     * with the Box2d coordinates. The bounds are in terms of the Box2d
     * world, not the screen.
     */
    public WorldController() {
        this(new Rectangle(0, 0, DEFAULT_WIDTH, DEFAULT_HEIGHT),
                new Vector2(0, DEFAULT_GRAVITY));
    }

    /**
     * Creates a new game world
     *
     * The game world is scaled so that the screen coordinates do not agree
     * with the Box2d coordinates. The bounds are in terms of the Box2d
     * world, not the screen.
     *
     * @param width   The width in Box2d coordinates
     * @param height  The height in Box2d coordinates
     * @param gravity The downward gravity
     */
    public WorldController(float width, float height, float gravity) {
        this(new Rectangle(0, 0, width, height), new Vector2(0, gravity));
    }

    /**
     * Creates a new game world
     *
     * The game world is scaled so that the screen coordinates do not agree
     * with the Box2d coordinates. The bounds are in terms of the Box2d
     * world, not the screen.
     *
     * @param bounds  The game bounds in Box2d coordinates
     * @param gravity The gravitational force on this Box2d world
     */
    public WorldController(Rectangle bounds, Vector2 gravity) {
        // world = new World(gravity, false);
        this.bounds = new Rectangle(bounds);
        this.scale = new Vector2(1, 1);
        complete = false;
        failed = false;
        debug = false;
        active = false;
        countdown = -1;

        // Player p = ic.createPlayer(0,0,0,true);

    }
    // public TextureRegion[] goToGDXRoot(){
    // sideTextureOutput = currentWorld.sideTexture(player);
    // for (int i = 0 ; i < sideTextureOutput.length; i++){
    // sideTextureRegion = new TextureRegion(sideTextureOutput[i], 20, 20, 50, 50);
    // sideTextureRegionOutput[i] = sideTextureRegion;
    // }
    // return sideTextureRegionOutput;
    // }

    public PlayerController getPlayerController(){
        return pc;
    }
    public Texture getTextureAtSide(int i) {
        return currentWorld.getTextureOfSide(i);
    }

    public Pixmap getPixmapAtSide(int i) {
        return currentWorld.getPixmapOfSide(i);
    }

    public World getCurrentWorld() {
        return currentWorld;
    }

    public LightController getLightController() {
        return lc;
    }

    // public void setLightController(LightController lc){this.lc =lc;}
    /**
     * Called to set the AudioController, as it is initialized in GDXRoot.
     *
     * @param a The AudioController passed to world
     */
    public void setAudioController(AudioController a){
        ac = a;
    }
    /**
     * Called when the screen should update itself.
     *
     * @param delta The time in seconds since the last render.
     */

    // @Override
    public void update(float delta) {

        if (active) {
            if(door.isActivated() && !playedUnlock) {
                ac.playSound("unlock");
                playedUnlock = true;
            }
            ic.update(delta);
            lc.update(delta);
            pc.update(delta);
            ec.update(delta);
            ac.update(delta);

        }
    }

    /**
     * @param width
     * @param height
     * @see ApplicationListener#resize(int, int)
     */
    // @Override
    public void resize(int width, int height) {
        // IGNORE FOR NOW
    }

    /**
     * @see ApplicationListener#pause()
     */
    // @Override
    public void pause() {
        // TODO Auto-generated method stub
    }

    /**
     * @see ApplicationListener#resume()
     */
    // @Override
    public void resume() {
        // TODO Auto-generated method stub
    }

    /**
     * Called when this screen is no longer the current screen for a {@link Game}.
     */
    // @Override
    public void hide() {
        active = false;

    }

    /**
     * Called when this screen should release all resources.
     */
    public void dispose() {
        if (currentWorld != null) {
            currentWorld.dispose();
        }
        bounds = null;
        scale = null;
        currentWorld = null;
        canvas = null;
    }

    public void setPlayedUnlock(boolean b) {
        playedUnlock = b;
    }
}
