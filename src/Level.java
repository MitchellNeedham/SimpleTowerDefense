import bagel.Image;
import bagel.Window;
import bagel.map.TiledMap;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class Level {

    private final static String MAP_PATH = "res/levels/";
    private final static String MAP_EXT = ".tmx";
    private static String MAP_FILE;

    private final int level;
    private final int maxWaves;
    private int currentWave = 0;
    private Wave wave;

    private boolean levelComplete = false;

    /**
     * level constructor
     * @param level Number of level
     */
    public Level(int level) {
        System.out.println(level);
        this.level = level;
        this.maxWaves = getMaxWaves();
    }

    /**
     * creates map for level
     * @return TiledMap object with map file loaded
     */
    public TiledMap createMap() {
        try (Stream<Path> paths = Files.walk(Paths.get(MAP_PATH + level))) {
            paths
                    .forEach( p -> {
                        if (p.toString().endsWith(".tmx")) {
                            MAP_FILE = p.toString();
                        }

                    });
        } catch (IOException e) {
            e.printStackTrace();
        }
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
     * Determines if a wave has completed (i.e no more enemies)
     * @return true or false depending on whether wave is nullified
     */
    public boolean isWaveComplete() {
        if (wave != null) {
            boolean waveComplete = wave.isWaveComplete();
            if (currentWave == maxWaves && waveComplete) {
                levelComplete = true;
            }
            return waveComplete;
        }
        return true;
    }

    /**
     *
     * @return
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
     *
     * @return
     */
    public boolean isLevelComplete() {
        return levelComplete;
    }
}
