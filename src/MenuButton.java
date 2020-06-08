import bagel.Drawing;
import bagel.Font;
import bagel.Input;
import bagel.util.Colour;
import bagel.util.Point;

public class MenuButton implements MenuItem {

    private static final Font DEFAULT_FONT = new Font("res/fonts/DejaVuSans-Bold.ttf", 48);
    private double x;
    private double y;
    private double width;
    private double height;
    private String text;
    private int link;
    private BoundingBox bb;
    private double fontWidth;


    /**
     * Menu button without link
     * @param x x-position of button
     * @param y y-position of button
     * @param width width of button
     * @param height height of button
     * @param text text to display in button
     */
    public MenuButton(double x, double y, double width, double height, String text) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.text = text;
        this.link = -1;
        this.bb = new BoundingBox(x, y, width, height);
        fontWidth = DEFAULT_FONT.getWidth(text);
    }

    /**
     * Menu button with link
     * @param x x-position of button
     * @param y y-position of button
     * @param width width of button
     * @param height height of button
     * @param text text to display in button
     * @param link links to another section or level
     */
    public MenuButton(double x, double y, double width, double height, String text, int link) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.text = text;
        this.link = link;
        this.bb = new BoundingBox(x - width / 2, y - height / 2, width, height);
        fontWidth = DEFAULT_FONT.getWidth(text);
    }

    public boolean isOver(Input input) {
        return bb.isMouseOver(input);
    }

    public int clicked() {
        return link;
    }

    @Override
    public void draw() {
        Drawing.drawRectangle(new Point(x - width / 2, y - height / 2), width, height, Colour.RED);
        DEFAULT_FONT.drawString(text, x - fontWidth/2, y + 24);
    }
}
