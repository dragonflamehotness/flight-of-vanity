package com.genmiracle.flightofvanity.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.genmiracle.flightofvanity.GDXRoot;
import com.genmiracle.flightofvanity.InputController;
import com.genmiracle.flightofvanity.assets.AssetDirectory;
import com.genmiracle.flightofvanity.audio.AudioController;
import com.genmiracle.flightofvanity.graphics.GameCanvas;
import com.genmiracle.flightofvanity.graphics.cube.CubeController;
import com.genmiracle.flightofvanity.instance.Instance;
import com.genmiracle.flightofvanity.instance.InstanceController;
import com.genmiracle.flightofvanity.instance.PlayerController;
import com.genmiracle.flightofvanity.instance.model.Mirror;
import com.genmiracle.flightofvanity.instance.model.Player;
import com.genmiracle.flightofvanity.level.WorldController;
import com.genmiracle.flightofvanity.util.ScreenListener;
import com.genmiracle.flightofvanity.util.Utilities;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import javax.swing.text.View;


public class LevelMode implements Screen {
    private BitmapFont displayFont;

    private GameCanvas canvas;
    private boolean active;

    private boolean paused;

    private int currentLevel;

    private WorldController wc;
    private CubeController cc;
    private InstanceController instC;
    private InputController inpC;
    private PlayerController pc;
    private AudioController ac;

    private OrthographicCamera cam;

    private ScreenListener listener;

    private AssetDirectory dir;
    private boolean pauseScreenOpen;

    private boolean stillPressing;

    private float pauseAngle;
    private boolean showPause;
    private float pauseInterpCounter;
    private float pauseInterpSpeed;
    private float pauseInterpAngleStart;
    private float pauseInterpAngleEnd;
    private String lastFrame;

    private boolean stillLeftClicking;

    private Label currLevelLabel;

    private Viewport viewport;

    private TextureRegion tx;
    private Stage stage;

    // private static final Color BG_COLOR = new Color(0.07f, 0f, 0.07f, 1);
//    private static final Color BG_COLOR = new Color(28f / 255, 23f / 255, 38f / 255, 1);
     private static final Color BG_COLOR = new Color(0x08142cff);
    private Image arrow;
    private TextButton resumeButton;

    private TextButton nextLevelButton;
    private TextButton selectLevelButton;
    private TextButton selectMusicButton;
//    private TextButton mainMenuButton;
    private TextButton levelSelectButton;
    private ImageButton pauseButton;
    private TextButton restartLevelButton;

    private TextButton settingButton;
    private Texture emptyBagTexture;
    private Texture mirror45BagTexture;
    private Texture mirror90BagTexture;
    private Texture pauseTexture;

    private int victoryPressState;

    private int pausePressState;

    private float centerX;

    private float centerY;
//    private int mainMenuState;

    private boolean isVictory;

    private static final Color TRANSPARENT = new Color(1, 1, 1, 0.5f);
    private static final Color YELLOW = new Color(212f / 256, 210f / 256, 186f / 256, 1f);
    private boolean victoryScreen;
    private boolean playedVictory;

    private int currMaxLevel;

    public LevelMode(GameCanvas canvas, Viewport viewport) {
        this.viewport = viewport;
        this.canvas = canvas;
        stage = new Stage(viewport);
        victoryPressState = -1;
        pausePressState = -1;
        this.isVictory = false;
    }

    public BitmapFont testFont;

    public void init() {
        wc = new WorldController();

        wc.setCanvas(canvas);
        wc.setAssetDirectory(dir);
        wc.setAudioController(ac);
        wc.init();

        instC = wc.getInstanceController();

        cc = new CubeController(dir);
        pc = wc.getPlayerController();

        cc.setWorldController(wc);
        cc.setInstanceController(instC);
        cc.setLightController(wc.getLightController());
        cc.setInputController(InputController.getInstance());

        TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();
        buttonStyle.font = this.dir.getEntry("shared:retro", BitmapFont.class);
        buttonStyle.fontColor = Color.WHITE;
        buttonStyle.overFontColor = new Color(0x35e4d8ff);

        pauseTexture = dir.getEntry("pauseIcon", Texture.class);

        // trying (and failing) to add buttons.
        this.arrow = new Image(this.dir.getEntry("arrow", Texture.class));

        resumeButton = new TextButton("RESUME", buttonStyle);
        levelSelectButton = new TextButton("SELECT LEVEL", buttonStyle);
        settingButton = new TextButton("SETTINGS", buttonStyle);

        selectLevelButton = new TextButton("SELECT LEVEL", buttonStyle);

        selectMusicButton = new TextButton("CURRENT MUSIC: " + ac.getCurrPlayingName(), buttonStyle);

        stage.addActor(resumeButton);
        stage.addActor(arrow);
//        stage.addActor(mainMenuButton);
        stage.addActor(selectMusicButton);
        stage.addActor(levelSelectButton);
        stage.addActor(settingButton);

        nextLevelButton = new TextButton("NEXT LEVEL", buttonStyle);
        restartLevelButton = new TextButton("RESTART LEVEL", buttonStyle);

        stage.addActor(nextLevelButton);
        stage.addActor(selectLevelButton);
        stage.addActor(restartLevelButton);

        inpC = InputController.getInstance();

        tx = new TextureRegion(dir.getEntry("testBackground", Texture.class));
        testFont = dir.getEntry("shared:retro-36", BitmapFont.class);

        emptyBagTexture = dir.getEntry("emptyBagUI", Texture.class);
        mirror45BagTexture = dir.getEntry("45BagUI", Texture.class);
        mirror90BagTexture = dir.getEntry("90BagUI", Texture.class);
    }

