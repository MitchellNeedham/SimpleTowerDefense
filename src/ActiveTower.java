import bagel.*;
import bagel.util.Colour;
import bagel.util.Point;

import java.util.List;
import java.util.Random;

public abstract class ActiveTower implements Tower, Clickable {

    // render settings
    private final static int BASE_Z = 4;
    private final static int TOP_Z = 5;
    private final static int OVERLAY_Z = 6;
    private final static int UNDERLAY_Z = 3;

    // positions of tower base and top (turret)
    private Point towerPos;
    private Point towerTopPos;

    // type of tower
    private final String type;

    private static final String RES_PATH = "res/images/"; // image(s) path
    private static final String IMAGE_EXT = ".png"; // image extension

    // colours to display information to user
    private static final Colour RANGE_COLOUR = new Colour(0, 200, 0, 0.2); // colour of range radius
    private static final Colour HOVER_COLOUR = new Colour(255, 255, 255, 0.2); // colour of hover overlay
    private static final Colour BLOCKED_COLOUR = new Colour(255, 0, 0, 0.5); // colour of blocked overlay

    // upgrade panel information
    private static final String UPGRADE_PANEL = "res/images/panels/upgradepanel.png";
    private static final Point UPGRADE_PANEL_POS = new Point(Window.getWidth()-200f, 100);
    private Panel upgradePanel;

    //TODO: implement upgrades
    // all of these will all be editable when upgrades are implemented
    private String tower;
    private String towerBase;
    private String towerTop;
    private String towerProjectile;
    private double projectileSpeed;
    private double projectileDamage;
    private double range;
    private double fireRate;
    private double cost;
    private double turretAngle;
    private double bodyAngle;

