package com.genmiracle.flightofvanity.physics;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.World;
import com.genmiracle.flightofvanity.instance.model.Player;
import com.genmiracle.flightofvanity.physics.obstacle.BoxObstacle;
import com.genmiracle.flightofvanity.physics.obstacle.Obstacle;
import com.genmiracle.flightofvanity.physics.obstacle.PlayerObstacle;
import com.genmiracle.flightofvanity.physics.obstacle.PolygonObstacle;
import com.genmiracle.flightofvanity.util.Pair;
import com.genmiracle.flightofvanity.util.Utilities;


import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;

public class PhysicsSide {
    /** The current x-position of the center of this PhysicsSide within the world */
    private float x;
    /** The current y-position of the center of this PhysicsSide within the world */
    private float y;
    /** The current orientation of this PhysicsSide within the world */
    private int orientation;

    /** The world that this PhysicsSide is currently in */
    private World world;
    /** A list of all the obstacles that are currently within this side */
    private ArrayList<Obstacle> obstacles;
    /**
     * An array of neighboring sides, such that the one at the 0th index is a Pair of the PhysicsSide that's
     * connected to this PhysicsSide's top side along with the corresponding connected side on the neighboring side,
     * and it continues clockwise. The 4th index refers to the PhysicsSide on the opposite end of the cube, with the int
     * being the orientation (relative to the default orientation of this PhysicsSide)
     */
    private Pair<PhysicsSide, Integer>[] neighbors;

    private Vector2[] corners;
    private int id;

    /** The size of each side */
    private float worldSize;

    /** 2D array of platforms */
    private Obstacle[][] platforms;

    /** ArrayList of platforms after they've been combined by platforms */
    private ArrayList<Obstacle> polygonPlatforms;


    /** CACHING */
    private Vector2 v2temp;
    private Vector2 v2temp2;

    /**
     * Create a new PhysicsSide inside the given world w, with index i
     * and side lengths size.
     *
     * @param w world the PhysicsSide is within
     * @param i index of this PhysicsSide
     * @param size length of a single side
     */
    public PhysicsSide(World w, int i, float size) {
        v2temp = new Vector2();
        v2temp2 = new Vector2();

        world = w;
        worldSize = size;
        id = i;

        neighbors = new Pair[5];
        corners = new Vector2[4];
        obstacles = new ArrayList<>();
        polygonPlatforms = new ArrayList<>();

        platforms = new Obstacle[20][20];

        for (int j = 0; j < 4; j++) {
            corners[j] = new Vector2();
        }

        switch (i) {
            case 0:
                x = -size; y = 0;
                break;
            case 1:
                x = 0; y = 0;
                break;
            case 2:
                x = size; y = 0;
                break;
            case 3:
                x = 2 * size; y = 0;
                break;
            case 4:
                x = 0; y = size;
                break;
            case 5:
                x = 0; y = -size;
                break;

        }
    }

    /**
     * Adds a new neighbor to this PhysicsSide, such that [side] is what side of
     * this object the other PhysicsSide [n] connects to, and [nSide] is the
     * corresponding side on [n].
     *
     * @param side side number that the neighbor is connected to on this side
     * @param n neighboring PhysicsSide
     * @param nSide side number of the corresponding side on the neighboring PhysicsSide
     */
    public void setNeighbor(int side, PhysicsSide n, int nSide) {
        neighbors[side] = new Pair<>(n, nSide);
    }

    public int getOrientation() {
        return orientation;
    }

    public Pair<PhysicsSide, Integer> getNeighbor(int side) {
        return neighbors[side];
    }

    public void clear() {
        for (Obstacle o : obstacles) {
            o.deactivatePhysics(world);
        }
        obstacles.clear();
    }

    public boolean isWithin(Obstacle o) {
        float ox = o.getX(); float oy = o.getY();

        return ox >= x - worldSize / 2.0f && ox <= x + worldSize / 2.0f &&
                oy >= y - worldSize / 2.0f && oy <= y + worldSize / 2.0f;
    }

    public void addObstacle(Obstacle o) {
        obstacles.add(o);
        if (o.getName().equals("platform")) {
            Vector2 relPos = getRelativePos(o.getPosition());

            int x = Math.round((relPos.x + worldSize / 2.0f) / 5f - 0.5f);
            int y = Math.round((relPos.y + worldSize / 2.0f) / 5f - 0.5f);

            if (x < 0 || y < 0 || y >= platforms.length || x >= platforms[0].length) {
                return;
            }
            platforms[y][x] = o;
        }
    }