    public void setAudioController(AudioController ac) {
        this.ac = ac;

        if (wc != null) {
            wc.setAudioController(ac);
        }
    }

    public void create(int levelNumber) {
        init();

        loadLevel(levelNumber);
        wc.setPlayedUnlock(false);

        cc.createCube(WorldController.SIDE_LENGTH);
    }

    protected void loadLevel(int levelNumber) {
        playedVictory = false;
        ac.getCurrVictory().pause();
        instC.clear();

        currentLevel = levelNumber;

        JsonReader json = new JsonReader();
        JsonValue base = json.parse(Gdx.files.internal("levels/level" + String.valueOf(levelNumber) + ".json"));

        int levelNumberMod = levelNumber % 14;
        Music m = ac.getCurrPlaying();
        String songName = "";
        if(levelNumber > 12 && levelNumberMod < 3)
            songName = "Flight";
        else if (levelNumber > 12 && levelNumberMod < 6) {
            songName = "Bonfire Waltz";
        }
        if (levelNumberMod == 14)
            songName = "Lonely Light";
       if (levelNumberMod == 6 || levelNumberMod == 7)
           songName = "Flight";
       if (levelNumber < 6)
            songName = "Hazed Memory";
       if(levelNumberMod > 8)
            songName = "Bonfire Waltz (Orchestral)";
       if(levelNumberMod == 8)
            songName = "Bonfire Waltz";
       if (!ac.getCurrPlayingName().equals(songName)) {
           ac.playMusic(songName, true);
       } else {
           if (!m.isPlaying())
               m.play();
       }

        wc.loadWorld(base);

        int startingSide = pc.getPlayer().getSide();

        wc.setCurrentSide(startingSide);
        wc.setActive(true);

        cc.reset(startingSide, 0);
        saveFrame("restart");

        instC.getPhysicsController().setCenterSide(startingSide, 0, true);

        active = true;
    }

    public void saveFrame(String frameName) {
        lastFrame = frameName;
        instC.saveFrame(frameName);
    }

    public void loadFrame(String frameName) {
        instC.loadFrame(frameName);
    }

    public void loadLastFrame() {
        instC.loadFrame(lastFrame);
    }

    /**
     * Draw the physics objects to the canvas
     *
     * For simple worlds, this method is enough by itself. It will need
     * to be overriden if the world needs fancy backgrounds or the like.
     *
     * The method draws all objects in the order that they were added.
     *
     * @param dt Number of seconds since last animation frame
     */
    public void draw(float dt) {
        canvas.clear(BG_COLOR);
    }

    /**
     * Draw the physics objects to the canvas after the Cube has been drawn
     *
     * For simple worlds, this method is enough by itself. It will need
     * to be overriden if the world needs fancy backgrounds or the like.
     *
     * The method draws all objects in the order that they were added.
     *
     * @param dt Number of seconds since last animation frame
     */
    public void postDraw(float dt) {
        renderPauseScreen(dt);
        renderVictoryScreen(dt);

        stage.act(dt);
        stage.draw();
    }

    public int getCurrentLevel() {
        return currentLevel;
    }

    public int getCurrMaxLevel(){ return currMaxLevel;}

    public boolean isVictory() {
        return isVictory;
    }

