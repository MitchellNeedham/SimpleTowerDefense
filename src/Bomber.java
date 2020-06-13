import bagel.*;
import bagel.util.Colour;
import bagel.util.Point;
import bagel.util.Vector2;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class Bomber extends ActiveTower {

    // TODO: change name - it is a fighter plane silly

    //-------------------------RENDER PRIORITIES-------------------------//

    private static final int RUNWAY_Z = 2;
    private static final int PLANE_Z = 8;
    private final static int OVERLAY_Z = 9;
    private final static int UNDERLAY_Z = 3;


    //-------------------------INFO DISPLAY PROPERTIES-------------------------//

    private final static int PATH_LINE_THICKNESS = 5;
    private final static Colour PATH_LINE_COLOUR = new Colour(0, 0, 200, 0.3);
    private static final Colour BLOCKED_COLOUR = new Colour(255, 0, 0, 0.5);


    //-------------------------TOWER IMAGE FILES-------------------------//

    private String runwayImage = "res/images/bomber/runway.png";
    private String planeImage = "res/images/bomber/main.png";
    private String projectileImage = "res/images/bomber/projectile.png";


    //-------------------------TOWER PROPERTIES-------------------------//

    private static final String TYPE = "bomber";
    private double projectileSpeed = 6;
    private double projectileDamage = 2;
    private double range = 500;
    private double fireRate = 1500;
    private double cost = 800;
    private double pathSize = 300;
    private double speed = 1.5;


    //------------------------------------------------------------//

    private final DrawOptions rotation;
    private Point planePos = new Point(0, 0);
    private Vector2 path;
    private static final double RUNWAY_ANGLE = Math.PI * 3 / 4;
    private double angle;
    private int quadrant = 0;
    private int direction = 1;
    private Point airBasePos;

    private int attackPattern;

    private double explosionRadius;
    private double explosionDamage;

    private static final double MAX_GUN_WIDTH = 50;
    private static final double GUN_SPACING = 10;


    /**
     * Constructor for Bomber
     * @param x x-position of tower
     * @param y y-position of tower
     */
    public Bomber(double x, double y) {
        super(x, y, TYPE, RUNWAY_ANGLE);
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
        RenderQueue.addToQueue(RUNWAY_Z, new RenderImage(airBasePos.x, airBasePos.y, runwayImage, rotation));
        RenderQueue.addToQueue(PLANE_Z, new RenderImage(airBasePos.x + planePos.x, airBasePos.y + planePos.y,
                planeImage, new DrawOptions().setRotation(angle)));
    }

    /**
     * Calculates next position based on figure 8 path
     * @param timeScale game speed multiplier
     * @return vector determining path
     */
    private Vector2 calculatePath(float timeScale) {

        // determines x position to get y position from (like a graph)
        double x = planePos.x + direction * speed * timeScale;

        // keeps x within path width
        if (x > pathSize) {
            x = pathSize;
        } else if (x < -pathSize) {
            x = -pathSize;
        }
        // calculates y using equation to get a figyre 8
        double y = Math.sqrt(Math.pow(x, 2) - (Math.pow(x, 4))/(Math.pow(pathSize, 2)));

        // change quadrant and direction based on where the plane currently is
        // Quadrant 3, direction 1 (--->) | Quadrant 0, direction -1 (<---)
        // -----------------------------------------------------------------
        // Quadrant 2, direction -1 (<---) | Quadrant 1, direction 1 (--->)
        //
        if (inRange(planePos.x, pathSize - speed * timeScale, pathSize) && direction == 1) {
            quadrant = 0;
            direction = -1;
        } else if (inRange(planePos.x, 0, speed*timeScale) && direction == -1) {
            quadrant = 2;
        } else if (inRange(planePos.x, -pathSize, -pathSize + speed*timeScale) && direction == -1) {
            quadrant = 3;
            direction = 1;
        } else if (inRange(planePos.x, -speed*timeScale, 0) && direction == 1) {
            quadrant = 1;
        }

        // if plane is on top of figure 8, subtract y value
        if (quadrant == 0 || quadrant == 3) {
            y *= -1;
        }

        // return normalised path vector
        return new Vector2(x-planePos.x, y - planePos.y).normalised().mul(speed*timeScale);
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
                RenderQueue.addToQueue(RUNWAY_Z, new RenderImage(towerPos.x, towerPos.y, runwayImage,
                        new DrawOptions().setRotation(RUNWAY_ANGLE)));
                RenderQueue.addToQueue(PLANE_Z, new RenderImage(towerPos.x, towerPos.y, planeImage,
                        new DrawOptions().setRotation(RUNWAY_ANGLE)));
            }

            // if tower cannot be placed, display it as
            if (isBlocked()) {
                //RenderQueue.addToQueue(OVERLAY_Z, new RenderImage(towerPos.x, towerPos.y, RUNWAY_IMAGE,
                //        new DrawOptions().setRotation(RUNWAY_ANGLE).setBlendColour(200, 200, 200, 0.2)));
                RenderQueue.addToQueue(OVERLAY_Z, new RenderImage(
                        towerPos.x + planePos.x, towerPos.y + planePos.y, planeImage,
                        new DrawOptions().setRotation(angle).setBlendColour(200, 200, 200, 0.2)));
                drawPath();
            } else {
                RenderQueue.addToQueue(OVERLAY_Z, new RenderImage(towerPos.x, towerPos.y, runwayImage,
                        new DrawOptions().setRotation(RUNWAY_ANGLE).setBlendColour(BLOCKED_COLOUR)));
                RenderQueue.addToQueue(OVERLAY_Z, new RenderImage(towerPos.x, towerPos.y, planeImage,
                        new DrawOptions().setRotation(RUNWAY_ANGLE).setBlendColour(BLOCKED_COLOUR)));
            }
        }
    }

    /**
     * Draws figure 8 path of plane
     */
    private void drawPath() {
        Point prev = new Point(-pathSize + getPosition().x, 0 + getPosition().y);
        double x;
        int i = -1;

        // draw all points of graph function, once over for negative y values and again for positive y values
        while (i <= 1) {

            // draw small lines between every pixel from left to right on path to create figure 8
            for (x = -pathSize; x <= pathSize; x++) {

                // calculates y value for every x value
                double y = Math.sqrt(Math.pow(x, 2) - (Math.pow(x, 4))/(Math.pow(pathSize, 2))) * i;

                // set current point
                Point curr = new Point(x + getPosition().x, y + getPosition().y);
                // add line to renderQueue
                RenderQueue.addToQueue(UNDERLAY_Z, new Shape("line", prev, curr, PATH_LINE_THICKNESS,
                        PATH_LINE_COLOUR));

                prev = curr;
            }
            prev = new Point(-pathSize + getPosition().x, 0 + getPosition().y);
            i += 2;
        }
    }

    @Override
    public void upgrade(int stream0, int stream1) {
        try {
            File fp = new File("res/upgrades/" + TYPE + "/" + stream0 + "-" + stream1 + ".txt");
            Scanner myReader = new Scanner(fp);
            // read file
            while (myReader.hasNextLine()) {
                String[] upgradeData = myReader.nextLine().split(",");
                switch (upgradeData[0]) {
                    case "top_image":
                        planeImage = upgradeData[1];
                        break;
                    case "bottom_image":
                        runwayImage = upgradeData[1];
                        break;
                    case "projectile_image":
                        projectileImage = upgradeData[1];
                        break;
                    case "range":
                        range = Double.parseDouble(upgradeData[1]);
                        break;
                    case "cost":
                        cost = Double.parseDouble(upgradeData[1]);
                        break;
                    case "fire_rate":
                        setFireRate(Double.parseDouble(upgradeData[1]));
                        break;
                    case "damage":
                        projectileDamage = Double.parseDouble(upgradeData[1]);
                        break;
                    case "attack_pattern":
                        attackPattern = Integer.parseInt(upgradeData[1]);
                        break;
                    case "explosion_radius":
                        explosionRadius = Double.parseDouble(upgradeData[1]);
                        break;
                    case "explosion_damage":
                        explosionDamage = Double.parseDouble(upgradeData[1]);
                        break;
                    case "path_size":
                        pathSize = Double.parseDouble(upgradeData[1]);
                        break;
                    case "speed":
                        speed = Double.parseDouble(upgradeData[1]);
                        break;
                    case "projectile_speed":
                        projectileSpeed = Double.parseDouble(upgradeData[1]);
                        break;
                }
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        setStreams(stream0, stream1);
        updateButtons();
    }

    @Override
    public List<Projectile> fire(Enemy target) {
        ArrayList<Projectile> projectilesFired = new ArrayList<>();
        resetTime();
        double standardCount = 0;
        double trackingCount = 0;
        double directCount = 0;
        double random = 0;

        switch (attackPattern) {
            case 0:
                directCount = 2;
                break;
            case 1:
                standardCount = 8;
                random = 0.2;
                break;
            case 2:
                standardCount = 24;
                random = 0.4;
                break;
            case 3:
                trackingCount = 8;
                break;
            case 4:
                trackingCount = 16;
                break;
            case 5:
                directCount = 4;
                break;
            case 6:
                directCount = 2;
                standardCount = 4;
                break;

        }

        for (int i = 0; i < directCount; i++) {
            Vector2 firePos = getTowerTopPosition().asVector().add(new Vector2(path.y, -path.x).normalised().mul(MAX_GUN_WIDTH/2 - MAX_GUN_WIDTH*i/(directCount-1)));
            double v = -GUN_SPACING * Math.abs(i - (directCount - 1) / 2);
            Vector2 spacing = path.normalised().mul(v);

            if (directCount > 2 && target != null) {
                projectilesFired.add(new StandardProjectile(projectileImage, firePos.add(spacing).asPoint(), projectileSpeed, projectileDamage, target, path));
            } else {
                projectilesFired.add(new StandardProjectile(projectileImage, firePos.add(spacing).asPoint(), projectileSpeed, projectileDamage, path));
            }

        }

        for (int i = 0; i < standardCount; i++) {
            Vector2 projectilePath = new Vector2(Math.sin(angle + Math.PI/(standardCount/2) * i), Math.cos(angle + Math.PI/(standardCount/2) * i)).normalised();
            projectilesFired.add(new StandardProjectile(projectileImage, getTowerTopPosition(), projectileSpeed, projectileDamage, projectilePath));
        }

        for (int i = 0; i < trackingCount; i++) {
            Vector2 projectilePath = new Vector2(Math.sin(angle + Math.PI/(trackingCount/2) * i), Math.cos(angle + Math.PI/(trackingCount/2) * i)).normalised();
            projectilesFired.add(new StandardProjectile(projectileImage, getTowerTopPosition(), projectileSpeed, projectileDamage, target, projectilePath));        }

        if (new Random().nextDouble() < random) {
            projectilesFired.add(new ExplosiveProjectile(getTowerTopPosition(),explosionRadius, 2000, explosionDamage));
        }
        return projectilesFired;
    }
    @Override
    public double getRange() { return range; }

}
