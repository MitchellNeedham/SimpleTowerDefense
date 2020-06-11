import bagel.Input;
import bagel.util.Point;

public class BoundingBox {



    /**
     * This class creates a bounding box based on set specifications, rather than image dimensions
     *
     * It's sole purpose is to determine if a mouse is over the object, though it could adapted for more uses
     */

    //-------------------------BOUNDING BOX PROPERTIES-------------------------//

    private final double x;
    private final double y;
    private final double width;
    private final double height;

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
    }

    /**
     * Determines if mouse is over bounding box
     * @param input user-defined input
     * @return boolean if mouse is over bounding box
     */
    public boolean isMouseOver(Input input) {
        Point position = input.getMousePosition();
        return (position.x > x && position.x < x + width && position.y > y && position.y < y + height);
    }
}