    public boolean removeObstacle(Obstacle o) {
        return obstacles.remove(o);
    }

    public void move(int x, int y) {
        float ox = this.x; float oy = this.y;

        this.x = x * worldSize;
        this.y = y * worldSize;

        //System.out.println("New Position: " + x + " " + y);

        for (Obstacle o : obstacles) {
            float dx = o.getX() - ox;
            float dy = o.getY() - oy;

            o.setPosition(dx + this.x, dy + this.y);
            //System.out.println("Side " + id + ": " + o.getPosition());

            if (!(o instanceof PlayerObstacle)) {
                if (Utilities.equalFloats(0, x) && Utilities.equalFloats(0, y)) {
//                    o.setGravityScale(1);
                } else {
                    o.setGravityScale(0);
                }
            }
        }
    }

    public void setPosition(int x, int y) {
        float ox = this.x; float oy = this.y;

        this.x = x * worldSize;
        this.y = y * worldSize;

        for (Obstacle o : polygonPlatforms) {
            float dx = o.getX() - ox;
            float dy = o.getY() - oy;

            o.setPosition(dx + this.x, dy + this.y);
            //System.out.println("Side " + id + ": " + o.getPosition());
        }
    }

    public void rotate(int no) {
//        System.out.println();
//        System.out.println("Side ID: " + id);
//        System.out.println("Orientation Diff: " + (no - orientation));
//        System.out.println("NO: " + no);

//        if (id == 0) {
//            System.out.println("Num Obstacles: " + obstacles.size());
//            System.out.println(x + " " + y);
//        }


        for (Obstacle o : obstacles) {
            v2temp.set(o.getPosition());
//            System.out.println(v2temp.toString());

            v2temp.sub(x, y);

            v2temp.rotateDeg((no - orientation) * 90);
            v2temp.add(x, y);

//            System.out.println(v2temp.toString());

            o.setPosition(v2temp);
            o.setSideOrientation(no);

            if (!Utilities.equalFloats(o.getGravity().x, 0) || !Utilities.equalFloats(o.getGravity().y, 0)) {
                o.setGravity(o.getGravity().rotateDeg((no - orientation) * 90));
            }

            if (!(o instanceof PlayerObstacle)) {
                o.setAngle((float) (no * Math.PI / 2f));
            }

            v2temp.set(o.getLinearVelocity());
            v2temp.rotateDeg((no - orientation) * 90);
            o.setLinearVelocity(v2temp);
        }

        orientation = no;
    }

    public void setOrientation(int no) {
        for (Obstacle o : polygonPlatforms) {
            v2temp.set(o.getPosition());
//            System.out.println(v2temp.toString());

            v2temp.sub(x, y);

            v2temp.rotateDeg((no - orientation) * 90);
            v2temp.add(x, y);

//            System.out.println(v2temp.toString());

            o.setPosition(v2temp);
            o.setSideOrientation(no);

            if (!(o instanceof PlayerObstacle)) {
                o.setAngle((float) (no * Math.PI / 2f));
            }

            v2temp.set(o.getLinearVelocity());
            v2temp.rotateDeg((no - orientation) * 90);
            o.setLinearVelocity(v2temp);
        }

        orientation = no;
    }

    public Vector2 getWorldPos(float x, float y) {
        float rad = (float) (orientation * Math.PI / 2f);

        v2temp.set((float) (Math.cos(rad) * x - Math.sin(rad) * y + this.x),
                   (float) (Math.sin(rad) * x + Math.cos(rad) * y + this.y));

        return v2temp;
    }

    public Vector2 getWorldPos(Vector2 pos) {
        float rad = (float) (orientation * Math.PI / 2f);

        v2temp.set((float) (Math.cos(rad) * pos.x - Math.sin(rad) * pos.y + x),
                (float) (Math.sin(rad) * pos.x + Math.cos(rad) * pos.y + y));

        return v2temp;
    }

