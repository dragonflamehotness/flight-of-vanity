package com.genmiracle.flightofvanity.instance.model;

import java.util.ArrayList;
import java.util.List;

public class EnemyController {
    private static final float TURN_COOLDOWN = 5f;
    private ArrayList<Enemy> enemies;

    public EnemyController(){
        enemies = new ArrayList<Enemy>();
    }

    public void addEnemy(Enemy e) {
        enemies.add(e);

        e.setTurnCooldown(TURN_COOLDOWN / 2);
    }

    public void update(float delta) {
        for (Enemy e : enemies) {
            float tc = e.getTurnCooldown();
            if (tc <= 0) {
                e.setTurnCooldown(tc + TURN_COOLDOWN);
                e.setMovement(e.isFacingRight() ? -1 : 1);

                e.setVX(Enemy.MOVE_SPEED * (e.getMovement() > 0 ? 1 : -1));
            } else if (tc <= 1) {
                e.setMovement(0);
            }
        }
    }
}
