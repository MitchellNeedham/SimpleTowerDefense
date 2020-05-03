import bagel.*;
import bagel.util.Point;
import bagel.util.Vector2;
import java.lang.Math;

public class Enemy {
    private final float movementSpeed;
    private final Image img;
    private final float spawnDelay;
    private int pointsIndex = 0;
    private Vector2 position;
    private Vector2 moveVector;
    private boolean active = false;
    private double angle;

    /**
     * Enemy constructor
     * @param movementSpeed movement speed of enemy in pixels per frame
     * @param spawnDelay time to be elapsed before enemy spawns and begins path
     * @param filePath file path of image for particular enemy
     */
    public Enemy(float movementSpeed, float spawnDelay, String filePath) {
        this.movementSpeed = movementSpeed;
        this.img = new Image(filePath);
        this.spawnDelay = spawnDelay;
    }

    /**
     * creates a movement vector of length 1 that aims at the point enemy is aiming towards
     * multiplies vector movement speed and any time scaling present and adds new vector to current position
     * draws enemy at new position
     * @param timeScale game speed multiplie
     * @param nextPoint position on map that enemy is moving towards
     */
    public void draw(float timeScale, Vector2 nextPoint) {
        if (position == null) {
            position = nextPoint;
        }
        if (position.sub(nextPoint).length() <= movementSpeed * timeScale) {
            pointsIndex++;
            return;
        }

        moveVector = nextPoint.sub(position);
        moveVector = moveVector.div(moveVector.length());
        moveVector = moveVector.mul(movementSpeed * timeScale);
        position = position.add(moveVector);

        angle = Math.atan2(moveVector.y, moveVector.x);


        DrawOptions rotate = new DrawOptions().setRotation(angle);


        img.draw(position.x, position.y, rotate);
    }

    /**
     * returns an index which determines which point in the points list the enemy is moving towards
     * @return index in points list
     */
    public int getIndex() {
        return pointsIndex;
    }

    /**
     * @return return boolean if enemy is active
     */
    public boolean isActive() {
        return active;
    }

    /**
     * awakens enemy and allows it to move
     */
    public void awake() {
        active = true;
    }

    /**
     * getter for spawnDelay
     * @return time to be elapsed before spawning
     */
    public float getSpawnDelay() {
        return spawnDelay;
    }


}
