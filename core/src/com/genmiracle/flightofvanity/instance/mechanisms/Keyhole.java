package com.genmiracle.flightofvanity.instance.mechanisms;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.genmiracle.flightofvanity.instance.model.Mirror;
import com.genmiracle.flightofvanity.physics.obstacle.PlayerObstacle;

import java.util.HashMap;

public class Keyhole extends Mechanism {

    public Keyhole(float x, float y, int side, int orientation, TextureRegion textureRegion,
                   boolean isActive, boolean isVisible) {
        super(x, y, 8, 8, side, orientation, textureRegion, 1, 7, 7, "keyhole",
                isActive, isVisible, true);

        initializeAnimations();
        setFrameRate(7);
    }

    private void initializeAnimations() {
        addAnimation("light_up", 0, 6, TYPE.SINGLE);
        addAnimation("unlit", 0, 0, TYPE.SINGLE);
    }

    public void activate() {
        if (!isActivated()) {
            setAnimation("light_up");

            super.activate();

            for (Mechanism m : getConnected()) {
                m.activate();
            }
        }
    }

    public void deactivate() {
        if (isActivated()) {
            setAnimation("unlit");
        }
    }
}
