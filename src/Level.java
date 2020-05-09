
import bagel.Window;
import bagel.map.TiledMap;
import bagel.util.Point;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.Stack;
import java.util.stream.Stream;


public class Level {

    private final static String MAP_PATH = "res/levels/";
    private final static String MAP_EXT = ".tmx";
    private static String MAP_FILE;

    private final int level;
    private final int maxWaves;
    private int currentWave = 0;
    private Wave wave;
    private Stack<Wave> waves = new Stack<Wave>();
    private Time gameTime;

    private boolean levelComplete = false;
    private boolean inProgress = false;

    /**
     * level constructor
     * @param level Number of level
     */
    public Level(int level) {
        this.level = level;
        this.maxWaves = getMaxWaves();
        createWaves();
        this.gameTime = new Time();
        this.inProgress = false;
    }

    public void start() {
        waves.firstElement().startWave();
        inProgress = true;
    }

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
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    public void update(float timeScale, List<List<Point>> points) {
        waves.stream().filter(Wave::isInProgress).forEach(w -> {
            w.getTime().updateTime(timeScale);
            w.drawEnemies(timeScale, points);
        });
        if (waves.stream().noneMatch(Wave::isSpawning) && currentWave != waves.size() - 1) {
            currentWave++;
            waves.get(currentWave).startWave();
        }
    }


    // Previous stuff below //

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
        return new TiledMap(MAP_FILE);
    }

    /**
     * begins wave if next wave exists
     * @return Wave object
     */
    public Wave startNextWave() {
        currentWave++;
        if (currentWave <= maxWaves) {
            return this.wave = new Wave(level, currentWave);
        }
        return null;
    }

    /**
     * check level folder for total number of ".csv" files and return it
     * @return number of total waves in this level
     */
    private int getMaxWaves() {
        try (Stream<Path> paths = Files.walk(Paths.get("res/levels/" + level))) {
            return (int) paths.filter(p -> p.toString().endsWith(".csv")).count();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
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
