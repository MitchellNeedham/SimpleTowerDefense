import bagel.*;
import bagel.Window;
import bagel.map.TiledMap;
import bagel.Image;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import bagel.util.Point;

import java.awt.*;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class Game extends AbstractGame {

    /**
     * Entry point for Bagel game
     *
     * Explore the capabilities of Bagel: https://people.eng.unimelb.edu.au/mcmurtrye/bagel-doc/
     */

    private final static int OFF_SCREEN_X = -100;
    private final static int OFF_SCREEN_Y = -100;
    private final static String IMG_PATH = "res/images/";
    private final static String LEVEL_PATH = "res/levels/";

    private TiledMap map;
    private final int maxLevels;
    private Level level;
    private int currentLevel = 1;
    private Wave wave;
    private float timeScale = 1.0f;
    private long time;
    private Stack<Image> imageFiles = new Stack<>();

    public static void main(String[] args) {
        // Create new instance of game and run it
        new Game().run();
    }

    /**
     * Setup the game
     */
    public Game(){
        //create level where waves are created and run
        this.level = new Level(currentLevel);
        this.maxLevels = getMaxLevels();
        System.out.println(maxLevels);

        this.map = level.createMap();

        //set time as milliseconds since 1970
        this.time = System.currentTimeMillis();

        getAllImageFiles();

    }

    /**
     * Updates the game state approximately 60 times a second, potentially reading from input.
     * @param input The input instance which provides access to keyboard/mouse state information.
     */
    @Override
    protected void update(Input input) {


        //draw map
        map.draw(0, 0, 0, 0, Window.getWidth(), Window.getHeight());

        //render all image files off screen to prevent glitch when new image is created
        imageFiles.forEach(image -> image.draw(OFF_SCREEN_X, OFF_SCREEN_Y));


        //start wave when 'S' key is pressed, but only if a wave is not in progress
        if (input.wasPressed(Keys.S) && level.isWaveComplete()) {
            //update time
            time = System.currentTimeMillis();
            wave = level.startNextWave();
            if (wave == null) {
                Window.close();
            }
        }

        //increase timeScale
        if (input.wasPressed(Keys.L)) {
            timeScale++;
        }
        //decrease timeScale
        if (input.wasPressed(Keys.K)) {
            if (timeScale > 1) {
                timeScale--;
            }
        }

        //if wave is in progress, draw enemies
        if (!level.isWaveComplete()) {
            wave.getTime().updateTime(timeScale);
            wave.drawEnemies(timeScale, map.getAllPolylines());
        }

        //if level is complete, start new level
        //if no more levels, close window and exit game
        if (level.isLevelComplete()) {
            if (currentLevel == maxLevels) {
                Window.close();
                System.exit(0);
            }

            //next level
            currentLevel++;
            level = new Level(currentLevel);
            this.map = level.createMap();

            //reset timeScale
            timeScale = 1;
        }
    }

    /**
     * Renders any images off screen to prevent bug where screen is tiled with images
     */
    private void getAllImageFiles() {
        try (Stream<Path> paths = Files.walk(Paths.get(IMG_PATH))) {
            paths
                    .skip(1)
                    .forEach((p) -> imageFiles.push(new Image(p.toString())));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * get max levels by returning number of folders in levels
     * @return return max levels
     */
    private int getMaxLevels() {
        try (Stream<Path> paths = Files.walk(Paths.get(LEVEL_PATH))) {
            return (int) paths.skip(1).filter(Files::isDirectory).count();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

}

