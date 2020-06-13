import bagel.util.Point;
import bagel.util.Vector2;

import java.util.Map;

import static jdk.nashorn.internal.objects.Global.Infinity;


public class Line {
    // Creates a line from two points
    // TODO: Create buildings and implement line intersection method

    private static final int COEFFICIENTS = 3; // uses three coefficients for distance to line function

    private Point point0;
    private final Point point1;
    private final double[] lineCoefficients = new double[COEFFICIENTS];
    private final Vector2 adjustment = new Vector2(-1, 0);

    public Line(Point point0, Point point1) {
        this.point0 = point0;
        this.point1 = point1;

        getLineCoefficients(); // gets line coefficients necessary to get distance from point to path
    }

    /**
     * Calculates distance from point to line
     * @param point position to get distance from
     * @return distance to line
     */
    public double DistanceToLine(Point point) {
        // the function to calculate the distance from a point to a line is defined as the following
        //             A*x + B*y + C
        // distance =  --------------
        //            (A^2 + B^2)^(1/2)
        //

        // get distance in a readable way by calculating numerator and denominator separately
        double numerator = Math.abs(lineCoefficients[0] * point.x +
                lineCoefficients[1] * point.y + lineCoefficients[2]);
        double denominator = Math.sqrt(Math.pow(lineCoefficients[0], 2) + Math.pow(lineCoefficients[1], 2));
        double distance =  numerator/denominator;

        // if distance to either point is longer than the line length itself
        // the point is beyond the line and thus the distance is the distance between the point and the end of the line
        if (point.distanceTo(point0) >= point0.distanceTo(point1)) {
            distance = point.distanceTo(point1);
        } else if (point.distanceTo(point1) >= point0.distanceTo(point1)) {
            distance = point.distanceTo(point0);
        }

        return distance;
    }

    /**
     * gets line coefficients
     */
    public void getLineCoefficients() {
        double gradient = (point1.y - point0.y)/(point1.x - point0.x); // calculate gradient
        double intercept = point0.y - point0.x * gradient; // calculate y-intercept
        lineCoefficients[0] = gradient; // gradient of line
        lineCoefficients[1] = -1; // always -1
        lineCoefficients[2] = intercept; // intercept of line at x = 0

        if (intercept == Infinity || intercept == -Infinity) {
            point0 = point0.asVector().add(adjustment).asPoint();
            getLineCoefficients();
        }
    }
}
