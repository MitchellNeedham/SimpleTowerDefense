public class Tank extends ActiveTower{

    private static final String TYPE = "tank";
    private static final double PROJECTILE_SPEED = 10;
    private static final double PROJECTILE_DAMAGE = 1;
    private static final double RANGE = 100;
    private static final double FIRE_RATE = 1000;
    private static final double COST = 250;

    private double x;
    private double y;


    /**
     * Constructor for Tank
     *
     * @param x                 x-coordinate at centre of tank position
     * @param y                 y-coordinate at centre of tank position
     */
    public Tank(double x, double y) {
        super(x, y, TYPE, PROJECTILE_SPEED, PROJECTILE_DAMAGE, RANGE, FIRE_RATE, COST);
        this.x = x;
        this.y = y;
    }
}
