package com.genmiracle.flightofvanity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.controllers.Controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
//import java.util.stream.Stream;

/**
 * Device-independent input manager.
 *
 * This class supports a keyboard.  Each player is
 * assigned an ID.  When the class is created, we check to see if there is a
 * controller for that ID.  If so, we use the controller.  Otherwise, we default
 * the the keyboard.
 */
public class InputController {

    /** How much forward is the player moving (Left and Right)*/
    private float forward;

    /** Did we press the jump button */
    private boolean pressedJump;

    /** Did we press the down arrow button */
    private boolean pressedDown;

    /** Did we press the pick-up button (E) */
    private boolean pressedPickup;

    /** Did we press the observation mode button (TAB) */
    private boolean pressedObservationMode;

    /** Did we press the undo button (Q) */
    private boolean pressedUndo;

    /** Whether or not to reset the prevX and prevY positions next update */
    private boolean resetMousePosition;

    /** How much clockwise is the player rotating */
    private float rotation;

    /** The amount the mouse is currently moving along the x-axis */
    private int dx;

    /** The amount the mouse is currently moving along the y-axis */
    private int dy;

    /** The x-value of the mouse from the previous update */
    private int prevX;
    /** The y-value of the mouse from the previous update */
    private int prevY;

    private boolean isObserveControlKey;

    private boolean pressedRotate;

    private HashSet<Integer> keysPressed;

    private static InputController instance;
    private boolean stillPressingJump;

    private XBoxController xbox;

    public boolean isStillPressing() {
        return stillPressingJump;
    }

    public enum ControlCode {
        LEFT, RIGHT, JUMP, DOWN, OBS_MODE, PICK_UP, ROTATE, PAUSE, UNDO
    }
    public static HashMap<ControlCode, Integer> controls;
    private static final HashMap<ControlCode, Integer> DEFAULT_CONTROLS = new HashMap() {{
        put(ControlCode.LEFT, Input.Keys.LEFT);
        put(ControlCode.RIGHT, Input.Keys.RIGHT);
        put(ControlCode.JUMP, Input.Keys.SPACE);
        put(ControlCode.DOWN, Input.Keys.DOWN);
        put(ControlCode.OBS_MODE, Input.Keys.TAB);
        put(ControlCode.PICK_UP, Input.Keys.E);
        put(ControlCode.ROTATE, Input.Keys.R);
        put(ControlCode.PAUSE, Input.Keys.ESCAPE);
        put(ControlCode.UNDO, Input.Keys.Q);
    }};

    public InputController() {
        controls = (HashMap<ControlCode, Integer>) DEFAULT_CONTROLS.clone();
        keysPressed = new HashSet<>();
        Array<XBoxController> controllers = Controllers.get().getXBoxControllers();
        if (controllers.size > 0) {
            xbox = controllers.get( 0 );
        } else {
            xbox = null;
        }
        isObserveControlKey = false;
        loadControls();
    }

    /**
     * Set the keybind of a certain <code>ControlCode</code> to the corresponding key of the given integer
     * <code>key</code>.
     *
     * @param code <code>ControlCode</code> enum value of which control to change
     * @param key integer value of corresponding keyboard key
     */
    public ControlCode setControl(ControlCode code, Integer key) {
        for (ControlCode c : controls.keySet()) {
            if (!c.equals(code) && key == controls.get(c)) {
                return c;
            }
        }

        controls.put(code, key);
        saveControls();

        return null;
    }

    /**
     * Get the corresponding integer of the key associated to the control given by <code>ControlCode</code>.
     *
     * @param code <Code>ControlCode</Code> enum value of which control to get
     *
     * @return integer value of corresponding keyboard key
     */
    public int getControl(ControlCode code) {
        return controls.get(code);
    }

    /**
     * Get the corresponding string of the key associated to the control given by <code>ControlCode</code>.
     *
     * @param code <Code>ControlCode</Code> enum value of which control to get
     *
     * @return string form of corresponding keyboard key
     */
    public String getControlString(ControlCode code) {
        return Input.Keys.toString(controls.get(code));
    }

    /**
     * Reset the controls to their default values.
     */
    public void resetControls() {
        controls = (HashMap<ControlCode, Integer>) DEFAULT_CONTROLS.clone();
        saveControls();
    }