    private boolean clicked = false; // has tower been clicked?
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
                       double fireRate,
                       double cost) {
        this.towerPos = new Point(x, y);
        this.towerTopPos = new Point(x, y);
        this.type = type;
        this.tower = RES_PATH + type + "/main" +IMAGE_EXT;
        this.towerBase = RES_PATH + type + "/bottom" + IMAGE_EXT;
        this.towerTop = RES_PATH + type + "/top" + IMAGE_EXT;
        this.towerProjectile = RES_PATH + type + "/projectile" + IMAGE_EXT;
        this.projectileSpeed = projectileSpeed;
        this.projectileDamage = projectileDamage;
        this.range = range;
        this.fireRate = fireRate;
        this.cost = cost;

        this.placing = true;
        turretAngle = new Random().nextDouble() * Math.PI * 2;
        bodyAngle = turretAngle;
        createUpgradePanel();

    }

    public ActiveTower(double x,
                       double y,
                       String type,
                       double projectileSpeed,
                       double projectileDamage,
                       double range,
                       double fireRate,
                       double cost,
                       double angle) {
        this.towerPos = new Point(x, y);
        this.towerTopPos = new Point(x, y);
        this.type = type;
        this.tower = RES_PATH + type + "/main" +IMAGE_EXT;
        this.towerTop = RES_PATH + type + "/main" +IMAGE_EXT;
        this.towerBase = RES_PATH + type + "/runway" +IMAGE_EXT;
        this.towerProjectile = RES_PATH + type + "/projectile" + IMAGE_EXT;
        this.projectileSpeed = projectileSpeed;
        this.projectileDamage = projectileDamage;
        this.range = range;
        this.fireRate = fireRate;
        this.cost = cost;

        this.placing = true;
        turretAngle = angle;
        bodyAngle = turretAngle;

        createUpgradePanel();
    }

    /**
     * Update tank position and turret rotation
     * @param input user defined input
     * @param timeScale game speed multiplier
     */
    @Override
    public void update(Input input, float timeScale) {
        // show upgrade panel when clicked
        if (clicked) {
            upgradePanel.update();
        }

        // determines what to do when clicked on or hovered over
        // TODO: fix these?
        click(input);
        hover(input);

        //
        if (placing) {
            towerPos = new Point(input.getMouseX(), input.getMouseY());
            towerTopPos = towerPos;
        } else {
            time.updateTime(timeScale);
            draw(timeScale);
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
        this.towerTopPos = new Point(x, y);
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
        return new StandardProjectile(towerProjectile, towerTopPos, projectileSpeed, projectileDamage, target);
    }

    /**
     * TODO can this be in update?
     * @param input user defined input
     */
    @Override
    public void hover(Input input) {
        double p = 1;
        Point mousePos = new Point(input.getMouseX(), input.getMouseY());
        if (mousePos.distanceTo(towerPos) <= 25) {
            if (placing) {
                RenderQueue.addToQueue(BASE_Z, new RenderImage(towerPos.x, towerPos.y, towerBase));
                RenderQueue.addToQueue(TOP_Z, new RenderImage(towerTopPos.x, towerTopPos.y, towerTop));
                p = 0;
            }

            if (isBlocked()) {
                RenderQueue.addToQueue(UNDERLAY_Z, new Shape("circle",towerPos.x, towerPos.y, range, RANGE_COLOUR));
                RenderQueue.addToQueue(OVERLAY_Z, new RenderImage(towerPos.x, towerPos.y, towerBase,
                        new DrawOptions().setRotation(bodyAngle * p).setBlendColour(200, 200, 200, 0.2)));
                RenderQueue.addToQueue(OVERLAY_Z, new RenderImage(towerTopPos.x, towerTopPos.y, towerTop,
                        new DrawOptions().setRotation(turretAngle * p).setBlendColour(200, 200, 200, 0.2)));
            } else {
                RenderQueue.addToQueue(OVERLAY_Z, new RenderImage(towerPos.x, towerPos.y, tower,
                        new DrawOptions().setBlendColour(BLOCKED_COLOUR)));
            }
        }
    }

    @Override
    public void click(Input input) {
        Point mousePos = new Point(input.getMouseX(), input.getMouseY());
        if (input.wasPressed(MouseButtons.LEFT) && blocked && !placing) {

            if (mousePos.distanceTo(towerPos) <= 25) {
                clicked = true;
            } else if (mousePos.distanceTo(towerPos) > 25 && !upgradePanel.getBoundingBox().isMouseOver(input)) {
                clicked = false;
            }
        }
    }

    public void draw() {

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
            if (p.distanceTo(towerPos) < 50) {
                return blocked = false;
            }
        }
        for (Line l : blockedLines) {
            if (l.DistanceToLine(towerPos) < 50) {
                return blocked = false;
            }
        }
        return blocked = true;
    }

    public void draw(float timeScale) {
        RenderQueue.addToQueue(BASE_Z, new RenderImage(towerPos.x, towerPos.y, towerBase,
                new DrawOptions().setRotation(bodyAngle)));
        RenderQueue.addToQueue(TOP_Z, new RenderImage(towerTopPos.x, towerTopPos.y, towerTop,
                new DrawOptions().setRotation(turretAngle)));
    }

    /**
     * creates and initialises upgrade panel for tower
     */
    private void createUpgradePanel() {
        upgradePanel = new Panel(UPGRADE_PANEL_POS.x, UPGRADE_PANEL_POS.y, UPGRADE_PANEL);
        String capitalisedType = type.substring(0, 1).toUpperCase() + type.substring(1);
        upgradePanel.addText("title", 100, 50, capitalisedType);
    }


    public Point getPosition() { return towerPos; }

    @Override
    public double getRange() { return range; }

    @Override
    public void updateRotation(double turretAngle) { this.turretAngle = turretAngle; }

    public boolean isOffScreen() {
        return towerPos.x < 0 || towerPos.x > Window.getWidth() || towerPos.y < 0 || towerPos.y > Window.getHeight();
    }

    public boolean isReloaded() { return time.getTotalGameTime() > fireRate; }

    @Override
    public boolean isPlacing() { return placing; }


    public boolean isBlocked() { return blocked; }

    public void setTowerTopPos(Point pos) { towerTopPos = pos; }

    public Point getTowerTopPosition() { return towerTopPos; }


}
