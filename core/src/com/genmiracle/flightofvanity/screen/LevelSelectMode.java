package com.genmiracle.flightofvanity.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.controllers.ControllerMapping;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.genmiracle.flightofvanity.GDXRoot;
import com.genmiracle.flightofvanity.assets.AssetDirectory;
import com.genmiracle.flightofvanity.graphics.GameCanvas;
import com.genmiracle.flightofvanity.util.ScreenListener;

import javax.swing.*;
//import sun.jvm.hotspot.utilities.BitMap;


public class LevelSelectMode implements Screen {

    private AssetDirectory assets;

    private Image player;

    private Stage stage;

    private int heightY;

    /**
     * Background texture for start-up
     */
    private Texture background;

    private Texture[] levelButtons;

    private ImageButton[] buttons;

    private GameCanvas canvas;

    private GDXRoot listener;

    /** -1 -> nothing pressed, 0 -> level 1, 1-> level 2, etc*/
    private int pressedState;

    private boolean active;
    private Texture[] selectLevelButtons;

    private int hoverState;
    private ScrollPane scroll;

    private TextButton backButton;

    private int currentLevel;

    private boolean isVictory;


    /** Height of the progress bar */
    private static float BUTTON_SCALE  = 0.75f;
    private float scale;
    /** Standard window size (for scaling) */
    private static int STANDARD_WIDTH  = 800;
    /** Standard window height (for scaling) */
    private static int STANDARD_HEIGHT = 700;


    /** Ration of the bar height to the screen */
    private static float BAR_HEIGHT_RATIO = 0.25f;

    private Table table;
    private Viewport viewport;

    private Texture unlockedLevel;

    private Texture lockedLevel;

    private ImageTextButton[] levelSelectButtons;

    private static final int NUM_LEVEL = 32;

    private TextureRegionDrawable locked;

    private  TextureRegionDrawable unlocked;




//    private BitmapFont font;

    public LevelSelectMode(GameCanvas canvas, Viewport viewport) {
        this.canvas = canvas;
        this.viewport = viewport;
        this.currentLevel = 0;
        this.isVictory = false;
        stage = new Stage(viewport);
        scroll = null;
        background = null;
        player = null;
        backButton = null;
        levelButtons = new Texture[10];
        buttons = new ImageButton[10];

        hoverState = -1;
        pressedState = -1;

        unlockedLevel = null;
        lockedLevel = null;
        levelSelectButtons = new ImageTextButton[NUM_LEVEL];
        this.active = true;

    }

    public void setIsVictory(boolean isVictory){
        this.isVictory = isVictory;
    }

    public void setCurMaxLevel(int currentLevel){
        this.currentLevel = currentLevel;
    }



