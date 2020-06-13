
import bagel.Drawing;
import bagel.Image;
import bagel.util.Colour;
import bagel.util.Point;
import bagel.util.Vector2;

public class ExplosiveProjectile extends Projectile{

    private static final int Z_INDEX = 4;

    //-------------------------PROJECTILE DATA-------------------------//

    private static final Colour EXPLOSION_COLOUR = new Colour(0.9D, 0, 0, 0.4D);
    private static final int FRAMES_TO_COMPLETE_EXPLOSION = 60;

    private final String explosiveImage = "res/images/airsupport/explosive.png";
    private final Time time;
    private final Point pos;
    private final double delay;
    private final double expRadius;
    private double expSize = 0;
    private final double damage;

    /**
     * Constructor for explosive projectile
     * @param pos position at origin of projectile path
     * @param expRadius Area of Effect for explosion
     * @param delay timer delay before explosion
     */
    public ExplosiveProjectile(Point pos, double expRadius, double delay, double damage) {
        super(pos);
        this.pos = pos;
        this.expRadius = expRadius;
        this.delay = delay;
        this.damage = damage;
        this.time = new Time();
    }

    /**
     * Update position of projectile and explode when it hits ground
     */
    public void update(float timeScale) {

        time.updateTime(timeScale);

        // explode projectile if time has surpassed specified delay, otherwise draw projectile
        if (time.getTotalGameTime() > delay) {

            explode();
        } else {
            RenderQueue.addToQueue(Z_INDEX, new RenderImage(pos.x, pos.y, explosiveImage));
        }
    }

    /**
     * Detonates explosive and draws growing red circle
     */
    private void explode() {
        float timeScale = ShadowDefend.getTimeScale();
        // increase explosion radius
        expSize += expRadius / ((FRAMES_TO_COMPLETE_EXPLOSION/expRadius*10) / timeScale) * timeScale;

        // draw red circle
        RenderQueue.addToQueue(Z_INDEX, new Shape("circle", pos.x, pos.y, expSize, EXPLOSION_COLOUR));

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
