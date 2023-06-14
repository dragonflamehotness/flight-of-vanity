package com.genmiracle.flightofvanity.graphics.cube;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.viewport.*;
import com.genmiracle.flightofvanity.InputController;
import com.genmiracle.flightofvanity.assets.AssetDirectory;
import com.genmiracle.flightofvanity.instance.Instance;
import com.genmiracle.flightofvanity.instance.InstanceController;
import com.genmiracle.flightofvanity.level.LightController;
import com.genmiracle.flightofvanity.level.World;
import com.genmiracle.flightofvanity.level.WorldController;
import com.genmiracle.flightofvanity.physics.PhysicsController;
import com.genmiracle.flightofvanity.util.Utilities;
//import com.sun.tools.javac.comp.Environment;

public class CubeController {
    /** Viewport for rendering the cube properly */
    private Viewport viewport;

    /** The Model to be rendered */
    private Model model;
    /** The ModelBatch used to render the cube */
    private ModelBatch modelBatch;
    /** A reusable ModelBuilder to build upon */
    private ModelBuilder modelBuilder;
    /** An instance of the Model to be rendered */
    private ModelInstance mi;
    /** The 3D world environment */
    private Environment env;
    /** The camera within the 3D world */
    private OrthographicCamera cam;
    /** Theta rotation of the camera about the origin */
    private float camTheta;
    /** Phi rotation of the camera about the origin */
    private float camPhi;
    /** Theta rotation of the up vector of the camera */
    private float camUpTheta;
    /** Phi rotation of the up vector of the camera */
    private float camUpPhi;
    /** Starting theta rotation of the camera for interpolation */
    private float camInterpThetaStart;
    /** Starting phi rotation of the camera for interpolation */
    private float camInterpPhiStart;
    /** Starting up theta orientation of the camera for interpolation */
    private float camInterpUpThetaStart;
    /** Starting up phi orientation of the camera for interpolation */
    private float camInterpUpPhiStart;
    /** Ending theta rotation of the camera for interpolation */
    private float camInterpThetaEnd;
    /** Ending phi rotation of the camera for interpolation */
    private float camInterpPhiEnd;
    /** Ending up theta orientation of the camera for interpolation */
    private float camInterpUpThetaEnd;
    /** Ending up phi orientation of the camera for interpolation */
    private float camInterpUpPhiEnd;
    /** Current interpolation ratio (0 at start, 1 at complete) */
    private float interpRatio;
    /** The amount of time (in seconds) it should take for an interpolation to finish */
    private float interpSpeed;
    /** Whether the camera is currently interpolating */
    private boolean interpolation;
    /** Whether the cube is currently in observation mode */
    private boolean observationMode;
    /** Whether the cube has rotated within this current observation mode */
    private boolean observationRotated;
    /** If greater than 0, observationMode cannot be entered into or left */
    private float observationCooldown;

    private Quaternion quatTemp;

    private float doorCountdown;
    private boolean isDoorCountdown;

    /** The default amount of time (in seconds) it should take for an interpolation to finish */
    private static final float DEFAULT_INTERP_SPEED = 1.0f;

    /**
     * A TextureRegion representing the texture that each cube side is built upon
     */
    private TextureRegion baseTexture;
    /** An array of CubeSides, ordered according to the standard indices */
    private CubeSide[] sides;
    /**
     * A list of Pixmaps for each side of the cube, indices corresponding to the
     * standard indices for each side
     */
    private Pixmap[] pms;
    /** The length of a single side in pixels */

    private int pixelLength;

    /** The currently-used instance of WorldController */
    private WorldController wc;
    /** The currently-used instance of InstanceController */
    private InstanceController instC;
    /** The currently-used instance of InputController */
    private InputController inpC;
    /** The currently-used instance of LightController */
    private LightController lc;

    private final float SCALE = WorldController.SIDE_LENGTH / PhysicsController.SIZE;

    private final float OBS_X_CHANGE = 0.2f;
    private final float OBS_Y_CHANGE = 0.2f;

    private int globalCamCounter = 0;

    private Vector3 rotateAxis1;
    private Vector3 rotateAxis2;

    private enum ROTATE_DIRECTION {
        LEFT, RIGHT, UP, DOWN
    };

    private Enum currentDirection = null;
    private boolean rotating = false;

    boolean testing = false;

    /** The AssetDirectory this CubeController uses */
    private AssetDirectory dir;

