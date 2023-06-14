package com.genmiracle.flightofvanity.level;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.JsonValue;
import com.genmiracle.flightofvanity.assets.AssetDirectory;
import com.genmiracle.flightofvanity.graphics.GameCanvas;
import com.genmiracle.flightofvanity.graphics.cube.Renderable;
import com.genmiracle.flightofvanity.physics.PhysicsController;


import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;

public class World {

    Side[] sides = new Side[6];

    Texture[] sideTextures = new Texture[6];

    protected Texture background;

    private JsonValue constants;

    private AssetDirectory internal;

    protected GameCanvas canvas;

    protected Rectangle bounds;

    protected Vector2 scale;

    private ArrayList<ArrayList<Float>> platformLocations;

    //internal -- assets.json
    //constants -- levelTesting.json
    public void gatherTextures(){

        //constants = directory.getEntry("testingLevel", JsonValue.class);

        Platform.textures = new Pixmap[20];
        for (int i = 0; i<20; i++){
            Texture currTexture = internal.getEntry("platform" + i, Texture.class);
            currTexture.getTextureData().prepare();
            Platform.textures[i] = currTexture.getTextureData().consumePixmap();
        }

    }

    public void setPlatformLocations(JsonValue platformJson, int side) {
        for (int i = 0; i < platformJson.size; i++) {
            ArrayList<Float> plat = new ArrayList<>();
            float[] fa = platformJson.get(i).asFloatArray();

            for (int j = 0; j < fa.length; j++) {
                float f = fa[j];

                plat.add(f);
            }
            plat.add(side * 1f);
            platformLocations.add(plat);
        }
    }

    public ArrayList<ArrayList<Float>> getPlatformLocations(){
        return platformLocations;
    }

    /** Json: "platforms": [[x,y, width, height, side]] */
    public void loadPlatformsArray(ArrayList<ArrayList<Float>> platforms){

        for(ArrayList<Float> platform: platforms){
            int side = platform.get(6).intValue();
            float x = platform.get(0);
            float y = platform.get(1);
            float width = platform.get(2);
            float height = platform.get(3);
            int constructValue = platform.get(4).intValue();
            int partNum = platform.get(5).intValue();
            sides[side].addPlatform(x,y,width,height,side,partNum, constructValue);
        }
    }
    public void gatherSideAssets(JsonValue base) {
//        internal = new AssetDirectory("assets.json");
//        internal.loadAssets();
//        internal.finishLoading();
        gatherTextures();

        platformLocations = new ArrayList<>();

        JsonValue sideComponents = base.get("sides");

        int side = 0;
        for(JsonValue s : sideComponents){
            setPlatformLocations(s.get("platformLocations"), side);
            side++;
        }

        loadPlatformsArray(platformLocations);

    }

    public void setAssetDirectory(AssetDirectory asset){
        this.internal = asset;
    }

    public Texture getTextureOfSide(int i) {
        return sides[i].getTexture();
    }

    public Pixmap getPixmapOfSide(int i) {
        return sides[i].getPixmap();
    }

    public void setBackgroundTextureOfSide(int i, Texture texture) {
        sides[i].setBackgroundTexture(texture);
    }

    public void dispose() {
        for (Side side : sides) {
            side.dispose();
        }

        Platform.dispose();
    }

    public void setSides(JsonValue base, int sideWidth, int sideHeight){
        for(int i = 0; i< 6; i++){
            sides[i] = new Side(sideWidth, sideHeight);
        }

        gatherSideAssets(base);
    }

}
class Side{
    public int side;
    public static int sideWidth;
    public static int sideHeight;
    ArrayList<Platform> platforms;
    private Pixmap pm;
    private Pixmap backgroundTexture;

    private boolean dirty;
    private Texture texture;

    public Side(int sideWidth, int sideHeight) {
        this.platforms = new ArrayList<>();
        this.sideWidth = sideWidth;
        this.sideHeight = sideHeight;

        dirty = true;
        pm = new Pixmap(sideWidth, sideHeight, Pixmap.Format.RGBA8888);
        backgroundTexture = null;
        texture = new Texture(pm);
    }
    public void setBackgroundTexture(Texture backTexture) {
        TextureData td = backTexture.getTextureData();
        td.prepare();
        backgroundTexture = td.consumePixmap();
    }

    public void addPlatform(float x, float y, float width, float height, int side, int partNum, int constructValue){
        float[] pos = {x,y};
        platforms.add(new Platform(height, width, pos, side, partNum, constructValue));
        dirty = true;

    }
    public Texture getTexture() {
        if (!dirty) {
            return texture;
        }
        dirty = false;

        getPixmap();
        texture.draw(pm, 0, 0);
        return texture;
    }

    public Pixmap getPixmap() {
        if (backgroundTexture != null) {
            pm.drawPixmap(backgroundTexture, 0, 0, backgroundTexture.getWidth(), backgroundTexture.getHeight(),
                    0, 0, sideWidth, sideWidth);
        }

        if (dirty) {
            for (Platform plat : platforms) {
                float[] pos = plat.pos;

                plat.render(pm);
            }

            backgroundTexture.drawPixmap(pm, 0, 0);
            dirty = false;
        }


        return pm;
    }
    public boolean isDirty() {
        return dirty;
    }

    public void dispose() {
        pm.dispose();

        if (backgroundTexture != null) {
            backgroundTexture.dispose();
        }
    }
}
class Platform {
    public float height;
    public float width;
    public float[] pos;

    public Renderable part;

    public static Pixmap[] textures;
    public static Pixmap textureLeft;

    public static Pixmap textureMiddleLeft;

    public static Pixmap textureMiddleRight;

    public static Pixmap textureRight;

    private static PhysicsController physicsCon=PhysicsController.getInstance();


    public Platform(float height, float width, float[] pos, int side, int partNum, int constructValue) {
        this.height = height;
        this.width = width;
        this.pos = pos;
        physicsCon.addPlatform(side, pos[0],pos[1], width, height, constructValue);
        part = new Renderable(textures[partNum]);
    }


    public void render(Pixmap pm) {
            part.render(pm,  pos[0], pos[1], 0, width, height);
    }

    public static void dispose() {
        //texturePixmap.dispose();
        for (Pixmap t : textures){
            if (t!= null) {
                t.dispose();
            }
        }
    }
}


