import bagel.*;
import bagel.util.Colour;
import bagel.util.Point;

import java.util.List;
import java.util.Random;

public class AirSupport implements Tower {

    //-------------------------TOWER IMAGE FILES-------------------------//

    private static final Image TOWER_IMAGE = new Image("res/images/airsupport.png");
    private static final Image PROJECTILE_IMAGE = new Image("res/images/explosive.png");


    //-------------------------PROJECTILE PROPERTIES-------------------------//

    private static final double PROJECTILE_DAMAGE = 500.0D;
    private static final double EXPLOSION_RADIUS = 200.0D;


    //-------------------------TOWER PROPERTIES-------------------------//

    public static final String TYPE = "airsupport";
    public static final int COST = 500;
    private static final double[] FIRE_RATE_RANGE = {0, 2000.0D};
    private static final double RANGE = 0;
    private static final double DELAY = 2000.0D;
    private static final double LINE_PATH_THICKNESS = 5.0D;
    private static final double SPEED = 2.5D;

    // position of tower
    private Point pos;
    private double fireRate;
    private boolean placing;
    private Timer timer = null;
    private final double angle;
    private final DrawOptions rotate;

    /**
     * Constructor for AirSupport
     * @param x x-position of tower
     * @param y y-position of tower
     * @param angle angle tower faces
     */
    public AirSupport(double x, double y, double angle) {
        this.pos = new Point(x, y);
        this.angle = angle;
        rotate = new DrawOptions().setRotation(angle);
        fireRate = new Random().nextDouble() * (FIRE_RATE_RANGE[1] - FIRE_RATE_RANGE[0]) + FIRE_RATE_RANGE[0];
        placing = true;
    }

    /**
     * update function for airsupport
     * @param input input from user, specifically any mouse events
     */
    @Override
    public void update(Input input) {
        // while tower is being dragged into position, draw in correct orientation
        // and draw a line illustrating trajectory of plane
        if (placing) {
            // determine whether plane is travelling horizontal or vertical and draw line
            if (2 * angle / Math.PI % 2 == 0) {
                Drawing.drawLine(new Point(input.getMouseX(), 0), new Point(input.getMouseX(), Window.getHeight()),
                        LINE_PATH_THICKNESS, new Colour(200, 0, 0, 0.4));
            } else {
                Drawing.drawLine(new Point(0, input.getMouseY()), new Point(Window.getWidth(), input.getMouseY()),
                        LINE_PATH_THICKNESS, new Colour(200, 0, 0, 0.4));
            }
            // draw tower at mouse position
            TOWER_IMAGE.draw(input.getMouseX(), input.getMouseY(), rotate);
        } else {
            // increase y or x values based on direction of plane
            if (2 * angle / Math.PI % 2 == 0) {
                double y = pos.y + SPEED * -Math.cos(angle) * ShadowDefend.getTimescale();
                pos = new Point(pos.x, y);
            } else {
                double x = pos.x + SPEED * Math.sin(angle) * ShadowDefend.getTimescale();
                pos = new Point(x, pos.y);
            }
            TOWER_IMAGE.draw(pos.x, pos.y, rotate);
            timer.updateTime();
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
            pos = new Point(x,  Window.getHeight() * ((angle / Math.PI + 1) % 2) + TOWER_IMAGE.getHeight());
        } else {
            pos = new Point(Window.getWidth() * (((angle + Math.PI / 2) / Math.PI + 1) % 2)-TOWER_IMAGE.getWidth(), y);
        }
        //start timer for reloading
        timer = new Timer();
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

        // reset timer
        timer.reset();

        // create new explosive projectile
        return new ExplosiveProjectile(PROJECTILE_IMAGE, pos, EXPLOSION_RADIUS, DELAY, PROJECTILE_DAMAGE);
    }

    /**
     * Determines if tower is off screen
     * @return boolean if tower is off screen
     */
    public boolean isOffScreen() {
        return getPosition().x < -TOWER_IMAGE.getWidth() || pos.x > Window.getWidth()
                || pos.y < 0 || pos.y > Window.getHeight() + TOWER_IMAGE.getHeight();
    }

    /**
     * Determines if tower has reloaded
     * @return boolean if tower has reloaded
     */
    public boolean isReloaded() { return timer.getTotalGameTime() > fireRate; }

    /**
     * Determines if tower can be placed
     * @param blockedPoints positions of other tower
     * @param blockedTile if mouse if over a blocked tile
     * @return Always true because this is a passive tower
     */
    public boolean canBePlaced(List<Point> blockedPoints, boolean blockedTile) { return true; }

    public int getCost() { return COST; }

    public Point getPosition() { return pos; }

    @Override
    public boolean isPlacing() { return placing; }

    @Override
    public double getRange() { return RANGE; }

    @Override
    public void updateRotation(double angle) { }

}