    private static final boolean TESTING = true;
    private static final float CAMERA_DIST = 20f;
    int currentOrientation = 1;
    private static final Vector3[] vertices = new Vector3[] {
            new Vector3(-1, -1, 1),
            new Vector3(1, -1, 1),
            new Vector3(1, -1, -1),
            new Vector3(-1, -1, -1),
            new Vector3(-1, 1, 1),
            new Vector3(1, 1, 1),
            new Vector3(1, 1, -1),
            new Vector3(-1, 1, -1),
    };

    private static final int[][] sideVertices = new int[][] {
            new int[] { 7, 3, 0, 4 },
            new int[] { 3, 2, 1, 0 },
            new int[] { 2, 6, 5, 1 },
            new int[] { 6, 7, 4, 5 },
            new int[] { 0, 1, 5, 4 },
            new int[] { 7, 6, 2, 3 },
    };

    private static final Vector3[] normals = new Vector3[] {
            new Vector3(-1, 0, 0),
            new Vector3(0, -1, 0),
            new Vector3(1, 0, 0),
            new Vector3(0, 1, 0),
            new Vector3(0, 0, 1),
            new Vector3(0, 0, -1),
    };
    private Vector3 sidePositions[] = new Vector3[] {
            new Vector3(-40, 0, 0),
            new Vector3(0, -40, 0),
            new Vector3(40, 0, 0),
            new Vector3(0, 40, 0),
            new Vector3(0, 0, 40),
            new Vector3(0, 0, -40),
    };

    private static final float PI = (float) Math.PI;

    private static final float[][] CAM_POS = new float[][] {
            new float[] {PI, PI/2, 0, 0, 1.5f * PI, PI/2},
            new float[] {1.5f * PI, PI/2, 0, 0, 0, PI/2},
            new float[] {0, PI/2, 0, 0, PI/2, PI/2},
            new float[] {PI/2, PI/2, 0, 0, PI, PI/2},
            new float[] {0, 0, PI/2, PI/2, 0, PI/2},
            new float[] {0, PI, 1.5f * PI, PI/2, 0, PI/2},

    };

    int currSide = 0;

    private static final float SIDE_LENGTH = 10f;


//    /**
//     * Create a new empty CubeController
//     */
//    public CubeController() {
//        dir = new AssetDirectory("assets.json");
//
//        dir.loadAssets();
//        dir.finishLoading();
//
//        baseTexture = new TextureRegion(dir.getEntry("testBackground", Texture.class));
//
//        init();
//    }

    /**
     * Create a new empty CubeController with the given directory
     *
     * @param dir the AssetDirectory to use
     */
    public CubeController(AssetDirectory dir) {
        this(new TextureRegion(dir.getEntry("testBackground", Texture.class)));
        this.dir = dir;
    }

    /**
     * Create a new empty CubeController with the given background for each side
     *
     * @param baseBackground TextureRegion background for each side
     */
    public CubeController(TextureRegion baseBackground) {
        baseTexture = baseBackground;

        init();
    }

    /** Initializes values that carry across all constructors */
    public void init() {
        env = new Environment();
        modelBuilder = new ModelBuilder();
        pms = new Pixmap[6];
        quatTemp = new Quaternion();
    }

    /**
     * Set the WorldController that this CubeController refers to
     *
     * @param wc WorldController to be used
     */
    public void setWorldController(WorldController wc) {
        this.wc = wc;
    }

    /**
     * Set the InstanceController that this CubeController refers to
     *
     * @param ic InstanceController to be used
     */
    public void setInstanceController(InstanceController ic) {
        instC = ic;
    }

    public void setLightController(LightController lc) {this.lc = lc;}

    public void setInputController(InputController ic) {inpC = ic;}

    /**
     * Create a model for the cube with the given pixel length
     *
     * @param pixelLength the length (in pixels) of each side
     */
    public void createCube(int pixelLength) {
        this.pixelLength = pixelLength;
        Renderable.setScale(SCALE);

        env.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
        env.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.5f));

        modelBatch = new ModelBatch();

        cam = new OrthographicCamera(40, 40);
//        viewport = new ExtendViewport(540, 540, cam);

