import bagel.util.Point;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Wave {

    private final Set<Enemy> Enemies = new HashSet<>(); // enemies for each wave
    private Timer timer; // timer
    private int totalDelay = 0;

    /**
     * Creates and adds enemies to Set 'Enemies'
     * @param enemyData String array containing information about enemies in wave
     */
    public void addEnemies(String[] enemyData) {
        int enemyCount = Integer.parseInt(enemyData[2]); // get total enemies in spawn event

        for (int i = 0; i < enemyCount; i++) {

            // use switch to add enemies respective of type
            switch (enemyData[3]) {
                case "slicer":
                    Enemies.add(new Slicer(totalDelay));
                    break;
                case "superslicer":
                    Enemies.add(new SuperSlicer(totalDelay));
                    break;
                case "megaslicer":
                    Enemies.add(new MegaSlicer(totalDelay));
                    break;
                case "apexslicer":
                    Enemies.add(new ApexSlicer(totalDelay));
                    break;
            }

            // all delay of each event to total delay
            totalDelay += Integer.parseInt(enemyData[4]);
        }
    }

    /**
     * Adds enemy to enemy list (only occurs if a parent enemy was destroyed)
     * @param enemy Enemy object containing new enemy
     */
    public void addEnemy(Enemy enemy) { Enemies.add(enemy); }

    /**
     * Adds delay to total delay in wave
     * @param delay int containing delay in milliseconds
     */
    public void addDelay(int delay) { totalDelay += delay; }

    /**
     * Starts spawning enemies in wave
     */
    public void startWave() {
        timer = new Timer();
    }

    /**
     * Run through list of enemies and awakens them after their spawn delay time has elapsed
     * gets next point that enemy is moving towards and draws enemy
     * @param points a list of a list of points forming a polyline that enemies follow as a path
     */
    public void drawEnemies(List<List<Point>> points) {

        //iterate through enemies and awaken them if spawnDelay time has elapsed
        Enemies.forEach(enemy -> {

            // awaken enemy if spawn delay has elapsed
            if (this.timer.getTotalGameTime() > enemy.getSpawnDelay()) {
                enemy.awake();
            }

            //draw enemies
            if (enemy.getIndex() < points.get(0).size() && enemy.isActive() && !enemy.isDestroyed()) {
                enemy.draw(points.get(0).get(enemy.getIndex()));
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

    /**
     * return Timer object for this wave
     * @return Timer object timer
     */
    public Timer getTimer(){
        return timer;
    }
}
