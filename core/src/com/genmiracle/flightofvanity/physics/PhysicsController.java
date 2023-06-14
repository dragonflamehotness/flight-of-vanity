package com.genmiracle.flightofvanity.physics;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.RayCastCallback;
import com.badlogic.gdx.physics.box2d.World;
import com.genmiracle.flightofvanity.instance.Instance;
import com.genmiracle.flightofvanity.instance.InstanceController;
import com.genmiracle.flightofvanity.instance.mechanisms.Mechanism;
import com.genmiracle.flightofvanity.instance.model.Mirror;
import com.genmiracle.flightofvanity.instance.model.Player;
import com.genmiracle.flightofvanity.physics.obstacle.BoxObstacle;
import com.genmiracle.flightofvanity.physics.obstacle.Obstacle;
import com.genmiracle.flightofvanity.physics.raycasting.LightRayCastCallback;
import com.genmiracle.flightofvanity.physics.raycasting.Ray2D;
import com.genmiracle.flightofvanity.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;

public class PhysicsController {
    /** The default side size of the world */
    public static final float SIZE = 100f;//9.3f;

    public static class Hitinfo {
        private Instance hit;
        private boolean hitPlatform;
        private Vector2 normal;
        private Vector2 incidence;
        private int side;
        private float x;
        private float y;
        private Ray2D ray2;
        private ArrayList<ArrayList<Vector3>> lightPositions;
        private ArrayList<Vector3> lastLightPosLine;

        public Hitinfo(Instance hit, Vector2 normal, Vector2 incidence, int side, float x, float y) {
            this.hit = hit;

            this.normal = (normal == null)? new Vector2(): new Vector2(normal);
            this.incidence = (incidence == null)? new Vector2(): new Vector2(incidence);
            this.side = side;
            this.x = x;
            this.y = y;

            lightPositions = new ArrayList<>();
            lastLightPosLine = new ArrayList<>();
            hitPlatform = false;

            lightPositions.add(lastLightPosLine);
        }

        public ArrayList<ArrayList<Vector3>> getLightPositions(){
            return lightPositions;
        }

        public void clearLightPositions() {
            hitPlatform = false;
            hit = null;

            lightPositions.clear();
            lastLightPosLine.clear();

            lightPositions.add(lastLightPosLine);
        }

        public void addLightPosition(Vector3 pos) {
            lastLightPosLine.add(pos);
        }

        public void createNewLine() {
            lastLightPosLine = new ArrayList<>();
            lightPositions.add(lastLightPosLine);
        }

        public float getX() {
            return x;
        }

        public float getY() {
            return y;
        }

        public Instance getHit() {
            return hit;
        }

        public void setLastRay(Ray2D ray2) {
            this.ray2 = ray2;
        }

        public Ray2D getLastRay() {
            return ray2;
        }

        public boolean hasHitPlatform() {
            return hitPlatform;
        }

        public int getSide() {
            return side;
        }

        /**
         * Returns the position of where the light will start if goes to another side
         * @return
         */
        public Vector2 startLight(){
            return null;
        }

        public Vector2 getIncidence() {
            return incidence;
        }

        public Vector2 getNormal() {
            return normal;
        }

        public void setHit(Instance hit) {
            this.hit = hit;
        }

        public void setIncidence(Vector2 incidence) {
            this.incidence.set(incidence);
        }

        public void setHitPlatform(boolean hitPlatform) {
            this.hitPlatform = hitPlatform;
        }

        public void setNormal(Vector2 normal) {
            this.normal.set(normal);
        }

        public void setSide(int side) {
            this.side = side;
        }

        public void setX(float x) {
            this.x = x;
        }

        public void setY(float y) {
            this.y = y;
        }
    }

//    /** A list of obstacles within the current world */
//    private ArrayList<Obstacle> obstacles;
//
//    /** A list of dynamic obstacles within the current world */
//    private ArrayList<Obstacle> dynamicObstacles;

    /** WORLD */
    /** A list of all the sides of the cube, in a physics world */
    private PhysicsSide[] sides;
    /** A list of all the sides of the cube, such that the side at index 0 is also at position 0 */
    private PhysicsSide[] sidePositions;
    private int centerSideIndex;

