package com.genmiracle.flightofvanity;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerListener;

import java.util.Locale;

/**
 * This class is a safe wrapper for the LibGDX Controllers class
 *
 * The existing Controllers class has been known to segfault on some systems due to the
 * native libraries that it uses.  We need a way to "turn it off" in that case. This
 * wrapper does just that, as it controls access via the GDXAppSettings value.
 */
public class Controllers {
    /**
     * The singleton for this class
     */
    static private Controllers singleton;

    /**
     * Whether controller support is active
     */
    private boolean active;

    /**
     * Creates a new Controllers wrapper with active status.
     */
    private Controllers() {
        this(true);
    }

    /**
     * Creates a new Controllers wrapper with the given status.
     *
     * @param active Whether to activate the controllers
     */
    private Controllers(boolean active) {
        this.active = active;
    }

    /**
     * Toggles the active status of this wrapper
     *
     * @param active The active status of the wrapper
     */
    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * Returns the active status of this wrapper
     *
     * @return the active status of this wrapper
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Returns the wrapper singleton
     * <p>
     * This method constructs a new wrapper if one did not previously exist.
     *
     * @return the wrapper singleton
     */
    public static Controllers get() {
        if (singleton == null) {
            singleton = new Controllers();
        }
        return singleton;
    }

    /**
     * Returns an array of connected {@link Controller} instances.
     * <p>
     * If the wrapper is not active, the array will be empty. This method should only
     * be called on the rendering thread.
     *
     * @return an array of connected {@link Controller} instances.
     */
    public Array<Controller> getControllers() {
        if (active) {
            try {
                return com.badlogic.gdx.controllers.Controllers.getControllers();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return new Array<Controller>();
    }

    /**
     * Returns an array of connected {@link XBoxController} instances.
     * <p>
     * If the wrapper is not active, the array will be empty. This method should only
     * be called on the rendering thread.
     *
     * @return an array of connected {@link XBoxController} instances.
     */
    public Array<XBoxController> getXBoxControllers() {
        Array<XBoxController> xBoxControllers = new Array<XBoxController>();
        if (active) {
            try {
                for (Controller controller : com.badlogic.gdx.controllers.Controllers.getControllers()) {
                    String name = controller.getName().toLowerCase();
                    if (name.contains("xbox") || name.contains("pc")) {
                        xBoxControllers.add(new XBoxController(controller));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return xBoxControllers;
    }
}
