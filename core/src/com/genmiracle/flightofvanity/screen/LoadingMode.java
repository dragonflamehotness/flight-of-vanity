package com.genmiracle.flightofvanity.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.controllers.ControllerMapping;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.genmiracle.flightofvanity.GDXRoot;
import com.genmiracle.flightofvanity.assets.AssetDirectory;
import com.genmiracle.flightofvanity.graphics.GameCanvas;
import com.genmiracle.flightofvanity.util.ScreenListener;

/**
 * Class that provides a loading screen for the state of the game.
 *
 * This class provides a basic template for a loading
 * screen to be used at the start of the game or between levels.  Feel free to adopt
 * this to your needs.
 *
 * You will note that this mode has some textures that are not loaded by the AssetManager.
 * You are never required to load through the AssetManager.  But doing this will block
 * the application.  That is why we try to have as few resources as possible for this
 * loading screen.
 */
public class LoadingMode implements Screen {
    // There are TWO asset managers. One to load the loading screen. The other to load the assets.
    /** Internal assets for this loading screen */
    private AssetDirectory internal;
    //The actual assets to be loaded
    private AssetDirectory assets;

    /** Background texture for start-up */
    private Texture background;


//    // statusBar is a "texture atlas." Break it up into parts.
//    /** Left cap to the status background (grey region) */
//    private TextureRegion statusBkgLeft;
//
//    /** Middle portion of the status background (grey region) */
//    private TextureRegion statusBkgMiddle;
//
//    /** Right cap to the status background (grey region) */
//    private TextureRegion statusBkgRight;
//
//    /** Left cap to the status foreground (colored region) */
//    private TextureRegion statusFrgLeft;
//
//    /** Middle portion of the status foreground (colored region) */
//    private TextureRegion statusFrgMiddle;
//
//    /** Right cap to the status foreground (colored region) */
//    private TextureRegion statusFrgRight;

    /** Default budget for asset loader (do nothing but load 60 fps) */
    private static int DEFAULT_BUDGET = 15;
    /** Standard window size (for scaling) */
    private static int STANDARD_WIDTH  = 800;
    /** Standard window height (for scaling) */
    private static int STANDARD_HEIGHT = 700;
    /** Ratio of the bar width to the screen */
    private static float BAR_WIDTH_RATIO  = 0.66f;
    /** Ration of the bar height to the screen */
    private static float BAR_HEIGHT_RATIO = 0.25f;
    /** Height of the progress bar */
    private static float BUTTON_SCALE  = 0.75f;

    /** Reference to GameCanvas created by the root */
    private GameCanvas canvas;
    /** Listener that will update the player mode when we are done */
    private GDXRoot listener;

    /** The width of the progress bar */
    private int width;
    /** The y-coordinate of the center of the progress bar */
    private int centerY;
    /** The x-coordinate of the center of the progress bar */
    private int centerX;
    /** The height of the canvas window (necessary since sprite origin != screen origin) */
    private int heightY;
    /** Scaling factor for when the student changes the resolution. */
    private float scale;

    /** Current progress (0 to 1) of the asset manager */
    private float progress;

    /** The amount of time to devote to loading assets (as opposed to on screen hints, etc.) */
    private int   budget;

    /** Whether or not this player mode is still active */
    private boolean active;

    private Stage stage;

    private Viewport viewport;

    private BitmapFont displayFont;

    private Texture progressBarBackground;

    private Texture progressBarFront;

    private ProgressBar progressBar;

    private Image mirror;
    /**
     * Returns the budget for the asset loader.
     *
     * The budget is the number of milliseconds to spend loading assets each animation
     * frame.  This allows you to do something other than load assets.  An animation
     * frame is ~16 milliseconds. So if the budget is 10, you have 6 milliseconds to
     * do something else.  This is how game companies animate their loading screens.
     *
     * @return the budget in milliseconds
     */
    public int getBudget() {
        return budget;
    }

    /**
     * Sets the budget for the asset loader.
     *
     * The budget is the number of milliseconds to spend loading assets each animation
     * frame.  This allows you to do something other than load assets.  An animation
     * frame is ~16 milliseconds. So if the budget is 10, you have 6 milliseconds to
     * do something else.  This is how game companies animate their loading screens.
     *
     * @param millis the budget in milliseconds
     */
    public void setBudget(int millis) {
        budget = millis;
    }