    /** The current world, in Box2D form */
    private World world;

    /** The width of a side in the current level */
    private float sideWidth;

    private Obstacle cameraFocus;

    private HashMap<String, HashMap<String, Object>> savedFrames;

    /** DATA STRUCTURES */
    private HashMap<Body, Obstacle> bodyMap;
    private HashMap<Obstacle, Instance> obstacleMap;

    /** RAYCASTING */
    private LightRayCastCallback rcc;

    private Vector2[] corners;

    /** CACHING */
    private static Vector2 v2temp = new Vector2();
    private static Vector2 v2temp2 = new Vector2();

    private static Hitinfo hitTemp = new Hitinfo(null, null, null, 0, 0, 0);


    /** The default acceleration for gravity in the y-axis */
    public static final float GRAVITY = -60f;


    private static PhysicsController currInstance = null;

    /**
     * Creates a new PhysicsController object.
     *
     * @param size length of a single side of the 3D cube
     */
    public PhysicsController(float size) {
        init(size, GRAVITY);
    }

    /**
     * Creates a new PhysicsController object.
     *
     * @param size length of a single side of the 3D cube
     */
    public PhysicsController(float size, float gravity) {
        init(size, gravity);
    }

    private void init(float size, float gravity) {
        sideWidth = size;

        corners = new Vector2[] {
                new Vector2(-size / 2, size/2),
                new Vector2(size / 2, size/2),
                new Vector2(size / 2, -size/2),
                new Vector2(-size / 2, -size/2),
        };

        generateWorld(gravity);

        bodyMap = new HashMap<>();
        obstacleMap = new HashMap<>();

        savedFrames = new HashMap<>();

        world.setContactListener(new CollisionHandler(bodyMap, obstacleMap));

        centerSideIndex = -1;

        rcc = new LightRayCastCallback(bodyMap, 0);
        sides = new PhysicsSide[6];
        sidePositions = new PhysicsSide[6];
        for (int i = 0; i < 6; i++) {
            sides[i] = new PhysicsSide(world, i, sideWidth);
        }

        for (int i = 0; i < 6; i++) {
            PhysicsSide s = sides[i];

            switch (i) {
                case 0:
                    s.setNeighbor(0, sides[4], 3);
                    s.setNeighbor(1, sides[1], 3);
                    s.setNeighbor(2, sides[5], 3);
                    s.setNeighbor(3, sides[3], 1);
                    s.setNeighbor(4, sides[2], 0);
                    break;
                case 1:
                    s.setNeighbor(0, sides[4], 2);
                    s.setNeighbor(1, sides[2], 3);
                    s.setNeighbor(2, sides[5], 0);
                    s.setNeighbor(3, sides[0], 1);
                    s.setNeighbor(4, sides[3], 0);
                    break;
                case 2:
                    s.setNeighbor(0, sides[4], 1);
                    s.setNeighbor(1, sides[3], 3);
                    s.setNeighbor(2, sides[5], 1);
                    s.setNeighbor(3, sides[1], 1);
                    s.setNeighbor(4, sides[0], 0);
                    break;
                case 3:
                    s.setNeighbor(0, sides[4], 0);
                    s.setNeighbor(1, sides[0], 3);
                    s.setNeighbor(2, sides[5], 2);
                    s.setNeighbor(3, sides[2], 1);
                    s.setNeighbor(4, sides[1], 0);
                    break;
                case 4:
                    s.setNeighbor(0, sides[3], 0);
                    s.setNeighbor(1, sides[2], 0);
                    s.setNeighbor(2, sides[1], 0);
                    s.setNeighbor(3, sides[0], 0);
                    s.setNeighbor(4, sides[5], 2);
                    break;
                case 5:
                    s.setNeighbor(0, sides[1], 2);
                    s.setNeighbor(1, sides[2], 2);
                    s.setNeighbor(2, sides[3], 2);
                    s.setNeighbor(3, sides[0], 2);
                    s.setNeighbor(4, sides[4], 2);
                    break;
            }
        }
    }

    /**
     * Sets the width of a side in the current level, for wrap-around purposes
     *
     * @param width width of a side in the current level
     */
    public void setSideWidth(float width) {
        sideWidth = width;
    }

