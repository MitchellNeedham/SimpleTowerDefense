import bagel.DrawOptions;
import bagel.Image;
import bagel.util.Point;
import bagel.util.Vector2;

import java.util.Collections;
import java.util.List;

public class Enemy {

    //-------------------------ENEMY PROPERTIES-------------------------//

    private final static int Z_INDEX = 5;
    private final String enemyImage;
    private final double movementSpeed;
    private final float spawnDelay;
    private int pointsIndex = 0; // the indexed point on the polyline that the enemy is on path towards
    private Vector2 position;
    private boolean active = false;
    private boolean destroyed = false;
    private double angle = 0;
    private double hitPoints;
    private final double reward;
    private Vector2 moveVector = new Vector2(0, 0);
    private final double penalty;

    /**
     * Base constructor for Enemy
     * @param movementSpeed movement speed of enemy
     * @param spawnDelay spawn delay of enemy
     * @param filePath file path to enemy image
     * @param hitPoints hit points of enemy
     * @param reward reward for destroying enemy
     */
    public Enemy(double movementSpeed,
                 float spawnDelay,
                 String filePath,
                 double hitPoints,
                 double reward,
                 double penalty) {
        this.movementSpeed = movementSpeed;
        this.spawnDelay = spawnDelay;
        this.enemyImage = filePath;
        this.hitPoints = hitPoints;
        this.reward = reward;
        this.penalty = penalty;
    }

    /**
     * Constructor for children enemies that spawn on path instead of at the start
     * @param movementSpeed movement speed of enemy
     * @param hitPoints hit points of enemy
     * @param reward reward for destroying enemy
     * @param filePath file path of enemy image
     * @param position position to spawn enemy at
     * @param pointsIndex point to aim enemy towards
     */
    public Enemy(double movementSpeed,
                 double hitPoints,
                 double reward,
                 String filePath,
                 Point position,
                 int pointsIndex,
                 double penalty) {
        this.movementSpeed = movementSpeed;
        this.hitPoints = hitPoints;
        this.reward = reward;
        this.enemyImage = filePath;
        this.position = position.asVector();
        this.pointsIndex = pointsIndex;
        this.penalty = penalty;
        spawnDelay = 0;
    }


    /**
     * creates a movement vector of length 1 that aims at the point enemy is aiming towards
     * multiplies vector movement speed and any time scaling present and adds new vector to current position
     * draws enemy at new position
     * @param point position on map that enemy is moving towards
     */
    public void draw(Point point) {

        Vector2 nextPoint = point.asVector();

        //initialise position as nextPoint which will always be the first point in polyline
        if (position == null) position = nextPoint;

        //if enemy is within (movementSpeed * timeScale) pixels (i.e the closest position enroute to nextPoint)
        //change the point enemy is heading towards and draw enemy at nextPoint
        if (position.sub(nextPoint).length() <= movementSpeed * ShadowDefend.getTimeScale()) {
            pointsIndex++;
            RenderQueue.addToQueue(Z_INDEX, new RenderImage(nextPoint.x, nextPoint.y, enemyImage, new DrawOptions().setRotation(angle)));
            return;
        }

        //get direction vector by subtracting position from nextPoint
        moveVector = nextPoint.sub(position);

        // normalise vector to become a unit vector
        moveVector = moveVector.normalised();

        //multiply by timeScale and movement speed to get number of pixels it should move per frame
        moveVector = moveVector.mul(movementSpeed * ShadowDefend.getTimeScale());

        //add moveVector to position
        position = position.add(moveVector);

        // only update angle if game is not paused
        if (ShadowDefend.getTimeScale() > 0) angle = Math.atan2(moveVector.y, moveVector.x);

        DrawOptions rotate = new DrawOptions().setRotation(angle);

        //draw enemy
        RenderQueue.addToQueue(Z_INDEX, new RenderImage(position.x, position.y, enemyImage, rotate));
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

    /**
     * spawns children (base case is empty, overridden by slicers)
     * @return empty list
     */
    public List<Enemy> spawnChildren() {
        return Collections.emptyList();
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

    public Vector2 getMoveVector() {
        return moveVector;
    }

    public double getReward() { return reward; }

    public double getPenalty() { return penalty; }

}
