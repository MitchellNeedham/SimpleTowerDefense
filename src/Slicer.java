import bagel.util.Point;

import java.util.*;

public class Slicer extends Enemy {

    //TODO: Maybe make package for slicers and add more?

    // z index for render
    private static final int Z_INDEX = 7;

    private static final String TYPE = "slicer";
    private static final double SPEED = 1.0;
    private static final double HEALTH = 600.0;
    private static final double REWARD = 2.0;

    /**
     * Constructor for Slicer
     * @param spawnDelay time in milliseconds after start of waves before slicer spawns
     * @param type String containing name of slicer enemy
     */
    public Slicer(int spawnDelay, String type) {
        super(TYPE, Z_INDEX, SPEED, spawnDelay, "res/images/slicer/" + type + ".png",
                HEALTH, REWARD);

    }

    /**
     * Constructor for slicers created upon destruction of parent slicers
     * @param position position of parent slicer
     * @param pointsIndex point new slicer should move towards
     */
    public Slicer(Point position, int pointsIndex) {
        super(TYPE, Z_INDEX, SPEED, HEALTH, REWARD,
                "res/images/slicer/" + TYPE + ".png", position, pointsIndex);
    }

}
