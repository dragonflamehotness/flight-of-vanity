package com.genmiracle.flightofvanity.screen;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.genmiracle.flightofvanity.GDXRoot;
import com.genmiracle.flightofvanity.assets.AssetDirectory;
import com.genmiracle.flightofvanity.audio.AudioController;
import com.genmiracle.flightofvanity.graphics.GameCanvas;
import com.genmiracle.flightofvanity.util.ScreenListener;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.controllers.ControllerMapping;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.genmiracle.flightofvanity.GDXRoot;
import com.genmiracle.flightofvanity.assets.AssetDirectory;
import com.genmiracle.flightofvanity.graphics.GameCanvas;
import com.genmiracle.flightofvanity.util.ScreenListener;
import com.genmiracle.flightofvanity.util.Utilities;

import javax.swing.text.View;
import java.awt.*;


public class SettingMode implements Screen, InputProcessor {

    private final AudioController ac;
    private AssetDirectory assets;

    /**
     * Background texture for start-up
     */
    private Texture background;

    private Texture sound;
    private Texture music;

    private GDXRoot listener;
    private boolean pressedSound;
    private boolean pressedMusic;
    private boolean pressedControls;
    private boolean active;

    private int centerX;

    private int centerY;
    private float scale;
    private int heightY;


    private BitmapFont displayFont;
    private Stage stage;
    private TextButton control;
    private TextButton back;

    private Label soundLabel;

    private Label musicLabel;
    private Skin skin;
    private Texture slider_background, slider_knob, knob_background;
    private ProgressBar bar;
    private Slider musicBar;
    private Slider soundBar;

    /** -1 -> nothing pressed, 0 -> setting*/
    private int pressedState;

    /**
     * Default budget for asset loader (do nothing but load 60 fps)
     */
    private static int DEFAULT_BUDGET = 15;
    /**
     * Standard window size (for scaling)
     */
    private static int STANDARD_WIDTH = 800;
    /**
     * Standard window height (for scaling)
     */
    private static int STANDARD_HEIGHT = 700;
    /**
     * Ratio of the bar width to the screen
     */
    private static float BAR_WIDTH_RATIO = 0.66f;
    /**
     * Ration of the bar height to the screen
     */
    private static float BAR_HEIGHT_RATIO = 0.25f;
    /**
     * Height of the progress bar
     */
    private static float BUTTON_SCALE = 1f;

    /**
     * Reference to GameCanvas created by the root
     */
    private GameCanvas canvas;

    private static float MAX_VOLUME = 100f;

    private float count = 1f;

    private Viewport viewport;
    private boolean firstTime = true;

//    private ScreenListener listener;

    public SettingMode(GameCanvas canvas, Viewport viewport, AudioController audioController) {
        this.canvas = canvas;
        this.viewport = viewport;
        this.stage = new Stage(viewport);
        this.soundLabel = null;
        this.musicLabel = null;
        background = null;
        //background.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        pressedState = -1;
//        pressedSound = false;
//        pressedMusic = false;
//        pressedControls = false;
        control = null;
        back = null;
//        this.stage = new Stage();
        this.active = true;
        skin = new Skin();
//        skin.add("font", displayFont);
//        skin.addRegions();
        this.ac = audioController;
    }

