package geospatial;

public class Haversine {

    //courtesy to somebody at stack overflow:-)

    public static final double EQUAT_EARTH_RADIUS = 6378.1370D;
    public static final double D2R = (Math.PI / 180D);

    /**
     *
     */
    public static double haversineInM(double lat1, double lon1, double lat2, double lon2) {
        double dlon = (lon2 - lon1) * D2R;
        double dlat = (lat2 - lat1) * D2R;
        double a = Math.pow(Math.sin(dlat / 2D), 2D) + Math.cos(lat1 * D2R) * Math.cos(lat2 * D2R) * Math.pow(Math.sin(dlon / 2D), 2D);
        double c = 2D * Math.atan2(Math.sqrt(a), Math.sqrt(1D - a));
        double d = EQUAT_EARTH_RADIUS * c;

        return d * 1000;
    }

}
