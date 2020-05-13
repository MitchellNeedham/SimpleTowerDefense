import bagel.DrawOptions;
import bagel.Drawing;
import bagel.Image;
import bagel.Window;
import bagel.util.Colour;
import bagel.util.Vector2;

public class ExplosiveProjectile implements Projectile{

    private final Image projectileImg;
    private Vector2 pos;
    private final Vector2 path;
    private double angle;
    private double modifier;
    private boolean destroyed = false;
    private final double expRadius;
    private double expSize = 0;
    private double alpha = 0.5;

    /**
     * Constructor for explosive projectile
     * @param imgFile projectile image file loaded as bagel.Image
     * @param pos position at origin of projectile path
     * @param angle orientation of projectile image
     * @param speed movement speed of projectile in pixels per frame
     * @param expRadius Area of Effect for explosion
     * @param modifier reduction in speed over time
     */
    public ExplosiveProjectile(Image imgFile, Vector2 pos, double angle, double speed, double expRadius, double modifier) {
        this.projectileImg = imgFile;
        this.pos = pos;
        this.path = new Vector2(speed * Math.sin(angle), speed * -Math.cos(angle));
        this.angle = angle;
        this.expRadius = expRadius;
        this.modifier = modifier;
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
            pos = pos.add(path.mul(modifier*timeScale));
            projectileImg.draw(pos.x, pos.y);
        }
    }

    /**
     * Detonates explosive and draws growing red circle
     * @param timeScale game speed multiplier
     */
    private void explode(float timeScale) {

        // increase explosion radius
        expSize += expRadius / (60 / timeScale) * timeScale;
        alpha -= 1 / expRadius * timeScale;

        // draw red circle
        Drawing.drawCircle(pos.x, pos.y, expSize, new Colour(160, 0, 0, alpha));

        // when circle is larger than area of effect, destroy projectile
        if (expSize > expRadius) {
            this.destroyed = true;
        }

    }

    public boolean isDestroyed() {
        return destroyed;
    }

    public boolean isOffScreen() {
        return pos.x < 0 || pos.x > Window.getWidth() || pos.y < 0 || pos.y > Window.getHeight();
    }

    public boolean enemyInRadius(Vector2 pos) {
        return this.pos.sub(pos).length() < expSize;
    }
}
