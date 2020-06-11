import bagel.Window;
import bagel.util.Point;

public abstract class Projectile {


    private Point pos;
    private boolean destroyed;

    /**
     * Constructor for projectile class
     * @param pos position of projectile
     */
    public Projectile(Point pos) {
        this.pos = pos;
    }

    // Overridden by subclasses
    public void update() { }

    public boolean isOffScreen() {
        return pos.x < 0 || pos.x > Window.getWidth() || pos.y < 0 || pos.y > Window.getHeight();
    }


    public boolean isDestroyed() { return destroyed; }

    public void destroy() { this.destroyed = true; }

    public void updatePos(Point pos) { this.pos = pos; }

}
