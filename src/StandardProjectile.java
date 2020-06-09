import bagel.Image;
import bagel.util.Point;
import bagel.util.Vector2;

public class StandardProjectile extends Projectile{

    private static final double MIN_HIT_DIST = 20;
    private final Enemy target;
    private final double speed;
    private final Image projectileImage;
    private Point pos;
    private Vector2 path;
    private final double damage;

    /**
     * Constructor for standard projectile
     * @param image projectile image file loaded as bagel.Image
     * @param pos position at origin of projectile path
     * @param speed movement speed of projectile in pixels per frame
     * @param target enemy that projectile is locked onto
     */
    public StandardProjectile(Image image, Point pos, double speed, double damage, Enemy target) {
        super(pos);
        this.projectileImage = image;
        this.pos = pos;
        this.speed = speed;
        this.target = target;
        this.path = pathToEnemy(pos.asVector(), target.getPosition().asVector());
        this.damage = damage;

    }

    /**
     * Updates position of projectile and determines if collision occurred
     * @param timeScale game speed multiplier
     */
    public void update(float timeScale) {
        // get path vector from current position aiming at enemy
        if (!target.isDestroyed()) {
            path = pathToEnemy(pos.asVector(), target.getPosition().asVector());
        }

        // add path vector to position
        pos = pos.asVector().add(path.mul(timeScale)).asPoint();
        updatePos(pos);

        // draw projectile
        projectileImage.draw(pos.x, pos.y);
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
}
