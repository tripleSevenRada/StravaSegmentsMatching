package test.gpx_to_json_requested_route;

public class LatLonPair {

    private double lat;
    private double lon;

    public LatLonPair(double lat, double lon) {
        this.lat = lat;
        this.lon = lon;
    }

    public LatLonPair(){}

    public double getLat() { return lat; }
    public double getLon() {
        return lon;
    }

    public void setLat(double lat) { this.lat = lat; }
    public void setLon(double lon) {
        this.lon = lon;
    }

    @Override
    public String toString(){
        return this.getClass().getSimpleName() + "; lat:" + lat + ", lon:" + lon;
    }

}
