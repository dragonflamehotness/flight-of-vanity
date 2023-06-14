package com.genmiracle.flightofvanity.graphics.cube;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.genmiracle.flightofvanity.util.Utilities;

import java.awt.*;
import java.util.HashMap;

public class Renderable {
    private Pixmap[] pms;
    private Texture texture;
    private int width;
    private int height;
    private float opacity;
    private Color tint;
    private boolean outline;
    private Color outlineColor;
    private String name;
    private static float scale;

    private static HashMap<String, Pixmap[]> textures = new HashMap<>();
    public static void setScale(float newScale) {
        scale = newScale;
    }

    public Renderable(Texture texture) {
        pms = new Pixmap[8];
        opacity = 1f;
        tint = Color.WHITE;

        name = "";

        setTexture(texture);
    }

    public Renderable(Pixmap pixmap) {
        pms = new Pixmap[8];
        opacity = 1f;
        tint = Color.WHITE;

        name = "";

        pms[0] = pixmap; width = pixmap.getWidth(); height = pixmap.getHeight();
    }

    public Renderable(String name, Texture texture) {
        pms = new Pixmap[8];
        opacity = 1f;
        tint = Color.WHITE;

        this.name = name;

        setTexture(texture);
    }

    /**
     * Set the texture that this object uses to render
     *
     * @param texture texture to render
     */
    public void setTexture(Texture texture) {
        if (pms[0] != null && name.isEmpty()) {
            dispose();
        }

        String key = name + ";" + Math.round(opacity * 100) + ";" + tint.getRed() + ";" + tint.getGreen() + ";" + tint.getBlue() + ";" + tint.getAlpha();
        if (!name.isEmpty()) {
            if (textures.containsKey(key)) {
                pms = textures.get(key);
                return;
            }
        }

        this.texture = texture;

        TextureData td = texture.getTextureData();
        td.prepare();

        Pixmap pm_up = td.consumePixmap();
        pms[0] = pm_up;

        width = pm_up.getWidth();
        height = pm_up.getHeight();

        for (int i = 0; i < 4; i++) {
            int nw = i % 2 == 0 ? width : height;
            int nh = i % 2 == 0 ? height : width;

            Pixmap pm = new Pixmap(nw + (outline ? 2 : 0), nh + (outline ? 2 : 0), Pixmap.Format.RGBA8888);
            for (int x = 0; x < nw; x++) {
                for (int y = 0; y < nh; y++) {
                    int sx = i >= 4 ? width - x : x;

                    int color;

                    switch (i) {
                        case 0:
                            color = pm_up.getPixel(x, y);
                            break;
                        case 1:
                            color = pm_up.getPixel(y, height - x);
                            break;
                        case 2:
                            color = pm_up.getPixel(width - x, height - y);
                            break;
                        case 3:
                        default:
                            color = pm_up.getPixel(width - y, x);
                            break;
                    }

                    int alpha = color & 0xFF; int blue = (color >> 8) & 0xFF; int green = (color >> 16) & 0xFF; int red = (color >> 24) & 0xFF;
                    alpha = Math.round(alpha * opacity);
                    blue = Math.round(blue / 256.0f * tint.getBlue());
                    green = Math.round(green / 256.0f * tint.getGreen());
                    red = Math.round(red / 256.0f * tint.getRed());

                    int finalColor = (red << 24) + (green << 16) + (blue << 8) + alpha;

                    pm.drawPixel(sx + (outline ? 1 : 0), y + (outline ? 1 : 0), finalColor);
                }
            }

//            if (outline) {
//                for (int x = 0; x < nw + 2; x++) {
//                    for (int y = 0; y < nh + 2; y++) {
//
//                    }
//                }
//            }

            pms[i] = pm;
        }

        if (!name.isEmpty()) {
            textures.put(key, pms.clone());
        }
    }

    public void setOpacity(float alpha) {
        if (!Utilities.equalFloats(alpha, opacity)) {
            opacity = alpha;

            setTexture(texture);
        }
    }

    public void setTint(Color tint) {
        if (!tint.equals(this.tint)) {
            this.tint = tint;

            setTexture(texture);
        }
    }

    public float getOpacity() {
        return opacity;
    }

    public void setOutline(boolean outline) {
        this.outline = outline;
    }

    public void setOutlineColor(Color outlineColor) {
        this.outlineColor = outlineColor;
    }

    /**
     * Render this textured object onto the given <code>Pixmap</code>, such that its center will be at coordinates
     * <code>(x, y)</code>, where <code>(x, y)</code> defines the center of the object in Physics coordinates, scaled
     * appropriately depending on the given width. The orientation value should be from 0 to 3 if the texture is not
     * meant to be flipped, where 0 is default and each increment is a 90 degree rotation clockwise. Orientations of 4
     * or more represent the same kind of rotation, but flipped across the y-axis.
     *
     * @param dest Pixmap to render this texture on
     * @param x the center x-coordinate of this object in world coordinates
     * @param y the center y-coordinate of this object in world coordinates
     * @param orientation orientation to render this texture in
     * @param width width of image in pixels on the destination Pixmap
     */
    public void render(Pixmap dest, float x, float y, int orientation, float width) {
        y = -y;

        int pixWidth = orientation % 2 == 0 ? this.width : this.height;
        int pixHeight = orientation % 2 == 0 ? this.height : this.width;

        float ratio = scale * width / this.width;

        float nWidth = ratio * pixWidth;
        float nHeight = ratio * pixHeight;

        int nx = (int) Math.round(scale * x + dest.getWidth() / 2f - nWidth / 2f);
        int ny = (int) Math.round(scale * y + dest.getHeight() / 2f - nHeight / 2f);

        dest.drawPixmap(pms[orientation], 0, 0, pixWidth, pixHeight, nx, ny,
                (int) Math.round(nWidth), (int) Math.round(nHeight));
    }

