package com.genmiracle.flightofvanity.instance;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.genmiracle.flightofvanity.InputController;
import com.genmiracle.flightofvanity.assets.AssetDirectory;
import com.genmiracle.flightofvanity.graphics.GameCanvas;
import com.genmiracle.flightofvanity.instance.mechanisms.ExitDoor;
import com.genmiracle.flightofvanity.instance.model.Enemy;
//import com.genmiracle.flightofvanity.instance.model.EnemyController;
import com.genmiracle.flightofvanity.instance.model.Mirror;
import com.genmiracle.flightofvanity.instance.model.Player;
import com.genmiracle.flightofvanity.physics.PhysicsController;
import com.genmiracle.flightofvanity.physics.PhysicsSide;
import com.genmiracle.flightofvanity.physics.obstacle.BoxObstacle;
import com.genmiracle.flightofvanity.physics.obstacle.Obstacle;
import com.genmiracle.flightofvanity.physics.obstacle.PlayerObstacle;
import com.genmiracle.flightofvanity.physics.obstacle.SensorObstacle;

import javax.swing.*;
import java.util.ArrayList;
import java.util.HashMap;

public class InstanceController {
    /** List of each instance that should be active */
    private ArrayList<Instance> instances;
    /** List of each mirror that should be active */
    private ArrayList<Mirror> mirrors;

    /** The current PhysicsController instance */
    private PhysicsController physController;
//    private PlayerController playController;

//    public EnemyController enemyController;
    private Player player;
//    public EnemyController enemyController;

    private Mirror mirror;

    private ExitDoor door;
    private TextureRegion texturePlayer;

    private String lastFrame;

    public InstanceController() {
        this.instances = new ArrayList<>();
        this.mirrors = new ArrayList<>();
        physController = PhysicsController.getInstance();
//        playController = new PlayerController(InputController.getInstance());
    }

    public void clear() {
        dispose();

        physController.clear();

        instances.clear();
        mirrors.clear();

        player = null;
    }

    public PhysicsController getPhysicsController(){
        return physController;
    }

    public Instance createInstance(float x, float y, float width, float height, int orientation, TextureRegion textureRegion, int side,
            String name, boolean isActive, boolean isVisible, boolean isAnimated) {
        Instance instance = new Instance(x, y, width, height, side, orientation, textureRegion, name, isActive, isVisible, isAnimated);
        if (isActive) {
            instances.add(instance);

            Obstacle obs = new BoxObstacle(x, y, width, height);
            instance.setObstacle(obs);

            physController.addInstance(instance, side, x, y);

            if(instance instanceof ExitDoor)
                door = (ExitDoor) instance;
        }
        return instance;
    }
    // get adjusted x, get adjusted y, get side in obstacle

    public Instance createAnimateInstance(float x, float y, float width, float height, int side, TextureRegion textureRegion,
                                          int animRows, int animCols, int animSize, String name, boolean isActive, boolean isVisible,
                                          boolean isAnimated) {
        Instance instance = new Instance(x, y, width, height, side, 0, textureRegion, animRows, animCols, animSize, name, isActive, isVisible, isAnimated);

        if (isActive) {
            instances.add(instance);

            Obstacle obs = new BoxObstacle(x, y, width, height);
            instance.setObstacle(obs);

            physController.addInstance(instance, side, x, y);

            if(instance instanceof ExitDoor)
                door = (ExitDoor) instance;
        }
        return instance;
    }

    public Player createPlayer(float x, float y, int side, boolean isActive, boolean isVisible) {
        if (isActive) {
            PlayerObstacle obs = new PlayerObstacle(x, y, 4.75f, 9.5f);

            player = new Player(x, y, side,0, 0, texturePlayer, isActive);

            player.setObstacle(obs);
            instances.add(player);

            obs.setMass(50);
            obs.setFriction(0f);
            obs.setFixedRotation(true);
            obs.setSide(side);

            physController.setCameraFocus(obs);
            physController.addInstance(player, side, x, y);

            return player;
        }
        return null;
    }

    public Enemy createEnemy(float x, float y, int side, boolean isActive, TextureRegion textureEnemy){
        if (isActive) {
            BoxObstacle obs = new BoxObstacle(x, y, 5, 5);
            obs.setGravityScale(0);



            Enemy enem = new Enemy(x, y,10,10, side,0, 0, textureEnemy, "enem", isActive, true);

            enem.setObstacle(obs);
            instances.add(enem);

            physController.addInstance(enem, side, x, y);

            return enem;
        }
        return null;
    }
    public Instance createSpike(float x, float y, float width, float height, int orientation, TextureRegion tr, int side,
                                String name, boolean isActive, boolean isVisible, boolean isAnimated) {

        Instance spike = new Instance(x, y, width, height, side, orientation, tr, name, isActive, isVisible, isAnimated);
        if (isActive) {
            instances.add(spike);

            Obstacle obs = new BoxObstacle(x, y, width, height);
            spike.setObstacle(obs);

            physController.addInstance(spike, side, x, y);
        }
        spike.setIsDanger(true);
        return spike;
    }
    public Instance createLightSource(float x, float y, float width, float height, int orientation, TextureRegion textureRegion, int side,
                                   String name, boolean isActive, boolean isVisible, boolean isAnimated) {
        Instance instance = new Instance(x, y, width, height, side, orientation, textureRegion, name, isActive, isVisible, isAnimated);
        if (isActive) {
            instances.add(instance);

            Obstacle obs = new BoxObstacle(x, y, width, height);
            instance.setObstacle(obs);
//            instance.setPosition();
            instance.setOrientation(orientation);

            physController.addLightSource(instance, side, instance.getX(), instance.getY());
        }
        return instance;
    }