//        viewport.apply();
        cam.position.set(-40f, 0f, 0f);
        cam.lookAt(0, 0, 0);
        cam.rotateAround(new Vector3(0, 0, 0), new Vector3(1, 0, 0), 90);
        cam.near = 5f;
        cam.far = 300f;
        cam.update();

        camTheta = (float) Math.PI;
        camPhi = (float) Math.PI / 2f;
        interpRatio = 1;

        sides = new CubeSide[6];

        modelBuilder.begin();

        for (int i = 0; i < 6; i++) {
            CubeSide cs = new CubeSide(modelBuilder, baseTexture, pixelLength);
            sides[i] = cs;

            Vector3 v0 = vertices[sideVertices[i][0]];
            Vector3 v1 = vertices[sideVertices[i][1]];
            Vector3 v2 = vertices[sideVertices[i][2]];
            Vector3 v3 = vertices[sideVertices[i][3]];

            cs.create(v0, v1, v2, v3, normals[i], SIDE_LENGTH);

        }

        model = modelBuilder.end();
        mi = new ModelInstance(model);
    }

    /**
     * Determines which sides can currently be seen by the camera.
     *
     * @return a string that has the index of each side that can be seen appended
     *         somewhere in the string
     */
    public String getSides() {
        String seen = "";

        if (cam.position.x > 0.1f) {
            seen += "2";
        } else if (cam.position.x < -0.1f) {
            seen += "0";
        }

        if (cam.position.y > 0.1f) {
            seen += "3";
        } else if (cam.position.y < -0.1f) {
            seen += "1";
        }

        if (cam.position.z > 0.1f) {
            seen += "4";
        } else if (cam.position.z < -0.1f) {
            seen += "5";
        }

        return seen;
    }

    public void reset(int side, int orientation) {
        currSide = side;
        currentOrientation = orientation;
        observationMode = false;

        isDoorCountdown = false;

        setCenterSide(side, orientation, 0.1f);
    }

    public float getCameraRotation() {
        float camAngle = -(float) Math.atan2(cam.up.x, cam.up.y) * MathUtils.radiansToDegrees + 180;
        return camAngle;
    }

    /**
     * Render the cube in its 3D environment
     */
    public void render(float dt) {
        int wWidth = Gdx.graphics.getBackBufferWidth();
        int wHeight = Gdx.graphics.getBackBufferHeight();

        Gdx.gl.glViewport(0, 0, wWidth, wHeight);
        cam.viewportHeight = 25;
        cam.viewportWidth = 25f * wWidth / wHeight;
        cam.update();

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        modelBuilder.begin();
        String sideIndexes = getSides();

        //System.out.println(cam.position);
        for (int i = 0; i < 6; i++) {
            if (sideIndexes.indexOf("" + i) != -1) {
                Pixmap sidePixmap = wc.getPixmapAtSide(i);
                pms[i] = sidePixmap;
            } else {
                pms[i] = null;
            }
        }

        lc.render(pms, SCALE);
        instC.render(pms);


        for (int i = 0; i < 6; i++) {
            if (sideIndexes.indexOf("" + i) != -1) {
                CubeSide cs = sides[i];
                cs.setPixmap(pms[i]);
                cs.render();
            }
        }

        int playerSide = instC.getPlayer().getSide();
        if (wc.getDoor().isActivated() && wc.getDoor().getSide() != currSide && !isDoorCountdown) {
            isDoorCountdown = true;
            doorCountdown = 1.7f;
            setCenterSide(wc.getDoor().getSide(), instC.getPhysicsController().getSide(wc.getDoor().getSide()).getOrientation());
        }

        if (doorCountdown > 0 && isDoorCountdown) {
            doorCountdown -= dt;
            if (doorCountdown <= 0) {
                doorCountdown = 0;
                setCenterSide(playerSide, instC.getPhysicsController().getSide(playerSide).getOrientation());
            }
        }

        if ((!isDoorCountdown || doorCountdown <= 0) && playerSide != currSide) {
            setCenterSide(playerSide, instC.getPhysicsController().getSide(playerSide).getOrientation());
        }

        if (observationCooldown > 0) {
            observationCooldown -= dt;
            if (observationCooldown <= 0) {
                observationCooldown = 0;
            }
        }

        if (inpC.didPressObservationMode() && Utilities.equalFloats(observationCooldown, 0)) {
            observationMode = !observationMode;
            observationCooldown = 0.5f;
            inpC.setCursorCatched(observationMode);
        }

        if (!observationMode) {
            if (observationRotated) {
                camPhi = (float) Math.acos(cam.position.z / 40f);
                camTheta = (float) (Math.atan2(cam.position.y, cam.position.x) + 2 * PI) % (2 * PI);
                camUpPhi = (float) Math.acos(cam.up.z);
                camUpTheta = ((float) Math.atan2(cam.up.y, cam.up.x) + 2 * PI) % (2 * PI);

                setCenterSide(currSide, currentOrientation);
                observationRotated = false;

                cam.update();
            }

            if (interpolation) {
                interpRatio += dt / interpSpeed;
                if (interpRatio > 1) {
                    interpRatio = 1f;
                    interpolation = false;
                }

                camTheta = Utilities.bezier(camInterpThetaStart, camInterpThetaEnd, interpRatio, camInterpThetaEnd)
                        % (float) (Math.PI * 2);
                camPhi = Utilities.bezier(camInterpPhiStart, camInterpPhiEnd, interpRatio, camInterpPhiEnd);
                camUpTheta = Utilities.bezier(camInterpUpThetaStart, camInterpUpThetaEnd, interpRatio, camInterpUpThetaEnd)
                        % (float) (Math.PI * 2);
                camUpPhi = Utilities.bezier(camInterpUpPhiStart, camInterpUpPhiEnd, interpRatio, camInterpUpPhiEnd);

                updateCameraPosition(true);
//
//            System.out.println("Up Theta: " + camUpTheta);
//            System.out.println("Up Phi: " + camUpPhi);
//            System.out.println(cam.up.toString());

                cam.update();
            }
        }

        if (observationMode) {
            int dx = inpC.getDx();
            int dy = inpC.getDy();
            if (inpC.getInstance().isObserveControlKey()) {
                if(Gdx.input.isKeyPressed(Input.Keys.D))
                    dx-=10;
                if(Gdx.input.isKeyPressed(Input.Keys.A))
                    dx+=10;
                if(Gdx.input.isKeyPressed(Input.Keys.W))
                    dy+=10;
                if(Gdx.input.isKeyPressed(Input.Keys.S))
                    dy-=10;
            }



            Vector3 right = cam.direction.crs(cam.up);

            cam.rotateAround(Vector3.Zero, right, -dy * OBS_Y_CHANGE);
            cam.rotateAround(Vector3.Zero, cam.up, -dx * OBS_X_CHANGE);

            observationRotated = true;

            cam.lookAt(0, 0, 0);
            cam.update();

//            camTheta = (camTheta + dx * OBS_THETA_CHANGE * dt) % (2 * PI);
//            camPhi += dy * OBS_PHI_CHANGE * dt;
//
////            System.out.println("Theta: " + camTheta);
////            System.out.println("Phi: " + camPhi);
//
//            if (camPhi > PI + Utilities.EPSILON) {
//                camPhi = PI - (camPhi % PI);
//                camTheta *= -1;
//            }
//
//            updateCameraPosition(false);

//            cam.lookAt(0, 0, 0);

//            cam.update();
        }

        model = modelBuilder.end();
        mi = new ModelInstance(model);

        // Key presses are only for testing puposes. Actual implementation will take in
        // integers representing side
        if (testing) {
            int newSide = -1;
            if (!rotating && Gdx.input.isKeyPressed(Input.Keys.A)) {
                if (currentOrientation == 1){
                    if(currSide <= 3) {
                        newSide = currSide - 1;
                        if(newSide < 0)
                            newSide += 4;
                    }
                    else
                        newSide = 3;

                } else if (currentOrientation == 2) {
                    if(currSide <= 3) {
                        newSide = 4;
                    } else if (currSide == 4) {
                        newSide = 2;

                    }else newSide = 0;
                } else if (currentOrientation == 3){
                    if(currSide <= 3) {
                        newSide = currSide + 1;
                        newSide %= 4;
                    }
                    else
                        newSide = 1;

                }else if (currentOrientation == 4) {
                    if(currSide <= 3) {
                        newSide = 5;
                    } else if (currSide == 4) {
                        newSide = 0;

                    }else newSide = 2;
                }
                setCenterSide(newSide, -1);
            } else if (!rotating && Gdx.input.isKeyPressed(Input.Keys.D)) {
                if (currentOrientation == 1){
                    if(currSide <= 3) {
                        newSide = currSide + 1;
                            newSide %= 4;
                    }
                    else
                        newSide = 1;

                } else if (currentOrientation == 2) {
                    if(currSide <= 3) {
                        newSide = 5;
                    } else if (currSide == 4) {
                        newSide = 0;

                    }else newSide = 2;
                } else if (currentOrientation == 3){
                    if(currSide <= 3) {
                        newSide = currSide - 1;
                        if(newSide < 0)
                            newSide += 4;
                    }
                    else
                        newSide = 3;

                }else if (currentOrientation == 4) {
                    if(currSide <= 3) {
                        newSide = 4;
                    } else if (currSide == 4) {
                        newSide = 2;

                    }else newSide = 0;
                }
                setCenterSide(newSide, -1);
            } else if (!rotating && Gdx.input.isKeyPressed(Input.Keys.W)) {
                if (currentOrientation == 1){
                    if(currSide <= 3) {
                        newSide = 4;
                    }
                    else if(currSide == 4)
                        newSide = 2;
                    else newSide = 0;

                } else if (currentOrientation == 2) {
                    if(currSide <= 3) {
                        newSide = currSide + 1;
                        newSide %= 4;
                    }
                    else
                        newSide = 1;

                } else if (currentOrientation == 3){
                    if(currSide <= 3) {
                        newSide = 5;
                    }else if(currSide == 4)
                        newSide = 0;
                    else newSide = 2;


                }else if (currentOrientation == 4) {
                    if(currSide <= 3) {
                        newSide = currSide - 1;
                        if(newSide < 0)
                            newSide += 4;
                    }
                    else
                        newSide = 3;

                }
                setCenterSide(newSide, -1);
            } else if (!rotating && Gdx.input.isKeyPressed(Input.Keys.S)) {
                if (currentOrientation == 1){
                    if(currSide <= 3) {
                        newSide = 5;
                    }
                    else if(currSide == 4)
                        newSide = 0;
                    else newSide = 2;

                } else if (currentOrientation == 2) {
                    if(currSide <= 3) {
                        newSide = currSide - 1;
                        if(newSide < 0)
                            newSide += 4;
                    }
                    else
                        newSide = 3;

                } else if (currentOrientation == 3){
                    if(currSide <= 3) {
                        newSide = 4;
                    }else if(currSide == 4)
                        newSide = 2;
                    else newSide = 0;


                }else if (currentOrientation == 4) {
                    if(currSide <= 3) {
                        newSide = currSide + 1;
                        newSide %= 4;
                    }
                    else
                        newSide = 1;

                }
                setCenterSide(newSide, -1);
            }
        }

        if (globalCamCounter < 90 && rotating == true) {
            updateRotation(currentDirection);
        } else{
            //System.out.println(globalCamCounter);
            globalCamCounter = 0;
            rotating = false;
        }

        modelBatch.begin(cam);
        modelBatch.setCamera(cam);
        modelBatch.render(mi);
        modelBatch.end();
//        System.out.println(currSide);
    }

    /**
     * Update the rotation depending on what keys are pressed
     */
    public void updateRotation(Enum dir) {
        float sideangle = 0;
        float upangle = 0;
        if (dir == ROTATE_DIRECTION.DOWN) {
            sideangle = -10f;
        } else if (dir == ROTATE_DIRECTION.UP) {
            sideangle = 10;
        } else if (dir == ROTATE_DIRECTION.RIGHT) {
            sideangle = 0f;
            upangle = 10f;
        } else if (dir == ROTATE_DIRECTION.LEFT) {
            sideangle = 0f;
            upangle = -10f;
        }

        cam.rotateAround(new Vector3(0, 0, 0), rotateAxis1, sideangle);
        cam.rotateAround(new Vector3(0, 0, 0), rotateAxis2, upangle);
//         cam.rotateAround(new Vector3(0, 0, 0), new Vector3(0, 1, 0), upangle);
//        cam.rotateAround(new Vector3(0, 0, 0), new Vector3(1, 0, 0), sideangle);

        // System.out.println(cam.position);
        globalCamCounter += 10;
        cam.update();

    }

    public void updateRotation(float yaw, float pitch, float roll) {
        quatTemp.setEulerAngles(yaw, pitch, roll);

        cam.rotate(quatTemp);
        cam.lookAt(0, 0, 0);

        cam.update();
    }

    /**
     * Sets the camera position such that it is placed according to the spherical coordinates given by
     * <code>theta</code> and <code>phi</code>, with an interpolation that takes
     * <code>DEFAULT_INTERP_SPEED</code> seconds.
     *
     * @param theta ending theta value of the camera in spherical coordinates
     * @param phi ending phi value of the camera in spherical coordinates
     */
    public void setCameraPosition(float theta, float phi, float upTheta, float upPhi) {
        setCameraPosition(theta, phi, upTheta, upPhi, DEFAULT_INTERP_SPEED);
    }

    /**
     * Sets the camera position such that it is placed according to the spherical coordinates given by
     * <code>theta</code> and <code>phi</code>, with an interpolation that takes <code>speed</code> seconds.
     *
     * @param theta ending theta value of the camera in spherical coordinates
     * @param phi ending phi value of the camera in spherical coordinates
     * @param speed length of interpolation in seconds
     */
    public void setCameraPosition(float theta, float phi, float upTheta, float upPhi, float speed) {
        boolean endPhiEdge = Utilities.equalFloats(phi, 0) || Utilities.equalFloats(phi, PI);
        boolean endUpPhiEdge = Utilities.equalFloats(upPhi, 0) || Utilities.equalFloats(upPhi, PI);

//        camInterpThetaStart = camTheta;
        camInterpThetaStart = !endPhiEdge && (Utilities.equalFloats(camPhi, 0) || Utilities.equalFloats(camPhi, PI)) ? theta : camTheta;
        camInterpPhiStart = camPhi;
//        camInterpUpThetaStart = camUpTheta;
        camInterpUpThetaStart = !endUpPhiEdge && (Utilities.equalFloats(camUpPhi, 0) || Utilities.equalFloats(camUpPhi, PI)) ? upTheta : camUpTheta;
        camInterpUpPhiStart = camUpPhi;

        if (Math.abs(theta - camInterpThetaStart) > Math.PI) {
            theta += (theta > camInterpThetaStart ? -1 : 1) * 2 * PI;
        }
        if (Math.abs(upTheta - camInterpUpThetaStart) > Math.PI) {
            upTheta += (upTheta > camInterpUpThetaStart ? -1 : 1) * 2 * PI;
        }

        camInterpThetaEnd = endPhiEdge ? camTheta : theta;
        camInterpPhiEnd = phi;
        camInterpUpThetaEnd = endUpPhiEdge ? camUpTheta : upTheta;
        camInterpUpPhiEnd = upPhi;

        interpSpeed = speed;

        interpolation = true;

        interpRatio = 0;
    }

    public void updateCameraPosition(boolean up) {
        cam.position.set((float) (Math.cos(camTheta) * Math.abs(Math.sin(camPhi)) * 40),
                (float) (Math.sin(camTheta) * Math.abs(Math.sin(camPhi)) * 40),
                (float) Math.cos(camPhi) * 40);

        if (up) {
            cam.up.set((float) (Math.cos(camUpTheta) * Math.abs(Math.sin(camUpPhi))),
                    (float) (Math.sin(camUpTheta) * Math.abs(Math.sin(camUpPhi))),
                    (float) Math.cos(camUpPhi));

            cam.direction.set(-cam.position.x, -cam.position.y, -cam.position.z);
        }
    }

    public void setCenterSide(int newSide, int orientation) {
        setCenterSide(newSide, orientation, DEFAULT_INTERP_SPEED);
    }

    public void setCenterSide(int newSide, int orientation, float interpSpeed) {
        float[] camPos = CAM_POS[newSide];

        float upTheta = ((orientation % 2 == 0 ? camPos[2] : camPos[4]) + (orientation > 1 ? PI : 0)) % (PI * 2f);
        float upPhi = (orientation % 2 == 0 ? camPos[3] : camPos[5]);
        if (orientation > 1) {
            upPhi = PI - upPhi;
        }

        setCameraPosition(camPos[0], camPos[1], upTheta, upPhi, interpSpeed);
        currSide = newSide;
        currentOrientation = orientation;

//        rotating = false;
//        int prevSide = currSide;
//        System.out.println(prevSide);
//        if (prevSide == 0) {
//            rotateAxis1 = new Vector3(0, 1, 0);
//            rotateAxis2 = new Vector3(0, 0, 1);
//        }
//        if (prevSide == 1) {
//            rotateAxis1 = new Vector3(-1, 0, 0);
//            rotateAxis2 = new Vector3(0, 0, 1);
//        }
//        if (prevSide == 2) {
//            rotateAxis1 = new Vector3(0, -1, 0);
//            rotateAxis2 = new Vector3(0, 0, 1);
//        }
//        if (prevSide == 3) {
//            rotateAxis1 = new Vector3(1, 0, 0);
//            rotateAxis2 = new Vector3(0, 0, 1);
//        }
//        if (prevSide == 4){
//            rotateAxis1 = new Vector3(0, 1, 0);
//            rotateAxis2 = new Vector3(1, 0, 0);
//        }
//        if (prevSide == 5){
//            rotateAxis1 = new Vector3(0, 1, 0);
//            rotateAxis2 = new Vector3(-1, 0, 0);
//        }
//
//        if ((prevSide == 0 && newSide == 1) || (prevSide == 1 && newSide == 2) || (prevSide == 2 && newSide == 3)
//                || (prevSide == 3 && newSide == 0)) {
//
//            rotating = true;
//            currentDirection = ROTATE_DIRECTION.RIGHT;
//        }
//        if (prevSide >= 0 && prevSide <= 3 && newSide == 4) {
//            rotating = true;
//            currentDirection = ROTATE_DIRECTION.UP;
//            if(currSide == 1)
//                currentOrientation += 3;
//            if(currSide == 2)
//                currentOrientation += 2;
//            if(currSide == 3)
//                currentOrientation += 1;
//            if(currentOrientation > 4)
//                currentOrientation -= 4;
//        }
//        if (prevSide >= 0 && prevSide <= 3 && newSide == 5) {
//            rotating = true;
//            currentDirection = ROTATE_DIRECTION.DOWN;
//            if(currSide == 1)
//                currentOrientation += 1;
//            if(currSide == 2)
//                currentOrientation += 2;
//            if(currSide == 3)
//                currentOrientation +=3;
//            if(currentOrientation > 4)
//                currentOrientation -= 4;
//        }
//        if ((prevSide == 0 && newSide == 3) || (prevSide == 3 && newSide == 2) || (prevSide == 2 && newSide == 1)
//                || (prevSide == 1 && newSide == 0)) {
//            rotating = true;
//            currentDirection = ROTATE_DIRECTION.LEFT;
//        }
//        if (prevSide == 4 && newSide == 1) {
//            rotating = true;
//            currentDirection = ROTATE_DIRECTION.RIGHT;
//            currentOrientation+=1;
//            if(currentOrientation > 4)
//                currentOrientation -=4;
//        }
//        if (prevSide == 4 && newSide == 2) {
//            rotating = true;
//            currentDirection = ROTATE_DIRECTION.UP;
//            currentOrientation+=2;
//            if(currentOrientation > 4)
//                currentOrientation -=4;
//        }
//        if (prevSide == 4 && newSide == 0) {
//            rotating = true;
//            currentDirection = ROTATE_DIRECTION.DOWN;
//            //currentOrientation = currentOrientation;
//        }
//        if (prevSide == 4 && newSide == 3) {
//            rotating = true;
//            currentDirection = ROTATE_DIRECTION.LEFT;
//            currentOrientation+=3;
//            if(currentOrientation > 4)
//                currentOrientation -=4;
//        }
//        if (prevSide == 5 && newSide == 1) {
//            rotating = true;
//            currentDirection = ROTATE_DIRECTION.RIGHT;
//            currentOrientation+=3;
//            if(currentOrientation>4)
//                currentOrientation-=4;
//        }
//        if (prevSide == 5 && newSide == 0) {
//            rotating = true;
//            currentDirection = ROTATE_DIRECTION.UP;
//
//            //currentOrientation = currentOrientation;
//        }
//        if (prevSide == 5 && newSide == 2) {
//            rotating = true;
//            currentDirection = ROTATE_DIRECTION.DOWN;
//            currentOrientation+=2;
//            if(currentOrientation > 4)
//                currentOrientation -=4;
//        }
//        if (prevSide == 5 && newSide == 3) {
//            rotating = true;
//            currentDirection = ROTATE_DIRECTION.LEFT;
//            currentOrientation+=1;
//            if(currentOrientation > 4)
//                currentOrientation -=4;
//        }
//        currSide = newSide;
    }

    public void reset() {
        observationMode = false;
        inpC.setCursorCatched(false);
    }

    /**
     * Dispose of all objects within its cube that need to be disposed
     */
    public void dispose() {
        modelBatch.dispose();
//        model.dispose();
    }
}
