import bagel.*;
import bagel.util.Colour;
import bagel.util.Point;

import java.util.List;
import java.util.Random;

public class AirSupport implements Tower {

    //-------------------------RENDER PRIORITIES-------------------------//

    private static final int TOWER_Z = 7;
    private static final int UNDERLAY_Z = 6;


    //-------------------------TOWER IMAGE FILES-------------------------//

    private static final String TOWER_IMG = "res/images/airsupport/main.png";
    private static final Image TOWER_IMAGE = new Image("res/images/airsupport/main.png");
    private static final String PROJECTILE_IMAGE = "res/images/tank/projectile.png";


    //-------------------------PROJECTILE PROPERTIES-------------------------//

    private static final double PROJECTILE_SPEED = 2.5;
    private static final double PROJECTILE_DAMAGE = 500;
    private static final double EXPLOSION_RADIUS = 200;


    //-------------------------TOWER PROPERTIES-------------------------//

    private static final double[] FIRE_RATE_RANGE = {0, 2500};
    private static final double RANGE = 0;
    private static final double MODIFIER = 0.90;
    private static final double LINE_PATH_THICKNESS = 5;
    private static final double SPEED = 5.0;

    // position of tower
    private Point pos;
    private double fireRate;
    private boolean placing;
    private Time time = null;
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
     * @param timeScale game speed multiplier
     */
    @Override
    public void update(Input input, float timeScale) {

        // while tower is being dragged into position, draw it facing top of screen
        // and draw a line illustrating trajectory of plane
        if (placing) {
            // determine whether plane is travelling horizontal or vertical and draw line
            if (2 * angle / Math.PI % 2 == 0) {
                //TODO: Add to renderQueue
                Drawing.drawLine(new Point(input.getMouseX(), 0), new Point(input.getMouseX(), Window.getHeight()),
                        LINE_PATH_THICKNESS, new Colour(200, 0, 0, 0.4));
            } else {
                Drawing.drawLine(new Point(0, input.getMouseY()), new Point(Window.getWidth(), input.getMouseY()),
                        LINE_PATH_THICKNESS, new Colour(200, 0, 0, 0.4));
            }
            // TODO: add to renderQueue
            TOWER_IMAGE.draw(input.getMouseX(), input.getMouseY(), rotate);
        } else {

            // increase y or x values based on direction of plane
            if (2 * angle / Math.PI % 2 == 0) {
                double y = pos.y + SPEED * -Math.cos(angle) * timeScale;
                pos = new Point(pos.x, y);
            } else {
                double x = pos.x + SPEED * Math.sin(angle) * timeScale;
                pos = new Point(x, pos.y);
            }
            RenderQueue.addToQueue(TOWER_Z, new RenderImage(pos.x, pos.y, TOWER_IMG, rotate));

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
            pos = new Point(x,  Window.getHeight() * ((angle / Math.PI + 1) % 2));
        } else {
            pos = new Point(Window.getWidth() * (((angle + Math.PI / 2) / Math.PI + 1) % 2), y);
        }

        //start time for reloading
        // TODO: fix this
        time = new Time();
    }

    public boolean isOffScreen() {
        return getPosition().x < 0 || pos.x > Window.getWidth() || pos.y < 0 || pos.y > Window.getHeight();
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
        return new ExplosiveProjectile(PROJECTILE_IMAGE, pos, angle,
                PROJECTILE_SPEED, EXPLOSION_RADIUS, MODIFIER, PROJECTILE_DAMAGE);
    }

    // TODO: fix these, not all are required

    public Point getPosition() {
        return pos;
    }

    public boolean isReloaded() {
        return time.getTotalGameTime() > fireRate;
    }

    @Override
    public boolean isPlacing() {
        return placing;
    }

    @Override
    public double getRange() {
        return RANGE;
    }

    @Override
    public void updateRotation(double angle) { }


    public boolean canBePlaced(List<Point> blockedPoints, List<Line> blockedLines) {
        return true;
    }

    @Override
    public boolean isBlocked() { return false; }

    @Override
    public Point getTowerTopPosition() {
        return getPosition();
    }


}
