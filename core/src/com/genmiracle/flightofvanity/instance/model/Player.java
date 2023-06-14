package com.genmiracle.flightofvanity.instance.model;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.genmiracle.flightofvanity.instance.Instance;
import com.badlogic.gdx.math.Vector2;
import com.genmiracle.flightofvanity.physics.obstacle.PlayerObstacle;

import java.util.HashMap;

public class Player extends Instance {

    private Vector2 position;

    private int side;

    private boolean faceRight;

    private Vector2 velocity;

    private boolean isJumping;
    private boolean pressingDown;

    private boolean isGrounded;
    private boolean isPickup;
    private int pickupCooldown;
    private int jumpCooldown;
    private float groundedCountdown;
    private int jumpCounter;
    private float movement;
    private Mirror inventory;

    private boolean holdAnimation;
    private boolean dropAnimation;
    private boolean landAnimation;
    private boolean jumpAnimation;

    private boolean isDead;
    private float deathCooldown;

    private boolean controllable;

    private boolean isCollidedDoor;
    private float victoryCooldown;

    private boolean isRotating;

    // CONSTANTS
    public static final float DEATH_COOLDOWN = 0.7f;
    public static final float VICTORY_COOLDOWN = 1.7f;

    public static final float JUMP_VELOCITY = 38.7417843581f;
    public static final float MOVE_SPEED = 30;
    private boolean placedMirror;

    /** Negative - left; positive - right*/

    public Player(float x, float y, int side, float vx, float vy, TextureRegion tr, boolean isActive) {
        super(x, y, 5.2f, 10, side, tr, 12, 17, 204, "vanity", "vanity", true, true, true);

        velocity = new Vector2();
        velocity.x = vx;
        velocity.y = vy;
        this.side = side;

        setActive(isActive);

        isGrounded = false;
        isPickup = false;
        isJumping = false;
        pressingDown = false;
        faceRight = true;
        isRotating = false;

        controllable = true;

        pickupCooldown = 0;
        jumpCooldown = 0;

        setFrameRate(5);

        initializeAnimations();

        this.isCollidedDoor = false;
    }

    public void initializeAnimations() {
        addAnimation("pick_up_right", 85, 90, TYPE.SINGLE);
        addAnimation("pick_up_left", 68, 73, TYPE.SINGLE);
        addAnimation("drop_right", 85, 90, TYPE.SINGLE_REV);
        addAnimation("drop_left", 68, 73, TYPE.SINGLE_REV);
        addAnimation("walk_left", 34, 37, TYPE.LOOPING);
        addAnimation("walk_right", 51, 54, TYPE.LOOPING);
        addAnimation("idle_left", 34, 34, TYPE.SINGLE);
        addAnimation("idle_right", 51, 51, TYPE.SINGLE);
        addAnimation("jump_start_right", 0, 3, TYPE.SINGLE);
        addAnimation("jump_fall_right", 119, 123, TYPE.LOOPING);
        addAnimation("jump_land_right", 4, 7, TYPE.SINGLE);
        addAnimation("jump_start_left", 17, 20, TYPE.SINGLE);
        addAnimation("jump_fall_left", 102, 106, TYPE.LOOPING);
        addAnimation("jump_land_left", 21, 24, TYPE.SINGLE);
        addAnimation("exit_left", 136, 150, TYPE.SINGLE);
        addAnimation("exit_right", 153, 167, TYPE.SINGLE);
        addAnimation("death_left", 170, 186, TYPE.SINGLE);
        addAnimation("death_right", 187, 203, TYPE.SINGLE);
    }

    public float getMovement(){
        return movement;
    }

    public void setMovement(float value){
        movement = value;

        if (movement < 0) {
            faceRight = false;
        } else if(movement > 0){
            faceRight = true;
        }
    }

    public void setIsCollidedDoor(boolean isCollidedDoor){
        if (controllable) {
            this.isCollidedDoor = isCollidedDoor;

            victoryCooldown = isCollidedDoor ? VICTORY_COOLDOWN : 0f;
            controllable = !isCollidedDoor;
        }
    }

    public boolean isCollidedDoor() {
        return isCollidedDoor && victoryCooldown <= 0;
    }

    public boolean isPickup(){
        return isPickup && pickupCooldown <= 0 && controllable;
    }


    public void setPickup(boolean value){
        isPickup = value;
    }
    public boolean isJumping(){
        return isJumping && jumpCooldown <= 0 && jumpCounter > 0 && controllable;
    }
    public boolean isPressingDown() { return pressingDown;}

    public void setJumping(boolean value){
        isJumping = value;
    }

    public void setDown(boolean value){
        pressingDown = value;
    }

    public void changeJumpCount(int count) {
        jumpCounter += count;
        if (count < 0) {
            jumpAnimation = true;
        }
    }

