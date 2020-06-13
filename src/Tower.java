
import bagel.Input;
import bagel.util.Point;

import java.util.List;

public interface Tower {

    void update(Input input, float timeScale);

    void place(double x, double y);

    List<Projectile> fire(Enemy target);

    boolean isOffScreen();

    boolean isReloaded();

    Point getPosition();

    double getRange();

    void updateRotation(double angle);

    boolean isPlacing();

    boolean canBePlaced(List<Point> blockedPoints, List<Line> blockedLines);

    boolean isBlocked();

    Point getTowerTopPosition();

    boolean isSold();

}
