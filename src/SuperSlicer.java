import bagel.util.Point;

import java.util.ArrayList;
import java.util.List;

public class SuperSlicer extends Enemy {

    private static final String IMAGE_PATH = "res/images/superslicer.png";
    private static final double CHILDREN_SPAWN_DIST = 10D;
    private static final double REWARD = 4.0D;
    private static final double CHILDREN = 2.0D;

    public static final double SPEED = 3.0D/4.0D * Slicer.SPEED;
    public static final double HEALTH = 1.0D * Slicer.HEALTH;

    /**
     * Constructor for Slicer
     * @param spawnDelay time in milliseconds after start of waves before slicer spawns
     */
    public SuperSlicer(int spawnDelay) {
        super(SPEED, spawnDelay, IMAGE_PATH, HEALTH, REWARD, getTotalPenalty());

    }

    /**
     * Constructor for slicers created upon destruction of parent slicers
     * @param position position of parent slicer
     * @param pointsIndex point new slicer should move towards
     */
    public SuperSlicer(Point position, int pointsIndex) {
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
            slicers.add(new Slicer(newPos, getIndex()));
            newPos = newPos.asVector().add(getMoveVector().mul(CHILDREN_SPAWN_DIST / CHILDREN)).asPoint();
        }
        return slicers;
    }

    public static double getTotalPenalty() {
        double penalty = 0;
        for (int i = 0; i < CHILDREN; i++) {
            penalty += Slicer.getTotalPenalty();
        }
        return penalty;
    }

}

