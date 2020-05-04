import bagel.*;
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
        if (position == null) {
            position = nextPoint;
        }

        //if enemy is within (movementSpeed * timeScale) pixels (i.e the closest position to nextPoint)
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

        //get angle of path
        angle = Math.atan2(moveVector.y, moveVector.x);
        DrawOptions rotate = new DrawOptions().setRotation(angle);

        //draw enemy
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
     * @return time to be elapsed before enemy can spawn
     */
    public float getSpawnDelay() {
        return spawnDelay;
    }

    /**
     * set destroyed to true (stop drawing enemy)
     */
    public void destroy() {
        this.destroyed = true;
    }

    /**
     * getter for destroyed boolean
     * @return boolean if enemy destroyed
     */
    public boolean isDestroyed() {
        return destroyed;
    }


}