    /**
     * Called when this screen becomes the current screen for a {@link GDXRoot}.
     */
    @Override
    public void show() {
        // Useless if called in outside animation loop
        lockedLevel = this.assets.getEntry("locked-level", Texture.class);
        unlockedLevel = this.assets.getEntry("unlocked-level",Texture.class);

        BitmapFont font = this.assets.getEntry("shared:retro",BitmapFont.class);
        TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();
        buttonStyle.font = font;
        buttonStyle.fontColor = Color.WHITE;
        buttonStyle.overFontColor = Color.GRAY;

        background = this.assets.getEntry("background-level-select",Texture.class);
        player = new Image(this.assets.getEntry("vanity-still", Texture.class));
        backButton =  new TextButton("BACK",buttonStyle );

        Skin skin = new Skin(Gdx.files.internal("skin/uiskin.json"));

        if (table != null) {
            table.clear();
        }

        table = new Table();
        table.setBackground(new TextureRegionDrawable(new TextureRegion(background)));
//        table.row();

//        for(int i = levelButtons.length-1; i>= 0; i--){
//            levelButtons[i] = this.assets.getEntry("level"+(i+1), Texture.class);
//            TextureRegion tr = new TextureRegion(levelButtons[i]);
//            TextureRegionDrawable dr = new TextureRegionDrawable(tr);
//            buttons[i] = new ImageButton(dr);
//            if(i > 0){
//                buttons[i].getImage().setColor(Color.GRAY);
//                buttons[i].setTouchable(Touchable.disabled);
//            }
//            if(isVictory && i <= currentLevel){
//                buttons[i].getImage().setColor(Color.WHITE);
//                buttons[i].setTouchable(Touchable.enabled);
//            }
//            table.add(buttons[i]).row();
//        }
        font = this.assets.getEntry("shared:retro-middle",BitmapFont.class);

        for(int i = levelSelectButtons.length-1; i>=0; i--) {
            TextureRegion tr1 = new TextureRegion(lockedLevel);
            locked = new TextureRegionDrawable(tr1);
            TextureRegion tr2 = new TextureRegion(unlockedLevel);
            unlocked = new TextureRegionDrawable(tr2);
            ImageTextButton.ImageTextButtonStyle imageTextButtonStyle = new ImageTextButton.ImageTextButtonStyle();
            imageTextButtonStyle.font = font;
            imageTextButtonStyle.overFontColor = Color.GRAY;
            levelSelectButtons[i] = new ImageTextButton("" + (i + 1), imageTextButtonStyle);
//            if(i>currentLevel + 1){
                imageTextButtonStyle.up = locked;
                imageTextButtonStyle.down = locked;
                imageTextButtonStyle.checked = locked;
//                levelSelectButtons[i].setTouchable(Touchable.disabled);
//
//            }else if(i<=currentLevel + 1){
//            imageTextButtonStyle.up = unlocked;
//            imageTextButtonStyle.down = unlocked;
//            imageTextButtonStyle.checked = unlocked;
//            levelSelectButtons[i].setTouchable(Touchable.enabled);
//            }
//            table.add(levelSelectButtons[i]).row();
        }
        int i = levelSelectButtons.length-1;

        while (i >= 0){
            ImageTextButton b = levelSelectButtons[i];

            if((i+1) % 6 == 1){
                table.row().left();
                table.add(b);
                table.add();
                table.add();
                table.add();
                i--;
            } else if((i+1) % 6 == 2 || (i+1) % 6 == 0){
                table.row().left();
                table.add();
                table.add(b).padRight(b.getWidth()*1.0f);
                if( (i+1) % 6 ==0) {
                    table.add(levelSelectButtons[i - 1]);
                    i-=2;
                }
                else {
                    table.add();
                    i--;
                }
                table.add();

            } else if((i+1) % 6 == 3 || (i+1) % 6 == 5){
                table.row().left();
                table.add();
                if( (i+1) % 6 == 3) {
                    table.add(levelSelectButtons[i - 1]).padRight(b.getWidth() * 1.0f);
                    i-=2;
                } else{
                    table.add();
                    i--;
                }
                table.add(b);
                table.add();
//                i-=2;

            } else if((i+1) % 6 == 4){
                table.row().left();
                table.add();
                table.add();
                table.add();
                table.add(b);
                i--;
            }
        }

        table.bottom().padBottom(70);
        table.setFillParent(false);

        scroll = new ScrollPane(table,skin);

        scroll.setHeight(stage.getHeight()*2);
        scroll.setWidth(stage.getWidth()+20);
        scroll.setupFadeScrollBars(0,0);
        scroll.setScrollingDisabled(true, false);
        scroll.scrollTo(stage.getHeight(),-stage.getWidth(),stage.getWidth(),stage.getHeight());
        stage.addActor(player);
        stage.addActor(scroll);
        stage.addActor(backButton);
        stage.setScrollFocus(scroll);

        Gdx.input.setInputProcessor(stage);
        active = true;


    }


    /**
     * TODO
     */
    private void draw() {
//        Gdx.gl.glClearColor(6/255,9/255,27/255,255/255);
//        positionButtons();
        backButton.setPosition(40f,2*stage.getHeight()/2 - 70f);
        backButton.setZIndex(10);

        stage.draw();

    }

