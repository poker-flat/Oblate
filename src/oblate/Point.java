/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package oblate;

/**
 *
 * @author thales
 */
public class Point {
    /**
     * Fields
     */
    double _x;
    double _y;

    /**
     * Constants: Radius
     * EARTH_RADIUS_MI - in miles (According to IUGG)
     * EARTH_RADIUS_FT - derived from <EARTH_RADIUS_MI>
     * EARTH_RADIUS_KM - in kilometers (According to IUGG)
     * EARTH_RADIUS_M  - derived from <EARTH_RADIUS_KM>
     * EARTH_RADIUS_NMI - in nautical miles (According to IUGG)
     * EARTH_RADIUS - default radius
     * DEG2RAD - factor to convert degrees to radians (PI/180)
     * RAD2DEG - factor to convert radians to degrees (180/PI)
     */
    public static final double EARTH_RADIUS_MI = 3958.761;
    public static final double EARTH_RADIUS_FT = EARTH_RADIUS_MI * 5280.0;
    public static final double EARTH_RADIUS_KM = 6371.009;
    public static final double EARTH_RADIUS_M = EARTH_RADIUS_KM * 1000.0;
    public static final double EARTH_RADIUS_NMI = 3440.069;
    public static final double EARTH_RADIUS = EARTH_RADIUS_KM;
    public static final double DEG2RAD =  0.01745329252;
    public static final double RAD2DEG = 57.29577951308;

    public Point(double lon, double lat) {
        _x = lon;
        _y = lat;
    }

    double geoDistanceTo(Point point) {
        double[] x = new double[2];
        double[] y = new double[2];
        double radius = EARTH_RADIUS_KM;

        x[0] = this._x * DEG2RAD;
        x[1] = point._x * DEG2RAD;
        y[0] = this._y * DEG2RAD;
        y[1] = point._y * DEG2RAD;

        double a = Math.pow( Math.sin(( y[1]-y[0] ) / 2.0 ), 2.0);
        double b = Math.pow( Math.sin(( x[1]-x[0] ) / 2.0 ), 2.0);
        double c = Math.pow(( a + Math.cos( y[1] ) * Math.cos( y[0] ) * b ), 0.5);

        return 2.0 * Math.asin( c ) * radius;
    }

    public double geoBearingTo(Point point) {
        double[] x = new double[2];
        double[] y = new double[2];
        double adjust;

        x[0] = this._x * DEG2RAD;
        x[1] = point._x * DEG2RAD;
        y[0] = this._y * DEG2RAD;
        y[1] = point._y * DEG2RAD;

        double a = Math.cos(y[1]) * Math.sin(x[1] - x[0]);
        double b = Math.cos(y[0]) * Math.sin(y[1]) - Math.sin(y[0]) * Math.cos(y[1]) * Math.cos(x[1] - x[0]);

        if (a == 0.0 && b == 0) {
            return 0.0;
        }

        if (b == 0.0) {
            if (a < 0.0) {
                return 270.0;
            }
            else {
                return 90.0;
            }
        }

        if (b < 0) {
            adjust = Math.PI;
        }
        else {
            if (a < 0) {
                adjust = 2.0 * Math.PI;
            }
            else {
                adjust = 0.0;
            }
        }
        return (Math.atan(a/b) + adjust) * RAD2DEG;
    }

    public Point geoWaypoint(double distance, double bearing) {
        Point wp = new Point(0, 0);
        double radius = EARTH_RADIUS_KM;
        double x = this._x * DEG2RAD;
        double y = this._y * DEG2RAD;
        double radBearing = bearing * DEG2RAD;

        // Convert arc distance to radians
        double c = distance / radius;

        wp._y = Math.asin( Math.sin(y) * Math.cos(c) + Math.cos(y) * Math.sin(c) * Math.cos(radBearing)) * RAD2DEG;

        double a = Math.sin(c) * Math.sin(radBearing);
        double b = Math.cos(y) * Math.cos(c) - Math.sin(y) * Math.sin(c) * Math.cos(radBearing);

        if (b == 0) {
            wp._x = this._x;
        }
        else {
            wp._x = this._x + Math.atan(a/b) * RAD2DEG;
        }

        return wp;
    }
}
