import bagel.*;
import bagel.util.Colour;
import bagel.util.Point;
import bagel.util.Vector2;

import java.util.Random;

public class Bomber extends ActiveTower {

    // TODO: change name - it is a fighter plane silly

    //-------------------------RENDER PRIORITIES-------------------------//

    private static final int RUNWAY_Z = 2;
    private static final int PLANE_Z = 8;
    private final static int OVERLAY_Z = 9;
    private final static int UNDERLAY_Z = 2;


    //-------------------------INFO DISPLAY PROPERTIES-------------------------//

    private final static int PATH_LINE_THICKNESS = 5;
    private final static Colour PATH_LINE_COLOUR = new Colour(0, 0, 200, 0.3);
    private static final Colour BLOCKED_COLOUR = new Colour(255, 0, 0, 0.5);


    //-------------------------TOWER IMAGE FILES-------------------------//

    private static final String RUNWAY_IMAGE = "res/images/bomber/runway.png";
    private static final String PLANE_IMAGE = "res/images/bomber/main.png";


    //-------------------------TOWER PROPERTIES-------------------------//

    private static final String TYPE = "bomber";
    private static final double PROJECTILE_SPEED = 15;
    private static final double PROJECTILE_DAMAGE = 2;
    private static final double RANGE = 250;
    private static final double FIRE_RATE = 500;
    private static final double COST = 800;
    private static final double PATH_SIZE = 300;
    private static final double SPEED = 1.5;


    //------------------------------------------------------------//

    private final DrawOptions rotation;
    private Point planePos = new Point(0, 0);
    private Vector2 path;
    private static final double RUNWAY_ANGLE = Math.PI * 3 / 4;
    private double angle;
    private int quadrant = 0;
    private int direction = 1;
    private Point airBasePos;

    /**
     * Constructor for Bomber
     * @param x x-position of tower
     * @param y y-position of tower
     */
    public Bomber(double x, double y) {
        super(x, y, TYPE, PROJECTILE_SPEED, PROJECTILE_DAMAGE, RANGE, FIRE_RATE, COST, RUNWAY_ANGLE);
        angle = RUNWAY_ANGLE;
        rotation = new DrawOptions().setRotation(angle);
    }

    /**
     * draws tower
     * @param timeScale game speed mutliplie
     */
    @Override
    public void draw(float timeScale) {
        // set position of air base
        if (airBasePos == null) {
            airBasePos = getPosition();
        }

        // if game is not paused, get new position relative to air base
        if (timeScale > 0) {
            // calculates path from equation forming a figure 8
            path = calculatePath(timeScale);
            planePos = new Point(planePos.x + path.x, planePos.y + path.y);
            angle = Math.atan2(path.y, path.x) + Math.PI/2;
        }

        // get position of tower relative to map instead of air base
        Point pos = new Point(airBasePos.x + planePos.x, airBasePos.y + planePos.y);
        setTowerTopPos(pos);

        // add images to render queue
        RenderQueue.addToQueue(RUNWAY_Z, new RenderImage(airBasePos.x, airBasePos.y, RUNWAY_IMAGE, rotation));
        RenderQueue.addToQueue(PLANE_Z, new RenderImage(airBasePos.x + planePos.x, airBasePos.y + planePos.y,
                PLANE_IMAGE, new DrawOptions().setRotation(angle)));

    }

    /**
     * Calculates next position based on figure 8 path
     * @param timeScale game speed multiplier
     * @return vector determining path
     */
    private Vector2 calculatePath(float timeScale) {

        // determines x position to get y position from (like a graph)
        double x = planePos.x + direction * SPEED * timeScale;

        // keeps x within path width
        if (x > PATH_SIZE) {
            x = PATH_SIZE;
        } else if (x < -PATH_SIZE) {
            x = -PATH_SIZE;
        }
        // calculates y using equation to get a figyre 8
        double y = Math.sqrt(Math.pow(x, 2) - (Math.pow(x, 4))/(Math.pow(PATH_SIZE, 2)));

        // change quadrant and direction based on where the plane currently is
        // Quadrant 3, direction 1 (--->) | Quadrant 0, direction -1 (<---)
        // -----------------------------------------------------------------
        // Quadrant 2, direction -1 (<---) | Quadrant 1, direction 1 (--->)
        //
        if (inRange(planePos.x, PATH_SIZE - SPEED * timeScale, PATH_SIZE) && direction == 1) {
            quadrant = 0;
            direction = -1;
        } else if (inRange(planePos.x, 0, SPEED*timeScale) && direction == -1) {
            quadrant = 2;
        } else if (inRange(planePos.x, -PATH_SIZE, -PATH_SIZE + SPEED*timeScale) && direction == -1) {
            quadrant = 3;
            direction = 1;
        } else if (inRange(planePos.x, -SPEED*timeScale, 0) && direction == 1) {
            quadrant = 1;
        }

        // if plane is on top of figure 8, subtract y value
        if (quadrant == 0 || quadrant == 3) {
            y *= -1;
        }

        // return normalised path vector
        return new Vector2(x-planePos.x, y - planePos.y).normalised().mul(SPEED*timeScale);
    }