    /**
     * Returns the asset directory produced by this loading screen
     *
     * This asset loader is NOT owned by this loading scene, so it persists even
     * after the scene is disposed.  It is your responsibility to unload the
     * assets in this directory.
     *
     * @return the asset directory produced by this loading screen
     */
    public AssetDirectory getAssets() {
        return assets;
    }

    /**
     * Creates a LoadingMode with the default budget, size and position.
     *
     * @param file  	The asset directory to load in the background
     * @param canvas 	The game canvas to draw to
     */
    public LoadingMode(String file, GameCanvas canvas, Viewport viewport) {
        this(file, canvas, DEFAULT_BUDGET);
        this.viewport = viewport;
        this.stage = new Stage(this.viewport);
    }

    /**
     * Creates a LoadingMode with the default size and position.
     *
     * The budget is the number of milliseconds to spend loading assets each animation
     * frame.  This allows you to do something other than load assets.  An animation
     * frame is ~16 milliseconds. So if the budget is 10, you have 6 milliseconds to
     * do something else.  This is how game companies animate their loading screens.
     *
     * @param file  	The asset directory to load in the background
     * @param canvas 	The game canvas to draw to
     * @param millis The loading budget in milliseconds
     */
    public LoadingMode(String file, GameCanvas canvas, int millis) {
        this.canvas = canvas;
        budget = millis;


        // We need these files loaded immediately
        internal = new AssetDirectory("loading.json");
        internal.loadAssets();
        internal.finishLoading();

        // Load the next two images immediately.
        background = internal.getEntry("background", Texture.class);
        background.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
//        statusBar = internal.getEntry("progress", Texture.class);
//
//        // Break up the status bar texture into regions
//        statusBkgLeft = internal.getEntry("progress.backleft", TextureRegion.class);
//        statusBkgRight = internal.getEntry("progress.backright", TextureRegion.class);
//        statusBkgMiddle = internal.getEntry("progress.background", TextureRegion.class);
//
//        statusFrgLeft = internal.getEntry("progress.foreleft", TextureRegion.class);
//        statusFrgRight = internal.getEntry("progress.foreright", TextureRegion.class);
//        statusFrgMiddle = internal.getEntry("progress.foreground", TextureRegion.class);

        progressBarBackground = internal.getEntry("progress-bar-background", Texture.class);
        progressBarFront = internal.getEntry("progress-bar-front", Texture.class);
        ProgressBar.ProgressBarStyle style = new ProgressBar.ProgressBarStyle();
//        style.knob = new TextureRegionDrawable(new TextureRegion(progressBarFront));
        style.knobBefore = new TextureRegionDrawable(new TextureRegion(progressBarFront));
        style.knobAfter = new TextureRegionDrawable(new TextureRegion(progressBarBackground));
        style.background = new TextureRegionDrawable(new TextureRegion(progressBarBackground));

        progressBar = new ProgressBar(0,1,0.01f,false,style);
        progressBar.setSize(560,24);

        mirror = new Image(internal.getEntry("progress-bar-mirror", Texture.class));

        // No progress so far.
        progress = 0;

        //Gdx.input.setInputProcessor(this);

        // Start loading the real assets
        assets = new AssetDirectory(file);
        assets.loadAssets();
        active = true;
    }

    /**
     * Called when this screen should release all resources.
     */
    public void dispose() {
        internal.unloadAssets();
        internal.dispose();
    }

    /**
     * Update the status of this player mode.
     *
     * We prefer to separate update and draw from one another as separate methods, instead
     * of using the single render() method that LibGDX does.  We will talk about why we
     * prefer this in lecture.
     *
     * @param delta Number of seconds since last animation frame
     */
    private void update(float delta) {

            assets.update(budget);
            this.progress = assets.getProgress();
            if (progress >= 1.0f) {
                this.progress = 1.0f;
            }
            progressBar.setValue(progress);
            stage.act(delta);
    }

    /**
     * Draw the status of this player mode.
     *
     * We prefer to separate update and draw from one another as separate methods, instead
     * of using the single render() method that LibGDX does.  We will talk about why we
     * prefer this in lecture.
     */
    private void draw() {

        canvas.beginScaled();

        canvas.draw(background, Color.WHITE, 0, 0, canvas.getWidth(), canvas.getHeight());
//            drawProgress(canvas);
        canvas.end();

        stage.draw();
    }

