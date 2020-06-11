import bagel.*;
import bagel.Window;
import bagel.map.TiledMap;
import bagel.Image;
import bagel.util.Point;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;

public class ShadowDefend extends AbstractGame {

    /**
     * Entry point for ShadowDefend game
     *
     * I realised later on that I needed two versions of the game in order to submit for the competition
     * As such, there may be some artifacts of that game (particularly in the comments)
     *
     */

    //-------------------------STATUS DATA-------------------------//

    private static final SortedSet<Integer> STATUS_SET = new TreeSet<>();
    private final static String[] STATUS_TYPE = new String[] {"Awaiting Start", "Wave In Progress", "Placing", "Winner!"};
    private final static int INITIAL_SP_STATUS = 0;


    //-------------------------FONT-------------------------//

    private static final String FONT_FILE = "res/fonts/DejaVuSans-Bold.ttf";
    private static final int BUY_PANEL_FONT_SIZE = 48;
    private static final int BINDINGS_FONT_SIZE = 16;
    private static final int STATUS_PANEL_FONT_SIZE = 16;


    //-------------------------BUY PANEL ITEMS-------------------------//

    public final static String MONEY = "$";
    private final static Point MONEY_POS = new Point(850, 60);
    private static final String[] BUY_PANEL_BINDINGS_INFO = {"Key bindings:", "S - Start Wave", "P - Pause Game",
            "L - Increase Timescale", "K - Decrease Timescale"};
    private static final double BINDINGS_X_POSITION = 500D;
    private static final double BINDINGS_HEIGHT = 100D;


    //-------------------------STATUS PANEL ITEMS-------------------------//

    public final static String SP_LIVES = "Lives: ";
    private final static Point SP_LIVES_POS = new Point(930, 16);
    public final static String SP_TIMESCALE = "Timescale: ";
    private final static Point SP_TIMESCALE_POS = new Point(200, 16);
    public final static String SP_WAVE = "Wave: ";
    private final static Point SP_WAVE_POS = new Point(20, 16);
    public final static String SP_STATUS = "Status: ";
    private final static Point SP_STATUS_POS = new Point(400, 16);

    //-------------------------IMAGE AND MAP DATA-------------------------//

    // off screen render location
    private final static int OFF_SCREEN_X = -100;
    private final static int OFF_SCREEN_Y = -100;
    private final Stack<Image> imageFiles = new Stack<>();
    private final static String IMG_PATH = "res/images/";
    private final static String LEVEL_PATH = "res/levels/";
    private static final String BUY_PANEL_IMAGE = "res/images/buypanel.png";
    private static final String STATUS_PANEL_IMAGE = "res/images/statuspanel.png";
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
        buyPanel = new Panel(0,0, BUY_PANEL_IMAGE);
        statusPanel = new Panel(0, 743, STATUS_PANEL_IMAGE);
        initPanels();

        updateStatus(INITIAL_SP_STATUS);

        // create level
        this.level = new Level(currentLevel);

        // determine number of levels
        this.maxLevels = getMaxLevels();

        // create map
        ShadowDefend.map = level.createMap();

        // get all image files
        getAllImageFiles(IMG_PATH);


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
        if (!level.isWaveInProgress() && input.wasPressed(Keys.S)) {
            level.start();
            if (timeScale < 1) timeScale = 1;
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

        // update level
        level.update(input);

        //if level is complete, start new level
        if (level.isLevelComplete()) {
            if (currentLevel != maxLevels) {
                //next level
                currentLevel++;
                level = new Level(currentLevel);
                ShadowDefend.map = level.createMap();

                //reset timeScale
                timeScale = 1;
            }
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
            return (int) paths.skip(1).filter(file -> file.toString().endsWith(LEVEL_MAP_EXT)).count();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Initialises text on panels
     */
    public static void initPanels() {

        // money text
        buyPanel.addText(MONEY, MONEY_POS.x, MONEY_POS.y, "", new Font(FONT_FILE, BUY_PANEL_FONT_SIZE));

        // key bindings text
        for (int i = 0; i < BUY_PANEL_BINDINGS_INFO.length; i++) {
            double yPos = BINDINGS_HEIGHT/BUY_PANEL_BINDINGS_INFO.length * i + (double)BINDINGS_FONT_SIZE;
            buyPanel.addText(String.valueOf(i), BINDINGS_X_POSITION, yPos, BUY_PANEL_BINDINGS_INFO[i],
                    new Font(FONT_FILE, BINDINGS_FONT_SIZE));
        }

        // lives text
        statusPanel.addText(SP_LIVES, SP_LIVES_POS.x, SP_LIVES_POS.y, "",
                new Font(FONT_FILE, STATUS_PANEL_FONT_SIZE));

        // wave number
        statusPanel.addText(SP_WAVE, SP_WAVE_POS.x, SP_WAVE_POS.y, "",
                new Font(FONT_FILE, STATUS_PANEL_FONT_SIZE));

        // status text
        statusPanel.addText(SP_STATUS, SP_STATUS_POS.x, SP_STATUS_POS.y, "",
                new Font(FONT_FILE, STATUS_PANEL_FONT_SIZE));

        // timescale text
        statusPanel.addText(SP_TIMESCALE, SP_TIMESCALE_POS.x, SP_TIMESCALE_POS.y, "",
                new Font(FONT_FILE, STATUS_PANEL_FONT_SIZE));
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

    /**
     * Exits game
     */
    public static void exitGame() {
        Window.close();
        System.exit(0);
    }

    public static Panel getBuyPanel() { return buyPanel; }

    public static Panel getStatusPanel() { return statusPanel; }

    public static float getTimescale() { return timeScale; }

}