import bagel.util.Point;

import java.util.*;

public class MegaSlicer extends Enemy {

    // z index for render
    private static final int Z_INDEX = 7;
    private static final double CHILDREN_SPAWN_DIST = 10;
    private static final String TYPE = "megaslicer";
    private static final double SPEED = 2.0;
    private static final double HEALTH = 5.0;
    private static final double REWARD = 15.0;
    private static final double CHILDREN = 2;

    /**
     * Constructor for Slicer
     * @param spawnDelay time in milliseconds after start of waves before slicer spawns
     * @param type String containing name of slicer enemy
     */
    public MegaSlicer(int spawnDelay, String type) {
        super(type, Z_INDEX, SPEED, spawnDelay, "res/images/slicer/" + type + ".png",
                HEALTH, REWARD);

    }

    /**
     * Constructor for slicers created upon destruction of parent slicers
     * @param position position of parent slicer
     * @param pointsIndex point new slicer should move towards
     */
    public MegaSlicer(Point position, int pointsIndex) {
        super(TYPE, Z_INDEX, SPEED, HEALTH, REWARD,
                "res/images/slicer/" + TYPE + ".png", position, pointsIndex);
    }

    /**
     * spawns children upon death of parent slicer
     * @return instances of children slicers
     */
    @Override
    public List<Enemy> spawnChildren() {

        List<Enemy> slicers = new ArrayList<>();
        Point newPos = getPosition();

        int i;
        for (i = 0; i < CHILDREN; i++) {
            slicers.add(new SuperSlicer(newPos, getIndex()));
            newPos = newPos.asVector().add(getMoveVector().mul(CHILDREN_SPAWN_DIST / CHILDREN)).asPoint();
        }
        return slicers;
    }

}