    /**
     * Updates the progress bar according to loading progress
     *
     * The progress bar is composed of parts: two rounded caps on the end,
     * and a rectangle in a middle.  We adjust the size of the rectangle in
     * the middle to represent the amount of progress.
     *
     * @param canvas The drawing context
     */
//    private void drawProgress(GameCanvas canvas) {
//        canvas.draw(statusBkgLeft,   Color.WHITE, centerX-width/2, centerY,
//                scale*statusBkgLeft.getRegionWidth(), scale*statusBkgLeft.getRegionHeight());
//        canvas.draw(statusBkgRight,  Color.WHITE,centerX+width/2-scale*statusBkgRight.getRegionWidth(), centerY,
//                scale*statusBkgRight.getRegionWidth(), scale*statusBkgRight.getRegionHeight());
//        canvas.draw(statusBkgMiddle, Color.WHITE,centerX-width/2+scale*statusBkgLeft.getRegionWidth(), centerY,
//                width-scale*(statusBkgRight.getRegionWidth()+statusBkgLeft.getRegionWidth()),
//                scale*statusBkgMiddle.getRegionHeight());
//
//        canvas.draw(statusFrgLeft,   Color.WHITE,centerX-width/2, centerY,
//                scale*statusFrgLeft.getRegionWidth(), scale*statusFrgLeft.getRegionHeight());
//        if (progress > 0) {
//            float span = progress*(width-scale*(statusFrgLeft.getRegionWidth()+statusFrgRight.getRegionWidth()))/2.0f;
//            canvas.draw(statusFrgRight,  Color.WHITE,centerX-width/2+scale*statusFrgLeft.getRegionWidth()+span, centerY,
//                    scale*statusFrgRight.getRegionWidth(), scale*statusFrgRight.getRegionHeight());
//            canvas.draw(statusFrgMiddle, Color.WHITE,centerX-width/2+scale*statusFrgLeft.getRegionWidth(), centerY,
//                    span, scale*statusFrgMiddle.getRegionHeight());
//        } else {
//            canvas.draw(statusFrgRight,  Color.WHITE,centerX-width/2+scale*statusFrgLeft.getRegionWidth(), centerY,
//                    scale*statusFrgRight.getRegionWidth(), scale*statusFrgRight.getRegionHeight());
//        }
//    }

    // ADDITIONAL SCREEN METHODS
    /**
     * Called when the Screen should render itself.
     *
     * We defer to the other methods update() and draw().  However, it is VERY important
     * that we only quit AFTER a draw.
     *
     * @param delta Number of seconds since last animation frame
     */
    public void render(float delta) {
        if (active) {
            update(delta);
            draw();

            // We are ready, notify our listener
            if (progress == 1 && listener != null) {
                listener.exitScreen(this, ScreenListener.ExitCode.MENU);
            }
        }
    }

    /**
     * Called when the Screen is resized.
     *
     * This can happen at any point during a non-paused state but will never happen
     * before a call to show().
     *
     * @param width  The new width in pixels
     * @param height The new height in pixels
     */
    public void resize(int width, int height) {
        // Compute the drawing scale
        float sx = ((float)width)/STANDARD_WIDTH;
        float sy = ((float)height)/STANDARD_HEIGHT;
        scale = (sx < sy ? sx : sy);

        this.width = (int)(BAR_WIDTH_RATIO*width);
        centerY = (int)(BAR_HEIGHT_RATIO*height);
        centerX = width/2;
        heightY = height;
        viewport.setWorldSize(width, height);
        stage.getViewport().update(width, height, true);
        canvas.setSize(width,height);
    }



    @Override
    public void pause() {

    }


    @Override
    public void resume() {

    }

    /**
     * Called when this screen becomes the current screen for a {@link GDXRoot}.
     */
    @Override
    public void show() {
        // Useless if called in outside animation loop
        progressBar.setAnimateDuration(0.01f);
        progressBar.setPosition(stage.getWidth()/2 - progressBar.getWidth()/2,stage.getHeight()/2-progressBar.getHeight()/2 - 30f);
        mirror.setPosition(stage.getWidth()/2 - progressBar.getWidth()/2,stage.getHeight()/2-progressBar.getHeight()/2 - 30f);
        stage.addActor(progressBar);
        stage.addActor(mirror);
        active = true;
    }

    /**
     * Called when this screen is no longer the current screen for a {@link GDXRoot}.
     */
    @Override
    public void hide() {
        // Useless if called in outside animation loop
        active = false;
    }

    /**
     * Sets the ScreenListener for this mode
     *
     * The ScreenListener will respond to requests to quit.
     */
    public void setScreenListener(GDXRoot listener) {
        this.listener = listener;
    }

}
