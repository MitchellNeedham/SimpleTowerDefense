import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import bagel.util.Point;


public class Wave {
    private final static String FILE_PATH = "res/levels/";
    private final static String FILE_NAME = "/wave ";
    private final static String FILE_EXT = ".csv";
    private final static String ENEMY_SLICER = "slicer";
    private final static String splitByCSV= ",";

    private final Time gameTime;
    private final int waveNumber;
    private final int levelNumber;
    private final Set<Enemy> Enemies = new HashSet<>();

    /**
     * wave constructor
     * @param level level number
     * @param waveNumber wave number
     */
    public Wave(int level, int waveNumber) {
        //constructor
        this.waveNumber = waveNumber;
        this.levelNumber = level;
        this.gameTime = new Time();

        //create enemies
        spawnEnemies();
    }

    /**
     * Reads file containing data for the wave and creates the appropriate enemy with parameters
     */
    private void spawnEnemies() {
        //get wave file
        String filePath = FILE_PATH + levelNumber + FILE_NAME + waveNumber + FILE_EXT;

        try {
            File fp = new File(filePath);
            Scanner myReader = new Scanner(fp);
            while (myReader.hasNextLine()) {
                Enemy enemy = null;

                //save enemy information
                String[] enemyInfo = myReader.nextLine().split(splitByCSV);

                //create new slicer enemy type
                if (enemyInfo[0].equals(ENEMY_SLICER)) {
                    enemy = new Slicer(Float.parseFloat(enemyInfo[2]), Float.parseFloat(enemyInfo[1]));
                }

                //add enemy to enemy set
                if (enemy != null) {
                    Enemies.add(enemy);
                }
            }
            myReader.close();

        } catch (FileNotFoundException e) {
            //print error if no file found
            e.printStackTrace();
        }
    }

    /**
     * Run through list of enemies and awakens them after their spawn delay time has elapsed
     * gets next point that enemy is moving towards and draws enemy
     * @param timeScale game speed multiplier
     * @param points a list of a list of points forming a polyline that enemies follow as a path
     */
    public void drawEnemies(float timeScale, List<List<Point>> points) {

        //iterate through enemies and awaken them if spawnDelay time has elapsed
        Enemies.forEach(enemy -> {
            if (this.gameTime.getTotalGameTime() > enemy.getSpawnDelay()) {
                enemy.awake();
            }
            //draw enemies
            if (enemy.getIndex() < points.get(0).size() && enemy.isActive()) {
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
}