    public boolean isGrounded(){
        return isGrounded;
    }

    public void setGrounded(boolean value){
        isGrounded = value;
        landAnimation = value;
        if (value) {
            jumpCounter = 2;
        }
    }

    public boolean isFacingRight(){
        return faceRight;
    }

    public boolean isHolding() {
        return inventory != null;
    }

    public Mirror getHolding() {
        return inventory;
    }

    public void setHolding(Mirror m) {
        inventory = m;
        holdAnimation = m != null;
        dropAnimation = m == null;
    }

    public boolean isRotating(){
        return isRotating;
    }

    public void setIsRotating(boolean isRotating){
        this.isRotating = isRotating;
    }

    public boolean isControllable() {return controllable; }

    /**
     * Returns a boolean determining whether or not the player is currently facing right
     *
     * @return true if facing right, false otherwise
     */
    public boolean getFacing() {
        return faceRight;
    }

    public Mirror emptyHolding() {
        Mirror t = inventory;
        inventory = null;

        return t;
    }

    public void update(float dt){
        super.update(dt);

        getObstacle().setGravity(0, 0);

        String currAnimation;
        if (isCollidedDoor) {
            currAnimation = "exit";
            if (victoryCooldown > 0f) {
                victoryCooldown -= dt;
            }
            setFrameRate(12);
        } else if (isDead) {
            currAnimation = "death";
            if (deathCooldown > 0f) {
                deathCooldown -= dt;
            }
            setFrameRate(16);
        }else if (!isGrounded) {
            currAnimation = "jump";
            holdAnimation = false;
            dropAnimation = false;
            landAnimation = false;

            if (jumpAnimation || (!getAnimationFinished() && getAnimation().startsWith("jump_start"))) {
                currAnimation += "_start";
                setFrameRate(6);
                jumpAnimation = false;
            } else {
                currAnimation += "_fall";
                setFrameRate(6);
            }
        } else if (Math.abs(movement) > 0.1f) {
            currAnimation = "walk";
            holdAnimation = false;
            dropAnimation = false;
            landAnimation = false;
            setFrameRate(8);
        } else {
            currAnimation = holdAnimation ? "pick_up" : dropAnimation ? "drop" :
                    landAnimation ? "jump_land" : "idle";
            setFrameRate(8);
        }

        if (faceRight) {
            currAnimation += "_right";
        } else {
            currAnimation += "_left";
        }


        if (!currAnimation.equals(getAnimation())) {
            setAnimation(currAnimation);
        }

        if (isJumping()) {
            jumpCooldown = 15;
        } else if (jumpCooldown > 0) {
            jumpCooldown--;
        }

        if (isPickup()) {
            pickupCooldown = 40;
            isPickup = false;
        } else if (pickupCooldown > 0) {
            pickupCooldown--;
        }

        if (isGrounded) {
            jumpCounter = 2;
            groundedCountdown = 0;
        } else {
            groundedCountdown += dt;

            if (jumpCounter > 1 && groundedCountdown > 0.5f) {
                jumpCounter = 1;
            }
        }

        getObstacle().update(dt);
    }

    public void saveFrame(String frameName) {
        super.saveFrame(frameName);

        HashMap<String, Object> frame = savedFrames.get(frameName);

        frame.put("faceRight", faceRight);
        frame.put("inventory", inventory);
        frame.put("isGrounded", isGrounded);
        frame.put("sensorCount", ((PlayerObstacle) getObstacle()).getSensorCount());
    }

    public void loadFrame(String frameName) {
        super.loadFrame(frameName);

        getObstacle().setActive(false);

        HashMap<String, Object> frame = savedFrames.get(frameName);

        faceRight = (boolean) frame.get("faceRight");
        inventory = (Mirror) frame.get("inventory");
        isGrounded = (boolean) frame.get("isGrounded");
        controllable = true;
        isDead = false;
        deathCooldown = 0;
        setVisible(true);

        ((PlayerObstacle) getObstacle()).setSensorCount(0);
        ((PlayerObstacle) getObstacle()).deactivateSensorTemp();

        System.out.println(isGrounded);

        holdAnimation = false;
        dropAnimation = false;
        jumpAnimation = false;
        landAnimation = false;

        getObstacle().setActive(true);
    }

    public void setDead(boolean b){
        if (isDead != b) {
            isDead = b;

            deathCooldown = b ? DEATH_COOLDOWN : 0;

            controllable = !b;
        }
    }
    public boolean isDead(){
        return isDead && deathCooldown <= 0;
    }
    public boolean isDeadAC(){
        return isDead;
    }

    public void setPlaced(boolean b) {
        this.placedMirror = b;
    }
    public boolean getPlaced() {
        return placedMirror;
    }
}
