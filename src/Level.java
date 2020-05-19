
import bagel.*;
import bagel.map.TiledMap;
import bagel.util.Point;
import bagel.util.Vector2;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;


public class Level {


    private final static String TEXT_GREEN = "#00FF00";
    private final static String TEXT_RED = "#FF0000";
    private final static String MAP_PATH = "res/levels/";
    private final static String MAP_EXT = ".tmx";
    private static String MAP_FILE;

    private final int level;
    private int currentWave = 0;
    private final Stack<Wave> waves = new Stack<>();

    private boolean inProgress;

    private static final String BUY_PANEL_IMAGE = "res/images/buypanel.png";
    private static final String STATUS_PANEL_IMAGE = "res/images/statuspanel.png";
    private static final int SLICER_CHILDREN = 2;
    private static final int SLICER_CHILDREN_SPAWN_DIST = 10;
    private final Panel buyPanel;
    private final Panel statusPanel;

    private int money;
    private static final String[] towerFiles = {"tank", "supertank", "airsupport"};
    private static final int[] towerPrices = {250, 600, 500};
    private static final double[] TOWER_BUTTON_POSITION = {64, 40};
    private static final double TOWER_BUTTON_OFFSET_X = 120;
    private final List<TowerButton> towerButtons = new ArrayList<>();
    private double planeOrientation = 0;
    private final List<Projectile> projectiles = new ArrayList<>();

    private final Stack<Tower> towers = new Stack<>();

    private Tower dragActive = null;

    private List<Point> blockedPoints = new ArrayList<>();
    private List<Line> blockedLines = new ArrayList<>();

    /**
     * Level constructor
     * @param level number of level
     */
    public Level(int level) {
        this.level = level;
        createWaves();
        this.inProgress = false;
        this.money = 500;

        // Create Panels
        // TODO: improve how panels are created and updated
        buyPanel = new Panel(0,0, BUY_PANEL_IMAGE);
        statusPanel = new Panel(0, 743, STATUS_PANEL_IMAGE);

        int i;
        for (i = 0; i < towerFiles.length; i++) {
            towerButtons.add(new TowerButton(towerFiles[i], TOWER_BUTTON_POSITION[0] + TOWER_BUTTON_OFFSET_X * i,
                    TOWER_BUTTON_POSITION[1], towerPrices[i]));
            buyPanel.addClickable(towerButtons.get(i));
        }
        buyPanel.addText("money", 500, 50, "&" + money, new Font("res/fonts/DejaVuSans-Bold.ttf", 48));
    }

    /**
     * Start waves
     */

    // TODO: Consider implementing many polylines and waves for each line
    protected void start() {
        waves.firstElement().startWave();
        inProgress = true;
    }

