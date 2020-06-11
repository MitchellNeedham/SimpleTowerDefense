import bagel.Input;
import bagel.util.Point;

public class SuperTank extends ActiveTower {

    //-------------------------SUPER TANK PROPERTIES-------------------------//

    public static final String TYPE = "supertank";
    public static final int COST = 600;
    private static final double PROJECTILE_SPEED = 10;
    private static final double PROJECTILE_DAMAGE = 3;
    private static final double RANGE = 150;
    private static final double FIRE_RATE = 500;

    /**
     * Constructor for SuperTank
     * @param x x-coordinate at centre of tank position
     * @param y y-coordinate at centre of tank position
     */
    public SuperTank(double x, double y) {
        super(x, y, TYPE, PROJECTILE_SPEED, PROJECTILE_DAMAGE, RANGE, FIRE_RATE);
    }

    @Override
    public int getCost() {
        return COST;
    }
}