import bagel.*;
import bagel.util.Colour;
import bagel.util.Point;

import java.util.List;
import java.util.Random;

public abstract class ActiveTower implements Tower {

    //-------------------------IMAGE FILE LOCATION AND FORMAT-------------------------//

    private static final String RES_PATH = "res/images/"; // image(s) path
    private static final String IMAGE_EXT = ".png"; // image extension


    //-------------------------TOWER STATE COLOURS-------------------------//

    private static final Colour RANGE_COLOUR = new Colour(0, 200, 0, 0.2); // colour of range radius
    private static final Colour BLOCKED_COLOUR = new Colour(255, 0, 0, 0.5); // colour of blocked overlay


    //-------------------------TOWER FILES AND DATA-------------------------//

    private static final double BOUNDING_RADIUS = 40.0D;
    //TODO: implement upgrades
    // all of these will all be editable when upgrades are implemented
    private Point towerPos;
    private final Image towerImage;
    private final Image projectileImage;
    private final double projectileSpeed;
    private final double projectileDamage;
    private final double range;
    private final double fireRate;
    private double angle;

    private boolean placing; // is tower active or placing?
    private Time time; //TODO: fix all times to be instance variables

    private boolean blocked = false; // is tower over a blocked position?




    /**
     * Constructor for Tank
     * @param x x-coordinate at centre of tank position
     * @param y y-coordinate at centre of tank position
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
        this.projectileImage = new Image(RES_PATH + type + "_projectile" + IMAGE_EXT);
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
     * @param timeScale game speed multiplier
     */
    @Override
    public void update(Input input, float timeScale) {
        // determines what to do when clicked on or hovered over
        hover(input);

        if (placing) {
            towerPos = new Point(input.getMouseX(), input.getMouseY());
        } else {
            time.updateTime(timeScale);
            draw();
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
        this.towerPos = new Point(x, y);
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
        return new StandardProjectile(projectileImage, towerPos, projectileSpeed, projectileDamage, target);
    }

    /**
     * TODO can this be in update?
     * @param input user defined input
     */
    public void hover(Input input) {
        Point mousePos = new Point(input.getMouseX(), input.getMouseY());
        if (mousePos.distanceTo(towerPos) <= BOUNDING_RADIUS/2) {
            if (placing) {
                towerImage.draw(towerPos.x, towerPos.y);
            }
            if (isBlocked()) {
                towerImage.draw(towerPos.x, towerPos.y, new DrawOptions().setBlendColour(BLOCKED_COLOUR));
            } else {
                Drawing.drawCircle(towerPos.x, towerPos.y, range, RANGE_COLOUR);
            }
        }
    }

    /**
     * checks blocked points and blocked lines to determine if tower can be placed.
     * @param blockedPoints list of points that tower can't be placed near
     * @param blockedLines list of lines that tower can't be placed on or near
     * @return boolean if tower can be placed
     */
    public boolean canBePlaced(List<Point> blockedPoints, List<Line> blockedLines) {
        // TODO: fix these, what the fuck did I do to blocked?
        for (Point p : blockedPoints) {
            if (p.distanceTo(towerPos) < BOUNDING_RADIUS) {
                return !(blocked = true);

            }
        }
        for (Line l : blockedLines) {
            if (l.DistanceToLine(towerPos) < BOUNDING_RADIUS) {
                return !(blocked = true);
            }
        }
        return !(blocked = false);
    }

    public void draw() {
        towerImage.draw(towerPos.x, towerPos.y, new DrawOptions().setRotation(angle) );
    }


    public Point getPosition() { return towerPos; }

    @Override
    public double getRange() { return range; }

    @Override
    public void updateRotation(double angle) {
        this.angle = angle;
    }

    public boolean isOffScreen() {
        return towerPos.x < 0 || towerPos.x > Window.getWidth() || towerPos.y < 0 || towerPos.y > Window.getHeight();
    }

    public boolean isReloaded() { return time.getTotalGameTime() > fireRate; }

    @Override
    public boolean isPlacing() { return placing; }

    public boolean isBlocked() { return blocked; }

    public int getCost() {
        return 0;
    }
}
