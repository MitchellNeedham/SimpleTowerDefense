import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import bagel.util.Point;

public class Wave {
    private final static String FILE_PATH = "res/levels/";
    private final static String FILE_NAME = "/wave ";
    private final static String FILE_EXT = ".csv";
    private final static String ENEMY_SLICER = "slicer";
    private final static String splitByCSV= ",";
    private Time gameTime;
    private final int waveNumber;
    private final int levelNumber;
    private boolean inProgress = false;
    private boolean spawning = false;
    private final Set<Enemy> Enemies = new HashSet<>();

    private int totalDelay = 0;

    /**
     * wave constructor
     * @param level level number
     * @param waveNumber wave number
     */
    public Wave(int level, int waveNumber) {
        //constructor
        this.waveNumber = waveNumber;
        this.levelNumber = level;
        System.out.println(this.waveNumber);
        //this.gameTime = new Time();

        //create enemies
        //spawnEnemies();
    }

    /**
     * Creates and adds enemies to Set 'Enemies'
     * @param enemyData String array containing information about enemies in wave
     */
    public void addEnemies(String[] enemyData) {
        int enemyCount = Integer.parseInt(enemyData[2]);
        int i;
        for (i = 0; i < enemyCount; i++) {
            Enemies.add(new Slicer(totalDelay, enemyData[3]));
            totalDelay += Integer.parseInt(enemyData[4]);
        }
    }

    public void addEnemy(Enemy enemy) {
        Enemies.add(enemy);
    }

    public void addDelay(int delay) {
        totalDelay += delay;
    }

    /**
     * Starts spawning enemies in wave
     */
    public void startWave() {
        gameTime = new Time();
        inProgress = true;
        spawning = true;
    }


    /**
     * Run through list of enemies and awakens them after their spawn delay time has elapsed
     * gets next point that enemy is moving towards and draws enemy
     * @param timeScale game speed multiplier
     * @param points a list of a list of points forming a polyline that enemies follow as a path
     */
    public void drawEnemies(float timeScale, List<List<Point>> points) {

        if (this.gameTime.getTotalGameTime() > totalDelay) {
            spawning = false;
        }
        //iterate through enemies and awaken them if spawnDelay time has elapsed
        Enemies.forEach(enemy -> {
            if (this.gameTime.getTotalGameTime() > enemy.getSpawnDelay()) {
                enemy.awake();
            }
            //draw enemies
            if (enemy.getIndex() < points.get(0).size() && enemy.isActive() && !enemy.isDestroyed()) {
                enemy.draw(timeScale, points.get(0).get(enemy.getIndex()).asVector());
            } else if (enemy.getIndex() == points.get(0).size()) {
                enemy.destroy();
            }

        });

    }

    /**
     * Wave is complete if all enemies are destroyed either by leaving map or some other method
     * @return boolean for wave ending
     */
    public boolean isWaveComplete() {
        return Enemies.stream().allMatch(enemy -> Boolean.TRUE.equals(enemy.isDestroyed()));
    }

    /**
     * return Time onject for this wave
     * @return Time object gameTime
     */
    public Time getTime(){
        return gameTime;
    }

    public boolean isInProgress() {
        return inProgress;
    }

    public boolean isSpawning() {
        return spawning;
    }

    /**
     * Creates list of all enemies in wave currently on screen
     * @return List of enemies on screen
     */
    public List<Enemy> getEnemiesOnScreen() {
        List<Enemy> enemiesOnScreen = new ArrayList<>();
        for (Enemy enemy : Enemies) {
            if (enemy.isActive() && !enemy.isDestroyed()) {
                enemiesOnScreen.add(enemy);
            }
        }

        return enemiesOnScreen;
    }


}