    /**
     * Read waves.txt and add enemies to waves
     */
    private void createWaves() {
        String filepath = "res/levels/" + level + "/waves.txt";

        try {
            File fp = new File(filepath);
            Scanner myReader = new Scanner(fp);
            while (myReader.hasNextLine()) {
                String[] waveData = myReader.nextLine().split(",");

                if (Integer.parseInt(waveData[0]) > waves.size() && waveData.length == 5) {
                    waves.add(new Wave(level, Integer.parseInt(waveData[0])));
                }
                if (waveData[1].equals("spawn")) {

                    waves.lastElement().addEnemies(waveData);
                } else if (waveData[1].equals("delay")) {
                    waves.lastElement().addDelay(Integer.parseInt(waveData[2]));
                }
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    /**
     * Update level and all components within
     * @param input user-defined input
     * @param timeScale game speed multiplier
     * @param points polylines defined by map
     */
    public void update(Input input, float timeScale, List<List<Point>> points) {


        // update projectiles
        updateProjectiles(timeScale);

        // update towers
        updateTowers(input, timeScale, points);

        // update enemies
        if (inProgress) {
            updateEnemies(timeScale, points);
        }

        // update panels
        updateBuyPanelText();
        buyPanel.update();
        statusPanel.update();

        // create tower from buy panel
        createTower(input);
    }

    /**
     * Updates enemy positions and states
     * @param timeScale game speed multiplier
     * @param points list of polylines as defined by the map
     */
    public void updateEnemies(float timeScale, List<List<Point>> points) {
        // get waves in progress and update enemies in them
        waves.stream().filter(Wave::isInProgress).forEach(w -> {
            w.getTime().updateTime(timeScale);
            w.drawEnemies(timeScale, points);
        });

        // if no waves are spawning, start spawning next wave
        if (waves.stream().noneMatch(Wave::isSpawning) && currentWave != waves.size() - 1) {
            currentWave++;
            waves.get(currentWave).startWave();
        }
    }

    /**
     * Update projectile positions and collisions
     * @param timeScale game speed multiplier
     */
    private void updateProjectiles(float timeScale) {

        // iterate through projectiles and destroy any off screen or that have collided
        Iterator<Projectile> p = projectiles.iterator();
        while (p.hasNext()) {
            Projectile pr = p.next();

            // cleans up projectiles without targets
            if (pr == null) {
                p.remove();
                continue;
            }

            // if projectile is explosive, destroy enemies within area of effect

            for (Enemy enemy : getEnemiesOnScreen()) {
                if (pr.getClass() == ExplosiveProjectile.class) {
                    if (((ExplosiveProjectile) pr).enemyInRadius(enemy.getPosition().asVector())) {
                        enemy.destroy();
                    }
                }
                if (pr.getClass() == StandardProjectile.class) {

                    double dmg = ((StandardProjectile) pr).hasHitEnemy(enemy.getPosition());
                    if (dmg == 0) {
                        continue;
                    }
                    if (enemy.destroyedByDamage(dmg)){
                        this.money += enemy.getReward();
                        if (enemy.getType().endsWith("slicer")) {
                            int index = ((Slicer) enemy).getTypes().indexOf(enemy.getType()) - 1;
                            if (index < 0) {
                                continue;
                            }
                            Point newPos = enemy.getPosition();
                            int i;
                            for (i = 0; i < SLICER_CHILDREN; i++) {
                                waves.get(currentWave).addEnemy(
                                        new Slicer(newPos, ((Slicer) enemy).getTypes().get(index), enemy.getIndex()));
                                newPos = newPos.asVector().add(enemy.getMoveVector().mul(SLICER_CHILDREN_SPAWN_DIST / (timeScale * SLICER_CHILDREN))).asPoint();
                            }
                        }
                    }
                }
            }
            // if off screen or destroyed, remove from array for garbage collector
            if (pr.isDestroyed() || pr.isOffScreen()) {
                p.remove();
            } else {
                pr.update(timeScale);
            }
        }

    }

    /**
     * Updates tower positions and launches projectiles
     * @param input user define input
     * @param timeScale game speed multiplier
     */
    private void updateTowers(Input input, float timeScale, List<List<Point>> points) {
        // remove tower if it is off screen
        towers.removeIf(Tower::isOffScreen);



        towers.forEach(tower -> {

            if (tower instanceof Clickable) {
                ((Clickable) tower).hover(input);
            }

            // determine target for tower
            Enemy target = null;

            for (Enemy enemy : getEnemiesOnScreen()) {

                // get distance from enemy to tower
                double distance = enemy.getPosition().distanceTo(tower.getPosition());

                // consider it a potential target if within range
                if (distance < tower.getRange()) {

                    // if no target set, set this one
                    if (target == null) {
                        target = enemy;

                    // else if enemy is further in map, set as new target
                    } else if (enemy.getIndex() > target.getIndex() || enemy.getIndex() >= target.getIndex() &&
                            enemy.getPosition().distanceTo(points.get(0).get(enemy.getIndex()))
                                    < target.getPosition().distanceTo(points.get(0).get(enemy.getIndex()))) {
                        target = enemy;
                    }
                }
            }
            if (target != null) {
                // direction vector from tower to enemy
                Vector2 dirVector = target.getPosition().asVector().sub(tower.getPosition().asVector());

                // update tower rotation to face enemy
                tower.updateRotation(Math.atan2(dirVector.y, dirVector.x) + Math.PI/2);
            }

            // if not placing and is reloaded and has a target, shoot it
            if (!tower.isPlacing()) {
                if (tower.isReloaded()) {
                    projectiles.add(tower.fire(target));
                }
            }

            // update tower
            tower.update(input, timeScale);

        });
    }

    /**
     * Creates tower from buy panel
     * @param input user defined input
     */
    private void createTower(Input input) {

        // if left button used and no tower is currently being placed
        if (input.isDown(MouseButtons.LEFT) && dragActive == null) {

            // get tower determined by tower button pressed
            for (TowerButton tb : towerButtons) {

                // determine if mouse is over button
                if (tb.getBoundingBox().isMouseOver(input)) {

                    // create new tower centred at mouse location
                    if (tb.isPurchasable()) {
                        switch (tb.getTowerType()) {
                            case "tank" -> {
                                Tower newTower = new Tank(input.getMouseX(), input.getMouseY());
                                towers.add(newTower);
                                dragActive = newTower;
                                money -= newTower.getCost();
                            }
                            case "supertank" -> {
                                Tower newTower = new SuperTank(input.getMouseX(), input.getMouseY());
                                towers.add(newTower);
                                dragActive = newTower;
                                money -= newTower.getCost();
                            }
                            case "airsupport" -> {
                                Tower newTower = new AirSupport(input.getMouseX(), input.getMouseY(), planeOrientation);
                                towers.add(newTower);
                                dragActive = newTower;
                                planeOrientation = (planeOrientation + Math.PI/2) % (Math.PI);
                                money -= newTower.getCost();
                            }
                        }
                    }
                }
            }
        }


        else if (dragActive != null) {

            // if not over blocked positions, place tower
            if (!dragActive.isBlocked(blockedPoints, blockedLines)) {
                if (input.wasPressed(MouseButtons.LEFT)) {
                    dragActive.place(input.getMouseX(), input.getMouseY());

                    // add tower position to blocked points
                    blockedPoints.add(dragActive.getPosition());
                    dragActive = null;
                }
            }

            // if escape key is pressed, cancel placing tower and refund money lost
            if (input.wasPressed(Keys.ESCAPE)) {
                money += dragActive.getCost();
                dragActive = null;
                towers.pop();
            }
        }
    }

    // update test in buy panel
    private void updateBuyPanelText() {
        buyPanel.updateText("money", "$" + money);
        for (TowerButton tb : towerButtons) {
            tb.setPurchasable(money);
        }
    }

    /**
     * Gets all enemies on screen
     * @return List of all enemies on screen
     */
    private List<Enemy> getEnemiesOnScreen() {
        List<Enemy> enemiesOnScreen = new ArrayList<>();
        for (Wave wave : waves) enemiesOnScreen.addAll(wave.getEnemiesOnScreen());
        return enemiesOnScreen;
    }


    /**
     * creates map for level
     * @return TiledMap object with map file loaded
     */
    public TiledMap createMap() {
        //check level folder for a ".tmx" file and use this as the map
        try (Stream<Path> paths = Files.walk(Paths.get(MAP_PATH + level))) {
            paths
                    .forEach( p -> {
                        //check if file ends with ".tmx"
                        if (p.toString().endsWith(MAP_EXT)) {
                            MAP_FILE = p.toString();
                        }

                    });

        } catch (IOException e) {
            //if no map file found, exit game
            e.printStackTrace();
            Window.close();
            System.exit(-1);
        }

        // return TiledMap object with new map
        TiledMap tm = new TiledMap(MAP_FILE);

        tm.getAllPolylines().forEach(l -> {
            int i;
            for (i = 0; i < l.size() - 1; i++) {
                blockedPoints.add(l.get(i));
                blockedLines.add(new Line(l.get(i), l.get(i+1)));
            }
        });

        return tm;
    }


    /**
     * return boolean value for levelComplete
     * @return boolean if level is complete
     */
    public boolean isLevelComplete() {
        return waves.stream().allMatch(wave -> Boolean.TRUE.equals(wave.isWaveComplete()));
    }

    public boolean isInProgress() {
        return inProgress;
    }

}