    /**
     * Render this textured object onto the given <code>Pixmap</code>, such that its center will be at coordinates
     * <code>(x, y)</code>, where <code>(x, y)</code> defines the center of the object in Physics coordinates, scaled
     * to be exactly <code>width</code> pixels wide and <code>height</code> pixels tall.
     * The orientation value should be from 0 to 3 if the texture is not meant to be flipped, where 0 is default and
     * each increment is a 90 degree rotation clockwise. Orientations of 4 or more represent the same kind of rotation,
     * but flipped across the y-axis.
     *
     * @param dest Pixmap to render this texture on
     * @param x the center x-coordinate of this object in world coordinates
     * @param y the center y-coordinate of this object in world coordinates
     * @param orientation orientation to render this texture in
     * @param width width of image in pixels on the destination Pixmap
     * @param height height of image in pixels on the destination Pixmap
     */
    public void render(Pixmap dest, float x, float y, int orientation, float width, float height) {
        y = -y;

        int pixWidth = orientation % 2 == 0 ? this.width : this.height;
        int pixHeight = orientation % 2 == 0 ? this.height : this.width;

        int nWidth = (int) Math.round(scale * width);
        int nHeight = (int) Math.round(scale * height);
        int nx = (int) Math.round(scale * x + dest.getWidth() / 2f - nWidth / 2f);
        int ny = (int) Math.round(scale * y + dest.getHeight() / 2f - nHeight / 2f);

        dest.drawPixmap(pms[orientation], 0, 0, pixWidth, pixHeight, nx, ny, nWidth, nHeight);
    }

    /**
     * Render this textured object onto the given <code>Pixmap</code>, such that its center will be at coordinates
     * <code>(x, y)</code>, where <code>(x, y)</code> defines the center of the object in Physics coordinates, scaled
     * to be exactly <code>width</code> pixels wide and <code>height</code> pixels tall.
     * The orientation value should be from 0 to 3 if the texture is not meant to be flipped, where 0 is default and
     * each increment is a 90 degree rotation clockwise. Orientations of 4 or more represent the same kind of rotation,
     * but flipped across the y-axis.
     *
     * @param dest Pixmap to render this texture on
     * @param x the center x-coordinate of this object in world coordinates
     * @param y the center y-coordinate of this object in world coordinates
     * @param sx the top-left x-coordinate of the region of the source Pixmap to draw
     * @param sy the top-left y-coordinate of the region of the source Pixmap to draw
     * @param sWidth the width of the region of the source Pixmap to draw
     * @param sHeight the height of the region of the source Pixmap to draw
     * @param orientation orientation to render this texture in
     * @param fWidth width of image in pixels on the destination Pixmap
     */
    public void render(Pixmap dest, float x, float y, int sx, int sy, int sWidth, int sHeight,
                       int orientation, float fWidth) {
        y = -y;

        int pixWidth = orientation % 2 == 0 ? sWidth : sHeight;
        int pixHeight = orientation % 2 == 0 ? sHeight : sWidth;

        float ratio = scale * fWidth / sWidth;

        float nWidth = ratio * pixWidth;
        float nHeight = ratio * pixHeight;

        int nx = (int) Math.round(scale * x + dest.getWidth() / 2f - nWidth / 2f);
        int ny = (int) Math.round(scale * y + dest.getHeight() / 2f - nHeight / 2f);

        dest.drawPixmap(pms[orientation], sx, sy, pixWidth, pixHeight, nx, ny,
                (int) Math.round(nWidth), (int) Math.round(nHeight));
    }

    /**
     * Dispose of all resources associated with this object
     */
    public void dispose() {
        if (name.isEmpty()) {
            for (Pixmap pm : pms) {
                if (pm != null && !pm.isDisposed()) {
                    pm.dispose();
                }
            }
        }
    }

    public static void disposeAll() {
        for (Pixmap[] pms : textures.values()) {
            for (Pixmap pm : pms) {
                if (pm != null && !pm.isDisposed()) {
                    pm.dispose();
                }
            }
        }
    }

    public void saveFrame(String frameName) {
        HashMap<String, Object> frame = new HashMap<>();


    }

    public void renderRotated(Pixmap dest, float x, float y,
                              float angle, float width){
        y = -y;

        Pixmap rotated = Utilities.rotatePixmap(pms[0],angle);
        int pixWidth =rotated.getWidth();
        int pixHeight = rotated.getHeight();

        float ratio = scale * width / this.width;

        float nWidth = ratio * pixWidth;
        float nHeight = ratio * pixHeight;

        int nx = (int) Math.round(scale * x + dest.getWidth() / 2f - nWidth / 2f);
        int ny = (int) Math.round(scale * y + dest.getHeight() / 2f - nHeight / 2f);
        dest.drawPixmap(rotated,0,0,pixWidth,pixHeight,nx,ny,(int)Math.round(nWidth),(int)Math.round(nHeight));

    }
}