    public Mirror createNonRotableMirror(int type, int reflectionEdge, int side, float x, float y, boolean isActive, boolean isVisible,
                               boolean movable, TextureRegion tr) {
        if (isActive) {
            SensorObstacle obs = new SensorObstacle(x, y, 4.9f, 4.9f);

            obs.setMass(5f);
            obs.setFriction(3f);
            obs.setDensity(2f);
            obs.setGravityScale(0f);
            obs.setFixedRotation(true);
            if(!movable){
                obs.setBodyType(BodyDef.BodyType.StaticBody);
            }

            mirror = new Mirror(type, reflectionEdge, side, x, y, 5, 5, movable, tr, 0);
            mirror.setObstacle(obs);

            instances.add(mirror);
            mirrors.add(mirror);

            physController.addInstance(mirror, side, x, y);

            return mirror;
        }
        return null;
    }

    public Mirror createRotableMirror(float angleOfRotation, int side, float x, float y, boolean movable,boolean isActive, TextureRegion tr){
        if (isActive) {
            SensorObstacle obs = new SensorObstacle(x, y, 4.9f, 4.9f);

            obs.setMass(5f);
            obs.setFriction(3f);
            obs.setDensity(2f);
            obs.setGravityScale(0f);
            obs.setFixedRotation(true);

            if(!movable){
                obs.setBodyType(BodyDef.BodyType.StaticBody);
            }

            mirror = new Mirror(angleOfRotation, side, x, y, 5, 5, movable, tr,0);
            mirror.setObstacle(obs);

            instances.add(mirror);
            mirrors.add(mirror);

            physController.addInstance(mirror, side, x, y);

            return mirror;
        }
        return null;
    }

    public void createInstance(Instance i, boolean stationary) {
        if (i.isActive()) {
            BoxObstacle obs = new BoxObstacle(i.getX(), i.getY(), i.getWidth(), i.getHeight());
            if (stationary) {
                obs.setGravityScale(0);
                obs.setBodyType(BodyDef.BodyType.StaticBody);
            }

            i.setObstacle(obs);



            instances.add(i);


            physController.addInstance(i, i.getSide(), i.getX(), i.getY());
        }
    }

    public Player getPlayer() {
        return player;
    }

    public void addInstance(Instance ins) {
        if (ins.isActive()) {
            instances.add(ins);

        }
    }

    public void removeInstance(Instance ins) {
        instances.remove(ins);
    }

    public void setPlayerTexture(TextureRegion texture) {
        texturePlayer = texture;
    }

    protected ArrayList<Instance> getInstances() {
        return instances;
    }

    protected ArrayList<Mirror> getMirrors() {
        return mirrors;
    }

    public ExitDoor getDoor(){
        return door;
    }

    public void update(float delta) {
        physController.update(delta);
        for (Instance ins : instances) {
            ins.update(delta);

            checkDamage(delta, ins);

            Obstacle obs = ins.getObstacle();

            ins.setPosition(obs.getAdjustedX(), obs.getAdjustedY());
            ins.setSide(obs.getSide());
        }
    }

    public void render(Pixmap[] pms) {
        for (Instance ins : instances) {
            int side = ins.getSide();
            if (pms[side] != null && !(ins instanceof Player)) {
                ins.render(pms[side]);
            }

            for (int i = 1; i < 3; i++) {
                int nSide = ins.getObstacle().getSideIndices()[1];
                if (nSide != -1 && nSide != side && pms[nSide] != null && !(ins instanceof Player)) {
                    PhysicsSide ps = physController.getSide(nSide);
                    Vector2 relPos = ps.getRelativePos(ins.getObstacle().getPosition());

                    ins.render(pms[nSide], relPos.x, relPos.y, physController.getSide(side).getOrientation(), ps.getOrientation());
                }
            }
        }

        int side = player.getSide();
        if (pms[side] != null) {
            player.render(pms[side]);
        }

        for (int i = 1; i < 3; i++) {
            int nSide = player.getObstacle().getSideIndices()[1];
            if (nSide != -1 && nSide != side && pms[nSide] != null) {
                PhysicsSide ps = physController.getSide(nSide);
                Vector2 relPos = ps.getRelativePos(player.getObstacle().getPosition());

                player.render(pms[nSide], relPos.x, relPos.y, physController.getSide(side).getOrientation(), ps.getOrientation());
            }
        }
    }

