
import bagel.Drawing;
import bagel.Image;
import bagel.util.Colour;
import bagel.util.Point;
import bagel.util.Vector2;

public class ExplosiveProjectile extends Projectile{

    //-------------------------PROJECTILE DATA-------------------------//

    private static final Colour EXPLOSION_COLOUR = new Colour(0.9D, 0, 0, 0.4D);
    private static final int FRAMES_TO_COMPLETE_EXPLOSION = 60;

    private final Image projectileImage;
    private final Timer timer;
    private final Point pos;
    private final double delay;
    private final double expRadius;
    private double expSize = 0;
    private final double damage;

    /**
     * Constructor for explosive projectile
     * @param image projectile image file loaded as bagel.Image
     * @param pos position at origin of projectile path
     * @param expRadius Area of Effect for explosion
     * @param delay timer delay before explosion
     */
    public ExplosiveProjectile(Image image, Point pos, double expRadius, double delay, double damage) {
        super(pos);
        this.projectileImage = image;
        this.pos = pos;
        this.expRadius = expRadius;
        this.delay = delay;
        this.damage = damage;
        this.timer = new Timer();
    }

    /**
     * Update position of projectile and explode when it hits ground
     */
    public void update() {
        timer.updateTime();

        // explode projectile if time has surpassed specified delay, otherwise draw projectile
        if (timer.getTotalGameTime() > delay) {
            explode();
        } else {
            projectileImage.draw(pos.x, pos.y);
        }
    }

    /**
     * Detonates explosive and draws growing red circle
     */
    private void explode() {
        float timeScale = ShadowDefend.getTimescale();
        // increase explosion radius
        expSize += expRadius / (FRAMES_TO_COMPLETE_EXPLOSION / timeScale) * timeScale;

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
