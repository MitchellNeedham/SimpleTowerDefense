import bagel.*;
import bagel.map.TiledMap;

public class Game extends AbstractGame {

    /**
     * Entry point for Bagel game
     *
     * Explore the capabilities of Bagel: https://people.eng.unimelb.edu.au/mcmurtrye/bagel-doc/
     */

    private final TiledMap map;
    private Wave wave = null;
    private int currentWave = 0;

    public static void main(String[] args) {
        // Create new instance of game and run it
        new Game().run();
    }

    /**
     * Setup the game
     */
    public Game(){
        this.map = new TiledMap("res/levels/1.tmx");
    }

    /**
     * Updates the game state approximately 60 times a second, potentially reading from input.
     * @param input The input instance which provides access to keyboard/mouse state information.
     */
    @Override
    protected void update(Input input) {

        //draw map
        map.draw(0, 0, 0, 0, Window.getWidth(), Window.getHeight());

        //start wave when 'S' key is pressed, but only if a wave is not in progress
        if (input.isDown(Keys.S) && wave == null) {
            wave = new Wave(currentWave);
            currentWave++;
        }
        //set wave to null if it has completed
        if (wave.isWaveComplete()) {
            wave = null;
        }
    }
}
