import bagel.DrawOptions;
import bagel.Image;
import bagel.Window;
import bagel.util.Point;
import bagel.util.Vector2;

public class StandardProjectile extends Projectile{

    private static final int Z_INDEX = 4;
    private static final double MIN_HIT_DIST = 20;
    private Enemy target;
    private final double speed;
    private final String imgFile;
    private Point pos;
    private Vector2 path;
    private boolean destroyed = false;
    private final double damage;

    private boolean tracking;
    private static final double MAX_ANGLE_CHANGE = Math.PI/10;

    private Point enemyPosition;

    /**
     * Constructor for standard projectile
     * @param imgFile projectile image file loaded as bagel.Image
     * @param pos position at origin of projectile path
     * @param speed movement speed of projectile in pixels per frame
     * @param target enemy that projectile is locked onto
     */
    public StandardProjectile(String imgFile, Point pos, double speed, double damage, Enemy target) {
        super(pos);
        this.imgFile = imgFile;
        this.pos = pos;
        this.speed = speed;
        this.target = target;
        this.path = pathToEnemy(pos.asVector(), target.getPosition().asVector());
        this.damage = damage;
        this.tracking = false;
    }

    public StandardProjectile(String imgFile, Point pos, double speed, double damage, Vector2 path) {
        super(pos);
        this.imgFile = imgFile;
        this.pos = pos;
        this.speed = speed;
        this.target = null;
        this.path = path.mul(speed);
        this.damage = damage;
        this.tracking = false;
    }

    public StandardProjectile(String imgFile, Point pos, double speed, double damage, Enemy target, Vector2 path) {
        super(pos);
        this.imgFile = imgFile;
        this.pos = pos;
        this.speed = speed;
        this.target = target;
        this.path = path.mul(speed);
        this.damage = damage;
        this.tracking = true;
    }

    /**
     * Updates position of projectile and determines if collision occurred
     * @param timeScale game speed multiplier
     */
    public void update(float timeScale) {
        // get path vector from current position aiming at enemy
        if (tracking) {
            if (target != null && !target.isDestroyed()) {
                path = trackToEnemy(pos.asVector(), target.getPosition().asVector());
                enemyPosition = target.getPosition();
            }
        } else if (target != null && !target.isDestroyed()) {
            path = pathToEnemy(pos.asVector(), target.getPosition().asVector());
        }
        double angle = Math.atan2(path.y, path.x);

        DrawOptions rotate = new DrawOptions().setRotation(angle + Math.PI/2);

        // add path vector to position
        pos = pos.asVector().add(path.mul(timeScale)).asPoint();
        updatePos(pos);

        // draw projectile
        RenderQueue.addToQueue(Z_INDEX, new RenderImage(pos.x, pos.y, imgFile, rotate));
    }





    /**
     * Determines path vector to enemy
     * @param projPos Vector2 containing projectile position
     * @param enemyPos Vector2 containing enemy position
     * @return Vector2 containing path vector for projectile
     */
    private Vector2 pathToEnemy(Vector2 projPos, Vector2 enemyPos) {
        return enemyPos.sub(projPos).normalised().mul(speed);
    }

    private Vector2 trackToEnemy(Vector2 projPos, Vector2 enemyPos) {
        Vector2 directPath = pathToEnemy(projPos, enemyPos);
        return directPath.add(path.mul(10/ShadowDefend.getTimeScale())).normalised().mul(speed);
    }

    /**
     * Determines if enemy has been hit
     * @param pos Position of enemy target
     * @return damage done to target if hit
     */
    public double hasHitEnemy(Point pos) {
        if (pos.distanceTo(this.pos) < MIN_HIT_DIST && !isDestroyed()) {
            super.destroy();
            return damage;
        }
        return 0;
    }

    @Override
    public boolean isOffScreen() {
        return pos.x < -Window.getWidth() || pos.x > Window.getWidth() * 2
                || pos.y < -Window.getHeight() || pos.y > Window.getHeight() * 2;
    }
}
