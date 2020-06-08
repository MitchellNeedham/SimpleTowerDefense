import bagel.*;
import bagel.util.Point;
import bagel.util.Vector2;
import java.lang.Math;
import java.util.Collections;
import java.util.List;

public class Enemy {

    // z index for render
    private final int z_index;

    private final String imgPath;


    private final double movementSpeed;
    private final float spawnDelay;
    private int pointsIndex = 0; // the point that the enemy is on path towards
    private Vector2 position;
    private boolean active = false;
    private boolean destroyed = false;
    private double angle = 0;
    private double hitPoints;
    private final double reward;
    private final String type;
    private Vector2 moveVector = new Vector2(0, 0);

    /**
     * Base constructor for Enemy
     * @param type string containing type of enemy
     * @param z_index z index for rendering position of enemy
     * @param movementSpeed movement speed of enemy
     * @param spawnDelay spawn delay of enemy
     * @param filePath file path to enemy image
     * @param hitPoints hit points of enemy
     * @param reward reward for destroying enemy
     */
    public Enemy(String type,
                 int z_index,
                 double movementSpeed,
                 float spawnDelay,
                 String filePath,
                 double hitPoints,
                 double reward) {
        this.type = type;
        this.z_index = z_index;
        this.movementSpeed = movementSpeed;
        this.spawnDelay = spawnDelay;
        //create image for enemy
        this.imgPath = filePath;
        this.hitPoints = hitPoints;
        this.reward = reward;
    }

    /**
     * Constructor for children enemies that spawn on path instead of at the start
     * @param type String containing type of enemy
     * @param z_index z index for render position
     * @param movementSpeed movement speed of enemy
     * @param hitPoints hit points of enemy
     * @param reward reward for destroying enemy
     * @param filePath file path of enemy image
     * @param position position to spawn enemy at
     * @param pointsIndex point to aim enemy towards
     */
    public Enemy(String type,
                 int z_index,
                 double movementSpeed,
                 double hitPoints,
                 double reward,
                 String filePath,
                 Point position,
                 int pointsIndex) {
        this.type = type;
        this.z_index = z_index;
        this.movementSpeed = movementSpeed;
        this.hitPoints = hitPoints;
        this.reward = reward;
        this.imgPath = filePath;
        this.position = position.asVector();
        this.pointsIndex = pointsIndex;
        spawnDelay = 0;
    }

    /**
     * creates a movement vector of length 1 that aims at the point enemy is aiming towards
     * multiplies vector movement speed and any time scaling present and adds new vector to current position
     * draws enemy at new position
     * @param timeScale game speed multiplier
     * @param nextPoint position on map that enemy is moving towards
     */
    public void draw(float timeScale, Vector2 nextPoint) {

        //initialise position as nextPoint which will always be the first point in polyline
        if (position == null) position = nextPoint;


        //if enemy is within (movementSpeed * timeScale) pixels (i.e the closest position enroute to nextPoint)
        //change the point enemy is heading towards and draw enemy at nextPoint
        if (position.sub(nextPoint).length() <= movementSpeed * timeScale) {
            pointsIndex++;
            RenderQueue.addToQueue(z_index, new RenderImage(nextPoint.x, nextPoint.y, imgPath,
                    new DrawOptions().setRotation(angle)));
            return;
        }

        //get direction vector by subtracting position from nextPoint
        moveVector = nextPoint.sub(position);

        // normalise vector to become a unit vector
        moveVector = moveVector.normalised();

        //multiply by timeScale and movement speed to get number of pixels it should move per frame
        moveVector = moveVector.mul(movementSpeed * timeScale);

        //add moveVector to position
        position = position.add(moveVector);

        // only update angle if game is not paused
        if (timeScale > 0) angle = Math.atan2(moveVector.y, moveVector.x);

        DrawOptions rotate = new DrawOptions().setRotation(angle);

        //draw enemy
        RenderQueue.addToQueue(z_index, new RenderImage(position.x, position.y, imgPath, rotate));
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

    public String getType() {
        return type;
    }

    public Vector2 getMoveVector() {
        return moveVector;
    }

    /**
     * subtracts damage from hit points and returns boolean if destroyed
     * @param dmgPoints damage dealt by projectile
     * @return boolean if enemy is destroyed
     */
    public boolean destroyedByDamage(double dmgPoints) {
        hitPoints -= dmgPoints;
        if (hitPoints <= 0) {
            this.destroy();
            return true;
        }
        return false;
    }

    public double getReward() { return reward; }

    public double getPenalty() { return 0; }

    /**
     * spawns children (base case is empty)
     * @return empty list
     */
    public List<Enemy> spawnChildren() {
        return Collections.emptyList();
    }


}
