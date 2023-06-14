package com.genmiracle.flightofvanity.screen;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.genmiracle.flightofvanity.GDXRoot;
import com.genmiracle.flightofvanity.InputController;
import com.genmiracle.flightofvanity.assets.AssetDirectory;
import com.genmiracle.flightofvanity.graphics.GameCanvas;
import com.genmiracle.flightofvanity.screen.stages.ControlModeStage;
import com.genmiracle.flightofvanity.util.ScreenListener;

import javax.naming.ldap.Control;

public class ControlMode implements Screen {

    private AssetDirectory assets;

    /**
     * Background texture for start-up
     */
    private Texture background;

    private Viewport viewport;

    private GDXRoot listener;

    private boolean active;

    private int centerX;

    private int centerY;

    private float scale;
    private int heightY;

    private Stage stage;
    private TextField text;

    private Label movementLabel;

    private Label observationLabel;

    private Label leftLabel;

    private Label rightLabel;

    private Label jumpLabel;

    private Label downLabel;

    private Label pickupLabel;

    private Label rotateLabel;

    private Label observeOnOff;

    private Label observeControl;


    private Label save;

    private Label automaticSave;
    private TextButton default_keys;
    private TextButton back;
    private TextButton left;
    private TextButton right;
    private TextButton jump;
    private TextButton pick_up;
    private TextButton on_off;
    private TextButton rotate;

    private TextButton observeControlButton;
    private TextButton down;

    private boolean pressTime;
    private TextButton[] controlButtons;
    private static final InputController.ControlCode[] CONTROL_CODES = new InputController.ControlCode[] {
            InputController.ControlCode.LEFT,
            InputController.ControlCode.RIGHT,
            InputController.ControlCode.JUMP,
            InputController.ControlCode.PICK_UP,
            InputController.ControlCode.OBS_MODE,
            InputController.ControlCode.DOWN,
            InputController.ControlCode.ROTATE
    };
    private Skin skin;
    private boolean pressed;

    /** -1 -> nothing pressed, 0 -> control*/
    private int pressedState;

    private float pressCountdown;

    private boolean ispressCountdown;
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

    public ControlMode(GameCanvas canvas, Viewport viewport){
        this.canvas = canvas;
        this.viewport = viewport;
        background = null;
        back = null;
        automaticSave=null;
        default_keys = null;
        left = null;
        right = null;
        down = null;
        jump = null;
        pick_up = null;
        on_off = null;
        rotate = null;
        movementLabel = null;
        observationLabel = null;
        observeControl = null;
        observeControlButton = null;
        leftLabel = null;
        rightLabel = null;
        jumpLabel = null;
        downLabel = null;
        pickupLabel = null;
        rotateLabel = null;
        observeOnOff = null;
        pressed = false;
        pressedState = -1;
        pressTime = false;
        this.stage = new ControlModeStage(viewport);
        this.active = true;
        skin = new Skin();
        ispressCountdown = false;
        pressCountdown = 0;
    }

    private void update(float delta){
        stage.act(delta);
//        System.out.println(pressedState);
        if(back.getClickListener().getPressedButton() != -1){
            pressedState = 0;
        }
        else if(default_keys.getClickListener().getPressedButton() != -1){
            pressedState = 1;
            pressed = true;
        }
        else if(left.getClickListener().getPressedButton() != -1){
            pressedState = 3;
            pressed = true;
        }
        else if(right.getClickListener().getPressedButton() != -1){
            pressedState = 4;
            pressed = true;
        }
        else if(jump.getClickListener().getPressedButton() != -1){
            pressedState = 5;
            pressed = true;
        }
        else if(pick_up.getClickListener().getPressedButton() != -1){
            pressedState = 6;
            pressed = true;
        }
        else if(on_off.getClickListener().getPressedButton() != -1){
            pressedState = 7;
            pressed = true;
        } else if(down.getClickListener().getPressedButton() != -1){
            pressedState = 8;
            pressed = true;
        } else if(rotate.getClickListener().getPressedButton() != -1){
            pressedState = 9;
            pressed = true;
        } else if(observeControlButton.getClickListener().getPressedButton() != -1){
            ispressCountdown = true;
            pressCountdown = 0.05f;
        }

        if(pressCountdown > 0 && ispressCountdown){
            pressCountdown -= delta;
            if(pressCountdown <= 0){
                pressCountdown = 0;
                pressTime = !pressTime;
                if(!pressTime){
                    observeControlButton.setText("CURSOR                      ");
                    InputController.getInstance().setIsObserveControlKey(false);

                }else{
                    observeControlButton.setText("CURSOR + WSAD KEYS");
                    InputController.getInstance().setIsObserveControlKey(true);

                }
            }
        }

    }

