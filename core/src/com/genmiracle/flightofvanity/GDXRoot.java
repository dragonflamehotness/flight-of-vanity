/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.genmiracle.flightofvanity;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.FillViewport;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.genmiracle.flightofvanity.assets.AssetDirectory;
import com.genmiracle.flightofvanity.audio.AudioController;
import com.genmiracle.flightofvanity.audio.AudioEngine;
import com.genmiracle.flightofvanity.graphics.GameCanvas;
import com.genmiracle.flightofvanity.graphics.cube.CubeController;
import com.genmiracle.flightofvanity.graphics.cube.Renderable;
import com.genmiracle.flightofvanity.instance.Instance;
import com.genmiracle.flightofvanity.instance.InstanceController;
import com.genmiracle.flightofvanity.level.LightController;
import com.genmiracle.flightofvanity.level.WorldController;
import com.genmiracle.flightofvanity.screen.*;
import com.genmiracle.flightofvanity.util.ScreenListener;

/**
 * See: http://blog.xoppa.com/basic-3d-using-libgdx-2/
 * @author Xoppa
 */
public class GDXRoot extends Game implements ScreenListener {
	private Viewport viewport;
	public Camera cam;
	public ModelBatch modelBatch;
	public Model model;
	public ModelInstance instance;
	public Environment environment;
	public TextureRegion[] tr;
	public WorldController wc;
	private GameCanvas canvas;

	private LoadingMode loading;

	private MainMenuMode menu;

	private LevelSelectMode levelSelect;


//	private CubeController cubeController;

	private boolean isBackLevel;

	private InstanceController ic;
	private AudioController ac;

	private AssetDirectory directory;

	private LevelMode levelMode;
	private SettingMode settingScreen;
	private ControlMode controlScreen;

	/** 0 for mainMenu, 1 for setting, 2 for control, 3 for pause*/
	private int previousScreen;


	private static final float SIDE_LENGTH = 12f;

	public void create () {
		viewport = new ExtendViewport(960,540);
		canvas = new GameCanvas();

		loading = new LoadingMode("assets.json", canvas, viewport);
		menu = new MainMenuMode(canvas,viewport);
		levelSelect = new LevelSelectMode(canvas, viewport);

		ac = new AudioController();
		ac.addMusic("Hazed Memory", "HazedMemory.wav",true);
		ac.playMusic("Hazed Memory", true);

		ac.loadAudio();

		settingScreen = new SettingMode(canvas,viewport, ac);
		controlScreen = new ControlMode(canvas,viewport);
		levelMode = new LevelMode(canvas,viewport);
		loading.setScreenListener(this);
		setScreen(loading);

		Preferences prefs = Gdx.app.getPreferences("prefs");
	}

	public void setScreen(Screen screen) {
		super.setScreen(screen);

		if (screen instanceof MainMenuMode) {
			ac.playMusic("Hazed Memory", true);
		} else if (screen instanceof LevelSelectMode) {
			ac.playMusic("Daydreaming", true);
		}
	}

