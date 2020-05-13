import bagel.*;
import bagel.util.Point;
import bagel.util.Vector2;
import java.lang.Math;

public class Enemy {
    private final Image img;
    private final float movementSpeed;
    private final float spawnDelay;

    private int pointsIndex = 0;
    private Vector2 position = null;
    private boolean active = false;
    private boolean destroyed = false;
    private double angle = 0;

    /**
     * Enemy constructor
     * @param movementSpeed movement speed of enemy in pixels per frame
     * @param spawnDelay time to be elapsed before enemy spawns and begins path
     * @param filePath file path of image for particular enemy
     */
    public Enemy(float movementSpeed, float spawnDelay, String filePath) {
        this.movementSpeed = movementSpeed;
        this.spawnDelay = spawnDelay;
        //create image for enemy
        this.img = new Image(filePath);

    }

    /**
     * creates a movement vector of length 1 that aims at the point enemy is aiming towards
     * multiplies vector movement speed and any time scaling present and adds new vector to current position
     * draws enemy at new position
     * @param timeScale game speed multiplie
     * @param nextPoint position on map that enemy is moving towards
     */
    public void draw(float timeScale, Vector2 nextPoint) {

        //initialise position as nextPoint which will always be the first point in polyline
        if (position == null) position = nextPoint;


        //if enemy is within (movementSpeed * timeScale) pixels (i.e the closest position enroute to nextPoint)
        //change the point enemy is heading towards and draw enemy at nextPoint
        if (position.sub(nextPoint).length() <= movementSpeed * timeScale) {
            pointsIndex++;
            img.draw(nextPoint.x, nextPoint.y, new DrawOptions().setRotation(angle));
            return;
        }

        //get direction vector by subtracting position from nextPoint
        Vector2 moveVector = nextPoint.sub(position);

        //divide moveVector by its length to make it a unit vector
        moveVector = moveVector.div(moveVector.length());

        //multiply by timeScale and movement speed to get number of pixels it should move per frame
        moveVector = moveVector.mul(movementSpeed * timeScale);

        //add moveVector to position
        position = position.add(moveVector);

        if (timeScale > 0) angle = Math.atan2(moveVector.y, moveVector.x);

        DrawOptions rotate = new DrawOptions().setRotation(angle);

        //draw enemy
        img.draw(position.x, position.y, rotate);
    }

    public int getIndex() {
        return pointsIndex;
    }

    public boolean isActive() {
        return active;
    }

    public void awake() {
        active = true;
    }

    public float getSpawnDelay() {
        return spawnDelay;
    }

    public void destroy() {
        this.destroyed = true;
    }

    public boolean isDestroyed() { return destroyed; }

    public Point getPosition() { return position.asPoint(); }

    // TODO: add decrease health function and split function

}