    public Vector2 getRelativePos(Vector2 pos) {
        float rad = (float) (orientation * Math.PI / 2f);

        float ox = pos.x - x;
        float oy = pos.y - y;

        v2temp.set(ox, oy);
        v2temp.rotateDeg(-orientation * 90);

//        v2temp.set((float) (Math.cos(rad) * ox + Math.sin(rad) * oy),
//                   (float) (-Math.sin(rad) * ox + Math.cos(rad) * oy));
        return v2temp;
    }

    public Vector2 getRelativePos(float x, float y) {
        float rad = (float) (orientation * Math.PI / 2f);

        float ox = x - this.x;
        float oy = y - this.y;

        v2temp.set((float) (Math.cos(rad) * ox + Math.sin(rad) * oy),
                   (float) (-Math.sin(rad) * ox + Math.cos(rad) * oy));
        return v2temp;
    }

    public Vector2[] getCorners() {
        for (int i = 0; i < 4; i++) {
            corners[(i + orientation) % 4].set(i > 0 && i < 3 ? x + worldSize / 2f : x - worldSize / 2f,
                           i < 2 ? y + worldSize / 2f : y - worldSize / 2f);
        }

        return corners;
    }

    public int getId() {
        return id;
    }

    public Vector2 getAdjustedNeighborPosRelative(float dx, float dy) {
        int exit = -1;

        float axis = 0;

        if (dy >= 49.9) {
            exit = 0;
            axis = -dx;
        } else if (dx >= 49.9) {
            exit = 1;
            axis = dy;
        } else if (dy <= -49.9) {
            exit = 2;
            axis = dx;
        } else if (dx <= -49.9) {
            exit = 3;
            axis = -dy;
        }

        if (exit > -1) {
            Pair<PhysicsSide, Integer> exitTo = neighbors[exit];
            PhysicsSide n = exitTo.first;
            int no = exitTo.latter;

            float rx; float ry;

            switch (no) {
                case 0:
                    rx = axis;
                    ry = worldSize / 2f;
                    break;
                case 1:
                    rx = worldSize / 2f;
                    ry = -axis;
                    break;
                case 2:
                    rx = -axis;
                    ry = -worldSize / 2f;
                    break;
                case 3:
                default:
                    rx = -worldSize / 2f;
                    ry = axis;
                    break;
            }

            v2temp.set(rx, ry);
            return v2temp;
        }
        return null;
    }

