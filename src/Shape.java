import bagel.Drawing;
import bagel.util.Colour;
import bagel.util.Point;

public class Shape extends Drawing{

    // intermediary class to store any shapes until RenderQueue needs to draw them

    private static final String CIRCLE = "circle";
    private static final String LINE = "line";

    private final String type;

    // circle data
    private double x;
    private double y;
    private double radius;
    private final Colour colour;

    // line data
    private Point a;
    private Point b;
    private double thickness;

    /**
     * Constructor for Circle shapes
     * @param type String containing type of shape
     * @param x x-position to draw shape
     * @param y y-position to draw shape
     * @param radius radius of circle
     * @param colour colour of circle
     */
    public Shape(String type, double x, double y, double radius, Colour colour) {
        this.type = type;
        this.x = x;
        this.y = y;
        this.radius = radius;
        this.colour = colour;
    }

    /**
     * Constructor for Line shapes
     * @param type String containing shape type
     * @param a Point where line starts
     * @param b Point where lines ends
     * @param thickness thickness of line
     * @param colour colour of line
     */
    public Shape(String type, Point a, Point b, double thickness, Colour colour) {
        this.type = type;
        this.a = a;
        this.b = b;
        this.thickness = thickness;
        this.colour = colour;
    }

    /**
     * draw shapes with respective draw functions
     */
    public void draw() {
        if (type.equals(CIRCLE)) {
            drawCircle(x, y, radius, colour);
        } else if (type.equals(LINE)) {
            drawLine(a, b, thickness, colour);
        }

    }

}