    private void draw(){
        back.setPosition(40f,2*stage.getHeight()/2 - 70f);
        default_keys.setPosition(stage.getWidth() -default_keys.getWidth()-40f,2*stage.getHeight()/2 - 70f);
        automaticSave.setPosition(stage.getWidth() -automaticSave.getWidth()/1.4f -35f,2*stage.getHeight()/2 - 150f);
        save.setPosition(stage.getWidth() -save.getWidth()/1.4f -40f,2*stage.getHeight()/2 - 175f);
        stage.draw();
        Color tintBack = pressedState == 0 ? Color.GRAY:Color.WHITE;
        Color tintDefault = pressedState == 1 ? Color.GRAY:Color.WHITE;
        Color tintSave = pressedState == 2 ? Color.GRAY:Color.WHITE;
        Color tintLeft = pressedState == 3 ? Color.GRAY:Color.WHITE;
        Color tintRight = pressedState == 4 ? Color.GRAY:Color.WHITE;
        Color tintJump = pressedState == 5 ? Color.GRAY:Color.WHITE;
        Color tintPick= pressedState == 6 ? Color.GRAY:Color.WHITE;
        Color tintMap = pressedState == 7 ? Color.GRAY:Color.WHITE;

        back.setColor(tintBack);
        default_keys.setColor(tintDefault);
        save.setColor(tintSave);
        left.setColor(tintLeft);
        right.setColor(tintRight);
        jump.setColor(tintJump);
        pick_up.setColor(tintPick);
        on_off.setColor(tintMap);

    }

    private BitmapFont getFont(){
        return this.assets.getEntry("shared:retro", BitmapFont.class);
    }