    @Override
    public void show() {
        nextLevelButton.setVisible(false);
        selectLevelButton.setVisible(false);
        // resumeButton.setVisible(false);
        selectMusicButton.setVisible(false);
//        mainMenuButton.setVisible(false);
        resumeButton.setVisible(false);
        levelSelectButton.setVisible(false);
        arrow.setVisible(false);
        restartLevelButton.setVisible(false);
        settingButton.setVisible(false);
        currLevelLabel = new Label("" + (currentLevel), new Label.LabelStyle(testFont, Color.WHITE));
        currLevelLabel.setFontScale(0.6f);
        currLevelLabel.setVisible(true);
        stage.addActor(currLevelLabel);
        Gdx.input.setInputProcessor(stage);
        active = true;
    }

    /**
     * Called when the screen should render itself.
     *
     * @param delta The time in seconds since the last render.
     */
    @Override
    public void render(float delta) {
        // System.out.println("FPS: " + Math.round(1f / delta));

        viewport.apply();
        currLevelLabel.setText("" + currentLevel);
        currLevelLabel.setPosition(30,stage.getHeight() - 80f);
        // System.out.println(stillPressing);
        if (Gdx.input.isKeyPressed(Input.Keys.ESCAPE) && !stillPressing && !victoryScreen) {
            stillPressing = true;
            if (!paused)
                pause();
            else {
                resume();
                // pausePressState = -1;
            }
        } else if (!Gdx.input.isKeyPressed(Input.Keys.ESCAPE))
            stillPressing = false;
        if (pc.getPlayer().isDead()) {
            instC.loadLastFrame();
        } else if (pc.getPlayer().isCollidedDoor()) {
            isVictory = true;
            inpC.setCursorCatched(false);

            if (active)
                active = false;
                if (currentLevel + 1 > currMaxLevel){
                    currMaxLevel = currentLevel + 1;
                }
            else {
                if (victoryPressState == 0) {
                    victoryPressState = -1;
                    // active = true;
                    nextLevelButton.setVisible(false);
                    selectLevelButton.setVisible(false);

                    loadLevel(currentLevel + 1);
                    victoryScreen = false;
                }
            }
        } else {
            isVictory = false;
        }

        if (!paused && active) {
            wc.update(delta);
        }

        draw(delta);

        cc.render(delta);

        postDraw(delta);

        //renderUI(delta);
        if (Gdx.input.isKeyPressed(Input.Keys.DEL) || restartLevelButton.getClickListener().getPressedButton() != -1) {
            paused = false;
            loadFrame("restart");
            Music m = ac.getCurrPlaying();
            if(!m.isPlaying())
                m.play();
        }

        int mx = inpC.getMouseX();
        int my = inpC.getMouseY();

        selectMusicButton.setText("CURRENT MUSIC: " + ac.getCurrPlayingName());
        selectMusicButton.pack();
        if (mx < stage.getWidth() + 10 && mx > stage.getWidth() - 100 &&
                my > 0 && my < 100) {

            if (!showPause) {
                pauseInterpCounter = 1;
                pauseInterpSpeed = 1f;
            }

            showPause = true;
        } else {
            if (showPause) {
                pauseInterpCounter = 1;
                pauseInterpSpeed = 1f;
            }

            showPause = false;
        }

        if (pauseInterpCounter > 0) {
            pauseInterpCounter -= delta / pauseInterpSpeed;
        }

        if (!active && victoryPressState == 1 && listener != null) {
            victoryPressState = -1;
            victoryScreen = false;
            stillPressing = false;
            pc.getPlayer().setIsCollidedDoor(false);
            selectLevelButton.setVisible(false);
            nextLevelButton.setVisible(false);
            currLevelLabel.setVisible(false);
            listener.exitScreen(this, ScreenListener.ExitCode.SELECT);
        }

        if (pausePressState == 0 && listener != null) {
            pausePressState = -1;
            victoryScreen = false;
            stillPressing = false;
            selectMusicButton.setVisible(false);
//            mainMenuButton.setVisible(false);
            levelSelectButton.setVisible(false);
            resumeButton.setVisible(false);
            restartLevelButton.setVisible(false);
            settingButton.setVisible(false);
            currLevelLabel.setVisible(false);
            listener.exitScreen(this, ScreenListener.ExitCode.SELECT);
        }
        else if (pausePressState == 1 && listener != null){
            pausePressState = -1;
            selectMusicButton.setVisible(false);
//            mainMenuButton.setVisible(false);
            levelSelectButton.setVisible(false);
            resumeButton.setVisible(false);
            restartLevelButton.setVisible(false);
            settingButton.setVisible(false);
            currLevelLabel.setVisible(false);
            listener.exitScreen(this, ScreenListener.ExitCode.SETTING);

        }

    }

