import bagel.*;
import bagel.Window;
import bagel.map.TiledMap;
import bagel.Image;
import bagel.util.Point;

import java.io.IOException;
import java.nio.file.*;
import java.util.SortedSet;
import java.util.Stack;
import java.util.TreeSet;
import java.util.stream.Stream;

public class ShadowDefend extends AbstractGame {

    /**
     * Entry point for ShadowDefend game
     *
     * PLEASE NOTE: The res directory has been modified slightly
     * Instead, you'll find the files in res/levels/1
     * and a new file called 'waves.txt' that contains all information regarding the enemies for that wave
     *
     */

    //-------------------------STATUS DATA-------------------------//

    private static final SortedSet<Integer> STATUS_SET = new TreeSet<>();
    private final static String[] STATUS_TYPE = new String[] {"Awaiting Start", "Wave In Progress", "Placing", "Winner!"};
    private final static int INITIAL_STATUS = 0;


    //-------------------------FONT-------------------------//

    private static final String FONT_FILE = "res/fonts/DejaVuSans-Bold.ttf";
    private static final int BUY_PANEL_FONT_SIZE = 48;
    private static final int BINDINGS_FONT_SIZE = 16;
    private static final int STATUS_PANEL_FONT_SIZE = 16;


    //-------------------------BUY PANEL ITEMS-------------------------//

    private final static Point BUY_PANEL_POS = new Point(0, 0);
    public final static String MONEY = "$";
    private final static Point MONEY_POS = new Point(850, 60);
    private static final String[] BUY_PANEL_BINDINGS_INFO = {"Key bindings:", "S - Start Wave", "P - Pause Game",
            "L - Increase Timescale", "K - Decrease Timescale"};
    private static final double BINDINGS_X_POSITION = 500D;
    private static final double BINDINGS_HEIGHT = 100D;


    //-------------------------STATUS PANEL ITEMS-------------------------//

    private final static Point STATUS_PANEL_POS = new Point(0, 743);
    public final static String SP_LIVES = "Lives: ";
    private final static Point SP_LIVES_POS = new Point(950, 16);
    public final static String SP_TIMESCALE = "Timescale: ";
    private final static Point SP_TIMESCALE_POS = new Point(200, 16);
    public final static String SP_WAVE = "Wave: ";
    private final static Point SP_WAVE_POS = new Point(40, 16);
    public final static String SP_STATUS = "Status: ";
    private final static Point SP_STATUS_POS = new Point(400, 16);

    //-------------------------IMAGE AND MAP DATA-------------------------//

    // off screen render location
    private final static int OFF_SCREEN_X = -100;
    private final static int OFF_SCREEN_Y = -100;
    private final Stack<Image> imageFiles = new Stack<>();
    private final static String IMG_PATH = "res/images/";
    private final static String LEVEL_PATH = "res/levels/";
    private static final String BUY_PANEL_IMAGE = "res/images/panels/buypanel.png";
    private static final String STATUS_PANEL_IMAGE = "res/images/panels/statuspanel.png";
    private final static String LEVEL_MAP_EXT = ".tmx";


    //-------------------------GAME PROPERTIES-------------------------//

    private static Panel buyPanel;
    private static Panel statusPanel;
    private static TiledMap map;
    private final int maxLevels;
    private Level level;
    private int currentLevel = 1;
    private static float timeScale = 1.0f;
    private float timeScaleStore = 1.0f;



    public static void main(String[] args) {
        // Create new instance of game and run it
        new ShadowDefend().run();
    }

    /**
     * Setup the game
     */
    public ShadowDefend() {
        // create buy and status panel
        buyPanel = new Panel(BUY_PANEL_POS.x,BUY_PANEL_POS.y, BUY_PANEL_IMAGE);
        statusPanel = new Panel(STATUS_PANEL_POS.x, STATUS_PANEL_POS.y, STATUS_PANEL_IMAGE);
        initPanels();
        updateStatus(INITIAL_STATUS);

        // determine number of levels
        this.maxLevels = getMaxLevels();


        // initialise menu
        Menu.init(maxLevels);

        // get all image files
        getAllImageFiles(IMG_PATH);


    }

    /**
     * Updates the game state approximately 60 times a second, potentially reading from input.
     * @param input The input instance which provides access to keyboard/mouse state information.
     */
    @Override
    protected void update(Input input) {

        // update menu and if menu is not active (in gamestate), update game
        if (Menu.update(input) == 0) {
            // if level selected is not current level, load it instead
            if (level == null || Menu.getLevel() != level.getLevel()) {
                level = new Level(Menu.getLevel());
                map = level.createMap();
            }
            // update game
            gameUpdate(input);
        } else {
            // while in menu, set timescale to 0
            timeScale = 0;
        }
    }

