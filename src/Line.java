import bagel.util.Point;

import java.util.Map;


public class Line {
    private final Point point0;
    private final Point point1;
    private final double[] lineCoefficients = new double[3];

    public Line(Point point0, Point point1) {
        this.point0 = point0;
        this.point1 = point1;
        getLineCoefficients();
        System.out.printf("%s %s\n", point0.toString(), point1.toString());
        System.out.printf("%f*x + %f*y + %f\n", lineCoefficients[0], lineCoefficients[1], lineCoefficients[2]);
    }

    public double DistanceToLine(Point point) {

        double numerator = Math.abs(lineCoefficients[0] * point.x +
                lineCoefficients[1] * point.y + lineCoefficients[2]);

        double denominator = Math.sqrt(Math.pow(lineCoefficients[0], 2) + Math.pow(lineCoefficients[1], 2));

        double distance =  numerator/denominator;

        if (point.distanceTo(point0) >= point0.distanceTo(point1)) {
            distance = point.distanceTo(point1);
        } else if (point.distanceTo(point1) >= point0.distanceTo(point1)) {
            distance = point.distanceTo(point0);
        }

        return distance;
    }


    public void getLineCoefficients() {
        double gradient = (point1.y-point0.y)/(point1.x-point0.x);
        double intercept = point0.y - point0.x * gradient;
        lineCoefficients[0] = gradient;
        lineCoefficients[1] = -1;
        lineCoefficients[2] = intercept;
    }
}
