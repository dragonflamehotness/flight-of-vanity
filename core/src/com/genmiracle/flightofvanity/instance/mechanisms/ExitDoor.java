package com.genmiracle.flightofvanity.instance.mechanisms;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class ExitDoor extends Mechanism {

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
    public ExitDoor(float x, float y, int side, int orientation, TextureRegion textureRegion,
                    boolean isActive, boolean isVisible) {
        super(x, y, 10.625f, 11.25f, side, orientation, textureRegion, 1, 12, 12, "exit_door",
                isActive, isVisible, true);

        addAnimation("open", 0, 11, TYPE.SINGLE);
        addAnimation("closed", 0, 0, TYPE.SINGLE);

        setAnimation("closed");
//        setOrientation(orientation);
    }

    public void activate() {
        if (!isActivated()) {
            super.activate();

            setAnimation("open");
            setFrameRate(4.5f);
        }
    }


    public void deactivate() {
        setAnimation("closed");
    }
}
