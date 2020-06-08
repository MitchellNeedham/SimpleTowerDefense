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

    private static final SortedSet<Integer> STATUS_SET = new TreeSet<>();
    private final static String[] STATUS_TYPE = new String[] {"Awaiting Start", "Wave In Progress", "Placing", "Winner!"};
    private final static int INITIAL_STATUS = 0;
    private static final String FONT_FILE = "res/fonts/DejaVuSans-Bold.ttf";


    //-------------------------BUY PANEL ITEMS-------------------------//

    private static final int BUY_PANEL_FONT_SIZE = 48;
    public final static String MONEY = "$ ";
    private final static Point MONEY_POS = new Point(800, 60);


    //-------------------------STATUS PANEL ITEMS-------------------------//

    private static final int STATUS_PANEL_FONT_SIZE = 16;
    public final static String LIVES = "Lives: ";
    private final static Point LIVES_POS = new Point(970, 16);
    public final static String TIMESCALE = "Timescale: ";
    private final static Point TIMESCALE_POS = new Point(200, 16);
    public final static String WAVE = "Wave: ";
    private final static Point WAVE_POS = new Point(40, 16);
    public final static String STATUS = "Status: ";
    private final static Point STATUS_POS = new Point(400, 16);




    private final static int OFF_SCREEN_X = -100;
    private final static int OFF_SCREEN_Y = -100;
    private final static String IMG_PATH = "res/images/";
    private final static String LEVEL_PATH = "res/levels/";

    private static final String BUY_PANEL_IMAGE = "res/images/panels/buypanel.png";
    private static final String STATUS_PANEL_IMAGE = "res/images/panels/statuspanel.png";
    private static Panel buyPanel;
    private static Panel statusPanel;

    private TiledMap map;
    private final int maxLevels;
    private Level level;
    private int currentLevel = 1;
    //private Wave wave;
    private static float timeScale = 1.0f;
    private final Stack<Image> imageFiles = new Stack<>();

    private float timeScaleSave = 1.0f;



    public static void main(String[] args) {
        // Create new instance of game and run it
        new ShadowDefend().run();
    }

    /**
     * Setup the game
     */
    public ShadowDefend() {
        // create buy and status panel
        // TODO: create endgame panel
        buyPanel = new Panel(0,0, BUY_PANEL_IMAGE);
        statusPanel = new Panel(0, 743, STATUS_PANEL_IMAGE);
        initPanels();
        updateStatus(INITIAL_STATUS);

        // create level
        this.level = new Level(currentLevel);

        // determine number of levels
        this.maxLevels = getMaxLevels();

        // create map
        this.map = level.createMap();

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
            if (Menu.getLevel() != level.getLevel()) {
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
        if (input.wasPressed(Keys.S) && !level.isWaveInProgress()) {
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
                timeScaleSave = timeScale;
                timeScale = 0;
            } else if (timeScale == 0) {
                timeScale = timeScaleSave;
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
            this.map = level.createMap();

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
        statusPanel.addText(LIVES, LIVES_POS.x, LIVES_POS.y, "", new Font(FONT_FILE, STATUS_PANEL_FONT_SIZE));
        statusPanel.addText(WAVE, WAVE_POS.x, WAVE_POS.y, "", new Font(FONT_FILE, STATUS_PANEL_FONT_SIZE));
        statusPanel.addText(STATUS, STATUS_POS.x, STATUS_POS.y, "", new Font(FONT_FILE, STATUS_PANEL_FONT_SIZE));
        statusPanel.addText(TIMESCALE, TIMESCALE_POS.x, TIMESCALE_POS.y, "", new Font(FONT_FILE, STATUS_PANEL_FONT_SIZE));
    }

    public static void updateStatus(int status) {
        STATUS_SET.add(status);
        System.out.println(STATUS_TYPE[STATUS_SET.last()]);
        statusPanel.updateText(STATUS, STATUS + STATUS_TYPE[STATUS_SET.last()]);
    }

    public static void removeStatus(int status) {
        STATUS_SET.remove(status);
    }



    public static Panel getBuyPanel() {
        return buyPanel;
    }

    public static Panel getStatusPanel() {
        return statusPanel;
    }

    public static double getTimeScale() {
        return timeScale;
    }
}