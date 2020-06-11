import bagel.*;
import bagel.util.Colour;
import bagel.util.Point;

import java.util.List;
import java.util.Random;

public abstract class ActiveTower implements Tower {

    //-------------------------IMAGE FILE LOCATION AND FORMAT-------------------------//

    private static final String RES_PATH = "res/images/"; // image(s) path
    private static final String IMAGE_EXT = ".png"; // image extension
    private static final String PROJECTILE_FILE = "_projectile";


    //-------------------------TOWER STATE COLOURS-------------------------//

    private static final Colour RANGE_COLOUR = new Colour(0, 0.7D, 0.3D, 0.2D); // colour of range radius
    private static final Colour BLOCKED_COLOUR = new Colour(255, 0, 0, 0.5); // colour of blocked overlay


    //-------------------------TOWER FILES AND DATA-------------------------//

    private static final double BOUNDING_RADIUS = 50.0D; // used bounding radius instead of boundingboxes
    private Point towerPos;
    private final Image towerImage;
    private final Image projectileImage;
    private final double projectileSpeed;
    private final double projectileDamage;
    private final double range;
    private final double fireRate;
    private double angle;
    private boolean placing;
    private boolean blocked;
    private Timer timer;

    /**
     * Constructor for active towers
     * @param x x-position of tower
     * @param y y-position of tower
     * @param type String containing type of tower
     * @param projectileSpeed speed of tower's projectiles
     * @param projectileDamage damage of tower's projectiles
     * @param range range of tower
     * @param fireRate fire rate of tower
     */
    public ActiveTower(double x,
                       double y,
                       String type,
                       double projectileSpeed,
                       double projectileDamage,
                       double range,
                       double fireRate) {
        this.towerPos = new Point(x, y);
        this.towerImage = new Image(RES_PATH + type + IMAGE_EXT);
        this.projectileImage = new Image(RES_PATH + type + PROJECTILE_FILE + IMAGE_EXT);
        this.projectileSpeed = projectileSpeed;
        this.projectileDamage = projectileDamage;
        this.range = range;
        this.fireRate = fireRate;
        this.placing = true;
        angle = new Random().nextDouble() * Math.PI * 2;
    }

    /**
     * Update tank position and turret rotation
     * @param input user defined input
     */
    @Override
    public void update(Input input) {
        // determines what to do when hovered over
        if (input.getMousePosition().distanceTo(towerPos) <= BOUNDING_RADIUS/2) {
            hover();
        }
        if (placing) {
            towerPos = new Point(input.getMouseX(), input.getMouseY());
        } else {
            timer.updateTime();
            towerImage.draw(towerPos.x, towerPos.y, new DrawOptions().setRotation(angle));
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
        timer = new Timer(); // start timer to allow tower to shoot and reload
        this.towerPos = new Point(x, y);
    }

    /**
     * Creates projectile targeted at enemy
     * @param target enemy that tower is pointing at
     * @return Projectile launched at enemy
     */
    @Override
    public Projectile fire(Enemy target) {
        // if nothing to shoot, return nothing
        if (target == null) { return null; }

        timer.reset();
        return new StandardProjectile(projectileImage, towerPos, projectileSpeed, projectileDamage, target);
    }

    /**
     * Decides how to display tower if it is hovered over with mouse
     */
    public void hover() {
        if (placing) {
            towerImage.draw(towerPos.x, towerPos.y);
        }
        if (blocked) {
            towerImage.draw(towerPos.x, towerPos.y, new DrawOptions().setBlendColour(BLOCKED_COLOUR));
        } else {
            Drawing.drawCircle(towerPos.x, towerPos.y, range, RANGE_COLOUR);
        }
    }

    /**
     * checks blocked points and blocked lines to determine if tower can be placed.
     * @param blockedPoints list of points that tower can't be placed near
     * @return boolean if tower is over a blocked tile
     */
    public boolean canBePlaced(List<Point> blockedPoints, boolean blockedTile) {
        if (blockedTile) {
            return !(blocked = true); // unsure if this is bad code. Want to assign variable and return opposite
        }
        for (Point p : blockedPoints) {
            if (p.distanceTo(towerPos) < BOUNDING_RADIUS) {
                return !(blocked = true);
            }
        }
        return !(blocked = false);
    }

    /**
     * determines if tower is off screen
     * @return boolean if off screen
     */
    public boolean isOffScreen() {
        return towerPos.x < 0 || towerPos.x > Window.getWidth() || towerPos.y < 0 || towerPos.y > Window.getHeight();
    }

    /**
     * Determines if tower has reloaded
     * @return boolean if reloaded
     */
    public boolean isReloaded() { return timer.getTotalGameTime() > fireRate; }

    // base case is zero, subclasses override this
    public int getCost() { return 0; }

    public Point getPosition() { return towerPos; }

    @Override
    public double getRange() { return range; }

    @Override
    public void updateRotation(double angle) { this.angle = angle; }

    @Override
    public boolean isPlacing() { return placing; }


}
