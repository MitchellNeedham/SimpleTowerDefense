
import bagel.*;
import bagel.map.TiledMap;
import bagel.util.Colour;
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

    private final static int INITIAL_LIVES = 50; // initial lives

    // status ints
    private final static int STATE_WAITING = 0;
    private final static int STATE_IN_PROGRESS = 1;
    private final static int STATE_PLACING = 2;
    private final static int STATE_WIN = 3;

    //
    private final static String[] STATUS = new String[] {"Awaiting Start", "Wave In Progress", "Placing", "Winner!"};
    private final static int INITIAL_MONEY = 500;
    private final static int REWARD_MONEY = 150;

    private final static String MAP_PATH = "res/levels/";
    private final static String MAP_EXT = ".tmx";
    private static String MAP_FILE;

    private final SortedSet<Integer> statusSet = new TreeSet<>();
    private final int level;
    private int currentWave = 0;
    private final Stack<Wave> waves = new Stack<>();

    private boolean waveInProgress;

    private int lives;
    private int money;
    private static final String[] towerFiles = {"tank", "supertank", "airsupport", "bomber"};
    private static final int[] towerPrices = {250, 600, 500, 800};
    private static final double[] TOWER_BUTTON_POSITION = {64, 40};
    private static final double TOWER_BUTTON_OFFSET_X = 120;
    private final List<TowerButton> towerButtons = new ArrayList<>();
    private double planeOrientation = 0;
    private final List<Projectile> projectiles = new ArrayList<>();

    private final Stack<Tower> towers = new Stack<>();

    private Map<Projectile, ArrayList<Enemy>> explosiveProjectileHits = new HashMap<>();

    private Tower dragActive = null;

    private final List<Point> blockedPoints = new ArrayList<>();
    private final List<Line> blockedLines = new ArrayList<>();

    /**
     * Level constructor
     * @param level number of level
     */
    public Level(int level) {
        this.level = level;
        createWaves();
        this.waveInProgress = false;
        this.money = INITIAL_MONEY;
        this.lives = INITIAL_LIVES;


        // Create Panels
        // TODO: improve how panels are created and updated
        Panel buyPanel = ShadowDefend.getBuyPanel();
        Panel statusPanel = ShadowDefend.getStatusPanel();
        updateStatus(STATE_WAITING);

        int i;
        for (i = 0; i < towerFiles.length; i++) {
            towerButtons.add(new TowerButton(towerFiles[i], TOWER_BUTTON_POSITION[0] + TOWER_BUTTON_OFFSET_X * i,
                    TOWER_BUTTON_POSITION[1], towerPrices[i]));
            buyPanel.addClickable(towerButtons.get(i));
        }
        buyPanel.addText("money", 500, 50, "&" + money, new Font("res/fonts/DejaVuSans-Bold.ttf", 48));
        statusPanel.addText("lives", 970, 16, "Lives: " + lives, new Font("res/fonts/DejaVuSans-Bold.ttf", 16));
        statusPanel.addText("timescale", 200, 16, "Timescale: ", new Font("res/fonts/DejaVuSans-Bold.ttf", 16));
        statusPanel.addText("wave", 40, 16, "Wave: ", new Font("res/fonts/DejaVuSans-Bold.ttf", 16));
        statusPanel.addText("status", 400, 16, "Status: " + STATUS[statusSet.last()], new Font("res/fonts/DejaVuSans-Bold.ttf", 16));

    }

    /**
     * Start waves
     */

    // TODO: Consider implementing many polylines and waves for each line
    protected void start() {
        waves.get(currentWave).startWave();
        waveInProgress = true;
        updateStatus(STATE_IN_PROGRESS);
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
                    waves.add(new Wave(Integer.parseInt(waveData[0])));
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

        // update enemies
        if (waveInProgress) {
            updateEnemies(timeScale, points);
        }
        // update projectiles
        updateProjectiles(timeScale);

        // update towers
        updateTowers(input, timeScale, points);



        // update panels

        updatePanelText(timeScale);

        // create tower from buy panel
        createTower(input);


    }

    /**
     * Updates enemy positions and states
     * @param timeScale game speed multiplier
     * @param points list of polylines as defined by the map
     */
    public void updateEnemies(float timeScale, List<List<Point>> points) {
        Wave currWave = waves.get(currentWave);
        // get waves in progress and update enemies in them
        currWave.getTime().updateTime(timeScale);
        currWave.drawEnemies(timeScale, points);
        if (waves.get(currentWave).isWaveComplete()) {
            currentWave ++;
            money += REWARD_MONEY * currentWave;
            waveInProgress = false;
            removeStatus(1);
        }
        for (Enemy enemy : currWave.getEnemiesOnScreen()) {
            if (enemy.getIndex() >= points.get(0).size()) {
                enemy.destroy();
                lives -= enemy.getPenalty();
            }
        }




        // if no waves are spawning, start spawning next wave

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
                    if (!explosiveProjectileHits.containsKey(pr)) {
                        explosiveProjectileHits.put(pr, new ArrayList<>());
                    }
                    if (((ExplosiveProjectile) pr).enemyInRadius(enemy.getPosition().asVector()) &&
                            !explosiveProjectileHits.get(pr).contains(enemy)) {
                        enemy.destroyedByDamage(((ExplosiveProjectile) pr).getDamage());
                        explosiveProjectileHits.get(pr).add(enemy);
                    }
                }
                if (pr.getClass() == StandardProjectile.class) {

                    double dmg = ((StandardProjectile) pr).hasHitEnemy(enemy.getPosition());
                    if (dmg == 0) {
                        continue;
                    }
                    if (enemy.destroyedByDamage(dmg)){
                        this.money += enemy.getReward();
                        if (enemy.spawnChildren() != null) {
                            enemy.spawnChildren().forEach(e -> waves.get(currentWave).addEnemy(e));
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


        // remove tower if it is off screen and is not being placed
        towers.removeIf(tower -> !tower.isPlacing() && tower.isOffScreen());



        towers.forEach(tower -> {

            // determine target for tower
            Enemy target = null;

            for (Enemy enemy : getEnemiesOnScreen()) {

                // get distance from enemy to tower
                double distance = enemy.getPosition().distanceTo(tower.getTowerTopPosition());

                // consider it a potential target if within range
                if (distance < tower.getRange() && enemy.getIndex() != points.get(0).size()) {

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
                                money -= tb.getPrice();
                                tb.increasePrice();
                            }
                            case "supertank" -> {
                                Tower newTower = new SuperTank(input.getMouseX(), input.getMouseY());
                                towers.add(newTower);
                                dragActive = newTower;
                                money -= tb.getPrice();
                                tb.increasePrice();
                            }
                            case "airsupport" -> {
                                Tower newTower = new AirSupport(input.getMouseX(), input.getMouseY(), planeOrientation);
                                towers.add(newTower);
                                dragActive = newTower;
                                planeOrientation = (planeOrientation + Math.PI/2) % (Math.PI);
                                money -= tb.getPrice();
                                tb.increasePrice();
                            }
                            case "bomber" -> {
                                Tower newTower = new Bomber(input.getMouseX(), input.getMouseY());
                                towers.add(newTower);
                                dragActive = newTower;
                                planeOrientation = (planeOrientation + Math.PI/2) % (Math.PI);
                                money -= tb.getPrice();
                                tb.increasePrice();
                            }
                        }
                    }
                }
            }
        }


        else if (dragActive != null) {
            updateStatus(STATE_PLACING);
            // if not over blocked positions, place tower
            if (dragActive.canBePlaced(blockedPoints, blockedLines)) {
                if (input.wasPressed(MouseButtons.LEFT)) {
                    dragActive.place(input.getMouseX(), input.getMouseY());

                    // add tower position to blocked points
                    blockedPoints.add(dragActive.getPosition());
                    dragActive = null;
                }
            }
        } else {
            removeStatus(STATE_PLACING);
        }
    }

    /**
     * Gets all enemies on screen
     * @return List of all enemies on screen
     */
    private List<Enemy> getEnemiesOnScreen() {

        return new ArrayList<>(waves.get(currentWave).getEnemiesOnScreen());
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

    private void updatePanelText(float timeScale) {
        Panel buyPanel = ShadowDefend.getBuyPanel();
        Panel statusPanel = ShadowDefend.getStatusPanel();
        buyPanel.updateText("money", "$" + money);
        for (TowerButton tb : towerButtons) {
            tb.setPurchasable(money);
        }

        if (timeScale > 1) {
            statusPanel.updateTextColour("timescale", Colour.GREEN);
        } else if (timeScale == 1) {
            statusPanel.updateTextColour("timescale", Colour.WHITE);
        }

        statusPanel.updateText("timescale", "Timescale " + timeScale);
        statusPanel.updateText("lives", "Lives: " + lives);
        statusPanel.updateText("wave", "Wave: " + (currentWave + 1));
        statusPanel.updateText("status", "Status: " + STATUS[statusSet.last()] );

    }

    /**
     * return boolean value for levelComplete
     * @return boolean if level is complete
     */
    public boolean isLevelComplete() {
        if (waves.stream().allMatch(wave -> Boolean.TRUE.equals(wave.isWaveComplete()))) {
            updateStatus(STATE_WIN);
            return true;
        } else {
            removeStatus(STATE_WIN);
        }
        return false;
    }


    public boolean isWaveInProgress() {
        System.out.println(waveInProgress);
        if (!waveInProgress) {
            System.out.println(2);
        }
        return waveInProgress;
    }

    public void updateStatus(int status) {

        statusSet.add(status);
        System.out.println(statusSet.last());

    }

    public void removeStatus(int status) {
        statusSet.remove(status);
    }

    public int getLevel() {
        return level;
    }
}
