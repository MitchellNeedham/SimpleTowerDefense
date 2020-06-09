import bagel.util.Point;

import java.util.ArrayList;
import java.util.List;

public class ApexSlicer extends Enemy {

    //-------------------------SLICER PROPERTIES-------------------------//

    private static final String IMAGE_PATH = "res/images/apexslicer.png";
    private static final double CHILDREN_SPAWN_DIST = 10;
    private static final double REWARD = 150.0;
    private static final double CHILDREN = 4;

    public static final double SPEED = 0.5D * MegaSlicer.SPEED;
    public static final double HEALTH = 25.0D * Slicer.HEALTH;


    /**
     * Constructor for Slicer
     * @param spawnDelay time in milliseconds after start of waves before slicer spawns
     */
    public ApexSlicer(int spawnDelay) {
        super(SPEED, spawnDelay, IMAGE_PATH, HEALTH, REWARD, getTotalPenalty());
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
            slicers.add(new MegaSlicer(newPos, getIndex()));
            newPos = newPos.asVector().add(getMoveVector().mul(CHILDREN_SPAWN_DIST / CHILDREN)).asPoint();
        }
        return slicers;
    }

    public static double getTotalPenalty() {
        double penalty = 0;
        for (int i = 0; i < CHILDREN; i++) {
            penalty += MegaSlicer.getTotalPenalty();
        }
        return penalty;
    }
}

