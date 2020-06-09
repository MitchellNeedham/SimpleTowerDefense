import bagel.Font;
import bagel.Image;
import bagel.util.Colour;
import bagel.util.Point;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Panel {
    private static final String DEFAULT_FONT = "res/fonts/DejaVuSans-Bold.ttf";
    private static final int DEFAULT_SIZE = 24;
    private final Point pos;
    private final Image backgroundImg;
    private final Map<String, Text> text = new HashMap<>();
    private final List<Clickable> clickable = new ArrayList<>();

    private final Map<String, Font> fonts = new HashMap<>();
    private final BoundingBox bb;
    // TODO: improve panel using OOP

    /**
     * Constructor for panel
     * @param x x-coordinate at top left of panel
     * @param y y-coordinate at top left of panel
     * @param filePath background image of panel
     */
    protected Panel(double x, double y, String filePath) {
        this.pos = new Point(x, y);
        this.backgroundImg = new Image(filePath);
        bb = new BoundingBox(x, y, backgroundImg.getWidth(), backgroundImg.getHeight());


    }

    /**
     * Updates panel
     */
    protected void update() {
        draw();
    }

    public void draw() {
        backgroundImg.drawFromTopLeft(pos.x, pos.y);
        text.forEach((key, t) -> t.draw());
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
        text.put(type, new Text(font, textContent, x + pos.x, y + pos.y));
    }

    protected void addText(String type, double x, double y, String textContent, int size) {
        text.put(type, new Text(new Font(DEFAULT_FONT, size), textContent, pos.x + x, pos.y + y));
    }

    protected void addText(String type, double x, double y, String textContent) {
        text.put(type, new Text(new Font(DEFAULT_FONT, DEFAULT_SIZE), textContent, pos.x + x, pos.y + y));
    }

    /**
     * Updates text (to be redesigned)
     * @param type String containing current text
     * @param newText String containing updated text
     */
    public void updateText(String type, String newText) {
        text.get(type).updateText(newText);
    }

    public void updateTextColour(String type, Colour colour) {text.get(type).updateColour(colour);}

    public BoundingBox getBoundingBox() {
        return bb;
    }
}
