import bagel.DrawOptions;
import bagel.Font;
import bagel.Image;
import bagel.util.Colour;

import java.util.*;

public class Panel {

    private static final String DEFAULT_COLOUR = "#FFFFFF";
    private final int xPos;
    private final int yPos;
    private final Image backgroundImg;
    private final Map<String, Text> text = new HashMap<>();
    private final List<Clickable> clickable = new ArrayList<>();
    private static final String FONT_FILE = "res/fonts/DejaVuSans-Bold.ttf";
    private final Map<String, Font> fonts = new HashMap<>();

    // TODO: improve panel using OOP

    /**
     * Constructor for panel
     * @param x x-coordinate at top left of panel
     * @param y y-coordinate at top left of panel
     * @param filePath background image of panel
     */
    protected Panel(int x, int y, String filePath) {
        this.xPos = x;
        this.yPos = y;
        this.backgroundImg = new Image(filePath);

    }

    /**
     * Updates panel
     */
    protected void update() {

        // draw background image
        backgroundImg.drawFromTopLeft(xPos, yPos);

        // draw each text object in text map
        text.forEach((key, t) -> t.draw());

        // draw clickable objects and any text
        clickable.forEach(Clickable::draw);
    }

    /**
     * Add clickable object to panel
     * @param object Clickable object
     */
    protected void addClickable(Clickable object) {
        clickable.add(object);
    }

    /**
     * Add text to panel
     * @param textContent String containing text
     * @param x centre x-coordinate of text
     * @param y centre y-coordinate of text
     */
    protected void addText(String type, double x, double y, String textContent, Font font) {
        text.put(type, new Text(font, textContent, x, y));
    }

    /**
     * Updates text (to be redesigned)
     * @param type String containing current text
     * @param newText String containing updated text
     */
    public void updateText(String type, String newText) {
        text.get(type).updateText(newText);
    }




}
