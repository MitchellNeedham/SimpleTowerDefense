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
import java.util.List;
import java.util.Set;
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

    private TiledMap map;
    private Level level;
    private Wave wave;
    private int currentLevel = 1;
    private float timeScale = 1.0f;
    private long time;


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
        this.map = level.createMap();

        //set time as milliseconds since 1970
        this.time = System.currentTimeMillis();
    }

    /**
     * Updates the game state approximately 60 times a second, potentially reading from input.
     * @param input The input instance which provides access to keyboard/mouse state information.
     */
    @Override
    protected void update(Input input) {


        //draw map
        map.draw(0, 0, 0, 0, Window.getWidth(), Window.getHeight());
        renderImagesOffScreen();

        //start wave when 'S' key is pressed, but only if a wave is not in progress
        if (input.isDown(Keys.S) && level.isWaveComplete()) {
            //update time
            time = System.currentTimeMillis();
            wave = level.startNextWave();
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
            wave.drawEnemies(time, timeScale, map.getAllPolylines());
        }

    }

    /**
     * Renders any images off screen to prevent bug where screen is tiled with images
     */
    private void renderImagesOffScreen() {
        try (Stream<Path> paths = Files.walk(Paths.get("res/images"))) {
            paths
                    .skip(1)
                    .forEach((p) -> new Image(p.toString()).draw(OFF_SCREEN_X,OFF_SCREEN_Y));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