    /**
     * Generates a new world using a default value of -9.81f
     */
    public void generateWorld() {
        clear();
        world = new World(new Vector2(0, GRAVITY), true);
    }

    /**
     * Generates a new world using the given gravity value
     *
     * @param gravity acceleration of gravity in the y-axis
     */
    public void generateWorld(float gravity) {
        clear();
        world = new World(new Vector2(0, gravity), true);
    }

    /**
     * Clears the current world, and all obstacles within it.
     */
    public void clear() {
        if (world == null) {
            return;
        }

        for (PhysicsSide side : sides) {
            side.clear();
        }
        world.clearForces();
    }

    /**
     * Adds the given obstacle as a physical body in the current level
     *
     * @param obs obstacle to be added as a physical body
     * @param side side the obstacle is within
     * @param x local x-position of the obstacle within its side
     * @param y local y-position of the obstacle within its side
     */
    public void addObstacle(Obstacle obs, int side, float x, float y) {
        obs.activatePhysics(world);
        obs.setPosition(sides[side].getWorldPos(x, y));
        obs.setSide(side);

        sides[side].addObstacle(obs);
        bodyMap.put(obs.getBody(), obs);
    }

    public Obstacle addPlatform(int side, float x, float y, float width, float height, int constructValue) {
        Obstacle obs = new BoxObstacle(width, height);

        obs.setBodyType(BodyDef.BodyType.StaticBody);
        if (constructValue != -1) {
            obs.setName("platform");
        } else {
            obs.setName("constructPlatform");
        }

        addObstacle(obs, side, x, y);

        return obs;
    }

    public void addInstance(Instance ins, int side, float x, float y) {
        Obstacle obs = ins.getObstacle();
        obstacleMap.put(obs, ins);
        if (ins instanceof Player) {
            obs.getFilterData().categoryBits = PhysicsConstants.PLAYER;
//            obs.getFilterData().maskBits = PhysicsConstants.MASK_PLAYER;
        }
        if (ins instanceof Mechanism){
            obs.getFilterData().categoryBits = PhysicsConstants.MECHANISM;
//            obs.getFilterData().maskBits = PhysicsConstants.MASK_MECHANISM;
        }
        if (ins instanceof Mirror){
            obs.getFilterData().categoryBits = PhysicsConstants.MIRROR;
//            obs.getFilterData().maskBits = PhysicsConstants.MASK_MECHANISM;
        }
        if(ins.getName().equals("hint") || ins.getName().equals("buttons")){
            obs.getFilterData().categoryBits = PhysicsConstants.OUTLINE;
            obs.getFilterData().maskBits = 0;
        }
//        if(ins.getName().equals("spike")){
//            obs.getFilterData().categoryBits = PhysicsConstants.ENEMY;
//
//        }
        addObstacle(obs, side, x, y);
    }

    public void addLightSource(Instance ins, int side, float x, float y){
        Obstacle obs = ins.getObstacle();
        obstacleMap.put(obs, ins);
        obs.setBodyType(BodyDef.BodyType.StaticBody);
        addObstacle(obs, side, x, y);
    }

    public void removeInstance(Instance ins) {
        Obstacle obs = ins.getObstacle();
        obstacleMap.remove(obs);

        removeObstacle(obs);
    }

    public int getSideIndexAtPos(Vector2 pos) {
        int posXAdj = (int) Math.floor((pos.x + sideWidth / 2) / sideWidth);
        int posYAdj = (int) Math.floor((pos.y + sideWidth / 2) / sideWidth);

        if (posXAdj == 0) {
            switch (posYAdj) {
                case 0: return sidePositions[1].getId();
                case 1: return sidePositions[4].getId();
                case -1: return sidePositions[5].getId();
            }
        } else {
            if (posXAdj > -2 && posXAdj < 3) {
                return sidePositions[posXAdj + 1].getId();
            }
        }
        return -1;
    }

    public PhysicsSide getSide(int index) {
        return sides[index];
    }

    /**
     * Removes the given obstacle from being a physical body in the current level
     *
     * @param obs the obstacle to be removed as a physical body
     */
    public void removeObstacle(Obstacle obs) {
        int side = obs.getSide();

        sides[side].removeObstacle(obs);
        bodyMap.remove(obs.getBody());
        obstacleMap.remove(obs);

        obs.deactivatePhysics(world);
    }

