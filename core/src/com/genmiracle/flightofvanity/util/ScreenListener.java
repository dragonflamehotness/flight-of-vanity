package com.genmiracle.flightofvanity.util;

import com.badlogic.gdx.Screen;

/**
 * This is different from the physics lab ~
 */

public interface ScreenListener {
    public enum ExitCode {
        QUIT, MENU, SELECT, SETTING, CONTINUE, CONTROL, BACK
    };
    /**
     * The given screen has made a request to exit its player mode.
     *
     * The value exitCode can be used to implement menu options.
     *
     * @param screen   The screen requesting to exit
     * @param exitCode The state of the screen upon exit
     */
    public void exitScreen(Screen screen, ScreenListener.ExitCode exitCode);
}
