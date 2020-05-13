import bagel.Input;
import bagel.util.Point;

public class BoundingBox {

    private double x;
    private double y;
    private final double width;
    private final double height;
    private final Clickable clickable;

    /**
     * Constructor for BoundingBox
     * @param x x-coordinate at top left of bounding box
     * @param y y-coordinate at top left of bounding box
     * @param width width of bounding box
     * @param height height of bounding box
     */
    public BoundingBox(double x, double y, double width, double height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        clickable = null;
    }

    /**
     * Updates x and y coordinate of bounding box
     * @param x new x-coordinate
     * @param y new y-coordinate
     */
    public void updatePosition(double x, double y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Determines if mouse is over bounding box
     * @param input user-defined input
     * @return boolean if mouse is over bounding box
     */
    public boolean isMouseOver(Input input) {
        Point position = input.getMousePosition();
        if (position.x > x && position.x < x + width) {
            return position.y > y && position.y < y + height;
        }
        return false;
    }

}
