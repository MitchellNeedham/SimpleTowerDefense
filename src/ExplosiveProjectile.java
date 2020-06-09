
import bagel.Drawing;
import bagel.Image;
import bagel.util.Colour;
import bagel.util.Point;
import bagel.util.Vector2;

public class ExplosiveProjectile extends Projectile{


    //-------------------------RENDER PRIORITIES-------------------------//

    private static final Colour EXPLOSION_COLOUR = new Colour(0.9D, 0, 0, 0.4D);

    private final Image projectileImage;
    private final Time time;
    private Point pos;
    private final Vector2 path;
    private double delay;
    private final double expRadius;
    private double expSize = 0;
    private final double damage;

    /**
     * Constructor for explosive projectile
     * @param image projectile image file loaded as bagel.Image
     * @param pos position at origin of projectile path
     * @param angle orientation of projectile image
     * @param speed movement speed of projectile in pixels per frame
     * @param expRadius Area of Effect for explosion
     * @param delay time delay before explosion
     */
    public ExplosiveProjectile(Image image, Point pos, double angle, double speed, double expRadius, double delay, double damage) {
        super(pos);
        this.projectileImage = image;
        this.pos = pos;
        this.path = new Vector2(speed * Math.sin(angle), speed * -Math.cos(angle));
        this.expRadius = expRadius;
        this.delay = delay;
        this.damage = damage;
        this.time = new Time();
    }

    /**
     * Update position of projectile and explode when it hits ground
     * @param timeScale game speed multiplier
     */
    public void update(float timeScale) {
        time.updateTime(timeScale);

        // explode projectile if modifier reaches limit
        // otherwise, draw projectile
        // TODO: scale projectile image down to simulate falling
        // TODO: use time instead of modifier
        if (time.getTotalGameTime() > delay) {
            explode(timeScale);
        } else {
            projectileImage.draw(pos.x, pos.y);
        }
    }

    /**
     * Detonates explosive and draws growing red circle
     * @param timeScale game speed multiplier
     */
    private void explode(float timeScale) {

        // increase explosion radius
        expSize += expRadius / (60 / timeScale) * timeScale;

        // draw red circle
        Drawing.drawCircle(pos.x, pos.y, expSize, EXPLOSION_COLOUR);

        // when circle is larger than area of effect, destroy projectile
        if (expSize > expRadius) {
            destroy();
        }
    }

    /**
     * Determines if enemy is in explosion radius
     * @param pos Position of enemy
     * @return boolean if in range
     */
    public boolean enemyInRadius(Vector2 pos) {
        return this.pos.asVector().sub(pos).length() < expSize;
    }

    public double getDamage() {
        return damage;
    }
}