    /**
     * Load the controls from preferences.
     */
    public void loadControls() {
        Preferences prefs = Gdx.app.getPreferences("controls");

        controls.put(ControlCode.LEFT, prefs.getInteger("left", DEFAULT_CONTROLS.get(ControlCode.LEFT)));
        controls.put(ControlCode.RIGHT, prefs.getInteger("right", DEFAULT_CONTROLS.get(ControlCode.RIGHT)));
        controls.put(ControlCode.JUMP, prefs.getInteger("jump", DEFAULT_CONTROLS.get(ControlCode.JUMP)));
        controls.put(ControlCode.DOWN, prefs.getInteger("down", DEFAULT_CONTROLS.get(ControlCode.DOWN)));
        controls.put(ControlCode.PICK_UP, prefs.getInteger("pickUp", DEFAULT_CONTROLS.get(ControlCode.PICK_UP)));
        controls.put(ControlCode.OBS_MODE, prefs.getInteger("obsMode", DEFAULT_CONTROLS.get(ControlCode.OBS_MODE)));
        controls.put(ControlCode.ROTATE, prefs.getInteger("rotate", DEFAULT_CONTROLS.get(ControlCode.ROTATE)));
        controls.put(ControlCode.PAUSE, prefs.getInteger("pause", DEFAULT_CONTROLS.get(ControlCode.PAUSE)));
        controls.put(ControlCode.UNDO, prefs.getInteger("undo", DEFAULT_CONTROLS.get(ControlCode.UNDO)));
    }

    /**
     * Save the controls from preferences.
     */
    public void saveControls() {
        Preferences prefs = Gdx.app.getPreferences("controls");

        prefs.putInteger("left", controls.get(ControlCode.LEFT));
        prefs.putInteger("right", controls.get(ControlCode.RIGHT));
        prefs.putInteger("jump", controls.get(ControlCode.JUMP));
        prefs.putInteger("down", controls.get(ControlCode.DOWN));
        prefs.putInteger("pickUp", controls.get(ControlCode.PICK_UP));
        prefs.putInteger("obsMode", controls.get(ControlCode.OBS_MODE));
        prefs.putInteger("rotate", controls.get(ControlCode.ROTATE));
        prefs.putInteger("pause", controls.get(ControlCode.PAUSE));
        prefs.putInteger("undo", controls.get(ControlCode.UNDO));

        prefs.flush();
    }

    public static InputController getInstance() {
        if (instance == null){
            instance = new InputController();
            return instance;
        }else {
            return instance;
        }
    }

    /**
     * Return the amount of forward movement.
     * -1 = backward, 1 = forward, 0 = still
     * @return amount of forward movement
     */
    public float getForward() { return forward; }

    /**
     * Returns whether the jump button was pressed.
     *
     * @return whether the jump button was pressed.
     */
    public boolean didPressJump() { return pressedJump; }

    /**
     * Returns whether the down button was pressed.
     *
     * @return whether the down button was pressed.
     */
    public boolean didPressDown() { return pressedDown; }

    /**
     * Returns whether the pickup mirror button was pressed.
     *
     * @return whether the pickup mirror button was pressed.
     */
    public boolean didPressPickup() { return pressedPickup; }

    /**
     * Returns whether the observation mode button was pressed.
     *
     * @return whether the observation mode button was pressed.
     */
    public boolean didPressObservationMode() { return pressedObservationMode; }

    public boolean isObserveControlKey(){
        return isObserveControlKey;
    }

    public void setIsObserveControlKey(boolean iskey){
        isObserveControlKey = iskey;
    }

    /**
     * Returns whether the undo button was pressed.
     *
     * @return whether the undo button was pressed.
     */
    public boolean didPressUndo() {
        return pressedUndo;
    }

    /**
     * Returns the amount of clockwise mirror movement
     * -1 = counterclockwise, 1 = clockwise, 0 = still
     * @return amount of clockwise mirror movement
     */
    public float getRotation() { return rotation; }

    /**
     * Returns the distance (in pixels) that the pointer has moved since the last update along the x-axis
     *
     * @return distance (in pixels) that the pointer has moved since the last update along the x-axis
     */
    public int getDx() {
        return dx;
    }

