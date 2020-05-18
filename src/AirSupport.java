import bagel.*;
import bagel.util.Colour;
import bagel.util.Point;
import bagel.util.Vector2;

import java.util.List;
import java.util.Random;

public class AirSupport implements Tower {

    private double x;
    private double y;
    private static final Image TOWER_IMAGE = new Image("res/images/airsupport.png");
    private static final Image PROJECTILE_IMAGE = new Image("res/images/tank_projectile.png");
    private static final double PROJECTILE_SPEED = 2.5;
    private static final double[] FIRE_RATE_RANGE = {0, 2500};
    private static final double RANGE = 0;
    private static final double EXPLOSION_RADIUS = 200;
    private static final double MODIFIER = 0.90;
    private double fireRate;
    private static final double SPEED = 5.0;
    private boolean placing;
    private Time time = null;
    private final double angle;
    private final DrawOptions rotate;
    private double cost = 500;

    public AirSupport(double x, double y, double angle) {
        this.x = x;
        this.y = y;
        this.angle = angle;
        rotate = new DrawOptions().setRotation(angle);
        fireRate = new Random().nextDouble() * (FIRE_RATE_RANGE[1] - FIRE_RATE_RANGE[0]) + FIRE_RATE_RANGE[0];
        placing = true;
    }

    /**
     * update function for airsupport
     * @param input input from user, specifically any mouse events
     * @param timeScale game speed multiplier
     */
    @Override
    public void update(Input input, float timeScale) {

        // while tower is being dragged into position, draw it facing top of screen
        // and draw a line illustrating trajectory of plane
        if (placing) {
            // determine whether plane is travelling horizontal or vertical and draw line
            if (2 * angle / Math.PI % 2 == 0) {
                Drawing.drawLine(new Point(input.getMouseX(), 0), new Point(input.getMouseX(), Window.getHeight()),
                        5, new Colour(200, 0, 0, 0.4));
            } else {
                Drawing.drawLine(new Point(0, input.getMouseY()), new Point(Window.getWidth(), input.getMouseY()),
                        5, new Colour(200, 0, 0, 0.4));
            }
            TOWER_IMAGE.draw(input.getMouseX(), input.getMouseY(), rotate);
        } else {

            // increase y or x values based on direction of plane
            if (2 * angle / Math.PI % 2 == 0) {
                y += SPEED * -Math.cos(angle) * timeScale;
            } else {
                x += SPEED * Math.sin(angle) * timeScale;
            }

            TOWER_IMAGE.draw(x, y, rotate);
            time.updateTime(timeScale);
        }
    }

    /**
     * initialise plane outside game window on either y or x axis determined by plane orientation
     * @param x x-coordinate
     * @param y y-coordinate
     */
    @Override
    public void place(double x, double y) {
        // stop placing and set coordinated outside window at start of plane's path
        placing = false;
        if (2 * angle / Math.PI % 2 == 0) {
            this.x = x;
            this.y = Window.getHeight() * ((angle / Math.PI + 1) % 2);
        } else {
            this.x = Window.getWidth() * (((angle + Math.PI / 2) / Math.PI + 1) % 2);
            this.y = y;
        }

        //start time for reloading
        time = new Time();
    }


    /**
     * Drop explosive projectile to ground
     * @param target not applicable here (inherited from interface)
     * @return Explosive projectile dropped
     */
    @Override
    public Projectile fire(Enemy target) {
        // generate new random fire rate
        fireRate = new Random().nextDouble() * (FIRE_RATE_RANGE[1] - FIRE_RATE_RANGE[0]) + FIRE_RATE_RANGE[0];
        System.out.println(fireRate);

        // reset time
        time = new Time();

        // create new explosive projectile
        // TODO: use time instead of modifier
        return new ExplosiveProjectile(PROJECTILE_IMAGE, new Vector2(x, y), angle,
                PROJECTILE_SPEED, EXPLOSION_RADIUS, MODIFIER);
    }

    /**
     * Determines if plane is offscreen by factor of 2
     * @return boolean if plane is off screen
     */
    public boolean isOffScreen() {
        return x < -Window.getWidth() || x > Window.getWidth() * 2 ||
                y < -Window.getHeight() || y > Window.getHeight() * 2;
    }


    public Point getPosition() {
        return new Point(x, y);
    }

    public boolean isReloaded() {
        return time.getTotalGameTime() > fireRate;
    }

    @Override
    public boolean isPlacing() {
        return placing;
    }

    @Override
    public double getCost() {
        return cost;
    }

    @Override
    public double getRange() {
        return RANGE;
    }

    @Override
    public void updateRotation(double angle) {
    }

    public boolean isBlocked(List<Point> blockedPoints, List<Line> blockedLines) {
        return false;
    }
}
