public class Tank extends ActiveTower {

    public static final String TYPE = "tank";
    public static final int COST = 250;

    private static final double PROJECTILE_SPEED = 10;
    private static final double PROJECTILE_DAMAGE = 1;
    private static final double RANGE = 100;
    private static final double FIRE_RATE = 1000;


    /**
     * Constructor for Tank
     *
     * @param x                 x-coordinate at centre of tank position
     * @param y                 y-coordinate at centre of tank position
     */
    public Tank(double x, double y) {
        super(x, y, TYPE, PROJECTILE_SPEED, PROJECTILE_DAMAGE, RANGE, FIRE_RATE);
    }

    @Override
    public int getCost() {
        return COST;
    }
}
