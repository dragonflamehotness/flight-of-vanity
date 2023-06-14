package com.genmiracle.flightofvanity.instance;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.genmiracle.flightofvanity.InputController;
import com.genmiracle.flightofvanity.instance.model.Mirror;
import com.genmiracle.flightofvanity.instance.model.Player;
import com.genmiracle.flightofvanity.physics.PhysicsController;
import com.genmiracle.flightofvanity.physics.obstacle.Obstacle;
import com.genmiracle.flightofvanity.physics.obstacle.SensorObstacle;
import com.genmiracle.flightofvanity.util.Utilities;

import java.awt.*;

public class PlayerController {
    private InputController inController;
    private InstanceController instController;
    private PhysicsController physController;
    private Player player;

    private Vector2 v2temp;
    private float decrementVY;


    private Mirror tempMirror;

    private int mirrorStartOrientation;
    private int checkpointNum;

    private float rotationCountDown;
    private static final float MIRROR_PLACE_DIST = 5f;
    private static final float MIRROR_PICKUP_DIST = 10f;

    private static final float ROTATEOFFSET = 40f;

    public PlayerController(InputController ic) {
        this.inController = ic;
        v2temp = new Vector2();
//        checkpointNum = 0;
    }

    public void setInstanceController(InstanceController instanceC) {
        this.instController = instanceC;
    }

    public void setPlayer(Player p) {
        player = p;
    }

    public void clear() {
        tempMirror = null;
    }

    public Player getPlayer() {
        return player;
    }

