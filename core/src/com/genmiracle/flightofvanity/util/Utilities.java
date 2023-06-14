package com.genmiracle.flightofvanity.util;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;

public class Utilities {
    public static final float EPSILON = 0.00001f;
    public static final int BASE_WIDTH = 960;
    public static final int BASE_HEIGHT = 540;
    public static final float TILE_SIZE = 5f;
    public static final int[][] NEIGHBORS = new int[][] {
            new int[] {0, 1},
            new int[] {1, 0},
            new int[] {0, -1},
            new int[] {-1, 0},
    };

    public static boolean equalFloats(float a, float b) {
        return Math.abs(a - b) < EPSILON;
    }
    public static float easeInOut(float start, float end, float t) {
        if (t <= 0) return start;
        if (t >= 1) return end;

        return start + (end - start) * t * t * (3f - 2f * t);
    }

    public static float bezier(float start, float end, float t, float p) {
        if (t <= 0) return start;
        if (t >= 1) return end;

        float nt = 1 - t;

        return nt * nt * start + 2 * nt * t * p + t * t * end;
    }

    public static Vector3 bezier(Vector3 start, Vector3 end, Vector3 v, float t, Vector3 p) {
        if (t <= 0) return v.set(start);
        if (t >= 1) return v.set(end);

        return v.set(bezier(start.x, end.x, t, p.x), bezier(start.y, end.y, t, p.y), bezier(start.z, end.z, t, p.z));
    }

    public static float averageFour(float v11, float v12, float v21, float v22, float xRat, float yRat) {
        return v11 * xRat * yRat + v12 * xRat * (1 - yRat) + v21 * (1 - xRat) * yRat + v22 * (1 - xRat) * (1 - yRat);
    }

    /**
     * Draw the region of the {@link Pixmap} {@code source} that starts at {@code sx}, {@code sy} at the top-left, with
     * width {@code sw} and height {@code sh} onto the region in {@code dest} that starts at {@code} dx, {@ode} dy,
     * with width {@code dw} and height {@code dh}. The image will be rotated {@code angle} (in radians) clockwise, with
     * {@code blending} dictating whether to draw the average of the four pixels, or just the closest one
     *
     * @param dest {@link Pixmap} to draw on
     * @param source {@link Pixmap} to draw from
     * @param dx x-coordinate (from center) of the destination region
     * @param dy y-coordinate (from center) of the destination region
     * @param dw width of the destination region
     * @param dh height of the destination region
     * @param sx x-coordinate (from top-left) of the source region
     * @param sy y-coordinate (from top-left) of the source region
     * @param sw width of the source region
     * @param sh height of the source region
     * @param angle angle (in radians) the source region is rotated clockwise
     * @param blending whether to use the average of four pixels or the closest one
     */
    public static void drawPixmapRotated(Pixmap dest, Pixmap source, int dx, int dy, int dw, int dh,
                                         int sx, int sy, int sw, int sh, float angle, boolean blending) {
        float cos = (float) Math.cos(angle);
        float sin = (float) Math.sin(angle);

        for (int x = dx - (int) Math.floor(dw / 2f); x < dx + (int) Math.ceil(dw / 2f); x++) {
            for (int y = dy - (int) Math.floor(dh / 2f); y < dy + (int) Math.ceil(dh / 2f); y++) {
                float oxRat = (x - dx) / (dw * 1.0f);
                float oyRat = (y - dy) / (dh * 1.0f);

                float ox = sw * (oxRat + 0.5f) + sx;
                float oy = sh * (oyRat + 0.5f) + sy;

                int dxR = (int) Math.round(((x - dx) * cos - (y - dy) * sin + dx));
                int dyR = (int) Math.round(((x - dx) * sin + (y - dy) * cos + dy));

                if (blending) {
                    int ox1 = (int) Math.floor(ox); int ox2 = (int) Math.ceil(ox);
                    int oy1 = (int) Math.floor(oy); int oy2 = (int) Math.ceil(oy);

                    int o11 = source.getPixel(ox1, oy1);
                    int o11Red = o11 >>> 24; int o11Blue = (o11 & 0xFF0000) >>> 16;
                    int o11Green = (o11 & 0xFF00) >>> 8; int o11Alpha = o11 & 0xFF;

                    int o21 = source.getPixel(ox2, oy1);
                    int o21Red = o21 >>> 24; int o21Blue = (o21 & 0xFF0000) >>> 16;
                    int o21Green = (o21 & 0xFF00) >>> 8; int o21Alpha = o21 & 0xFF;

                    int o22 = source.getPixel(ox2, oy2);
                    int o22Red = o22 >>> 24; int o22Blue = (o22 & 0xFF0000) >>> 16;
                    int o22Green = (o22 & 0xFF00) >>> 8; int o22Alpha = o22 & 0xFF;

                    int o12 = source.getPixel(ox1, oy2);
                    int o12Red = o12 >>> 24; int o12Blue = (o12 & 0xFF0000) >>> 16;
                    int o12Green = (o12 & 0xFF00) >>> 8; int o12Alpha = o12 & 0xFF;

                    float oxRat1 = (ox2 - ox); float oyRat1 = (oy2 - oy);

                    int red = Math.round(averageFour(o11Red, o12Red, o21Red, o22Red, oxRat1, oyRat1));
                    int blue = Math.round(averageFour(o11Blue, o12Blue, o21Blue, o22Blue, oxRat1, oyRat1));
                    int green = Math.round(averageFour(o11Green, o12Green, o21Green, o22Green, oxRat1, oyRat1));
                    int alpha = Math.round(averageFour(o11Alpha, o12Alpha, o21Alpha, o22Alpha, oxRat1, oyRat1));

                    dest.setColor(red, blue, green, alpha);
                    dest.drawPixel(dxR, dyR);
                } else {
//                    dest.setColor(Color.BLACK);
                    dest.setColor(source.getPixel(Math.round(ox), Math.round(oy)));
                    dest.drawPixel(dxR, dyR);
                }
            }
        }
    }

