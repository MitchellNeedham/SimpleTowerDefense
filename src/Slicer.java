import bagel.util.Point;

public class Slicer extends Enemy {

    //-------------------------SLICER PROPERTIES-------------------------//

    private static final String IMAGE_PATH = "res/images/slicer/slicer.png";
    private static final double REWARD = 2.0;
    private static final double PENALTY = 1.0;

    public static final double SPEED = 2.0;
    public static final double HEALTH = 1.0;

    /**
     * Constructor for Slicer
     * @param spawnDelay time in milliseconds after start of waves before slicer spawns
     */
    public Slicer(int spawnDelay) {
        super(SPEED, spawnDelay, IMAGE_PATH, HEALTH, REWARD, getTotalPenalty());
    }

    /**
     * Constructor for slicers created upon destruction of parent slicers
     * @param position position of parent slicer
     * @param pointsIndex point new slicer should move towards
     */
    public Slicer(Point position, int pointsIndex) {
        super(SPEED, HEALTH, REWARD, IMAGE_PATH, position, pointsIndex, getTotalPenalty());
    }

    /**
     * @return penalty for this slicer
     */
    public static double getTotalPenalty() {
        return PENALTY;
    }
}
