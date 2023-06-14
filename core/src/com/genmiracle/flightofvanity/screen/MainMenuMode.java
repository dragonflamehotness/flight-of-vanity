package com.genmiracle.flightofvanity.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.genmiracle.flightofvanity.GDXRoot;
import com.genmiracle.flightofvanity.assets.AssetDirectory;
import com.genmiracle.flightofvanity.graphics.GameCanvas;
import com.genmiracle.flightofvanity.util.ScreenListener;

public class MainMenuMode implements Screen {

    private AssetDirectory assets;

    private Texture background;

    private TextButton playButton;

    private TextButton selectLevelButton;

    private TextButton settingButton;

    private TextButton quitButton;

    private Image arrow;

    private Viewport viewport;

    private GameCanvas canvas;

    private GDXRoot listener;

    private int centerX;

    private int centerY;

    private int heightY;

    private boolean active;

    private int pressedState;

    private int hoverState;

    private Stage stage;

    private final float OFFSET = centerY;
    /**
     * Height of the progress bar
     */
    private static float BUTTON_SCALE = 0.75f;
    private float scale;
    /**
     * Standard window size (for scaling)
     */
    private static int STANDARD_WIDTH = 800;
    /**
     * Standard window height (for scaling)
     */
    private static int STANDARD_HEIGHT = 700;


    /**
     * Ration of the bar height to the screen
     */
    private static float BAR_HEIGHT_RATIO = 0.25f;



    public MainMenuMode(GameCanvas canvas, Viewport viewport) {
        this.canvas = canvas;
        this.viewport = viewport;
        this.stage = new Stage(viewport);


        background = null;
        arrow = null;
        //background.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        playButton = null;
        selectLevelButton = null;
        settingButton = null;
        quitButton = null;

        pressedState = -1;
        hoverState = -1;


        this.active = true;
    }

    public void setAssetDirectory(AssetDirectory assets) {
        this.assets = assets;
    }

    /**
     * Sets the ScreenListener for this mode
     * <p>
     * The ScreenListener will respond to requests to quit.
     */
    public void setScreenListener(GDXRoot listener) {
        this.listener = listener;
    }


    private void update(float delta) {
        stage.act(delta);
        if(playButton.getClickListener().getPressedButton() != -1){
            pressedState = 0;
        }
        else if(selectLevelButton.getClickListener().getPressedButton() != -1){
            pressedState = 1;
        }
        else if(settingButton.getClickListener().getPressedButton() != -1){
            pressedState=2;
        } else if (quitButton.getClickListener().getPressedButton() != -1){
            pressedState = 3;
        }


        if(playButton.isOver()){
            hoverState = 0;
//            playButton.setColor(Color.GRAY);
        }
        else if (selectLevelButton.isOver()){
            hoverState = 1;
//            selectLevelButton.setColor(Color.GRAY);
        }
        else if(settingButton.isOver()){
            hoverState = 2;
//            settingButton.setColor(Color.GRAY);
        } else if(quitButton.isOver()){
            hoverState = 3;
        }
        else{
            hoverState = -1;

        }

    }

    private void draw() {
        Color tintplay = pressedState == 0 ? Color.GRAY:Color.WHITE;

        playButton.setColor(tintplay);
//        playButton.setPosition(centerX+OFFSET,centerY+OFFSET);

        Color tintselect = pressedState == 1 ? Color.GRAY:Color.WHITE;
        selectLevelButton.setColor(tintselect);
//        selectLevelButton.setPosition(centerX,centerY);


        Color tintsetting = pressedState == 2 ? Color.GRAY:Color.WHITE;
        settingButton.setColor(tintsetting);
//        settingButton.setPosition(centerX+OFFSET,centerY-OFFSET);


        if (hoverState == 0){
//            Gdx.graphics.setSystemCursor(Cursor.SystemCursor.Hand);
            arrow.setZIndex(10);
            arrow.setPosition(playButton.getX() - arrow.getWidth() - 10, playButton.getY() + playButton.getHeight()/6);
        }
        if (hoverState == 1){
//            Gdx.graphics.setSystemCursor(Cursor.SystemCursor.Hand);
            arrow.setZIndex(10);
            arrow.setPosition(selectLevelButton.getX() - arrow.getWidth() - 10, selectLevelButton.getY() + selectLevelButton.getHeight()/6);
        }
        if (hoverState == 2){
//            Gdx.graphics.setSystemCursor(Cursor.SystemCursor.Hand);
            arrow.setZIndex(10);
            arrow.setPosition(settingButton.getX() - arrow.getWidth() - 10, settingButton.getY() + settingButton.getHeight()/6);
        }
        if(hoverState == 3){
            arrow.setZIndex(10);
            arrow.setPosition(quitButton.getX() - arrow.getWidth() - 10, quitButton.getY() + quitButton.getHeight()/6);
        }
        if(hoverState == -1){
//            Gdx.graphics.setSystemCursor(Cursor.SystemCursor.Arrow);
            arrow.setZIndex(0);
        }



        stage.draw();
    }

    @Override
    public void show() {

        background = this.assets.getEntry("background", Texture.class);
        arrow = new Image(this.assets.getEntry("arrow",Texture.class));

        TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();
        buttonStyle.font = this.assets.getEntry("shared:retro",BitmapFont.class);
        buttonStyle.fontColor = new Color(36/255,30/255,49/255,1f);
//        buttonStyle.downFontColor = Color.GRAY;
        buttonStyle.overFontColor = Color.GRAY;
        Table table = new Table();
        table.setBackground(new TextureRegionDrawable(new TextureRegion(background)));


        playButton =  new TextButton("START GAME", buttonStyle);
        selectLevelButton = new TextButton("SELECT LEVEL", buttonStyle);
        settingButton = new TextButton("SETTINGS",buttonStyle);
        quitButton = new TextButton("QUIT GAME", buttonStyle);

        table.add(playButton).padTop(110).row();
        table.add(selectLevelButton).padTop(10).padLeft(5).row();
        table.add(settingButton).padTop(10).row();
        table.add(quitButton).padTop(10).row();
//        table.pack();
//        table.setTransform(true);
//        table.setOrigin(centerX,centerY);
//        table.setSize(stage.getWidth(), stage.getHeight());
        table.setFillParent(true);

        stage.addActor(table);
//        stage.addActor(arrow);
        Gdx.input.setInputProcessor(stage);
        active = true;
    }

    @Override
    public void render(float delta) {


        if (active) {
            update(delta);
            draw();
        }

        if (listener != null) {
            if (pressedState == 0 && playButton != null) {
                listener.exitScreen(this, ScreenListener.ExitCode.CONTINUE);
            } else if (pressedState == 1 && selectLevelButton != null)
                listener.exitScreen(this, ScreenListener.ExitCode.SELECT);
            else if (pressedState == 2 && settingButton != null)
                listener.exitScreen(this, ScreenListener.ExitCode.SETTING);
            else if (pressedState == 3 && quitButton != null)
                listener.exitScreen(this,ScreenListener.ExitCode.QUIT);
        }

        pressedState = -1;
    }

    @Override
    public void resize(int width, int height) {
        // Compute the drawing scale
        float sx = ((float) width) / STANDARD_WIDTH;
        float sy = ((float) height) / STANDARD_HEIGHT;
        scale = (sx < sy ? sx : sy);

        heightY = height;
        centerX = width / 2;
        centerY = height/2;
        viewport.setWorldSize(width, height);
        stage.getViewport().update(width, height, true);

//        stage.getCamera().position.set(960/2,540/2,0);
//        stage.getCamera().update();
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {
        active = false;
    }

    @Override
    public void dispose() {

    }

}