    public void updateObstacle(float dt, Obstacle o) {
        v2temp = getRelativePos(o.getPosition());
        o.setRelativePosition(v2temp);

        int exit = -1;
//            int exitHorizontal = v2temp.x > worldSize / 2f ? 1 : v2temp.x < -worldSize / 2f ? -1 : 0;
//            int exitVertical = v2temp.y > worldSize / 2f ? 1 : v2temp.y < -worldSize / 2f ? -1 : 0;

        float dx = (Math.max(Math.abs(v2temp.x) - worldSize / 2f, 0)) * Math.signum(v2temp.x);
        float dy = (Math.max(Math.abs(v2temp.y) - worldSize / 2f, 0)) * Math.signum(v2temp.y);

//        System.out.println(dx);
//        System.out.println(dy);

        Vector2 v = o.getLinearVelocity();
        v2temp2.set(v);
        v2temp2.rotateDeg(-90 * orientation);

//        System.out.println("Relative Velocity: " + v2temp2);


        float into = 0; float axis = 0; float vInto = 0; float vAxis = 0;

        if (dx > 0.1) {
            exit = 1;
            into = dx;
            axis = v2temp.y;
            vInto = v2temp2.x;
            vAxis = v2temp2.y;
        } else if (dx < -0.1) {
            exit = 3;
            into = -dx;
            axis = -v2temp.y;
            vInto = -v2temp2.x;
            vAxis = -v2temp2.y;
        } else if (dy > 0.1) {
            exit = 0;
            into = dy;
            axis = -v2temp.x;
            vInto = v2temp2.y;
            vAxis = -v2temp2.x;
        } else if (dy < -0.1) {
            exit = 2;
            into = -dy;
            axis = v2temp.x;
            vInto = -v2temp2.y;
            vAxis = v2temp2.x;
        }

        if (exit > -1) {

//            System.out.println("DX and DY: " + dx + " " + dy);

//            System.out.println(v.toString());

            Pair<PhysicsSide, Integer> exitTo = neighbors[exit];
            PhysicsSide n = exitTo.first;
//            System.out.println(getRelativePos(o.getPosition()));
//            System.out.println("MOVED FROM " + id + " TO " + n.id);
            int no = exitTo.latter;//new orientation

            float rx; float ry;
            float vx; float vy;

            switch (no) {
                case 0:
                    rx = axis;
                    ry = worldSize / 2f - into;
                    vx = vAxis;
                    vy = -vInto;
                    break;
                case 1:
                    rx = worldSize / 2f - into;
                    ry = -axis;
                    vx = -vInto;
                    vy = -vAxis;
                    break;
                case 2:
                    rx = -axis;
                    ry = -worldSize / 2f + into;
                    vx = -vAxis;
                    vy = vInto;
                    break;
                case 3:
                default:
                    rx = -worldSize / 2f + into;
                    ry = axis;
                    vx = vInto;
                    vy = vAxis;
                    break;
            }

            v2temp.set(rx, ry);

//            System.out.println(v2temp.toString());

            o.setRelativePosition(v2temp);
            o.setSide(n.getId());
            o.setSideOrientation((orientation + no + 6 - exit) % 4);
            o.setOrientation((o.getOrientation() + no + 6 - exit) % 4);
//            System.out.println("Curr Orientation: " + orientation);
//            System.out.println("Curr NO: " + no);
//            System.out.println("Curr Exit: " + exit);
//            System.out.println("Orientation: " + o.getSideOrientation());
//            System.out.println(n.id);

//            System.out.println(o.getSideOrientation());

            Vector2 wPos = n.getWorldPos(rx, ry);
            o.setPosition(wPos);

            v2temp.set(vx, vy);
//            float rad = (float) (n.orientation * Math.PI / 2f);
//
//            v2temp.set((float) (Math.cos(rad) * vx + Math.sin(rad) * vy),
//                       (float) (-Math.sin(rad) * vx + Math.cos(rad) * vy));

//            System.out.println(v2temp.toString());
            v2temp.rotateDeg(90 * n.getOrientation());
//            System.out.println(v2temp.toString());

            o.setLinearVelocity(v2temp);

            removeObstacle(o);
            n.addObstacle(o);
            n.updateObstacle(dt, o);
        } else {
            o.update(dt);
        }
    }

    public Vector2 switchVectorToOtherSide(Vector2 v, int exitSide, int entrySide) {
        v2temp.set(0, 0);
        float vInto = 0; float vAxis = 0;

        switch (exitSide) {
            case 0: vInto = v.y; vAxis = -v.x; break;
            case 1: vInto = v.x; vAxis = v.y; break;
            case 2: vInto = -v.y; vAxis = v.x; break;
            case 3: vInto = -v.x; vAxis = -v.y; break;
        }

        switch (entrySide) {
            case 0: v2temp.set(vAxis, -vInto); break;
            case 1: v2temp.set(-vInto, -vAxis); break;
            case 2: v2temp.set(-vAxis, vInto); break;
            case 3: v2temp.set(vInto, vAxis); break;
        }

        return v2temp;
    }

    public void updateObstacles(float dt) {
        for (int i = obstacles.size() - 1; i > -1; i--) {
            Obstacle obs = obstacles.get(i);
            if (obs instanceof PolygonObstacle) {
                continue;
            }

            obs.getBody().applyForceToCenter(obs.getGravity(), false);

            if (!obs.isSensor() || obs.getFilterData().categoryBits == PhysicsController.PhysicsConstants.MIRROR) {
                updateObstacle(dt, obs);
            }

            if (obs instanceof BoxObstacle) {
                updateObstacleOverlap((BoxObstacle) obs);
            }
        }
    }