	public void exitScreen(Screen screen, ScreenListener.ExitCode ec) {
		switch (ec) {
			case QUIT:
				Gdx.app.exit();
				break;
			case MENU:
				if (screen instanceof LoadingMode || screen instanceof LevelSelectMode || screen instanceof  LevelMode){
					directory = loading.getAssets();

					menu.setAssetDirectory(directory);
					menu.setScreenListener(this);

					setScreen(menu);

				}
				break;

			case SELECT:
				if (screen instanceof MainMenuMode || screen instanceof LevelMode){
					levelSelect.setAssetDirectory(directory);
					levelSelect.setScreenListener(this);
					levelSelect.setIsVictory(levelMode.isVictory());
//					System.out.println("current level: " + levelMode.getCurrentLevel());
					levelSelect.setCurMaxLevel(levelMode.getCurrMaxLevel());
					setScreen(levelSelect);
				}
				//create selected level
				else if (screen instanceof LevelSelectMode){
					levelMode.setAssetDirectory(directory);
					levelMode.setScreenListener(this);


					ac.loadAudio();
					ac.loadAssets(directory);


					levelMode.setAudioController(ac);
					levelMode.setAssetDirectory(directory);

					levelMode.create(((LevelSelectMode) screen).getSelectedLevel());

					setScreen(levelMode);
				}
				break;
			case SETTING:
				previousScreen = 0;
				if (screen instanceof MainMenuMode){
					isBackLevel = false;
				} else if(screen instanceof LevelMode){
					isBackLevel = true;
				}
				settingScreen.setAssetDirectory(directory);
				settingScreen.setScreenListener(this);
				settingScreen.setFirstTime(true);
				setScreen(settingScreen);
				break;
			case CONTINUE:
				//for clicking Select level in main menu
				if (screen instanceof MainMenuMode) {

					levelMode.setAssetDirectory(directory);
					levelMode.setScreenListener(this);

					ac.loadAudio();
					ac.loadAssets(directory);
//					ac.playMusic("test", true);

					levelMode.setAudioController(ac);
					levelMode.create(1);

					setScreen(levelMode);
				}
//				else if(screen instanceof LevelMode){
//					levelMode.setAssetDirectory(directory);
//					levelMode.setScreenListener(this);
//
//					ac.addMusic("test", "test.wav");
//					ac.addMusic("test2", "test2.wav");
//					ac.addMusic("victory", "victory.wav");
//					ac.addSound("death","death.wav");
//					ac.addMusic("footsteps1", "footsteps/footsteps1.wav");
//					ac.addMusic("footsteps2", "footsteps/footsteps2.wav");
//					ac.addMusic("footsteps3", "footsteps/footsteps3.wav");
//					ac.addMusic("footsteps4", "footsteps/footsteps4.wav");
//					ac.addSound("pickup", "pickup.wav");
////					ac.addSound("footsteps", "assets/audio/footsteps.wav");
//
//					ac.loadAssets(directory);
//					ac.playMusic("test", true);
//
//					levelMode.setAudioController(ac);
//					levelMode.create(((LevelMode) screen).getCurrentLevel());
//
//
//					setScreen(levelMode);
//				}
//
				break;
			case CONTROL:
				previousScreen = 1;
				if (screen instanceof SettingMode){
					controlScreen.setAssetDirectory(directory);
					controlScreen.setScreenListener(this);
					setScreen(controlScreen);
				}
				break;
			case BACK:
				if(screen instanceof SettingMode ){ //enter setting screen from menu and clicked back then go back to setting screen
					if(!isBackLevel) {
						directory = loading.getAssets();
						menu.setAssetDirectory(directory);
						menu.setScreenListener(this);
						setScreen(menu);
					}
					else{
						setScreen(levelMode);
					}
				}
				else if(screen instanceof ControlMode){ //setting clicked control and wants to go back to setting
					settingScreen.setAssetDirectory(directory);
					settingScreen.setScreenListener(this);
					setScreen(settingScreen);
//					exitScreen(controlScreen, ExitCode.SETTING);
				}
				break;

		}
	}

//	@Override
//	public void create() {
////		tr = new TextureRegion[6];
////		for(int i = 0; i < 6; i++){
////			Texture texture = new Texture(Gdx.files.internal("core/assets/image.png"));
////			TextureRegion region = new TextureRegion(texture, 20, 20, 50, 50);
////
////			tr[i] = region;
////		}
////		GameCanvas canvas = new GameCanvas();
//
//	}

	@Override
	public void render() {
		viewport.apply();
		super.render();
		//cubeController.render();

	}

	@Override
	public void dispose() {
		Screen screen = getScreen();
		setScreen(null);
		screen.dispose();
		canvas.dispose();
		canvas = null;

		// Unload all of the resources
		if (directory != null) {
			directory.unloadAssets();
			directory.dispose();
			directory = null;
		}

		loading.dispose();
		levelMode.dispose();

		Renderable.disposeAll();
//		settingScreen.dispose();
//		controlScreen.dispose();
		super.dispose();
	}

	@Override
	public void resize(int width, int height) {
		canvas.setSize(width, height);
		viewport.update(width, height);
		getScreen().resize(width, height);

//		Gdx.graphics.setWindowedMode(width, height);
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
	}
}