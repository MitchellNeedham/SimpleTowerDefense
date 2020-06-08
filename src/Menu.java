
import bagel.Input;
import bagel.MouseButtons;
import bagel.Window;
import bagel.util.Point;
import bagel.util.Vector2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Menu {

    // Menu system for game

    //-------------------------MENU SCREEN IDS-------------------------//
    private static final int GAME = 0;
    private static final int MAIN = 1;
    private static final int OPTIONS = 2;
    private static final int LEVEL_SELECT = 3;

    //-------------------------RETURN TO MAIN MENU BUTTON-------------------------//

    private static final Point TOP_MENU_BUTTON_POS = new Point(Window.getWidth() - 50, 50);
    private static final Vector2 TOP_MENU_BUTTON_SIZE = new Vector2(40, 40);

    //-------------------------MAIN SCREEN INITIALISATION-------------------------//



    //-------------------------LEVEL SELECT INITIALISATION-------------------------//
    private static final int MAX_BUTTONS_PER_LINE = 3;
    private static final int LEVEL_BUTTON_WIDTH = 200;
    private static final int LEVEL_BUTTON_HEIGHT = 200;
    private static final Point LEVEL_BUTTON_INIT_POS = new Point(200, 200);
    private static final int X_OFFSET = 250;
    private static final int Y_OFFSET = 250;

    //-------------------------OPTIONS INITIALISATION-------------------------//




    private static final Map<Integer, ArrayList<MenuItem>> MenuSections = new HashMap<>();
    private static int activeSection;
    private static int level = 1; // base level to load

    /**
     * Initialise menu screens and creates buttons
     * @param maxLevels total levels in game
     */
    public static void init(int maxLevels) {
        // get centre of screen
        Point centre = new Point(Window.getWidth()/2f, Window.getHeight()/2f);

        // Return button to main menu
        // TODO: implement this properly
        MenuButton topMenuReturn = new MenuButton(TOP_MENU_BUTTON_POS.x, TOP_MENU_BUTTON_POS.y,
                TOP_MENU_BUTTON_SIZE.x, TOP_MENU_BUTTON_SIZE.y, "", MAIN);

        // main menu items for main menu screen
        ArrayList<MenuItem> menuItems = new ArrayList<>();

        // Initialise Main Screen
        menuItems.add(new MenuButton(centre.x, centre.y - 200, 400, 100, "Play/Continue", GAME));
        menuItems.add(new MenuButton(centre.x, centre.y, 400, 100, "Levels", LEVEL_SELECT));
        menuItems.add(new MenuButton(centre.x, centre.y + 200, 400, 100, "Options", OPTIONS));
        MenuSections.put(MAIN, menuItems);

        // Initialise Levels Screen
        ArrayList<MenuItem> levelSelectItems = new ArrayList<>();

        // dynamically add level buttons to select specific level
        // TODO: add scroll bar fro overflow
        for (int i = 0; i < Math.floorDiv(maxLevels, MAX_BUTTONS_PER_LINE) + 1; i++) {
            for (int j = 0; j < MAX_BUTTONS_PER_LINE; j++) {
                // get level number
                int level = (j + (i * MAX_BUTTONS_PER_LINE) + 1);
                // if max levels reached, exit loop
                if (level > maxLevels) {
                    break;
                }

                // create button for level
                levelSelectItems.add(new MenuButton(200 + X_OFFSET * j, 200 + Y_OFFSET * i,
                        200, 200, "level" + level, level));
            }
        }
        levelSelectItems.add(topMenuReturn);
        MenuSections.put(LEVEL_SELECT, levelSelectItems);



        // Initiliase Options Screen
        ArrayList<MenuItem> optionsItems = new ArrayList<>();
        optionsItems.add(topMenuReturn);
        MenuSections.put(OPTIONS, optionsItems);

        // load main menu screen
        load(MAIN);
    }

    /**
     * loads either the game or a menu screen
     * @param section ID of sections in menu
     */
    public static void load(int section) {
        activeSection = section;
    }

    /**
     * if in menu, displayed appropriate menu section
     * @param input user defined input
     * @return section of menu or game
     */
    public static int update(Input input) {

        if (activeSection != GAME) {
            display(input, activeSection);
        }

        return activeSection;
    }

    /**
     * displays menu buttons and images
     * @param input user defined input
     * @param section section ID
     */
    public static void display(Input input, int section) {

        // get all menu items for section
        for (MenuItem item : MenuSections.get(section)) {
            item.draw();
            if (item instanceof MenuButton) {
                if (((MenuButton) item).isOver(input) && input.wasPressed(MouseButtons.LEFT)) {
                    if (MenuSections.get(LEVEL_SELECT).contains(item)) {
                        level = ((MenuButton) item).clicked();
                        activeSection = GAME;
                        //TODO: find a better way to do this
                    } else {
                        activeSection = ((MenuButton) item).clicked();
                    }
                }
            }
        }
    }


    public static int getLevel() { return level; }
}
