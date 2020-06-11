import bagel.Input;
import bagel.MouseButtons;
import bagel.map.TiledMap;
import bagel.util.Colour;
import bagel.util.Point;
import bagel.util.Vector2;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class Level {

    //-------------------------GAME STATES-------------------------//

    private final static int STATE_WAITING = 0;
    private final static int STATE_IN_PROGRESS = 1;
    private final static int STATE_PLACING = 2;
    private final static int STATE_WIN = 3;


    //-------------------------INITIAL LEVEL PROPERTIES-------------------------//

    private final static int INITIAL_SP_LIVES = 25;
    private final static int INITIAL_MONEY = 500;
    private final static int BASE_REWARD = 150;
    private final static int INCREMENTAL_REWARD = 100;


    //-------------------------TOWER BUTTON DATA-------------------------//

    private static final String[] TOWER_TYPES = {Tank.TYPE, SuperTank.TYPE, AirSupport.TYPE};
    private static final int[] TOWER_PRICES = {Tank.COST, SuperTank.COST, AirSupport.COST};
    private static final Point TOWER_BUTTON_POSITION = new Point(64, 40);
    private static final double TOWER_BUTTON_OFFSET_X = 120;


    //-------------------------MAP FILE-------------------------//

    private final static String MAP_PATH = "res/levels/";
    private final static String MAP_EXT = ".tmx";
    private final static String BLOCKED_PROPERTY = "blocked";


    //-------------------------WAVE FILE-------------------------//

    private final static String WAVES_FILE = "res/levels/waves.txt";
    private final static String SPAWN_EVENT = "spawn";
    private final static String DELAY_EVENT = "delay";


    //-------------------------LEVEL STORAGE-------------------------//

    private final Stack<Wave> waves = new Stack<>();
    private final List<TowerButton> towerButtons = new ArrayList<>();
    private final List<Projectile> projectiles = new ArrayList<>();
    private final Stack<Tower> towers = new Stack<>();
    // due to how I implemented the bomb (growing radius explosion) I need to store which enemies have been hit
    private final Map<Projectile, ArrayList<Enemy>> explosiveProjectileHits = new HashMap<>();
    private final List<Point> blockedPoints = new ArrayList<>(); // positions of towers


    //-------------------------LEVEL DATA-------------------------//

    private TiledMap map;
    private final int level;
    private int currentWave = 0;
    private boolean waveInProgress = false;
    private int lives = INITIAL_SP_LIVES;
    private int money = INITIAL_MONEY;
    private double planeOrientation = 0;
    private Tower dragActive = null; // tower that is currently being dragged after purchase


    /**
     * Level constructor
     * @param level number of level
     */
    public Level(int level) {
        this.level = level;
        createWaves(); // read waves file and initialise waves

        // get panels
        Panel buyPanel = ShadowDefend.getBuyPanel();
        Panel statusPanel = ShadowDefend.getStatusPanel();
        ShadowDefend.updateStatus(STATE_WAITING);

        // create tower buttons and add them to the buy panel
        for (int i = 0; i < TOWER_TYPES.length; i++) {
            towerButtons.add(new TowerButton(TOWER_TYPES[i], TOWER_BUTTON_POSITION.x + TOWER_BUTTON_OFFSET_X * i,
                    TOWER_BUTTON_POSITION.y, TOWER_PRICES[i]));
            buyPanel.addButton(towerButtons.get(i));
        }

        // update text on status panel
        statusPanel.updateText(ShadowDefend.SP_TIMESCALE, ShadowDefend.SP_TIMESCALE +
                ShadowDefend.getTimescale());
        statusPanel.updateText(ShadowDefend.SP_LIVES, ShadowDefend.SP_LIVES + lives);
        statusPanel.updateText(ShadowDefend.SP_WAVE, ShadowDefend.SP_WAVE + currentWave);
    }

    /**
     * Start next wave
     */
    protected void start() {
        if (currentWave < waves.size()) {
            waves.get(currentWave).startWave();
            waveInProgress = true;
            ShadowDefend.updateStatus(STATE_IN_PROGRESS);
        }
    }

    /**
     * Read waves.txt and add enemies to waves
     */
    private void createWaves() {
        try {
            File fp = new File(WAVES_FILE);
            Scanner myReader = new Scanner(fp);

            // read file
            while (myReader.hasNextLine()) {
                // split lines by the commas to get the wave data in a string array
                String[] waveData = myReader.nextLine().split(",");

                // creates new wave
                if (Integer.parseInt(waveData[0]) > waves.size() && waveData.length == 5) {
                    waves.add(new Wave());
                }

                // adds enemies
                if (waveData[1].equals(SPAWN_EVENT)) {
                    waves.lastElement().addEnemies(waveData);

                } else if (waveData[1].equals(DELAY_EVENT)) {
                    // adds a delay event
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
     */
    public void update(Input input) {

        // update towers
        updateTowers(input);

        // if wave has completed, start a new one and increase player's money
        if (currentWave < waves.size()) {
            if (waves.get(currentWave).isWaveComplete()) {
                currentWave++;
                money += BASE_REWARD + (currentWave - 1) * INCREMENTAL_REWARD;
                waveInProgress = false;
            }
        }


        // update enemies
        if (waveInProgress) {
            updateEnemies();
        }
        // update projectiles
        updateProjectiles();

        // update towers
        updateTowers(input);

        // update panels
        updatePanelText();

        // create tower from buy panel
        createTower(input);
    }

    /**
     * Updates enemy positions and states
     */
    public void updateEnemies() {
        Wave currWave = waves.get(currentWave); // get current wave
        currWave.getTimer().updateTime(); // update time
        currWave.drawEnemies(map.getAllPolylines()); // draw enemies

        // update enemies on screen
        for (Enemy enemy : currWave.getEnemiesOnScreen()) {

            // if enemy has completed polyline path, destroy enemy
            if (enemy.getIndex() >= map.getAllPolylines().get(0).size()) {
                enemy.destroy();
                lives -= enemy.getPenalty();

                // if lives reaches 0, exit game
                if (lives <= 0) {
                    ShadowDefend.exitGame();
                }
            }
        }
    }

    /**
     * Update projectile positions and collisions
     */
    private void updateProjectiles() {

        // iterate through projectiles and destroy any off screen or that have collided
        Iterator<Projectile> projectileIterator = projectiles.iterator();

        while (projectileIterator.hasNext()) {
            Projectile pr = projectileIterator.next();

            // cleans up projectile "blanks"
            if (pr == null) {
                projectileIterator.remove();
                continue;
            }

            // if projectile is explosive, destroy enemies within area of effect
            for (Enemy enemy : getEnemiesOnScreen()) {
                double damage = 0;

                if (pr instanceof ExplosiveProjectile) {

                    // store projectile and any enemies its explosion hits
                    if (!explosiveProjectileHits.containsKey(pr)) {
                        explosiveProjectileHits.put(pr, new ArrayList<>());
                    }

                    // if enemy is in radius and hasn't already taken damage, deal damage
                    if (((ExplosiveProjectile) pr).enemyInRadius(enemy.getPosition().asVector()) &&
                            !explosiveProjectileHits.get(pr).contains(enemy)) {

                        damage = ((ExplosiveProjectile) pr).getDamage();
                        enemy.destroyedByDamage(((ExplosiveProjectile) pr).getDamage());

                        // add enemy to list of enemies hit
                        explosiveProjectileHits.get(pr).add(enemy);
                    }
                }

                else if (pr instanceof StandardProjectile) {
                    damage = ((StandardProjectile) pr).damageInflicted(enemy.getPosition());
                    // if the projectile didn't hit, do nothing
                    if (damage == 0) {
                        continue;
                    }
                }

                // apply damage and determine if enemy was destroyed
                if (enemy.destroyedByDamage(damage)){

                    // add reward to total money
                    this.money += enemy.getReward();

                    // spawn children and protect children from dying in explosion as well as add enemy to
                    // the wave's list of enemies
                    if (enemy.spawnChildren() != null) {
                        enemy.spawnChildren().forEach(e -> {
                            if (pr instanceof ExplosiveProjectile) explosiveProjectileHits.get(pr).add(e);
                            waves.get(currentWave).addEnemy(e);
                        });
                    }
                }
            }

            // if off screen or destroyed, remove from array for garbage collector
            if (pr.isDestroyed() || pr.isOffScreen()) {
                projectileIterator.remove();
            } else {
                pr.update();
            }
        }

    }

    /**
     * Updates tower positions and launches projectiles
     * @param input user define input
     */
    private void updateTowers(Input input) {

        // remove tower if it is off screen and is not being placed
        towers.removeIf(tower -> !tower.isPlacing() && tower.isOffScreen());

        towers.forEach(tower -> {

            // determine target for tower
            Enemy target = null;

            List<List<Point>> points = map.getAllPolylines();

            for (Enemy enemy : getEnemiesOnScreen()) {

                // get distance from enemy to tower
                double distance = enemy.getPosition().distanceTo(tower.getPosition());

                // consider it a potential target if within range
                if (distance < tower.getRange() && enemy.getIndex() != points.get(0).size()) {

                    // if no target set, set this one
                    if (target == null) {
                        target = enemy;

                    // else if enemy is further in map, set as new target
                    } else if (enemy.getIndex() >= target.getIndex() &&
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
            if (!tower.isPlacing() && tower.isReloaded()) {
                projectiles.add(tower.fire(target));
            }

            // update tower
            tower.update(input);

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
                        Tower newTower;
                        switch (tb.getTowerType()) {
                            case Tank.TYPE:
                                newTower = new Tank(input.getMouseX(), input.getMouseY());
                                break;
                            case SuperTank.TYPE:
                                newTower = new SuperTank(input.getMouseX(), input.getMouseY());
                                break;
                            case AirSupport.TYPE:
                                newTower = new AirSupport(input.getMouseX(), input.getMouseY(), planeOrientation);
                                planeOrientation = (planeOrientation + Math.PI/2) % (Math.PI);
                                break;
                            default:
                                throw new IllegalStateException("Tower type does not exist");
                        }
                        towers.add(newTower);
                        dragActive = newTower;
                        money -= dragActive.getCost();
                    }
                }
            }
        }


        else if (dragActive != null) {
            ShadowDefend.updateStatus(STATE_PLACING);

            // determine if tile is blocked
            boolean blockedTile = map.hasProperty((int)input.getMouseX(), (int)input.getMouseY(), BLOCKED_PROPERTY);

            // if not over blocked positions or over a panel, check for mouse left click
            if (dragActive.canBePlaced(blockedPoints, blockedTile) &&
                    !ShadowDefend.getBuyPanel().getBoundingBox().isMouseOver(input) &&
                    !ShadowDefend.getStatusPanel().getBoundingBox().isMouseOver(input)) {

                // place tower
                if (input.wasPressed(MouseButtons.LEFT)) {
                    dragActive.place(input.getMouseX(), input.getMouseY());
                    // add tower position to blocked points
                    blockedPoints.add(dragActive.getPosition());
                    dragActive = null;
                }
            }

            // cancel placing tower and return money
            if (input.wasPressed(MouseButtons.RIGHT)) {
                money += dragActive.getCost();
                dragActive = null;
                towers.pop();
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
        if (currentWave < waves.size()) return new ArrayList<>(waves.get(currentWave).getEnemiesOnScreen());
        return new ArrayList<>();
    }


    /**
     * creates map for level
     * @return TiledMap object with map file loaded
     */
    public TiledMap createMap() { return map = new TiledMap(MAP_PATH + level + MAP_EXT); }

    /**
     * Update panel text
     */
    private void updatePanelText() {
        // get panels
        Panel buyPanel = ShadowDefend.getBuyPanel();
        Panel statusPanel = ShadowDefend.getStatusPanel();

        // update if tower is purchasable
        for (TowerButton tb : towerButtons) {
            tb.setPurchasable(money);
        }

        // update timescale colour
        if (ShadowDefend.getTimescale() > 1) {
            statusPanel.updateTextColour(ShadowDefend.SP_TIMESCALE, Colour.GREEN);
        } else if (ShadowDefend.getTimescale() <= 1) {
            statusPanel.updateTextColour(ShadowDefend.SP_TIMESCALE, Colour.WHITE);
        }

        // update panel text
        buyPanel.updateText(ShadowDefend.MONEY, ShadowDefend.MONEY + money);
        statusPanel.updateText(ShadowDefend.SP_TIMESCALE, ShadowDefend.SP_TIMESCALE +
                ShadowDefend.getTimescale());
        statusPanel.updateText(ShadowDefend.SP_LIVES, ShadowDefend.SP_LIVES + lives);
        statusPanel.updateText(ShadowDefend.SP_WAVE, ShadowDefend.SP_WAVE + (currentWave + 1));
    }

    /**
     * return boolean value for levelComplete
     * @return boolean if level is complete
     */
    public boolean isLevelComplete() {

        // level is complete when all waves are completed
        if (waves.stream().allMatch(wave -> Boolean.TRUE.equals(wave.isWaveComplete()))) {
            ShadowDefend.updateStatus(STATE_WIN);
            return true;
        }
        return false;
    }


    /**
     * @return boolean if wave is in progress
     */
    public boolean isWaveInProgress() {
        // if wave has ended, remove in progress status
        if (!waveInProgress) {
            ShadowDefend.removeStatus(STATE_IN_PROGRESS);
        }
        return waveInProgress;
    }

}
