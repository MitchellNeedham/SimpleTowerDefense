
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



    //-------------------------GAME STATES-------------------------//

    private final static int STATE_WAITING = 0;
    private final static int STATE_IN_PROGRESS = 1;
    private final static int STATE_PLACING = 2;
    private final static int STATE_WIN = 3;


    //-------------------------INITIAL LEVEL PROPERTIES-------------------------//

    private final static int INITIAL_LIVES = 50;
    private final static int INITIAL_MONEY = 500;
    private final static int REWARD_MONEY = 150;

    private final static String MAP_PATH = "res/levels/";
    private final static String MAP_EXT = ".tmx";
    private static String MAP_FILE;

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
    private List<Projectile> projectiles = new ArrayList<>();

    private final Stack<Tower> towers = new Stack<>();

    private final Map<Projectile, ArrayList<Enemy>> explosiveProjectileHits = new HashMap<>();

    private Tower dragActive = null;

    private final List<Point> blockedPoints = new ArrayList<>();
    private final List<Line> blockedLines = new ArrayList<>();

    private TiledMap map;

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


        Panel buyPanel = ShadowDefend.getBuyPanel();
        Panel statusPanel = ShadowDefend.getStatusPanel();
        ShadowDefend.updateStatus(STATE_WAITING);

        for (int i = 0; i < towerFiles.length; i++) {
            towerButtons.add(new TowerButton(towerFiles[i], TOWER_BUTTON_POSITION[0] + TOWER_BUTTON_OFFSET_X * i,
                    TOWER_BUTTON_POSITION[1], towerPrices[i]));
            buyPanel.addClickable(towerButtons.get(i));
        }
        statusPanel.updateText(ShadowDefend.SP_TIMESCALE, ShadowDefend.SP_TIMESCALE + ShadowDefend.getTimeScale());
        statusPanel.updateText(ShadowDefend.SP_LIVES, ShadowDefend.SP_LIVES + lives);
        statusPanel.updateText(ShadowDefend.SP_WAVE, ShadowDefend.SP_WAVE + currentWave);
    }

    /**
     * Start waves
     */

    // TODO: Consider implementing many polylines and waves for each line
    protected void start() {
        waves.get(currentWave).startWave();
        waveInProgress = true;
        ShadowDefend.updateStatus(STATE_IN_PROGRESS);
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
            // update projectiles
            updateProjectiles(timeScale);
        } else {
            projectiles = new ArrayList<>();
        }


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
            ShadowDefend.removeStatus(1);
        }
        for (Enemy enemy : currWave.getEnemiesOnScreen()) {
            if (enemy.getIndex() >= points.get(0).size()) {
                enemy.destroy();
                lives -= enemy.getPenalty();
                if (lives < 0) {
                    ShadowDefend.exitGame();
                }
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
                if (pr instanceof ExplosiveProjectile) {
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
        towers.removeIf(tower -> (!tower.isPlacing() && tower.isOffScreen()));





        towers.forEach(tower -> {

            if (tower instanceof ActiveTower) {
                money = ((ActiveTower)tower).updateUpgradeButtons(input, money);
            }


            // determine target for tower
            Enemy target = null;

            for (Enemy enemy : getEnemiesOnScreen()) {

                // get distance from enemy to tower
                double distance = enemy.getPosition().distanceTo(tower.getTowerTopPosition());
                System.out.printf("range: %f", tower.getRange());
                // consider it a potential target if within range
                if (distance < tower.getRange() && enemy.getIndex() != points.get(0).size()) {

                    // if no target set, set this one
                    if (target == null) {
                        target = enemy;

                    // else if enemy is further in map, set as new target
                    } else if (enemy.getIndex() > target.getIndex() ||
                            enemy.getIndex() >= target.getIndex() &&
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
            if (!tower.isPlacing() && tower.isReloaded() && waveInProgress) {
                projectiles.addAll(tower.fire(target));
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
                            }
                            case "supertank" -> {
                                Tower newTower = new SuperTank(input.getMouseX(), input.getMouseY());
                                towers.add(newTower);
                                dragActive = newTower;
                                money -= tb.getPrice();
                            }
                            case "airsupport" -> {
                                Tower newTower = new AirSupport(input.getMouseX(), input.getMouseY(), planeOrientation);
                                towers.add(newTower);
                                dragActive = newTower;
                                planeOrientation = (planeOrientation + Math.PI/2) % (Math.PI);
                                money -= tb.getPrice();
                            }
                            case "bomber" -> {
                                Tower newTower = new Bomber(input.getMouseX(), input.getMouseY());
                                towers.add(newTower);
                                dragActive = newTower;
                                planeOrientation = (planeOrientation + Math.PI/2) % (Math.PI);
                                money -= tb.getPrice();
                            }
                        }
                    }
                }
            }
        }


        else if (dragActive != null) {
            ShadowDefend.updateStatus(STATE_PLACING);
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
            ShadowDefend.removeStatus(STATE_PLACING);
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
        map = new TiledMap(MAP_FILE);

        map.getAllPolylines().forEach(l -> {
            int i;
            for (i = 0; i < l.size() - 1; i++) {
                blockedLines.add(new Line(l.get(i), l.get(i+1)));
            }
        });

        return map;
    }

    private void updatePanelText(float timeScale) {
        Panel buyPanel = ShadowDefend.getBuyPanel();
        Panel statusPanel = ShadowDefend.getStatusPanel();
        buyPanel.updateText(ShadowDefend.MONEY, ShadowDefend.MONEY + money);
        for (TowerButton tb : towerButtons) {
            tb.setPurchasable(money);
        }

        if (timeScale > 1) {
            statusPanel.updateTextColour(ShadowDefend.SP_TIMESCALE, Colour.GREEN);
        } else if (timeScale == 1) {
            statusPanel.updateTextColour(ShadowDefend.SP_TIMESCALE, Colour.WHITE);
        }

        statusPanel.updateText(ShadowDefend.SP_TIMESCALE, ShadowDefend.SP_TIMESCALE + timeScale);
        statusPanel.updateText(ShadowDefend.SP_LIVES, ShadowDefend.SP_LIVES + lives);
        statusPanel.updateText(ShadowDefend.SP_WAVE, ShadowDefend.SP_WAVE + (currentWave + 1));

    }

    /**
     * return boolean value for levelComplete
     * @return boolean if level is complete
     */
    public boolean isLevelComplete() {
        if (waves.stream().allMatch(wave -> Boolean.TRUE.equals(wave.isWaveComplete()))) {
            ShadowDefend.updateStatus(STATE_WIN);
            ShadowDefend.getBuyPanel().resetButtons();
            return true;
        }
        return false;
    }


    public boolean isWaveInProgress() {
        if (!waveInProgress) {
        }
        return waveInProgress;
    }

    public int getLevel() {
        return level;
    }
}
