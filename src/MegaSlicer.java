import bagel.util.Point;

import java.util.ArrayList;
import java.util.List;

public class MegaSlicer extends Enemy {

    //-------------------------SLICER PROPERTIES-------------------------//

    private static final String IMAGE_PATH = "res/images/slicer/megaslicer.png";
    private static final double CHILDREN_SPAWN_DIST = 10;
    private static final double REWARD = 15.0;
    private static final double CHILDREN = 2;

    public static final double SPEED = 1.0D * SuperSlicer.SPEED;
    public static final double HEALTH = 1.0D * SuperSlicer.HEALTH;

    /**
     * Constructor for Slicer
     * @param spawnDelay time in milliseconds after start of waves before slicer spawns
     */
    public MegaSlicer(int spawnDelay) {
        super(SPEED, spawnDelay, IMAGE_PATH, HEALTH, REWARD, getTotalPenalty());
    }

    /**
     * Constructor for slicers created upon destruction of parent slicers
     * @param position position of parent slicer
     * @param pointsIndex point new slicer should move towards
     */
    public MegaSlicer(Point position, int pointsIndex) {
        super(SPEED, HEALTH, REWARD, IMAGE_PATH, position, pointsIndex, getTotalPenalty());
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

    /**
     * gets total penalty for this slicer based on its children
     * @return total penalty for this slicer
     */
    public static double getTotalPenalty() {
        double penalty = 0;
        for (int i = 0; i < CHILDREN; i++) {
            penalty += SuperSlicer.getTotalPenalty();
        }
        return penalty;
    }

}
