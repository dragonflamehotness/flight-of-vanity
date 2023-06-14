package com.genmiracle.flightofvanity.graphics.cube;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.genmiracle.flightofvanity.util.FilmStrip;

import java.util.HashMap;

public class RenderableFilmStrip extends Renderable {
    private int x;
    private int y;
    private int rows;
    private int cols;
    private int size;

    private int[] rowYs;
    private int[] colXs;

    private int frame;
    private int frameWidth;
    private int frameHeight;

    private HashMap<String, int[]> animations;
    private HashMap<String, TYPE> animationTypes;
    private String currentAnimation;
    private float frameCounter;
    private float frameRate;

    public enum TYPE {LOOPING, SINGLE, LOOPING_REV, SINGLE_REV}

    public RenderableFilmStrip(String name, Texture texture, int rows, int cols, int size) {
        super(name, texture);

        this.rows = rows;
        this.cols = cols;
        this.size = size;

        animations = new HashMap<>();
        animationTypes = new HashMap<>();
        currentAnimation = "";

        frameWidth = texture.getWidth() / cols;
        frameHeight = texture.getHeight() / rows;
    }

    public RenderableFilmStrip(String name, Texture texture, int[] rows, int[] cols, int size) {
        super(name, texture);

        this.size = size;
        this.rows = rows.length;
        this.cols = cols.length;

        rowYs = rows;
        colXs = cols;

        animations = new HashMap<>();
        animationTypes = new HashMap<>();
        currentAnimation = "";
    }

    public void addAnimation(String name, int frameStart, int frameEnd, TYPE type) {
        animations.put(name, new int[] {frameStart, frameEnd});
        animationTypes.put(name, type);
    }

    public void setAnimation(String name) {
        switch (animationTypes.get(name)) {
            case SINGLE:
            case LOOPING:
                setFrame(animations.get(name)[0]);
                break;
            case SINGLE_REV:
            case LOOPING_REV:
                setFrame(animations.get(name)[1]);
                break;
        }
        currentAnimation = name;
    }

    public String getAnimation() {
        return currentAnimation;
    }

    public boolean getAnimationFinished() {
        if (currentAnimation == ""){
            return false;
        }

        switch (animationTypes.get(currentAnimation)) {
            case SINGLE:
                return frame == animations.get(currentAnimation)[1];
            case SINGLE_REV:
                return frame == animations.get(currentAnimation)[0];
        }

        return false;
    }

    public void nextAnimationFrame() {
        if (currentAnimation.isEmpty()) {
            return;
        }

        TYPE type = animationTypes.get(currentAnimation);

        int[] animData = animations.get(currentAnimation);

        if (type == TYPE.SINGLE || type == TYPE.LOOPING) {
            frame++;
        } else {
            frame--;
        }
        if (frame > animData[1] || frame < animData[0]) {
            switch (type) {
                case SINGLE:
                case LOOPING_REV:
                    frame = animData[1];
                    break;
                case SINGLE_REV:
                case LOOPING:
                    frame = animData[0];
                    break;
            }
        }

        setFrame(frame);
    }

    public void setFrameRate(float fps) {
        frameRate = 1f / fps;
    }

    public void updateAnimation(float dt) {
        frameCounter += dt;

        if (frameCounter >= frameRate) {
            frameCounter -= frameRate;
            nextAnimationFrame();
        }
    }

    public void setFrame(int frame) {
        if (frame >= size) {
            return;
        }
        this.frame = frame;

        if (rowYs == null) {
            x = frameWidth * (frame % cols) + 1;
            y = frameHeight * (frame / cols) + 1;
        } else {
            x = colXs[frame % cols];
            y = rowYs[frame / cols];
        }
    }

    public int getFrame() {
        return frame;
    }

    public void nextFrame() {
        setFrame((frame + 1) % size);
    }

    public void render(Pixmap dest, float x, float y, int orientation, float width) {
        switch (orientation) {
            case 0:
                super.render(dest, x, y, this.x, this.y, frameWidth, frameHeight, orientation, width);
                break;
            case 1:
                super.render(dest, x, y, frameHeight * (rows - 1) - this.y, this.x, frameWidth, frameHeight, orientation, width);
                break;
            case 2:
                super.render(dest, x, y, frameWidth * (cols - 1) - this.x, frameHeight * (rows - 1) - this.y, frameWidth, frameHeight, orientation, width);
                break;
            case 3:
                super.render(dest, x, y, this.y, frameWidth * (cols - 1) - this.x, frameWidth, frameHeight, orientation, width);
                break;

        }
    }

}
