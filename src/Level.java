
import bagel.Window;
import bagel.map.TiledMap;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;


public class Level {

    private final static String MAP_PATH = "res/levels/";
    private final static String MAP_EXT = ".tmx";
    private static String MAP_FILE;

    private final int level;
    private boolean inProgress = false;
    private int currentWave = 0;
    private Wave wave;
    private Stack<Wave> waves = new Stack<>();

    private boolean levelComplete = false;

    /**
     * level constructor
     * @param level Number of level
     */
    public Level(int level) {
        this.level = level;
        getWaves();
    }

    public void start() {
        inProgress = true;
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
        return new TiledMap(MAP_FILE);
    }

    /**
     * return boolean value for levelComplete
     * @return boolean if level is complete
     */
    public boolean isLevelComplete() {
        return !inProgress;
    }


    private void getWaves() {
        //get wave file
        String filePath = "res/levels/" + level + "/waves.txt";

        try {
            File fp = new File(filePath);
            Scanner myReader = new Scanner(fp);
            while (myReader.hasNextLine()) {
                String[] waveInfo = myReader.nextLine().split(",");

                if (waves.size() == Integer.parseInt(waveInfo[0])) {
                    waves.lastElement().updateWave(waveInfo);
                } else {
                    waves.add(new Wave(level, waveInfo));
                }
            }
            myReader.close();

        } catch (FileNotFoundException e) {
            //print error if no file found
            e.printStackTrace();
        }
    }
}