    public void update(float delta) {
        inController.readInput();
        player.update(delta);

        if (!player.isControllable()) {
            player.setVX(0);
            player.setVY(0);

            player.setMovement(0);
            player.setJumping(false);
            player.setDown(false);
            player.setPickup(false);
            return;
        }

        // Player movement
        player.setMovement(inController.getForward());
        player.setVX(player.getMovement() * Player.MOVE_SPEED);

        player.setJumping(inController.didPressJump());
        player.setDown(inController.didPressDown());

        if (player.isJumping()) {
            player.setVY(Player.JUMP_VELOCITY);
            player.changeJumpCount(-1);
        }

        if (player.isPressingDown()){
            decrementVY = Player.JUMP_VELOCITY;
            decrementVY += 6.5f;
            if (player.getVY() > -decrementVY) {
                player.setVY(-decrementVY);
                if(player.isGrounded())
                    player.setGrounded(true);
            }
        }

        // Player pickup
        Vector2 playerPos = player.getObstacle().getPosition();
        player.setPickup(inController.didPressPickup());
        if (player.isHolding()) { // currently holding a mirror
            Mirror m = player.getHolding();

            SensorObstacle mirrorObs = (SensorObstacle) m.getObstacle();

            PhysicsController phys = instController.getPhysicsController();

            v2temp.set(playerPos.x + (player.getFacing() ? MIRROR_PLACE_DIST : -MIRROR_PLACE_DIST), playerPos.y);
            Vector2 mirrorRelPosition = phys.getSide(player.getSide()).getRelativePos(v2temp);

            mirrorObs.setSideOrientation(player.getObstacle().getSideOrientation());
            mirrorObs.setPosition(v2temp);

            m.setOrientation((mirrorStartOrientation + mirrorObs.getSideOrientation()) % 4);

            m.setSide(player.getSide());
            m.setPosition(mirrorRelPosition.x, mirrorRelPosition.y);

            phys.switchObstacleSide(mirrorObs, mirrorObs.getSide(), player.getSide());

            if (mirrorObs.getSensorCount() > 0) {
                m.setTint(Color.RED);
            } else {
                m.setTint(Color.WHITE);
            }

            m.setVX(0);
            m.setVY(0);
            if (player.isPickup() && mirrorObs.getSensorCount() == 0) {
//                m.setActive(true);
                m.setOpacity(1f);

//                mirrorObs.setGravityScale(1);
                mirrorObs.setSensor(false);
                mirrorObs.setBullet(false);
                mirrorObs.setSleepingAllowed(true);
                mirrorObs.setGravity(0, PhysicsController.GRAVITY * mirrorObs.getMass());

                player.setHolding(null);
                player.setPlaced(true);

                if (player.isGrounded()) {
                    checkpointNum++;
                    instController.saveFrame("checkpoint" + checkpointNum);


                }
            }

            //processRotation
            player.setIsRotating(inController.didPressRotate());
            if(rotationCountDown > 0) {
                rotationCountDown -= delta;
            }
            if (player.isRotating()) {
                if (m.getType() == 360) {
                    player.setIsRotating(false);
                    m.setAngleOfRotation(m.getAngleOfRotation() + ROTATEOFFSET * delta);
                }
                else if((m.getType() == 45 || m.getType() == 90) && rotationCountDown <= 0){
                    rotationCountDown = 0.3f;
                    player.setIsRotating(false);
                    m.setReflectionAngle((m.getReflectionEdge() + 1) % 4);
                }
            }

        } else { // don't have a mirror need to pick up a mirror
            if (tempMirror != null) {
                tempMirror.setTint(Color.WHITE);
            }

            tempMirror = null;
            for (Instance inst : instController.getInstances()) {
                if(inst instanceof Mirror){
                    float x_dist = Math.abs(inst.getObstacle().getPosition().x - playerPos.x);
                    float y_dist = Math.abs(inst.getObstacle().getPosition().y - playerPos.y);
                    if(tempMirror == null && ((Mirror) inst).getMovable() && (x_dist<= MIRROR_PICKUP_DIST && y_dist <= MIRROR_PICKUP_DIST)){
                        tempMirror = (Mirror) inst;
                    }
                    else if(tempMirror!=null && ((Mirror) inst).getMovable()){
                        float x_distance = inst.getObstacle().getPosition().x - playerPos.x;
                        x_distance = x_distance*x_distance;
                        float y_distance = inst.getObstacle().getPosition().y - playerPos.y;
                        y_distance = y_distance*y_distance;
                        float t_x = tempMirror.getObstacle().getPosition().x - playerPos.x;
                        t_x = t_x*t_x;
                        float t_y = tempMirror.getObstacle().getPosition().y - playerPos.y;
                        t_y = t_y*t_y;
                        if(((x_distance + y_distance) <= (t_x + t_y)) && (x_dist<= MIRROR_PICKUP_DIST && y_dist <= MIRROR_PICKUP_DIST)){
                            tempMirror = (Mirror) inst;
                        }
                    }
                }
            }

            if (tempMirror != null) {
                if (player.isPickup()) {
                    player.setHolding(tempMirror);

                    tempMirror.setTint(Color.WHITE);
//                    tempMirror.setActive(false);
                    tempMirror.setOpacity(0.2f);
                    tempMirror.setOrientation((4 - tempMirror.getObstacle().getSideOrientation() + tempMirror.getOrientation()) % 4);

                    tempMirror.getObstacle().setSensor(true);
                    tempMirror.getObstacle().setBullet(true);
                    tempMirror.getObstacle().setSleepingAllowed(false);
                    tempMirror.getObstacle().setGravityScale(1f);
                    mirrorStartOrientation = tempMirror.getOrientation();
                } else {
                    tempMirror.setTint(Color.LIGHT_GRAY);
                }
            }
        }

        if (!player.isPickup() && inController.didPressUndo()) {
            String checkpointKey = "checkpoint" + (checkpointNum);
//            instController.saveFrame(checkpointKey);
            instController.loadLastFrame();
        }
    }



    /** Do not do anything */
    public static final int CONTROL_NO_ACTION  = 0x00;
    /** Move the ship to the left */
    public static final int CONTROL_MOVE_LEFT  = 0x01;
    /** Move the ship to the right */
    public static final int CONTROL_MOVE_RIGHT = 0x02;
    /** Move the ship to the up */
    public static final int CONTROL_MOVE_UP    = 0x04;
    /** Move the ship to the down */
    public static final int CONTROL_MOVE_DOWN  = 0x08;
    /** Fire the ship weapon */
    public static final int CONTROL_PICKUP     = 0x10;

    public int getAction(){
        int code = CONTROL_NO_ACTION;

        if (player.getMovement() < 0) code |= CONTROL_MOVE_LEFT;
        if (player.getMovement() > 0) code |= CONTROL_MOVE_RIGHT;
        if (player.isJumping()) code |= CONTROL_MOVE_UP;
        if (player.isPickup()) code |= CONTROL_PICKUP;
        return code;
    }
}