    /**
     * Returns the distance (in pixels) that the pointer has moved since the last update along the y-axis
     *
     * @return distance (in pixels) that the pointer has moved since the last update along the y-axis
     */
    public int getDy() {
        return dy;
    }

    /**
     * Returns the current x-coordinate of the mouse pointer
     *
     * @return current x-coordiante of the mouse pointer
     */
    public int getMouseX() {return Gdx.input.getX(); }

    /**
     * Returns the current y-coordinate of the mouse pointer
     *
     * @return current y-coordiante of the mouse pointer
     */
    public int getMouseY() {return Gdx.input.getY(); }

    public boolean didPressRotate(){
        return pressedRotate;
    }

    public void setCursorCatched(boolean cursorCaught) {
        Gdx.input.setCursorCatched(cursorCaught);

        resetMousePosition = true;
    }

    /**
     * Reads the input for player and converts the result into game logic.
     *
     * This is an example of polling input. Instead of registering a listener,
     * we ask the controller about its current state. When the game is running,
     * it is typically best to poll input instead of using listeners. Listeners
     * are more appropriate for menus and buttons (like the loading screen).
     */
    public void readKeyboard(boolean secondary) {
        //Only need keyboard controls for our game
        int left, right, down, jump, pickup, rotate, observationMode, undo;
        left = controls.get(ControlCode.LEFT);
        right = controls.get(ControlCode.RIGHT);
        down = controls.get(ControlCode.DOWN);
        jump = controls.get(ControlCode.JUMP);
        pickup = controls.get(ControlCode.PICK_UP);
        rotate = controls.get(ControlCode.ROTATE);
        observationMode = controls.get(ControlCode.OBS_MODE);
        undo = controls.get(ControlCode.UNDO);

        int x = Gdx.input.getX();
        int y = Gdx.input.getY();

        dx = x - prevX;
        dy = y - prevY;

        if (resetMousePosition && (dx != 0 || dy != 0)) {
            dx = 0;
            dy = 0;
            resetMousePosition = false;
        }

        prevX = x;
        prevY = y;

        //Convert keyboard state into game commands
        forward = 0;
        rotation = 0;

        //Movement forward/backward
        if (Gdx.input.isKeyPressed(left) && !Gdx.input.isKeyPressed(right)) {
            forward = -1;
        } else if (Gdx.input.isKeyPressed(right) && !Gdx.input.isKeyPressed(left)) {
            forward = 1;
        }

        //Jump
        if (Gdx.input.isKeyPressed(jump) && !stillPressingJump) {
            pressedJump = true;
            stillPressingJump = true;
        }else if(!Gdx.input.isKeyPressed(jump)) {
            stillPressingJump = false;
        }

        //Down
        if (Gdx.input.isKeyPressed(down) && (!secondary || !pressedDown)) {
            pressedDown = true;
        }

        //Pickup mirror
        if (Gdx.input.isKeyPressed(pickup) && (!secondary || !pressedPickup)) {
            pressedPickup = true;
        }

        if (Gdx.input.isKeyPressed(observationMode) && (!secondary || !pressedObservationMode)) {
            pressedObservationMode = true;
        }

        //Rotate mirror
        if (Gdx.input.isKeyPressed(rotate)  && (!secondary || !pressedRotate)) {
            rotation = 1;
            pressedRotate = true;
        }

        if (Gdx.input.isKeyPressed(undo)  && (!secondary || !pressedUndo)) {
            pressedUndo = true;
        }


    }

    public void readGamepad(){
        pressedUndo = xbox.getX();
        pressedDown = xbox.getLeftY() < -0.3;
        if(xbox.getLeftTrigger() > 0.6){
            rotation = 1;
            pressedRotate = true;
        }
        if(xbox.getRightTrigger() > 0.6){
            rotation = -1;
            pressedRotate = true;
        }
        pressedPickup = xbox.getB();
        pressedObservationMode = xbox.getY();
        pressedJump = xbox.getA();
    }

    public void readInput(){
        pressedJump = false;
        pressedDown = false;
        pressedPickup = false;
        pressedRotate = false;
        pressedObservationMode = false;
        pressedUndo = false;

        if (xbox != null && xbox.isConnected()) {
            readGamepad();
            readKeyboard(true); // Read as a back-up
        } else {
            readKeyboard(true);
        }
    }
}
