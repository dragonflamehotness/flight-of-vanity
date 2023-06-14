package com.genmiracle.flightofvanity.instance.model;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.genmiracle.flightofvanity.graphics.cube.RenderableFilmStrip;
import com.genmiracle.flightofvanity.instance.Instance;
import com.genmiracle.flightofvanity.physics.obstacle.PlayerObstacle;
import com.genmiracle.flightofvanity.util.Utilities;

import java.util.HashMap;

public class Enemy extends Instance {
    private boolean faceRight;

    private Vector2 velocity;

    private float movement;
    private float turnCooldown;
    private float turnAnimationCooldown;

    // CONSTANTS
    /** Negative - left; positive - right*/
    public static final float MOVE_SPEED = 20;
    public static final float TURN_COOLDOWN = 0.5f;

    /**
     * Creates an instance given the name, side, texture, instance of an object that is either enemy, player, or platform
     *
     * @param x
     * @param y
     * @param width
     * @param height
     * @param side
     * @param tr The texture for the object instance.
     * @param name          The name of the instance. Either "mirror", "player", "enemy", or "platform"
     * @param isActive      True if the instance object is active; otherwise false
     */
    public Enemy(float x, float y, int width, int height, int side, float vx, float vy,  TextureRegion tr, String name, boolean isActive, boolean isVisible) {
        super(x, y, width, height, side, tr,  2, 30, 60, name, true, true, true);
        //System.out.println("Vanity");
        velocity = new Vector2();
        velocity.x = vx;
        velocity.y = vy;

        setActive(isActive);

        faceRight = true;
        super.isDanger = true;

        setFrameRate(4);

        initializeAnimations();
    }

    public void initializeAnimations() {
        addAnimation("idle_center", 0, 5, TYPE.LOOPING);
//        addAnimation("idle_left", 45, 45, TYPE.LOOPING);
//        addAnimation("idle_right", 33, 33, TYPE.LOOPING);
        addAnimation("turn_right", 30, 35, TYPE.SINGLE);
        addAnimation("move_right", 35, 37, TYPE.LOOPING);
        addAnimation("turn_center_right", 30, 35, TYPE.SINGLE_REV);
        addAnimation("turn_left", 45, 50, TYPE.SINGLE);
        addAnimation("move_left", 50, 53, TYPE.LOOPING);
        addAnimation("turn_center_left", 45, 50, TYPE.SINGLE_REV);
        addAnimation("move_uo", 0, 5, TYPE.LOOPING);
    }

    public float getMovement(){
        return movement;
    }

    public void setMovement(float value){
        movement = value;

        if (movement < -0.1f) {
            if (faceRight) {
                turnAnimationCooldown = TURN_COOLDOWN;
            }
            faceRight = false;
        } else if (movement > 0.1f) {
            if (!faceRight) {
                turnAnimationCooldown = TURN_COOLDOWN;
            }
            faceRight = true;
        } else {
            turnAnimationCooldown = TURN_COOLDOWN;
        }
    }

    public boolean isFacingRight(){
        return faceRight;
    }

    public float getTurnCooldown() { return turnCooldown; }

    public void setTurnCooldown(float turnCooldown) { this.turnCooldown = turnCooldown; }

    public void update(float dt) {
        super.update(dt);

        String currAnimation = "";

        if (turnAnimationCooldown > 0) {
            turnAnimationCooldown -= dt;
            if (getAnimation().startsWith("turn_") && getAnimationFinished()) {
                turnAnimationCooldown = 0;
            }

            currAnimation = "turn_";
        } else {
            currAnimation = Utilities.equalFloats(movement, 0) ? "idle_" : "move_";
        }

        if (Utilities.equalFloats(movement, 0)) {
            currAnimation += "center";

            if (currAnimation.startsWith("turn")) {
                if (faceRight) {
                    currAnimation += "_right";
                } else {
                    currAnimation += "_left";
                }
            }
        } else if (faceRight) {
            currAnimation += "right";
        } else {
            currAnimation += "left";
        }

        if (!currAnimation.equals(getAnimation())) {
            setAnimation(currAnimation);
        }

        getObstacle().update(dt);

        if (turnCooldown > 0) {
            turnCooldown -= dt;
        }
    }

    public void saveFrame(String frameName) {
        super.saveFrame(frameName);

        HashMap<String, Object> frame = savedFrames.get(frameName);

        frame.put("faceRight", faceRight);
        frame.put("turnCooldown", turnCooldown);
        frame.put("turnAnimationCooldown", turnAnimationCooldown);
    }

    public void loadFrame(String frameName) {
        super.loadFrame(frameName);

        HashMap<String, Object> frame = savedFrames.get(frameName);

        faceRight = (boolean) frame.get("faceRight");
        turnCooldown = (float) frame.get("turnCooldown");
        turnAnimationCooldown = (float) frame.get("turnAnimationCooldown");
    }

}