    private void setLabels() {
        BitmapFont font = getFont();
        movementLabel = new Label("MOVEMENT", new Label.LabelStyle(font, Color.WHITE));
        movementLabel.setFontScale(BUTTON_SCALE - 0.1f, BUTTON_SCALE - 0.1f);
        observationLabel = new Label("OBSERVATION", new Label.LabelStyle(font, Color.WHITE));
        observationLabel.setFontScale(BUTTON_SCALE - 0.1f, BUTTON_SCALE - 0.1f);
        leftLabel = new Label("LEFT", new Label.LabelStyle(font, Color.WHITE));
        leftLabel.setFontScale(BUTTON_SCALE - 0.1f, BUTTON_SCALE - 0.1f);
        rightLabel = new Label("RIGHT", new Label.LabelStyle(font, Color.WHITE));
        rightLabel.setFontScale(BUTTON_SCALE - 0.1f, BUTTON_SCALE - 0.1f);
        jumpLabel = new Label("JUMP", new Label.LabelStyle(font, Color.WHITE));
        jumpLabel.setFontScale(BUTTON_SCALE - 0.1f, BUTTON_SCALE - 0.1f);
        downLabel = new Label("DOWN", new Label.LabelStyle(font, Color.WHITE));
        downLabel.setFontScale(BUTTON_SCALE - 0.1f, BUTTON_SCALE - 0.1f);
        pickupLabel = new Label("PICK-UP", new Label.LabelStyle(font, Color.WHITE));
        pickupLabel.setFontScale(BUTTON_SCALE - 0.1f, BUTTON_SCALE - 0.1f);
        rotateLabel = new Label("ROTATE", new Label.LabelStyle(font, Color.WHITE));
        rotateLabel.setFontScale(BUTTON_SCALE - 0.1f, BUTTON_SCALE - 0.1f);
        observeOnOff = new Label("ON/OFF", new Label.LabelStyle(font, Color.WHITE));
        observeOnOff.setFontScale(BUTTON_SCALE - 0.1f, BUTTON_SCALE - 0.1f);
        save = new Label("saved", new Label.LabelStyle(font, Color.WHITE));
        save.setFontScale(0.7f*BUTTON_SCALE , 0.7f*BUTTON_SCALE);
        automaticSave = new Label("changes automatically", new Label.LabelStyle(font, Color.WHITE));
        automaticSave.setFontScale(0.7f*BUTTON_SCALE , 0.7f*BUTTON_SCALE);
        observeControl = new Label("CONTROL", new Label.LabelStyle(font,Color.WHITE));
        observeControl.setFontScale(BUTTON_SCALE - 0.1f);
    }
    private void setButtons(){
        TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();
        buttonStyle.font = getFont();
        buttonStyle.fontColor = Color.WHITE;
        buttonStyle.overFontColor = Color.GRAY;
        back = new TextButton("BACK", buttonStyle);
        back.getLabel().setFontScale(BUTTON_SCALE, BUTTON_SCALE);
        default_keys = new TextButton("RESET TO DEFAULT", buttonStyle);
        default_keys.getLabel().setFontScale(BUTTON_SCALE, BUTTON_SCALE);
//        save = new TextButton("SAVE", buttonStyle);
//        save.getLabel().setFontScale(BUTTON_SCALE, BUTTON_SCALE);
        left = new TextButton(InputController.getInstance().getControlString(InputController.ControlCode.LEFT), buttonStyle);
        left.getLabel().setFontScale(BUTTON_SCALE-0.1f, BUTTON_SCALE-0.1f);
        right = new TextButton(InputController.getInstance().getControlString(InputController.ControlCode.RIGHT), buttonStyle);
        right.getLabel().setFontScale(BUTTON_SCALE-0.1f, BUTTON_SCALE-0.1f);
        jump = new TextButton(InputController.getInstance().getControlString(InputController.ControlCode.JUMP), buttonStyle);
        jump.getLabel().setFontScale(BUTTON_SCALE-0.1f, BUTTON_SCALE-0.1f);
        pick_up = new TextButton(InputController.getInstance().getControlString(InputController.ControlCode.PICK_UP), buttonStyle);
        pick_up.getLabel().setFontScale(BUTTON_SCALE-0.1f, BUTTON_SCALE-0.1f);
        down = new TextButton(InputController.getInstance().getControlString(InputController.ControlCode.DOWN), buttonStyle);
        down.getLabel().setFontScale(BUTTON_SCALE-0.1f, BUTTON_SCALE-0.1f);
        rotate = new TextButton(InputController.getInstance().getControlString(InputController.ControlCode.ROTATE), buttonStyle);
        rotate.getLabel().setFontScale(BUTTON_SCALE-0.1f, BUTTON_SCALE-0.1f);
        on_off = new TextButton(InputController.getInstance().getControlString(InputController.ControlCode.OBS_MODE), buttonStyle);
        on_off.getLabel().setFontScale(BUTTON_SCALE-0.1f, BUTTON_SCALE-0.1f);

        observeControlButton = new TextButton("CURSOR                           ",buttonStyle);
        observeControlButton.getLabel().setFontScale(BUTTON_SCALE-0.1f);

        left.getLabel().setAlignment(Align.left);
        right.getLabel().setAlignment(Align.left);
        jump.getLabel().setAlignment(Align.left);
        pick_up.getLabel().setAlignment(Align.left);
        down.getLabel().setAlignment(Align.left);
        rotate.getLabel().setAlignment(Align.left);
        on_off.getLabel().setAlignment(Align.left);
        observeControlButton.getLabel().setAlignment(Align.left);

        //TODO: add rotate and down as controlButtons
        controlButtons = new TextButton[] {
                left, right, jump, pick_up, on_off, down, rotate
        };

        for (TextButton tb : controlButtons) {
            tb.getLabel().setSize(tb.getWidth(), tb.getHeight());
        }
    }