    private void positionButtons(){
        float offsety = 0;

        for(int i = 0; i< levelSelectButtons.length; i+=6){
            levelSelectButtons[i].setPosition(270, stage.getHeight()/2 - stage.getHeight()/2/1.2f + offsety );
            offsety += 3.78*levelSelectButtons[i].getHeight();
        }
        float offsetx =0;
        offsety = 0;
        int even = 0;
        float offsetxinit = 0;
        float x = levelSelectButtons[0].getX() + levelSelectButtons[0].getWidth()/1.3f;
        float y = levelSelectButtons[0].getY() + 0.9f*levelSelectButtons[0].getHeight();
        for(int i = 1; i< levelSelectButtons.length-1; i+=3){

            levelSelectButtons[i].setPosition(x+ offsetxinit,  y + offsety);
            if (even %2 == 0){
                offsetx = levelSelectButtons[i].getWidth() + 3*levelSelectButtons[i].getWidth()/10.5f;
            } else{
                offsetx = 0;
            }
            levelSelectButtons[i+1].setPosition(x+offsetx, y+ offsety);
            offsety += 1.9*levelSelectButtons[0].getHeight();
            even += 1;
            offsetxinit = offsetx;

        }
        offsety = 1.08f*levelSelectButtons[0].getHeight();
        for(int i = 3; i< levelSelectButtons.length; i+=6){
            levelSelectButtons[i].setPosition(levelSelectButtons[2].getX() + levelSelectButtons[i].getWidth()/1.3f, stage.getHeight()/2 - stage.getHeight()/2/2 + offsety );
            offsety += 3.78*levelSelectButtons[i].getHeight();
        }
//        float offsety = 0;
//
//        for(int i = 0; i< buttons.length; i+=6){
//            buttons[i].setPosition(75, stage.getHeight()/2 - stage.getHeight()/2/1.2f + offsety );
//            offsety += 4*buttons[i].getHeight();
//        }
//        float offsetx =0;
//        offsety = 0;
//        int even = 0;
//        float offsetxinit = 0;
//        float x = buttons[0].getX() + buttons[0].getWidth()/2;
//        float y = buttons[0].getY() + buttons[0].getHeight();
//        for(int i = 1; i< buttons.length-1; i+=3){
//
//            buttons[i].setPosition(x+ offsetxinit,  y + offsety);
//            if (even %2 == 0){
//                offsetx = buttons[i].getWidth() + buttons[i].getWidth()/7.5f;
//            } else{
//                offsetx = 0;
//            }
//            buttons[i+1].setPosition(x+offsetx, y+ offsety);
//            offsety += 2*buttons[0].getHeight();
//            even += 1;
//            offsetxinit = offsetx;
//
//        }
//        offsety = 1.63f*buttons[0].getHeight();
//        for(int i = 3; i< buttons.length; i+=6){
//            buttons[i].setPosition(buttons[2].getX() + buttons[i].getWidth()/2 , stage.getHeight()/2 - stage.getHeight()/2/2 + offsety );
//            offsety += 4*buttons[i].getHeight();
//        }
    }

    private void update(float delta){

        stage.act(delta);
        backButton.setVisible(true);

        for (int i = 0; i< levelSelectButtons.length; i++){
            if (levelSelectButtons[i].getClickListener().getPressedButton() != -1) {
                pressedState = i;
                break;
            }
//            if(locked != null && unlocked != null) {
//                if (i > currentLevel || (i != 0 && !isVictory)) {
//                    levelSelectButtons[i].getStyle().up = locked;
//                    levelSelectButtons[i].getStyle().down = locked;
//                    levelSelectButtons[i].getStyle().checked = locked;
//                    levelSelectButtons[i].setTouchable(Touchable.disabled);
//
//                } else {
//                    levelSelectButtons[i].getStyle().up = unlocked;
//                    levelSelectButtons[i].getStyle().down = unlocked;
//                    levelSelectButtons[i].getStyle().checked = unlocked;
//                    levelSelectButtons[i].setTouchable(Touchable.enabled);
//                }
//            }
        }

        if(backButton.getClickListener().getPressedButton() != -1){
            pressedState = -2;
        }

//        for(int i = 0; i<buttons.length;i++){
//            if(buttons[i].isOver()){
//                hoverState = i;
//                break;
//            }
//        }

    }

    /**
     * TODO
     */
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (active){
            update(delta);
            draw();

        }

        if(pressedState != -1 && pressedState != -2 && listener != null){
            listener.exitScreen(this, ScreenListener.ExitCode.SELECT);
            pressedState = -1;
        } else if(pressedState == -2 && listener != null){
            listener.exitScreen(this,ScreenListener.ExitCode.MENU);
            pressedState = -1;
        }


    }

    public int getSelectedLevel(){
        return pressedState+1;
    }

    /**
     * TODO
     */
    public void dispose() {
        stage.dispose();
    }

    public void resize(int width, int height) {
        float sx = ((float)width)/STANDARD_WIDTH;
        float sy = ((float)height)/STANDARD_HEIGHT;

        scale = (sx < sy ? sx : sy);

        heightY = height;
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

}