    /**
     * Sets the center side to be the one with the given index, moving all other sides
     * to be relative to the center one.
     *
     * @param ind index of the new center side
     */
    public void setCenterSide(int ind, int orientation, boolean adjustObstacles) {
        centerSideIndex = ind;

        PhysicsSide side = sides[ind];
        if (adjustObstacles) {
            side.move(0, 0);
            side.rotate(orientation);
        } else {
            side.setPosition(0, 0);
            side.setOrientation(orientation);
        }

        sidePositions[1] = side;

        for (int i = 0; i < 5; i++) {
            Pair<PhysicsSide, Integer> p = side.getNeighbor(i);

            if (i == 4) {
                if (adjustObstacles) {
                    p.first.move(2, 0);
                } else {
                    p.first.setPosition(2, 0);
                }
                sidePositions[3] = p.first;
            } else {
                int x, y;

                switch ((i - orientation + 4) % 4) {
                    default:
                    case 0:
                        x = 0; y = 1;
                        sidePositions[4] = p.first;
                        break;
                    case 1:
                        x = 1; y = 0;
                        sidePositions[2] = p.first;
                        break;
                    case 2:
                        x = 0; y = -1;
                        sidePositions[5] = p.first;
                        break;
                    case 3:
                        x = -1; y = 0;
                        sidePositions[0] = p.first;
                        break;
                }

                if (adjustObstacles) {
                    p.first.move(x, y);
                } else {
                    p.first.setPosition(x, y);
                }
            }

            int no = ((i == 4 ? p.latter : 2 + p.latter - i) + 4 + orientation) % 4;

            if (adjustObstacles) {
                p.first.rotate(no);
            } else {
                p.first.setOrientation(no);
            }
        }
    }

    /**
     * Updates all physical bodies in the current level given the delta time for this step
     *
     * @param delta the delta time for this step
     */
    public void update(float delta) {
        world.step(delta, 10, 10);

        for (PhysicsSide s : sides) {
            s.updateObstacles(delta);
        }

        if (cameraFocus != null) {
            int focusSide = cameraFocus.getSide();
            if (focusSide != centerSideIndex) {
                setCenterSide(focusSide, cameraFocus.getSideOrientation(), true);
            }
        }
    }

    /**
     * Applies a raycast along a ray, with an upper limit bound for the number of recursive calls it can make.
     * Returns a Hitinfo class with information about the first thing it hits.
     *
     * @param ray2 ray to raycast along
     * @param numReflections maximum number of recursive calls (when sides change)
     * @return a Hitinfo object with information pertaining to what it hits
     */
    private Hitinfo raycastHelper(Ray2D ray2, int numReflections, boolean isSilhouette) {
        hitTemp.addLightPosition(new Vector3(ray2.getOrigin().x, ray2.getOrigin().y, ray2.getSide()));
        hitTemp.setLastRay(ray2);
        if (numReflections == 0) {
            hitTemp.setHit(null);
            hitTemp.setHitPlatform(false);

            return hitTemp;
        }

        PhysicsSide s = sides[ray2.getSide()];
        v2temp.set(s.getWorldPos(ray2.getOrigin()));

        v2temp2.set(ray2.getDir());
        v2temp2.rotateDeg(90 * s.getOrientation());
        v2temp2.scl(sideWidth * 1.5f);
        v2temp2.add(v2temp);

        rcc.reset();
        rcc.setSilhouette(isSilhouette);
        world.rayCast(rcc, v2temp, v2temp2);

        Vector2 hitPos = rcc.getHitPos();
        int sideInd = getSideIndexAtPos(hitPos);
        hitTemp.setSide(sideInd);

        if (sideInd == ray2.getSide()) {
            if (obstacleMap.containsKey(rcc.getHit())) {
                hitTemp.setHit(obstacleMap.get(rcc.getHit()));
            } else {
                hitTemp.setHit(null);
                hitTemp.setHitPlatform(true);
            }

            v2temp.set(sides[sideInd].getRelativePos(hitPos));

            hitTemp.setNormal(rcc.getNormal());
            hitTemp.setX(v2temp.x);
            hitTemp.setY(v2temp.y);
            hitTemp.setSide(rcc.getSide());

            hitTemp.addLightPosition(new Vector3(v2temp.x, v2temp.y, sideInd));
        } else {
            Vector2 newOrigin = null;

            int exitSide = -1;
            for (int i = 0; i < 4; i++) {
                Vector2 a = corners[i];
                Vector2 b = corners[(i + 1) % 4];

                newOrigin = ray2.getIntersectToLineSeg(a.x, a.y, b.x, b.y);
                if (newOrigin != null) {
                    exitSide = i;

                    hitTemp.addLightPosition(new Vector3(newOrigin.x, newOrigin.y, s.getId()));

                    break;
                }
            }

            if (newOrigin == null) {
                hitTemp.setHit(null);
                hitTemp.setHitPlatform(false);

//                Gdx.app.error("PhysicsController", "Raycast left cube", new RuntimeException());
                return hitTemp;
            } else {
//                hitTemp.addLightPosition(new Vector3(newOrigin.x, newOrigin.y, s.getId()));
                hitTemp.createNewLine();

                Pair<PhysicsSide, Integer> p = s.getNeighbor(exitSide);
                PhysicsSide newSide = p.first;

                v2temp = s.getAdjustedNeighborPosRelative(newOrigin.x, newOrigin.y);

                ray2.setOrigin(v2temp);
                ray2.setDir(s.switchVectorToOtherSide(ray2.getDir(), exitSide, p.latter));
//                ray2.getDir().rotateDeg((newSide.getOrientation() - s.getOrientation()) * -90);
                ray2.setSide(newSide.getId());

                raycastHelper(ray2, numReflections - 1, isSilhouette);
            }
        }

        return hitTemp;
    }

