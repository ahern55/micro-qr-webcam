import georegression.struct.shapes.Polygon2D_F64;
import org.opencv.core.Point;

public class Position {
    public double heading;

    public double x;

    public double y;

    private Position(double x, double y, double heading) {
        this.x = x;
        this.y = y;
        this.heading = heading;
    }

    // https://stackoverflow.com/questions/13002979/how-to-calculate-rotation-angle-from-rectangle-points
    // "If something sounds useful, someone has done it before you"
    /*  General case solution for a rectangle
     *
     *  Given coordinages of [x1, y1, x2, y2, x3, y3, x4, y4]
     *  where the corners are:
     *            top left    : x1, y1
     *            top right   : x2, y2
     *            bottom right: x3, y3
     *            bottom left : x4, y4
     *
     *  The centre is the average top left and bottom right coords:
     *  center: (x1 + x3) / 2 and (y1 + y3) / 2
     *
     *  Clockwise rotation: Math.atan((x1 - x4)/(y1 - y4)) with
     *  adjustment for the quadrant the angle is in.
     *
     *  Note that if using page coordinates, y is +ve down the page which
     *  is the reverse of the mathematic sense so y page coordinages
     *  should be multiplied by -1 before being given to the function.
     *  (e.g. a page y of 400 should be -400).
     */
    public static Position fromPolygon(Polygon2D_F64 bounds) {
        // Center as avg of top left and bot right
        Point center = new Point((bounds.get(0).x + bounds.get(2).x) / 2, (bounds.get(0).y + bounds.get(2).y) / 2);
        // Get differences top left minus bottom right
        double[] diffs = { bounds.get(2).x - bounds.get(3).x, bounds.get(2).y - bounds.get(3).y };

        // Get rotation in degrees
        double rotation = Math.atan(diffs[0] / diffs[1]) * 180 / Math.PI;

        // Adjust for 2nd and 3rd quadrants (diff y is -ve)
        if (diffs[1] < 0)
        {
            rotation += 180;
        }
        // Adjust for 4th quadrant
        // diff x is -ve, diff y is +ve
        else if (diffs[0] < 0)
        {
            rotation += 360;
        }

        return new Position(center.x, center.y, rotation);
    }
}
