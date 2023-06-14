package com.genmiracle.flightofvanity.instance.mechanisms;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.genmiracle.flightofvanity.instance.Instance;
import com.genmiracle.flightofvanity.instance.model.Mirror;

import java.util.ArrayList;
import java.util.HashMap;

public abstract class Mechanism extends Instance {
    /** Whether or not this Mechanism is currently activated */
    private boolean active;

    /** A list of all other Mechanisms this one is connected to */
    private ArrayList<Mechanism> connected;

    /**
     * Creates an instance given the name, side, texture, instance of an object that is either enemy, player, or platform
     *
     * @param x
     * @param y
     * @param width
     * @param height
     * @param side
     * @param textureRegion The texture for the object instance.
     * @param name          The name of the instance. Either "mirror", "player", "enemy", or "platform"
     * @param isActive      True if the instance object is active; otherwise false
     * @param isVisible
     * @param isAnimated
     */
    public Mechanism(float x, float y, float width, float height, int side, int orientation, TextureRegion textureRegion, String name, boolean isActive, boolean isVisible, boolean isAnimated) {
        super(x, y, width, height, side, orientation, textureRegion, name, isActive, isVisible, isAnimated);

        connected = new ArrayList<>();
    }

    public Mechanism(float x, float y, float width, float height, int side, int orientation, TextureRegion textureRegion,
                    int animRows, int animCols, int animSize, String name, boolean isActive, boolean isVisible,
                    boolean isAnimated) {
        super(x, y, width, height, side, orientation, textureRegion, animRows, animCols, animSize, name, isActive, isVisible, isAnimated);

        connected = new ArrayList<>();
    }

    /**
     * Activates this Mechanism, making variable changes if this Mechanism started as inactive
     */
    public void activate() {
        active = true;
    }

    /**
     * Deactivates this Mechanism, making variable changes if this Mechanism started as active
     */
    public void deactivate() {
        active = false;
    }

    public boolean isActivated() {
        return active;
    }

    /**
     * Connects a new Mechanism to this one, allowing for it to be modified when this Mechanism is
     * activated/deactivaated
     *
     * @param otherMech Mechanism to connect
     */
    public void addConnected(Mechanism otherMech) {
        connected.add(otherMech);
    }

    /**
     * Disconnects a Mechanism from this one. Returns true if it was successfully removed, false otherwise.
     *
     * @param otherMech Mechanism to disconnect
     *
     * @return true if the mechanism was successfully removed, false otherwise
     */
    public boolean removeConnected(Mechanism otherMech) {
        return connected.remove(otherMech);
    }

    protected ArrayList<Mechanism> getConnected() {
        return connected;
    }

    public void saveFrame(String frameName) {
        super.saveFrame(frameName);

        HashMap<String, Object> frame = savedFrames.get(frameName);

        frame.put("active", active);
    }

    public void loadFrame(String frameName) {
        super.loadFrame(frameName);

        HashMap<String, Object> frame = savedFrames.get(frameName);

        active = (boolean) frame.get("active");
        if (!active) {
            deactivate();
        }
    }
}
