import bagel.util.Point;

public class Slicer extends Enemy {

    private static final String IMAGE_PATH = "res/images/slicer.png";
    private static final double REWARD = 2.0;

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

    public static double getTotalPenalty() {
        return HEALTH;
    }
}