    @Override
    public void show() {
        background = this.assets.getEntry("background_control", Texture.class);
        Skin skin = new Skin(Gdx.files.internal("skin/uiskin.json"));

        setLabels();
        setButtons();

//        table.top().left();
//        table.add(back).pad(10f).top().left();
//        table.add().expandX();
//        table.add(default_keys).pad(10f).top().right().align(Align.right).row();
        Table table = new Table();
        table.setBackground(new TextureRegionDrawable(new TextureRegion(background)));

        table.row().left().padRight(70f).padTop(160f);
        table.add(movementLabel).padLeft(100);
        table.add(leftLabel);
        table.add(left).width(left.getWidth());
        table.row().left().padRight(70f).padTop(10f);

        table.add().padLeft(100);
        table.add(rightLabel);
        table.add(right).width(right.getWidth());
        table.row().left().padRight(70f).padTop(10f);

        table.add().padLeft(100);
        table.add(jumpLabel);
        table.add(jump).width(jump.getWidth());
        table.row().left().padRight(70f).padTop(10f);

        table.add().padLeft(100);
        table.add(downLabel);
        table.add(down).width(down.getWidth());
        table.row().left().padRight(70f).padTop(10f);

        table.add().padLeft(100);
        table.add(pickupLabel);
        table.add(pick_up).width(pick_up.getWidth());
        table.row().left().padRight(70f).padTop(10f);

        table.add().padLeft(100);
        table.add(rotateLabel);
        table.add(rotate).width(rotate.getWidth());
        table.row().left().padRight(70f).padTop(30f);

        table.add(observationLabel).padLeft(100);
        table.add(observeOnOff);
        table.add(on_off).width(on_off.getWidth());
        table.row().left().padRight(70f).padTop(10f);

        table.add().padLeft(100);
        table.add(observeControl);
        table.add(observeControlButton).width(observeControlButton.getWidth());
        table.row().left().padRight(70f).padTop(10f);

        table.setFillParent(true);
//        table.debugAll();
        stage.addActor(table);

        stage.addActor(back);
        stage.addActor(save);
        stage.addActor(automaticSave);
        stage.addActor(default_keys);

        render(0f);

        Gdx.input.setInputProcessor(stage);
        active = true;
    }

    @Override
    public void render(float delta) {
        if (active) {
            update(delta);
            draw();
        }

        for (int i = 0; i < controlButtons.length; i++) {
            TextButton button = controlButtons[i];

            if (button != null) {
                button.setText(InputController.getInstance().getControlString(CONTROL_CODES[i]));
                button.getLabel().setSize(button.getWidth(), button.getHeight());
            }
        }

        if (listener != null) {
            if (pressedState == 0 && back != null) {
                listener.exitScreen(this, ScreenListener.ExitCode.BACK);

                pressedState = -1;
            }
            else if(pressedState == 1 && default_keys != null && pressed){
                InputController.getInstance().resetControls();

                pressedState = -1;
            }
            else if(pressedState == 2 && save != null){
                pressed = false;

                pressedState = -1;
            }
            else if (pressedState > 2 && pressed && !observeControlButton.isPressed()) {
                TextButton button = controlButtons[pressedState - 3];
                if (button != null) {
                    button.setText("ENTER INPUT");
                    button.align(Align.left);

                    int keycode = ((ControlModeStage) stage).pollFirst();

                    if (keycode != -1) {
                        InputController.ControlCode conflict = InputController.getInstance().setControl(CONTROL_CODES[pressedState - 3], keycode);
                        if (conflict != null) {
//                            int i = 0;
//                            for (InputController.ControlCode c : CONTROL_CODES) {
//                                if (c.equals(conflict)) {
//                                    controlButtons[i].setColor(Color.RED);
//                                    break;
//                                }
//                                i++;
//                            }
                        }

                        pressedState = -1;
                    }
                }
            }
        }
    }

    @Override
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

    @Override
    public void hide() {
        active = false;
    }

    @Override
    public void dispose() {

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