    /**
     * Applies a raycast along a ray, with an upper limit bound for the number of recursive calls it can make.
     * Returns a Hitinfo class with information about the first thing it hits.
     *
     * @param ray2 ray to raycast along
     * @param numReflections maximum number of recursive calls (when sides change)
     * @return a Hitinfo object with information pertaining to what it hits
     */
    public Hitinfo raycast(Ray2D ray2, int numReflections, boolean isSilhouette) {
        hitTemp.clearLightPositions();
        return raycastHelper(ray2, numReflections, isSilhouette);
    }

    public void setCameraFocus(Obstacle obs) {
        cameraFocus = obs;

        if (cameraFocus != null) {
            int focusSide = cameraFocus.getSide();
            if (focusSide != centerSideIndex) {
                setCenterSide(focusSide, cameraFocus.getSideOrientation(), true);
            }
        }
    }

    public void switchObstacleSide(Obstacle obs, int from, int to) {
        if (obs.getSide() == from) {
            sides[from].removeObstacle(obs);
            sides[to].addObstacle(obs);

            obs.setSide(to);
        }
    }

    public static PhysicsController getInstance() {
        if (currInstance == null) {
            currInstance = new PhysicsController(SIZE, GRAVITY);
        }

        return currInstance;
    }
    public class PhysicsConstants {
        // Categories
        public static final int PLAYER = 64;
        public static final int MIRROR = 2;
        public static final int MECHANISM = 4;
        public static final int ENEMY = 8;


        public static final int PLATFORM = 16;
        public static final int OUTLINE = 32;


        // Collision masks
        public static final int MASK_PLAYER = ~MECHANISM;
        public static final int MASK_MECHANISM = ~PLAYER;

    }

    public void saveFrame(String frameName) {
        HashMap<String, Object> frame = new HashMap<>();

        frame.put("centerSide", centerSideIndex);
        frame.put("centerSideOrientation", getSide(centerSideIndex).getOrientation());

        savedFrames.put(frameName, frame);
    }

    public void loadFrame(String frameName) {
//        String name = frameName.equals("checkpoint0") ? "restart": frameName;

        HashMap<String, Object> frame = savedFrames.get(frameName);

        setCenterSide((int) frame.get("centerSide"), (int) frame.get("centerSideOrientation"), false);
    }

    public void combinePlatforms() {
        for (PhysicsSide side : sides) {
            side.combinePlatforms();
        }
    }
}