    @Override
    public void resize(int width, int height) {
        // canvas.setSize(width, height);
        // System.out.println(width + " " + height);
        //// stage.getViewport().update(width,height,true);
        // stage.getViewport().update(width,height,true);
        //// stage.getCamera().position.set(960/2,540/2,0);
        //// stage.getCamera().update();
        centerX = width / 2;
        centerY = height / 2;

        viewport.setWorldSize(width, height);
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {
        paused = true;
    }

    @Override
    public void resume() {
        paused = false;
        pauseScreenOpen = false;

    }

    /**
     */
    @Override
    public void hide() {

    }

    /**
     * Called when this screen should release all resources.
     */
    @Override
    public void dispose() {
        if (instC != null) {
            instC.dispose();
        }
        if (wc != null) {
            wc.dispose();
        }
        if (cc != null) {
            cc.dispose();
        }

    }

    /**
     * Sets the ScreenListener for this mode
     *
     * The ScreenListener will respond to requests to quit.
     */
    public void setScreenListener(GDXRoot listener) {
        this.listener = listener;
    }

    public void setAssetDirectory(AssetDirectory dir) {
        this.dir = dir;
    }

    public void renderUI(float delta) {
        canvas.beginScaled();

        Mirror m = pc.getPlayer().getHolding();
        if (m == null) {
            canvas.draw(emptyBagTexture, TRANSPARENT, 5, 5, 100, 100);
        } else if (m.getType() == 45) {
            canvas.draw(mirror45BagTexture, TRANSPARENT, 5, 5, 100, 100);
        } else if (m.getType() == 90) {
            canvas.draw(mirror90BagTexture, TRANSPARENT, 5, 5, 100, 100);
        }

        // canvas.draw(pauseTexture, Color.WHITE, (Utilities.BASE_WIDTH - 25) /
        // canvas.getScaleX(), (Utilities.BASE_HEIGHT - 25) / canvas.getScaleY(), 20,
        // 20);
        canvas.end();

//        pauseButton.setPosition((stage.getWidth() - pauseButton.getWidth() - 5),
//                (stage.getHeight() - pauseButton.getHeight() - 5));
//        pauseButton.setVisible(false);
    }

    public void renderPauseScreen(float delta) {
        if (paused) {
            // if(!pauseScreenOpen) {

            canvas.beginScaled();
            canvas.draw(tx, new Color(0, 0, 0, .5f), 0, 0, Utilities.BASE_WIDTH, Utilities.BASE_HEIGHT);

            testFont.setColor(YELLOW);
            canvas.drawTextCentered("PAUSED", testFont,
                    (Utilities.BASE_WIDTH / 2 - Utilities.BASE_WIDTH / 4) / canvas.getScaleX(),
                    (Utilities.BASE_HEIGHT / 2 + Utilities.BASE_HEIGHT / 4) / canvas.getScaleY());
            testFont.setColor(new Color(212, 210, 186, 1));

            canvas.end();

            resumeButton.setVisible(true);
//            mainMenuButton.setVisible(true);
            selectMusicButton.setVisible(true);
            levelSelectButton.setVisible(true);
            restartLevelButton.setVisible(true);
            settingButton.setVisible(true);
//            pauseButton.setVisible(false);

//            // mainMenuButton.setColor(Color.RED);

            resumeButton.setZIndex(10);
            resumeButton.setPosition((Utilities.BASE_WIDTH / 2 - Utilities.BASE_WIDTH / 4 - 100) / canvas.getScaleX(),
                    Utilities.BASE_HEIGHT / 1.7f - resumeButton.getHeight());

//            mainMenuButton.setZIndex(10);
//            mainMenuButton.setPosition((Utilities.BASE_WIDTH / 2 - Utilities.BASE_WIDTH / 4 - 100) / canvas.getScaleX(),
//                    Utilities.BASE_HEIGHT / 1.7f - 2.2f * mainMenuButton.getHeight());
            selectMusicButton.setZIndex(10);
            selectMusicButton.setPosition(
                    (Utilities.BASE_WIDTH / 2 - Utilities.BASE_WIDTH / 4 - 100) / canvas.getScaleX(),
                    Utilities.BASE_HEIGHT / 1.7f - 8.2f * selectMusicButton.getHeight());
            restartLevelButton.setZIndex(10);
            restartLevelButton.setPosition(
                    (Utilities.BASE_WIDTH / 2 - Utilities.BASE_WIDTH / 4 - 100) / canvas.getScaleX(),
                    Utilities.BASE_HEIGHT / 1.7f - 2.2f * restartLevelButton.getHeight());

            levelSelectButton.setZIndex(10);
            levelSelectButton.setPosition(
                    (Utilities.BASE_WIDTH / 2 - Utilities.BASE_WIDTH / 4 - 100) / canvas.getScaleX(),
                    Utilities.BASE_HEIGHT / 1.7f - 3.4f * levelSelectButton.getHeight());

            settingButton.setZIndex(10);
            settingButton.setPosition((Utilities.BASE_WIDTH / 2 - Utilities.BASE_WIDTH / 4 - 100) / canvas.getScaleX(),
                    Utilities.BASE_HEIGHT / 1.7f - 4.6f * settingButton.getHeight());
            // }
            // else{
            //
            // TextureRegion tx = new TextureRegion(new
            // Texture(Gdx.files.internal("assets/loading/background.png")));
            // canvas.draw(tx, Color.WHITE, 0,0, canvas.getWidth(), canvas.getHeight());
            //
            // }
            pauseScreenOpen = true;
            if (resumeButton.getClickListener().getPressedButton() != -1) {
                paused = false;
            } else if (levelSelectButton.getClickListener().getPressedButton() != -1) {
                pausePressState = 0;
                paused = false;
            } else if(settingButton.getClickListener().getPressedButton() != -1){
                pausePressState = 1;
                paused = false;
            }
            else if (selectMusicButton.getClickListener().getPressedButton() != -1 && !stillLeftClicking) {
                ac.playNext();
                stillLeftClicking = true;
                // selectMusicButton.setZIndex(10);
                // selectMusicButton.setPosition((Utilities.BASE_WIDTH / 2 -
                // Utilities.BASE_WIDTH / 4 -100)/ canvas.getScaleX() , Utilities.BASE_HEIGHT /
                // 1.7f - 5.4f*selectMusicButton.getHeight() );
            } else if (selectMusicButton.getClickListener().getPressedButton() == -1)
                stillLeftClicking = false;

        } else {
            resumeButton.setVisible(false);
//            mainMenuButton.setVisible(false);
            selectMusicButton.setVisible(false);
            levelSelectButton.setVisible(false);
            restartLevelButton.setVisible(false);
            settingButton.setVisible(false);
        }
    }

    private void renderVictoryScreen(float delta) {
        if (!active && pc.getPlayer().isCollidedDoor() && !showPause) {
            victoryScreen = true;
            active = false;
            canvas.beginScaled();
            canvas.draw(tx, new Color(0, 0, 0, .5f), 0, 0, centerX * 2, centerY * 2);

            Texture vic = dir.getEntry("victory", Texture.class);
            canvas.draw(vic, Color.WHITE, vic.getWidth() / 2, vic.getHeight() / 2,
                    Utilities.BASE_WIDTH / 2 / canvas.getScaleX(), Utilities.BASE_HEIGHT * 3 / 4 / canvas.getScaleY(),
                    vic.getWidth(), vic.getHeight());
            canvas.end();
            if(currentLevel <= 32) {
                nextLevelButton.setVisible(true);
            }
            else{
                nextLevelButton.setVisible(false);
            }
            selectLevelButton.setVisible(true);

            nextLevelButton.setZIndex(10);
            nextLevelButton.setPosition(Utilities.BASE_WIDTH / 2 - nextLevelButton.getWidth() / 2,
                    Utilities.BASE_HEIGHT / 2 - nextLevelButton.getHeight() / 2);

            selectLevelButton.setZIndex(10);
            selectLevelButton.setPosition(Utilities.BASE_WIDTH / 2 - nextLevelButton.getWidth() / 2 - 15,
                    Utilities.BASE_HEIGHT / 2 - 2.3f * nextLevelButton.getHeight());

            if(!this.playedVictory) {
                playedVictory = true;
                if (ac.getCurrPlayingName().equals("Bonfire Waltz (Jack's Remix)"))
                    ac.playVictory(true);
                else
                    ac.playVictory(false);
            }
            if (nextLevelButton.getClickListener().getPressedButton() != -1) {
                victoryPressState = 0;
                wc.setPlayedUnlock(false);
            } else if (selectLevelButton.getClickListener().getPressedButton() != -1) {
                ac.playMusic("Daydreaming", true);
                victoryPressState = 1;
            }

            if (levelSelectButton.getClickListener().getPressedButton() != -1) {
                pausePressState = 0;
            }
            if(settingButton.getClickListener().getPressedButton() != -1){
                pausePressState = 1;
            }
        }
    }
}