    /**
     * Updates game
     * @param input user defined input
     */
    public void gameUpdate(Input input) {
        //draw map
        map.draw(0, 0, 0, 0, Window.getWidth(), Window.getHeight());

        //render all image files off screen to prevent glitch when new image is created
        imageFiles.forEach(image -> image.draw(OFF_SCREEN_X, OFF_SCREEN_Y));

        RenderQueue.renderObjects();


        //start wave when 'S' key is pressed, but only if a wave is not in progress
        if (!level.isWaveInProgress() && input.wasPressed(Keys.S)) {
            level.start();
        }

        //increase timeScale
        if (input.wasPressed(Keys.L) && timeScale >= 0) {
            timeScale++;
        }

        //decrease timeScale
        if (input.wasPressed(Keys.K) && timeScale > 1 && timeScale != 0) {
            timeScale--;
        }

        // pause game
        if (input.wasPressed(Keys.P)) {
            if (timeScale > 0) {
                timeScaleStore = timeScale;
                timeScale = 0;
            } else if (timeScale == 0) {
                timeScale = timeScaleStore;
            }
        }

        //if wave is in progress, update gameTime from start of wave and draw enemies

        level.update(input, timeScale, map.getAllPolylines());

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
            map = level.createMap();

            //reset timeScale
            timeScale = 1;
        }

        buyPanel.update();
        statusPanel.update();
    }



    /**
     * Renders any images off screen to prevent bug where screen is tiled with images
     */
    private void getAllImageFiles(String imgPath) {

        // if path is a directory, recursively go through directories
        try (Stream<Path> paths = Files.walk(Paths.get(imgPath))) {
            paths
                    .skip(1)
                    .filter(Files::isDirectory)
                    .forEach(p -> getAllImageFiles(p.toString()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        // if path is a file, push to images stack for rendering
        try (Stream<Path> paths = Files.walk(Paths.get(imgPath))) {
            paths
                    .filter(Files::isRegularFile)
                    .forEach(p -> imageFiles.push(new Image(p.toString())));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * get max levels by returning number of folders in 'levels' folder
     * @return return max levels
     */
    private int getMaxLevels() {
        // count number of directories in levels
        try (Stream<Path> paths = Files.walk(Paths.get(LEVEL_PATH))) {
            return (int) paths.skip(1).filter(Files::isDirectory).count();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static void initPanels() {
        buyPanel.addText(MONEY, MONEY_POS.x, MONEY_POS.y, "", new Font(FONT_FILE, BUY_PANEL_FONT_SIZE));
        statusPanel.addText(SP_LIVES, SP_LIVES_POS.x, SP_LIVES_POS.y, "", new Font(FONT_FILE, STATUS_PANEL_FONT_SIZE));
        statusPanel.addText(SP_WAVE, SP_WAVE_POS.x, SP_WAVE_POS.y, "", new Font(FONT_FILE, STATUS_PANEL_FONT_SIZE));
        statusPanel.addText(SP_STATUS, SP_STATUS_POS.x, SP_STATUS_POS.y, "", new Font(FONT_FILE, STATUS_PANEL_FONT_SIZE));
        statusPanel.addText(SP_TIMESCALE, SP_TIMESCALE_POS.x, SP_TIMESCALE_POS.y, "", new Font(FONT_FILE, STATUS_PANEL_FONT_SIZE));
    }

    /**
     * Update status
     * @param status int containing new status
     */
    public static void updateStatus(int status) {
        if (!STATUS_SET.contains(status)) {
            STATUS_SET.add(status); // add status to sorted set
            // update text to last status in status set
            statusPanel.updateText(SP_STATUS, SP_STATUS + STATUS_TYPE[STATUS_SET.last()]);
        }

    }

    /**
     * Remove status that is no longer current
     * @param status int containing status to be removed
     */
    public static void removeStatus(int status) {
        if (STATUS_SET.contains(status)) {
            STATUS_SET.remove(status); // remove status from status set
            // update text to show last status in status set
            statusPanel.updateText(SP_STATUS, SP_STATUS + STATUS_TYPE[STATUS_SET.last()]);
        }
    }


    public static Panel getBuyPanel() {
        return buyPanel;
    }

    public static Panel getStatusPanel() {
        return statusPanel;
    }

    public static float getTimeScale() {
        return timeScale;
    }

    public static void exitGame() {
        Window.close();
        System.exit(0);
    }
}