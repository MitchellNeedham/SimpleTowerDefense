import bagel.*;
import bagel.util.Colour;
import bagel.util.Point;
import bagel.util.Vector2;

import java.util.Random;

public class SuperTank implements Tower, Clickable {

    private double x;
    private double y;
    private static final Image TANK_BODY = new Image("res/images/supertank_body.png");
    private static final Image TANK_TURRET = new Image("res/images/supertank_turret.png");
    private static final Image TANK_PROJECTILE = new Image("res/images/supertank_projectile.png");
    private static final double PROJECTILE_SPEED = 10;
    private static final double TURRET_OFFSET = 4;
    private static final double RANGE = 150;
    private static final Colour RANGE_COLOUR = new Colour(0, 0, 200, 0.4);
    private static final double FIRE_RATE = 500;
    private boolean placing;
    private boolean reloaded;
    private Time time;
    private double angle;
    private final DrawOptions rotate;

    /**
     * Constructor for Super Tank
     * @param x x-coordinate at centre of tank position
     * @param y y-coordinate at centre of tank position
     */
    public SuperTank(double x, double y) {
        this.x = x;
        this.y = y;
        this.placing = true;
        angle = new Random().nextDouble() * Math.PI * 2;
        rotate = new DrawOptions().setRotation(angle);
    }

    //TODO: implement Clickable functions and tower upgrades
    public double[] getPos() {
        return new double[]{x, y};
    }

    @Override
    public void draw() {

    }

    /**
     * Update Super Tank position and rotation
     * @param input user defined input
     * @param timeScale game speed multiplier
     */
    public void update(Input input, float timeScale) {

        // TODO: fix turret placement
        double turret_x = x - TURRET_OFFSET * Math.sin(angle);
        double turret_y = y - TURRET_OFFSET * -Math.cos(angle);

        // if placing tower, draw at cursor position
        if (placing) {
            x = input.getMouseX();
            y = input.getMouseY();
            Drawing.drawCircle(x, y, RANGE, RANGE_COLOUR);
            TANK_BODY.draw(x, y);
            TANK_TURRET.draw(x, y + TURRET_OFFSET);

        // else, draw at position and rotate turret
        } else {
            time.updateTime(timeScale);
            TANK_BODY.draw(x, y, rotate);
            TANK_TURRET.draw(turret_x, turret_y, new DrawOptions().setRotation(angle));
        }

    }

    /**
     * place tower at x and y position and start tower functioning
     * @param x x-coordinate to place tower at
     * @param y y-coordinate to place tower at
     */
    public void place(double x, double y) {
        placing = false;
        reloaded = true;
        time = new Time();
        this.x = x;
        this.y = y;
    }

    /**
     * Creates projectile targeted at enemy
     * @param target enemy that tower is pointing at
     * @return Projectile launched at enemy
     */
    public Projectile fire(Enemy target) {
        if (target == null) { return null; }
        reloaded = false;
        time = new Time();
        return new StandardProjectile(TANK_PROJECTILE, new Vector2(x, y), PROJECTILE_SPEED, target);
    }

    public boolean isOffScreen() {
        return x < 0 || x > Window.getWidth() || y < 0 || y > Window.getHeight();
    }

    public Point getPosition() {
        return new Point(x, y);
    }

    public boolean isReloaded() {
        if (time.getTotalGameTime() > FIRE_RATE) {
            return reloaded = true;
        }
        return false;
    }

    @Override
    public double getRange() {
        return RANGE;
    }

    @Override
    public void updateRotation(double angle) {
        this.angle = angle;
    }

    @Override
    public boolean isPlacing() {
        return placing;
    }
}
