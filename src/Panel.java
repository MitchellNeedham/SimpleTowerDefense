import bagel.Font;
import bagel.Image;
import bagel.util.Colour;
import bagel.util.Point;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Panel {

    //-------------------------PANEL PROPERTIES-------------------------//

    private final Point pos;
    private final Image backgroundImg;
    private final Map<String, Text> text = new HashMap<>();
    private final List<TowerButton> buttons = new ArrayList<>();
    private final BoundingBox bb;

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
        backgroundImg.drawFromTopLeft(pos.x, pos.y);
        text.forEach((key, t) -> t.draw());
        buttons.forEach(TowerButton::draw);
    }

    /**
     * Add clickable object to panel
     * @param object TowerButton object
     */
    protected void addButton(TowerButton object) {
        buttons.add(object);
    }

    /**
     * Add text to panel
     * @param textContent String containing text
     * @param x centre x-coordinate of text
     * @param y centre y-coordinate of text
     */
    protected void addText(String key, double x, double y, String textContent, Font font) {
        text.put(key, new Text(font, textContent, x + pos.x, y + pos.y));
    }

    /**
     * Updates text
     * @param key String containing key for this text field
     * @param newText String containing updated text
     */
    public void updateText(String key, String newText) {
        text.get(key).updateText(newText);
    }

    /**
     * Updates text colour
     * @param key String containing key for this text field
     * @param colour colour to update text to
     */
    public void updateTextColour(String key, Colour colour) { text.get(key).updateColour(colour); }

    public BoundingBox getBoundingBox() {
        return bb;
    }
}
