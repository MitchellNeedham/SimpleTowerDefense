import bagel.Window;

public interface Projectile {

    void update(float timeScale);

    boolean isDestroyed();

    boolean isOffScreen();
}
