import java.awt.*;
import java.io.File;  // Import the File class
import java.io.FileNotFoundException;  // Import this class to handle errors
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner; // Import the Scanner class to read text files
import java.util.Set;
import bagel.util.Point;
import bagel.util.Vector2;
import org.lwjgl.system.CallbackI;


public class Wave {
    private final static String FILE_PATH = "res/levels/";
    private final static String FILE_NAME = "/wave ";
    private final static String FILE_EXT = ".csv";
    private final static String ENEMY_SLICER = "slicer";
    private final static String splitByCSV= ",";

    private int waveNumber;
    private int levelNumber;
    private Set<Enemy> Enemies = new HashSet<>();

    /**
     * wave constructor
     * @param level level number
     * @param waveNumber wave number
     */
    public Wave(int level, int waveNumber) {
        this.waveNumber = waveNumber;
        this.levelNumber = level;

        spawnEnemies();

    }

    /**
     * Reads file containing data for the wave and creates the appropriate enemy with parameters
     */
    private void spawnEnemies() {
        String filePath = FILE_PATH + levelNumber + FILE_NAME + waveNumber + FILE_EXT;
        try {
            File fp = new File(filePath);
            Scanner myReader = new Scanner(fp);
            while (myReader.hasNextLine()) {
                Enemy enemy = null;

                String[] enemyInfo = myReader.nextLine().split(splitByCSV);

                if (enemyInfo[0].equals(ENEMY_SLICER)) {
                    enemy = new Slicer(Float.parseFloat(enemyInfo[2]), Float.parseFloat(enemyInfo[1]));
                }

                if (enemy != null) {
                    Enemies.add(enemy);
                }
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    /**
     * Run through list of enemies and awakens them after their spawn delay time has elapsed
     * gets next point that enemy is moving towards and draws enemy
     * @param time time elapsed since start of wave
     * @param timeScale game speed multiplier
     * @param points a list of a list of points forming a polyline that enemies follow as a path
     */
    public void drawEnemies(long time, float timeScale, List<List<Point>> points) {
        Enemies.forEach((Enemy enemy) -> {
            if ((System.currentTimeMillis() - time)/1000f/timeScale > enemy.getSpawnDelay()) {
                enemy.awake();
            }
            if (enemy.getIndex() < points.get(0).size() && enemy.isActive()) {
                enemy.draw(timeScale, points.get(0).get(enemy.getIndex()).asVector());
            }
        });
    }


}
