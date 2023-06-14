package com.genmiracle.flightofvanity.physics;

import com.badlogic.gdx.physics.box2d.*;
import com.genmiracle.flightofvanity.instance.Instance;
import com.genmiracle.flightofvanity.instance.mechanisms.ExitDoor;
import com.genmiracle.flightofvanity.instance.model.Mirror;
import com.genmiracle.flightofvanity.instance.model.Player;
import com.genmiracle.flightofvanity.physics.obstacle.Obstacle;
import com.genmiracle.flightofvanity.physics.obstacle.PlayerObstacle;
import com.genmiracle.flightofvanity.physics.obstacle.SensorObstacle;

import java.util.HashMap;

public class CollisionHandler implements ContactListener {
    private HashMap<Body, Obstacle> bodyMap;
    private HashMap<Obstacle, Instance> obstacleMap;

    public CollisionHandler(HashMap<Body, Obstacle> bodyMap, HashMap<Obstacle, Instance> obstacleMap) {
        this.bodyMap = bodyMap;
        this.obstacleMap = obstacleMap;
    }

    /**
     * Called when two fixtures begin to touch.
     *
     * @param contact
     */
    @Override
    public void beginContact(Contact contact) {
        WorldManifold wm = contact.getWorldManifold();

        Fixture fixA = contact.getFixtureA();
        Fixture fixB = contact.getFixtureB();

        Body a = fixA.getBody();
        Body b = fixB.getBody();

        Obstacle obsA = bodyMap.get(a);
        Obstacle obsB = bodyMap.get(b);

        Instance insA = obstacleMap.get(obsA);
        Instance insB = obstacleMap.get(obsB);

        if (fixA.isSensor()) {
            if (obsA instanceof PlayerObstacle) {
                if ((int) fixA.getUserData() == 1) {
                    PlayerObstacle p = (PlayerObstacle) obsA;
                    p.changeSensorCount(1);

                    if (insA instanceof Player) {
                        Player pl = (Player) insA;
                        pl.setGrounded(true);
                    }
                }
            } else if (obsA instanceof SensorObstacle) {
                if (fixA.getUserData() != null) {
                    SensorObstacle s = (SensorObstacle) obsA;
                    s.changeSensorCount(1);
//                    System.out.println(s.getSensorCount());
                }

            }
        }

//        if (insA instanceof Mirror) {
//            Mirror m = (Mirror) insA;
//            m.changeSensorCount(1);
//        }
//
//        if (insB instanceof Mirror) {
//            Mirror m = (Mirror) insB;
//            m.changeSensorCount(1);
//        }

        if (insA instanceof Player) {
            if (insB instanceof ExitDoor && ((ExitDoor) insB).isActivated()) {
                Player pl = (Player) insA;
                pl.setIsCollidedDoor(true);
            } else if (insB instanceof Mirror) {
                obsB.setGravity(0, PhysicsController.GRAVITY * b.getMass());
            }
        } else if (insA != null) {
            if (insB instanceof Mirror) {
                obsB.setGravity(obsA.getGravity());
            }
        }

        if (insB instanceof Player) {
            if (insA instanceof ExitDoor && ((ExitDoor) insA).isActivated()) {
                Player pl = (Player) insB;
                pl.setIsCollidedDoor(true);
            } else if (insA instanceof Mirror) {
                obsA.setGravity(0, PhysicsController.GRAVITY * a.getMass());
            }
        } else if (insB != null) {
            if (insA instanceof Mirror) {
                obsA.setGravity(obsB.getGravity());
            }
        }

        if (fixB.isSensor()) {
            if (obsB instanceof PlayerObstacle) {
                if ((int) fixB.getUserData() == 1) {
                    PlayerObstacle p = (PlayerObstacle) obsB;
                    p.changeSensorCount(1);

                    if (insB instanceof Player) {
                        Player pl = (Player) insB;
                        pl.setGrounded(true);
                    }
                }
            } else if (obsB instanceof SensorObstacle) {
                if (fixB.getUserData() != null) {
                    SensorObstacle s = (SensorObstacle) obsB;
                    s.changeSensorCount(1);
//                    System.out.println(s.getSensorCount());
                }
            }
        }

//        if (insA instanceof Player) {
//            Player p = (Player) insA;
//            if (wm.getNormal().y < 0) {
//                p.setGrounded(true);
//            }
//        }
//
//        if (insB instanceof Player) {
//            Player p = (Player) insB;
//            if (wm.getNormal().y > 0) {
//                p.setGrounded(true);
//            }
//        }
    }

