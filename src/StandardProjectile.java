import bagel.Image;
import bagel.Window;
import bagel.util.Point;
import bagel.util.Vector2;

public class StandardProjectile implements Projectile{

    private static final double MIN_HIT_DIST = 20;
    private final Enemy target;
    private final double speed;
    private final Image projectileImg;
    private Vector2 pos;
    private Vector2 path;
    private boolean destroyed = false;
    private double damage;
    private boolean hitTarget = false;

    /**
     * Constructor for standard projectile
     * @param imgFile projectile image file loaded as bagel.Image
     * @param pos position at origin of projectile path
     * @param speed movement speed of projectile in pixels per frame
     * @param target enemy that projectile is locked onto
     */
    public StandardProjectile(Image imgFile, Vector2 pos, double speed, double damage, Enemy target) {
        this.projectileImg = imgFile;
        this.pos = pos;
        this.speed = speed;
        this.target = target;
        this.path = pathToEnemy(pos, target.getPosition().asVector());
        this.damage = damage;

    }

    /**
     * Updates position of projectile and determines if collision occurred
     * @param timeScale game speed multiplier
     */
    public void update(float timeScale) {
        // get path vector from current position aiming at enemy
        if (!target.isDestroyed()) {
            path = pathToEnemy(pos, target.getPosition().asVector());
        }


        // add path vector to position
        pos = pos.add(path.mul(timeScale));

        // draw projectile

        projectileImg.draw(pos.x, pos.y);



        // if distance from projectile to target is less than minimum hit distance, destroy projectile and
        // reduce enemy health
        // TODO: implement reduction of enemy health


    }

    public boolean isDestroyed() {
        return destroyed;
    }

    public boolean isOffScreen() {
        return pos.x < 0 || pos.x > Window.getWidth() || pos.y < 0 || pos.y > Window.getHeight();
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


    public double hasHitEnemy(Point pos) {
        if (pos.distanceTo(this.pos.asPoint()) < MIN_HIT_DIST && !this.destroyed) {
            this.destroyed = true;
            return damage;
        }
        return 0;
    }
}
