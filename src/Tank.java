import bagel.*;
import bagel.util.Colour;
import bagel.util.Point;
import bagel.util.Vector2;

import java.util.Random;

public class Tank implements Tower, Clickable {

    private double x;
    private double y;
    private static final Image TANK_BODY = new Image("res/images/tank_body.png");
    private static final Image TANK_TURRET = new Image ("res/images/tank_turret.png");
    private static final Image TANK_PROJECTILE = new Image("res/images/tank_projectile.png");
    private static final double PROJECTILE_SPEED = 10;
    private static final double TURRET_OFFSET = 2;
    private static final double RANGE = 100;
    private static final Colour RANGE_COLOUR = new Colour(0, 0, 200, 0.4);
    private static final double FIRE_RATE = 1000;
    private boolean placing;
    private Time time;
    private double angle;
    private final DrawOptions rotate;

    //TODO: Should Super Tank be included as part of this class?

    /**
     * Constructor for Tank
     * @param x x-coordinate at centre of tank position
     * @param y y-coordinate at centre of tank position
     */
    public Tank(double x, double y) {
        this.x = x;
        this.y = y;
        this.placing = true;
        angle = new Random().nextDouble() * Math.PI * 2;
        rotate = new DrawOptions().setRotation(angle);
    }

    /**
     * Update tank position and turret rotation
     * @param input user defined input
     * @param timeScale game speed multiplier
     */
    @Override
    public void update(Input input, float timeScale) {
        double turret_x = x - TURRET_OFFSET * Math.sin(angle);
        double turret_y = y - TURRET_OFFSET * -Math.cos(angle);
        if (placing) {
            x = input.getMouseX();
            y = input.getMouseY();
            Drawing.drawCircle(x, y, RANGE, RANGE_COLOUR);
            TANK_BODY.draw(x, y);
            TANK_TURRET.draw(x, y + TURRET_OFFSET);
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
    @Override
    public void place(double x, double y) {
        placing = false;
        time = new Time();
        this.x = x;
        this.y = y;
    }

    /**
     * Creates projectile targeted at enemy
     * @param target enemy that tower is pointing at
     * @return Projectile launched at enemy
     */
    @Override
    public Projectile fire(Enemy target) {
        if (target == null) { return null; }
        time = new Time();
        return new StandardProjectile(TANK_PROJECTILE, new Vector2(x, y), PROJECTILE_SPEED, target);
    }

    @Override
    public double[] getPos() {
        return new double[0];
    }

    @Override
    public void draw() {

    }

    public Point getPosition() {
        return new Point(x, y);
    }

    @Override
    public double getRange() {
        return RANGE;
    }

    @Override
    public void updateRotation(double angle) {
        this.angle = angle;
    }

    public boolean isOffScreen() {
        return x < 0 || x > Window.getWidth() || y < 0 || y > Window.getHeight();
    }

    public boolean isReloaded() {
        return time.getTotalGameTime() > FIRE_RATE;
    }

    @Override
    public boolean isPlacing() {
        return placing;
    }
}
