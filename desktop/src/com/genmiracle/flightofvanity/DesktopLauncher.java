package com.genmiracle.flightofvanity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Graphics;
import com.genmiracle.flightofvanity.GDXRoot;


// Please note that on macOS your application needs to be started with the -XstartOnFirstThread JVM argument
public class DesktopLauncher {
	public static void main (String[] arg) {
		Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
		//config.setWindowSizeLimits(1080, 1080, 1080, 1080);
		config.setForegroundFPS(60);
		config.setWindowedMode(960,540);
		config.setResizable(true);
		config.setTitle("Flight of Vanity");
		new Lwjgl3Application(new GDXRoot(), config);
	}
}