    /**
     * Called when two fixtures cease to touch.
     *
     * @param contact
     */
    @Override
    public void endContact(Contact contact) {
        WorldManifold wm = contact.getWorldManifold();

        Fixture fixA = contact.getFixtureA();
        Fixture fixB = contact.getFixtureB();

        Body a = fixA.getBody();
        Body b = fixB.getBody();

        Obstacle obsA = bodyMap.get(a);
        Obstacle obsB = bodyMap.get(b);

        Instance insA = obstacleMap.get(obsA);
        Instance insB = obstacleMap.get(obsB);

        if (fixA.isSensor()) {
            if (obsA instanceof PlayerObstacle) {
                if ((int) fixA.getUserData() == 1) {
                    PlayerObstacle p = (PlayerObstacle) obsA;
                    p.changeSensorCount(-1);

                    if (insA instanceof Player && p.getSensorCount() <= 0) {
                        if (p.getSensorCount() < 0) {
                            p.setSensorCount(0);
                        }

                        Player pl = (Player) insA;
                        pl.setGrounded(false);
                    }
                }
            } else if (obsA instanceof SensorObstacle) {
                if (fixA.getUserData() != null) {
                    SensorObstacle s = (SensorObstacle) obsA;
                    s.changeSensorCount(-1);
//                    System.out.println(s.getSensorCount());
                }
            }
        }

        if (fixB.isSensor()) {
            if (obsB instanceof PlayerObstacle) {
                if ((int) fixB.getUserData() == 1) {
                    PlayerObstacle p = (PlayerObstacle) obsB;
                    p.changeSensorCount(-1);

                    if (insB instanceof Player && p.getSensorCount() <= 0) {
                        if (p.getSensorCount() < 0) {
                            p.setSensorCount(0);
                        }

                        Player pl = (Player) insB;
                        pl.setGrounded(false);
                    }
                }
            } else if (obsB instanceof SensorObstacle) {
                if (fixB.getUserData() != null) {
                    SensorObstacle s = (SensorObstacle) obsB;
                    s.changeSensorCount(-1);
//                    System.out.println(s.getSensorCount());
                }
            }
        }

//        if (insA instanceof Mirror) {
//            Mirror m = (Mirror) insA;
//            m.changeSensorCount(-1);
//        }
//
//        if (insB instanceof Mirror) {
//            Mirror m = (Mirror) insB;
//            m.changeSensorCount(-1);
//        }

//        if (insA instanceof Player) {
//            Player p = (Player) insA;
//            if (wm.getNormal().y < 0) {
//                p.setGrounded(false);
//            }
//        }
//
//        if (insB instanceof Player) {
//            Player p = (Player) insB;
//            if (wm.getNormal().y > 0) {
//                p.setGrounded(false);
//            }
//        }
    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {
        short afilter =contact.getFixtureA().getFilterData().categoryBits;
        short bfilter = contact.getFixtureB().getFilterData().categoryBits;

        if(afilter == PhysicsController.PhysicsConstants.PLAYER && bfilter == PhysicsController.PhysicsConstants.MECHANISM ||
                afilter == PhysicsController.PhysicsConstants.MECHANISM && bfilter == PhysicsController.PhysicsConstants.PLAYER){
            contact.setEnabled(false);
        }
    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {
        WorldManifold wm = contact.getWorldManifold();

        Fixture fixA = contact.getFixtureA();
        Fixture fixB = contact.getFixtureB();

        Body a = fixA.getBody();
        Body b = fixB.getBody();

        Obstacle obsA = bodyMap.get(a);
        Obstacle obsB = bodyMap.get(b);

        Instance insA = obstacleMap.get(obsA);
        Instance insB = obstacleMap.get(obsB);

        if (obsA instanceof PlayerObstacle) {
            obsA.setAngularVelocity(0);
        }

        if (obsB instanceof PlayerObstacle) {
            obsB.setAngularVelocity(0);
        }
    }
}