    public static Pixmap rotatePixmap(Pixmap source, float angle) {
        int srcWidth = source.getWidth();
        int srcHeight = source.getHeight();

        // calculate the center of the source pixmap
        float cx = srcWidth / 2f;
        float cy = srcHeight / 2f;

        // calculate the sine and cosine of the rotation angle
        float cos = MathUtils.cos(angle);
        float sin = MathUtils.sin(angle);

        int destWidth = (int) Math.round(Math.abs(cos * srcWidth) + Math.abs(sin * srcHeight));
        int destHeight = (int) Math.round(Math.abs(sin * srcWidth) + Math.abs(cos * srcHeight));

        Pixmap dest = new Pixmap(destWidth, destHeight, Pixmap.Format.RGBA8888);

        // iterate over every pixel in the destination pixmap
        for (int y = 0; y < destHeight; y++) {
            for (int x = 0; x < destWidth; x++) {
                float dx = x - destWidth / 2f;
                float dy = y - destHeight / 2f;

                // calculate the position of the current pixel in the source pixmap
                int srcX = Math.round(cos * dx - sin * dy + cx);
                int srcY = Math.round(sin * dx + cos * dy + cy);

                // check if the current pixel is inside the source pixmap
                if (srcX >= 0 && srcX < srcWidth && srcY >= 0 && srcY < srcHeight) {

                    // set the color of the current pixel in the destination pixmap
                    int color = source.getPixel(srcX, srcY);
//                    dest.setColor(Color.BLACK);
//                    dest.drawPixel(x, y);
                    dest.drawPixel(x, y, color);
                } else {
//                    dest.setColor(Color.BLACK);
//                    dest.drawPixel(x, y);
                    dest.drawPixel(x, y, 0);
                }
            }
        }

        return dest;
    }

    public static void drawPixmapCentered(Pixmap dest, Pixmap source, int dx, int dy, int dw, int dh,
                                          int sx, int sy, int sw, int sh) {
        float hw = dw / 2f; float hh = dh / 2f;

        dest.drawPixmap(source, sx, sy, sw, sh, dx - (int) Math.floor(hw),
                dy - (int) Math.floor(hh), dw, dh);
    }

    public static Pixmap getPixmapWithTint(Pixmap source, float r, float g, float b, float a) {
        int width = source.getWidth(); int height = source.getHeight();

        Pixmap p = new Pixmap(width, height, Pixmap.Format.RGBA8888);

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int color = source.getPixel(x, y);

                int ca = color & 0xFF; int cb = (color >> 8) & 0xFF; int cg = (color >> 16) & 0xFF; int cr = (color >> 24) & 0xFF;
                int na = Math.round(ca * a); int nb = Math.round(cb * b); float ng = Math.round(cg * g); float nr = Math.round(cr * r);

                p.setColor(nr / 255f, ng / 255f, nb / 255f, na / 255f);
                p.drawPixel(x, y);
            }
        }

        return p;
    }
}