    public void render(GameCanvas canvas) {
        for (Instance obj : instances) {
            obj.render(canvas, 0);
        }
    }
    public void checkDamage(float delta, Instance ins){
//            Player player = playController.getPlayer();
        if(ins.getIsDanger() && ins != null && player != null){
            if(ins.getSide() == player.getSide()) {
                boolean collide;
                if(ins.getName() == "spike")
                    collide = checkSpikeCollision(ins.getX() - ins.getWidth()/2, ins.getY() - ins.getHeight()/2, ins.getWidth(), ins.getHeight(), player.getX(), player.getY(), player.getWidth(), player.getHeight());
                else collide = checkCollision(ins.getX(), ins.getY(), ins.getWidth(), ins.getHeight(), player.getX(), player.getY(), player.getWidth(), player.getHeight());
                if (collide && player.getObstacle().isActive()) {
                    player.setDead(true);
                }
            }
        }
    }

    public void saveFrame(String frameName) {
        lastFrame = frameName;

        physController.saveFrame(frameName);
        for (Instance ins : instances) {
            ins.saveFrame(frameName);
        }
    }

    public void loadFrame(String frameName) {
        String name = frameName.equals("checkpoint0") ? "restart": frameName;
        physController.loadFrame(name);
        for (Instance ins : instances) {
            HashMap<String, Object> frame = ins.savedFrames.get(name);
            int side = (int) frame.get("obsSide");

            if (side != ins.getSide()) {
                physController.switchObstacleSide(player.getObstacle(), ins.getSide(), side);
            }

            ins.loadFrame(name);
        }
    }

    public void loadLastFrame() {
        loadFrame(lastFrame);
    }

    public static boolean checkSpikeCollision(double triangleCenterX, double triangleCenterY, double triangleWidth, double triangleHeight, double rectangleCenterX, double rectangleCenterY, double rectangleWidth, double rectangleHeight) {
        // Calculate the half-widths and half-heights of both shapes
        double triangleHalfWidth = triangleWidth / 2.0;
        double triangleHalfHeight = triangleHeight / 2.0;
        double rectangleHalfWidth = rectangleWidth / 2.0;
        double rectangleHalfHeight = rectangleHeight / 2.0;

        // Calculate the minimum and maximum x and y coordinates for both shapes
        double triangleMinX = triangleCenterX - triangleHalfWidth;
        double triangleMaxX = triangleCenterX + triangleHalfWidth;
        double triangleMinY = triangleCenterY - triangleHalfHeight;
        double triangleMaxY = triangleCenterY + triangleHalfHeight;
        double rectangleMinX = rectangleCenterX - rectangleHalfWidth;
        double rectangleMaxX = rectangleCenterX + rectangleHalfWidth;
        double rectangleMinY = rectangleCenterY - rectangleHalfHeight;
        double rectangleMaxY = rectangleCenterY + rectangleHalfHeight;

        // Check for collision along the x-axis
        boolean collisionX = triangleMinX <= rectangleMaxX && triangleMaxX >= rectangleMinX;

        // Check for collision along the y-axis
        boolean collisionY = triangleMinY <= rectangleMaxY && triangleMaxY >= rectangleMinY;

        // Return true if there is a collision along both axes, indicating a collision between the triangle and the rectangle
        return collisionX && collisionY;
    }
    public boolean checkCollision(float centerX1, float centerY1, float width1, float height1, float centerX2, float centerY2, float width2, float height2) {
        // Calculate the half-widths and half-heights of the two game objects
        float halfWidth1 = width1 / 2;
        float halfHeight1 = height1 / 2;
        float halfWidth2 = width2 / 2;
        float halfHeight2 = height2 / 2;

        // Calculate the left, right, top, and bottom edges of each game object
        float left1 = centerX1 - halfWidth1;
        float right1 = centerX1 + halfWidth1;
        float top1 = centerY1 - halfHeight1;
        float bottom1 = centerY1 + halfHeight1;
        float left2 = centerX2 - halfWidth2;
        float right2 = centerX2 + halfWidth2;
        float top2 = centerY2 - halfHeight2;
        float bottom2 = centerY2 + halfHeight2;

        // Check if the two game objects floatersect
        boolean colliding = false;
        if (left1 <= right2 && right1 >= left2 && top1 <= bottom2 && bottom1 >= top2) {
            colliding = true;
        }
        return colliding;
    }

    public void dispose() {
        for (Instance ins : instances) {
            ins.dispose();
        }
    }



//    public void setPlayerController(PlayerController pc) {
//        playController = pc;
//    }

}
