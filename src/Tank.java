import bagel.*;
import bagel.util.Colour;
import bagel.util.Point;
import bagel.util.Vector2;

import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

public class Tank implements Tower, Clickable {

    private double x;
    private double y;
    private static final Image TANK_IMAGE = new Image("res/images/tank.png");
    private static final Image TANK_BODY = new Image("res/images/tank_body.png");
    private static final Image TANK_TURRET = new Image ("res/images/tank_turret.png");
    private static final Image TANK_PROJECTILE = new Image("res/images/tank_projectile.png");
    private static final double PROJECTILE_SPEED = 10;
    private static final double PROJECTILE_DAMAGE = 1;
    private static final double TURRET_OFFSET = 2;
    private static final double RANGE = 100;
    private static final Colour RANGE_COLOUR = new Colour(0, 200, 0, 0.2);
    private static final double FIRE_RATE = 1000;
    private double cost = 250;
    private boolean placing;
    private Time time;
    private double turretAngle;
    private double bodyAngle;
    private boolean hover = false;
    private boolean blocked = false;

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
        turretAngle = new Random().nextDouble() * Math.PI * 2;
        bodyAngle = turretAngle;
    }

    /**
     * Update tank position and turret rotation
     * @param input user defined input
     * @param timeScale game speed multiplier
     */
    @Override
    public void update(Input input, float timeScale) {
        double turret_x = x - TURRET_OFFSET * Math.sin(turretAngle);
        double turret_y = y - TURRET_OFFSET * -Math.cos(turretAngle);
        if (placing) {
            x = input.getMouseX();
            y = input.getMouseY();

            TANK_BODY.draw(x, y);
            TANK_TURRET.draw(x, y + TURRET_OFFSET);
            if (blocked) {
                TANK_IMAGE.draw(x, y, new DrawOptions().setBlendColour(255, 0, 0, 0.5));
            }
        } else {
            time.updateTime(timeScale);
            TANK_BODY.draw(x, y, new DrawOptions().setRotation(bodyAngle));
            TANK_TURRET.draw(turret_x, turret_y, new DrawOptions().setRotation(turretAngle));
            if (hover) {

                new Image("res/images/tank_body.png").draw(x, y, new DrawOptions().setRotation(bodyAngle).setBlendColour(200, 200, 200, 0.2));
                new Image("res/images/tank_turret.png").draw(turret_x, turret_y, new DrawOptions().setRotation(turretAngle).setBlendColour(200, 200, 200, 0.2));
            }

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
        return new StandardProjectile(TANK_PROJECTILE, new Vector2(x, y), PROJECTILE_SPEED, PROJECTILE_DAMAGE, target);
    }

    // TODO: fix this
    @Override
    public double[] getPos() {
        return new double[0];
    }

    /**
     * TODO can this be in update?
     * @param input user defined input
     */
    @Override
    public void hover(Input input) {
        Point mousePos = new Point(input.getMouseX(), input.getMouseY());
        if (mousePos.distanceTo(new Point(this.x, this.y)) <= 25) {
            if (!blocked) {
                Drawing.drawCircle(x, y, RANGE, RANGE_COLOUR);
            }
            hover = true;
        } else {
            hover = false;
        }
    }

    @Override
    public void click(Input input) {

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
    public void updateRotation(double turretAngle) {
        this.turretAngle = turretAngle;
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

    @Override
    public double getCost() {
        return cost;
    }

    /**
     * checks blocked points and blocked lines to determine if tower can be placed.
     * @param blockedPoints list of points that tower can't be placed near
     * @param blockedLines list of lines that tower can't be placed on or near
     * @return boolean if tower can be placed
     */
    public boolean isBlocked(List<Point> blockedPoints, List<Line> blockedLines) {
        for (Point p : blockedPoints) {
            if (p.distanceTo(new Point(x, y)) < 45) {
                return blocked = true;
            }
        }
        for (Line l : blockedLines) {
            if (l.DistanceToLine(new Point(x, y)) < 45) {
                return blocked = true;
            }
        }
        return blocked = false;
    }
}
