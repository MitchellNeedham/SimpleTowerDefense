
import bagel.Input;
import bagel.util.Point;

import java.util.List;

public interface Tower {

    void update(Input input);

    void place(double x, double y);

    Projectile fire(Enemy target);

    boolean isOffScreen();

    boolean isReloaded();

    Point getPosition();

    double getRange();

    void updateRotation(double angle);

    boolean isPlacing();

    int getCost();

    boolean canBePlaced(List<Point> blockedPoints, boolean blockedTile);


}
