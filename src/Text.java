import bagel.DrawOptions;
import bagel.Font;
import bagel.util.Colour;

public class Text implements MenuItem {

    private static final Colour DEFAULT_COLOUR = new Colour(255, 255, 255);
    private final double x;
    private final double y;
    private final Font font;
    private String textContent;
    private Colour colour;

    /**
     * Constructor for text object
     * @param font font to use for text
     * @param textContent String containing text to display
     * @param x x-position at centre of text
     * @param y y-position at centre of text
     */
    public Text(Font font, String textContent, double x, double y) {
        this.font = font;
        this.textContent = textContent;
        this.x = x;
        this.y = y;
        this.colour = DEFAULT_COLOUR;
    }

    public void drawAtTopLeft() {
        font.drawString(textContent, x, y, new DrawOptions().setBlendColour(colour));
    }

    @Override
    public void draw() {
        double width = font.getWidth(textContent);
        font.drawString(textContent, x - width / 2, y, new DrawOptions().setBlendColour(colour));
    }

    public void updateText(String newText) {
        textContent = newText;
    }

    public void updateColour(Colour colour) {
        this.colour = colour;
    }

    public void boxDraw() {
        double width = font.getWidth(textContent);
        font.drawString(textContent, x - width, y, new DrawOptions().setBlendColour(colour));
    }
}
