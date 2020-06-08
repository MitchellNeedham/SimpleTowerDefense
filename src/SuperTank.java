public class SuperTank extends ActiveTower{

    private static final String TYPE = "supertank";
    private static final double PROJECTILE_SPEED = 10;
    private static final double PROJECTILE_DAMAGE = 3;
    private static final double RANGE = 150;
    private static final double FIRE_RATE = 500;
    private static final double COST = 600;

    private double x;
    private double y;


    /**
     * Constructor for Tank
     * @param x                 x-coordinate at centre of tank position
     * @param y                 y-coordinate at centre of tank position
     */
    public SuperTank(double x, double y) {
        super(x, y, TYPE, PROJECTILE_SPEED, PROJECTILE_DAMAGE, RANGE, FIRE_RATE, COST);
        this.x = x;
        this.y = y;
    }
}