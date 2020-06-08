
import bagel.util.Colour;
import bagel.util.Point;
import bagel.util.Vector2;

public class ExplosiveProjectile extends Projectile{

    //-------------------------RENDER PRIORITIES-------------------------//

    private static final int Z_INDEX = 4;

    //-------------------------RENDER PRIORITIES-------------------------//

    private static final Colour EXPLOSION_COLOUR = new Colour(0.9, 0, 0, 0.4);
    private static final String EXPLOSION_TYPE = "circle";

    private final String imgFile;
    private Point pos;
    private final Vector2 path;
    private double modifier;
    private final double expRadius;
    private double expSize = 0;
    private final double damage;

    /**
     * Constructor for explosive projectile
     * @param imgFile projectile image file loaded as bagel.Image
     * @param pos position at origin of projectile path
     * @param angle orientation of projectile image
     * @param speed movement speed of projectile in pixels per frame
     * @param expRadius Area of Effect for explosion
     * @param modifier reduction in speed over time
     */
    public ExplosiveProjectile(String imgFile, Point pos, double angle, double speed, double expRadius, double modifier, double damage) {
        super(pos);
        this.imgFile = imgFile;
        this.pos = pos;
        this.path = new Vector2(speed * Math.sin(angle), speed * -Math.cos(angle));
        this.expRadius = expRadius;
        this.modifier = modifier;
        this.damage = damage;
    }

    /**
     * Update position of projectile and explode when it hits ground
     * @param timeScale game speed multiplier
     */
    public void update(float timeScale) {
        // decrease modifier every frame
        if (timeScale > 0) {
            this.modifier *= modifier/timeScale;
        }

        // explode projectile if modifier reaches limit
        // otherwise, draw projectile
        // TODO: scale projectile image down to simulate falling
        // TODO: use time instead of modifier
        if (this.modifier < 0.01) {
            explode(timeScale);
        } else {
            pos = pos.asVector().add(path.mul(modifier*timeScale)).asPoint();
            updatePos(pos);
            RenderQueue.addToQueue(Z_INDEX, new RenderImage(pos.x, pos.y, imgFile));
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
        RenderQueue.addToQueue(Z_INDEX, new Shape(EXPLOSION_TYPE, pos.x, pos.y, expSize, EXPLOSION_COLOUR));

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