    public void connectAdjacentPlatforms(int ox, int oy) {
        HashSet<String> visited = new HashSet<>();

        ArrayList<Pair<Integer, Integer>> connected = new ArrayList<>();
        ArrayList<Pair<Integer, Integer>> edges = new ArrayList<>();

        ArrayDeque<Pair<Integer, Integer>> toVisit = new ArrayDeque<>();
        toVisit.add(new Pair<>(ox, oy));
        visited.add(ox + " " + oy);

        while (!toVisit.isEmpty()) {
            Pair<Integer, Integer> next = toVisit.poll();
            connected.add(next);

            boolean edge = false;
            for (int[] dp : Utilities.NEIGHBORS) {
                int dx = dp[0]; int dy = dp[1];
                int nx = next.first + dx; int ny = next.latter + dy;

                Obstacle platform = nx < 0 || ny < 0 || nx >= platforms.length || ny >= platforms.length
                        ? null : platforms[ny][nx];
                if (platform == null) {
                    if (!edge) {
                        edge = true;
                        edges.add(next);
                    }

                    continue;
                }

                String key = nx + " " + ny;
                if (visited.contains(key)) {
                    continue;
                }

                visited.add(key);
                toVisit.add(new Pair<>(nx, ny));
            }
        }

        HashSet<String> visitedEdges = new HashSet<>();

        Pair<Integer, Integer> edge = edges.get(0);
        int x = edge.first; int y = edge.latter;

        String key;
        String newKey = x + " " + y + " 0";

        ArrayList<Vector2> outline = new ArrayList<>();

        int dir = 0;
        int prevDir = -1;
        do {
            key = newKey;

            visitedEdges.add(key);

            int[] dp = Utilities.NEIGHBORS[dir];
            int dx = dp[0];
            int dy = dp[1];

            int nx = x + dx; int ny = y + dy;
            if (nx < 0 || ny < 0 || nx >= platforms.length || ny >= platforms.length || platforms[ny][nx] == null) {
                if (dir != prevDir) {
                    prevDir = dir;

                    switch (dir) {
                        case 0:
                            outline.add(new Vector2(x * Utilities.TILE_SIZE, (y + 1) * Utilities.TILE_SIZE));
                            break;
                        case 1:
                            outline.add(new Vector2((x + 1) * Utilities.TILE_SIZE, (y + 1) * Utilities.TILE_SIZE));
                            break;
                        case 2:
                            outline.add(new Vector2((x + 1) * Utilities.TILE_SIZE, y * Utilities.TILE_SIZE));
                            break;
                        case 3:
                            outline.add(new Vector2(x * Utilities.TILE_SIZE, y * Utilities.TILE_SIZE));
                            break;
                    }
                }
            } else {
                dir += 2;
                x = nx; y = ny;
            }

            dir = (dir + 1) % 4;
            newKey = x + " " + y + " " + dir;
        } while (!visitedEdges.contains(newKey));

        float[] points = new float[outline.size() * 2];
        for (int i = 0; i < outline.size(); i++) {
            Vector2 v = outline.get(outline.size() - 1 - i);

            points[i * 2] = v.x - worldSize / 2; points[i * 2 + 1] = v.y - worldSize / 2;
        }

        PolygonObstacle obs = new PolygonObstacle(points, this.x, this.y);
        obs.setBodyType(BodyDef.BodyType.StaticBody);
        obs.setAngle((float) (orientation * Math.PI / 2f));
        obs.activatePhysics(world);

        obstacles.add(obs);
        polygonPlatforms.add(obs);

        for (Pair<Integer, Integer> p : connected) {
            obstacles.remove(platforms[p.latter][p.first]);
            platforms[p.latter][p.first].deactivatePhysics(world);
            platforms[p.latter][p.first] = null;
        }
    }

    public void combinePlatforms() {
        int tileNum = Math.round(worldSize / Utilities.TILE_SIZE);
        for (int x = 0; x < tileNum; x++) {
            for (int y = 0; y < tileNum; y++) {
                Obstacle plat = platforms[y][x];
                if (plat == null) {
                    continue;
                }

                connectAdjacentPlatforms(x, y);
            }
        }
    }

    public void updateObstacleOverlap(BoxObstacle obs) {
        Vector2 pos = getRelativePos(obs.getPosition());
        Vector2 size = obs.getDimension();

//        size.rotateRad(obs.getAngle());

        float width = size.x; float height = size.y;

        int[] indices = obs.getSideIndices();
        indices[0] = id;

        if (pos.x + width > worldSize / 2f) {
            indices[1] = neighbors[1].first.id;
        } else if (pos.x - width < -worldSize / 2f) {
            indices[1] = neighbors[3].first.id;
        } else {
            indices[1] = -1;
        }

        if (pos.y + height > worldSize / 2f) {
            indices[2] = neighbors[0].first.id;
        } else if (pos.y - height < -worldSize / 2f) {
            indices[2] = neighbors[2].first.id;
        } else {
            indices[2] = -1;
        }
    }
}