    public void create() {
        // Useless if called in outside animation loop
//        active = true;
        background = this.assets.getEntry("background_setting", Texture.class);
        Skin skin = new Skin(Gdx.files.internal("skin/uiskin.json"));

        BitmapFont font = this.assets.getEntry("shared:retro",BitmapFont.class);
        TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();
        buttonStyle.font = font;
        buttonStyle.fontColor = Color.WHITE;
        buttonStyle.overFontColor = Color.GRAY;

        musicLabel = new Label("MUSIC", new Label.LabelStyle(font, Color.WHITE));
        soundLabel = new Label("SOUND", new Label.LabelStyle(font, Color.WHITE));
        back = new TextButton("BACK", buttonStyle);
        control = new TextButton("CONTROLS", buttonStyle);
        musicLabel.setFontScale(BUTTON_SCALE);
        soundLabel.setFontScale(BUTTON_SCALE);
        back.getLabel().setFontScale(BUTTON_SCALE,BUTTON_SCALE);
        control.getLabel().setFontScale(BUTTON_SCALE,BUTTON_SCALE);

        slider_background = this.assets.getEntry("knob_background", Texture.class); // the bar
        slider_knob = this.assets.getEntry("knob", Texture.class);
        Texture knob_before = this.assets.getEntry("knob_before",Texture.class);
        Texture knob_after = this.assets.getEntry("knob_after",Texture.class);
        music = this.assets.getEntry("knob_after", Texture.class);
        Slider.SliderStyle ss = new Slider.SliderStyle();
        ss.knobAfter = new TextureRegionDrawable(new TextureRegion(knob_after));
        ss.knobBefore = new TextureRegionDrawable(new TextureRegion(knob_before));
        ss.knob = new TextureRegionDrawable(new TextureRegion(slider_knob));
//        ss.backgroundDown = new TextureRegionDrawable(new TextureRegion(slider_background));
        ss.background = new TextureRegionDrawable(new TextureRegion(slider_background));
        musicBar = new Slider(0f, 10f, 1f, false, ss);

        //create the slider
        soundBar = new Slider(0f, 0.1f*MAX_VOLUME, 1f, false, ss);


//        if(this.firstTime) {
        musicBar.setValue(ac.getGlobalMusicLevel() * 10);
        musicBar.setVisualPercent(ac.getGlobalMusicLevel());
        soundBar.setValue(ac.getGlobalSoundLevel() * 10);
        soundBar.setVisualPercent(ac.getGlobalSoundLevel());
//            firstTime=false;
//        }

        musicBar.setAnimateDuration(0.1f);
        soundBar.setAnimateDuration(0.1f);

        musicBar.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent changeEvent, Actor actor) {
                Gdx.app.log("TAG", "musicBar changed to:" + musicBar.getValue());
                ac.setGlobalMusicLevel(musicBar.getValue()/10);
            }
        });

        soundBar.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent changeEvent, Actor actor) {
                Gdx.app.log("TAG", "soundBar changed to:" + soundBar.getValue());
                if (!Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
                    ac.playSound("pickup", soundBar.getValue() / 10);
                }

                ac.setGlobalSoundLevel(soundBar.getValue()/10);
            }
        });

        Table tableMain = new Table();
        tableMain.setBackground(new TextureRegionDrawable(new TextureRegion(background)));

        tableMain.row().left();
        tableMain.add(soundLabel).padRight(60);
        tableMain.add(soundBar).width(slider_background.getWidth()).padRight(230);

        tableMain.row().left().padTop(15);
        tableMain.add(musicLabel).padRight(60);
        tableMain.add(musicBar).width(slider_background.getWidth()).padRight(250);

        tableMain.row().left().padRight(60).padTop(20);
        tableMain.add(control);

        tableMain.setFillParent(true);

        stage.addActor(tableMain);
        stage.addActor(back);
    }


    private void update(float delta){
        stage.act(delta);
        pressedState = -1;
        if (back.isPressed()) {
            pressedState = 0;
        }
        else if (control.isPressed()) {
            pressedState = 1;
        }
    }

    /**
     * TODO
     */
    public void dispose() {
        stage.dispose();
    }


    /**
     * TODO
     */
    private void draw() {
//        canvas.begin();
//        canvas.draw(background, Color.WHITE, 0, 0, stage.getWidth(), stage.getHeight());
        back.setPosition(40f,2*stage.getHeight()/2 - 70f);

        Color tintBack = pressedState == 0 ? Color.GRAY:Color.WHITE;
        Color tintControl = pressedState == 1 ? Color.GRAY:Color.WHITE;
        back.setColor(tintBack);
        control.setColor(tintControl);
        stage.draw();
//        Button control = new TextButton("Controls", skin, "");
//        canvas.end();
    }


    /**
     * TODO
     */
    public void render(float delta) {
        if(active){
            update(delta);
            draw();
//            stage.act(delta);
        }

        if (listener != null) {
            if (pressedState == 0 && back != null) {
                back.getClickListener().cancel();
                listener.exitScreen(this, ScreenListener.ExitCode.BACK);
            } else if (pressedState == 1 && control != null) {
                control.getClickListener().cancel();
                listener.exitScreen(this, ScreenListener.ExitCode.CONTROL);
            }
        }

        pressedState = -1;
    }


    public void resize(int width, int height) {
        float sx = ((float)width)/STANDARD_WIDTH;
        float sy = ((float)height)/STANDARD_HEIGHT;

        scale = (sx < sy ? sx : sy);

        heightY = height;
        centerX = width/2;
        centerY = height/2;
        viewport.setWorldSize(width, height);
        stage.getViewport().update(width, height, true);
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
        if (background == null) {
            create();
        }

        Gdx.input.setInputProcessor(stage);
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
     * <p>
     * The ScreenListener will respond to requests to quit.
     */
    public void setScreenListener(GDXRoot listener) {
        this.listener = listener;
    }

    public void setAssetDirectory(AssetDirectory assets) {
        this.assets = assets;
    }

//    private void initButton(){
//
//    }

    @Override
    public boolean keyDown(int i) {
        return false;
    }

    @Override
    public boolean keyUp(int i) {
        return false;
    }

    @Override
    public boolean keyTyped(char c) {
        return false;
    }

    @Override
    public boolean touchDown(int i, int i1, int i2, int i3) {
        return false;
    }

    @Override
    public boolean touchUp(int i, int i1, int i2, int i3) {
        return false;
    }

    @Override
    public boolean touchDragged(int i, int i1, int i2) {
        return false;
    }

    @Override
    public boolean mouseMoved(int i, int i1) {
        return false;
    }

    @Override
    public boolean scrolled(float v, float v1) {
        return false;
    }


    public void setFirstTime(boolean b) {
        firstTime = b;
    }
}