    /**
     * A more concise method of determining of a number is within a defined range, might delete later
     * @param num number to check if in range
     * @param lowerLimit lower limit of range
     * @param upperLimit upper limit of range
     * @return boolean (true if number is in range)
     */
    private boolean inRange(double num, double lowerLimit, double upperLimit) {
        return (num >= lowerLimit && num <= upperLimit);
    }

    /**
     * Determines what occurs if mouse is hovering over airbase
     * @param input user defined input
     */
    @Override
    public void hover(Input input) {
        // get position of airbase
        Point towerPos = getPosition();

        // get mouse position
        Point mousePos = new Point(input.getMouseX(), input.getMouseY());

        // if mouse is within range
        if (mousePos.distanceTo(towerPos) <= 25) {
            // if is placing, draw plane and runway
            if (isPlacing()) {
                RenderQueue.addToQueue(RUNWAY_Z, new RenderImage(towerPos.x, towerPos.y, RUNWAY_IMAGE,
                        new DrawOptions().setRotation(RUNWAY_ANGLE)));
                RenderQueue.addToQueue(PLANE_Z, new RenderImage(towerPos.x, towerPos.y, PLANE_IMAGE,
                        new DrawOptions().setRotation(RUNWAY_ANGLE)));
            }

            // if tower cannot be placed, display it as
            if (isBlocked()) {
                //RenderQueue.addToQueue(OVERLAY_Z, new RenderImage(towerPos.x, towerPos.y, RUNWAY_IMAGE,
                //        new DrawOptions().setRotation(RUNWAY_ANGLE).setBlendColour(200, 200, 200, 0.2)));
                RenderQueue.addToQueue(OVERLAY_Z, new RenderImage(
                        towerPos.x + planePos.x, towerPos.y + planePos.y, PLANE_IMAGE,
                        new DrawOptions().setRotation(angle).setBlendColour(200, 200, 200, 0.2)));
                drawPath();
            } else {
                RenderQueue.addToQueue(OVERLAY_Z, new RenderImage(towerPos.x, towerPos.y, RUNWAY_IMAGE,
                        new DrawOptions().setRotation(RUNWAY_ANGLE).setBlendColour(BLOCKED_COLOUR)));
                RenderQueue.addToQueue(OVERLAY_Z, new RenderImage(towerPos.x, towerPos.y, PLANE_IMAGE,
                        new DrawOptions().setRotation(RUNWAY_ANGLE).setBlendColour(BLOCKED_COLOUR)));
            }
        }
    }

    /**
     * Draws figure 8 path of plane
     */
    private void drawPath() {
        Point prev = new Point(-PATH_SIZE + getPosition().x, 0 + getPosition().y);
        double x;
        int i = -1;

        // draw all points of graph function, once over for negative y values and again for positive y values
        while (i <= 1) {

            // draw small lines between every pixel from left to right on path to create figure 8
            for (x = -PATH_SIZE; x <= PATH_SIZE; x++) {

                // calculates y value for every x value
                double y = Math.sqrt(Math.pow(x, 2) - (Math.pow(x, 4))/(Math.pow(PATH_SIZE, 2))) * i;

                // set current point
                Point curr = new Point(x + getPosition().x, y + getPosition().y);
                // add line to renderQueue
                RenderQueue.addToQueue(UNDERLAY_Z, new Shape("line", prev, curr, PATH_LINE_THICKNESS,
                        PATH_LINE_COLOUR));

                prev = curr;
            }
            prev = new Point(-PATH_SIZE + getPosition().x, 0 + getPosition().y);
            i += 2;
        }


    }

}
