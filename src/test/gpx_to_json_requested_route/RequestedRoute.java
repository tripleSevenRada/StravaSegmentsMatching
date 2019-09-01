package test.gpx_to_json_requested_route;

import test.gpx_to_json_requested_route.ActivityType;
import test.gpx_to_json_requested_route.LatLonPair;
import test.gpx_to_json_requested_route.MatchingScenario;

import java.util.List;

public class RequestedRoute {

    private List<LatLonPair> locations;
    private ActivityType type;
    private MatchingScenario matchingScenario;
    private String token;

    public RequestedRoute(List<LatLonPair> locations,
                          ActivityType type,
                          MatchingScenario matchingScenario,
                          String token) {
        this.locations = locations;
        this.type = type;
        this.matchingScenario = matchingScenario;
        this.token = token;
    }

    // in web service
    // act t = case t of "ride" -> Riding; "run" -> Running; _ -> error "unknown activity type"

    public RequestedRoute(){}

    public void setLocations(List<LatLonPair> locations) { this.locations = locations; }
    public void setType(ActivityType type) { this.type = type; }
    public void setMatchingScenario(MatchingScenario matchingScenario) { this.matchingScenario = matchingScenario; }
    public void setToken(String token) { this.token = token; }

    public List<LatLonPair> getLocations() {
        return locations;
    }
    public ActivityType getType() { return type; }
    public MatchingScenario getMatchingScenario() { return matchingScenario; }
    public String getToken(){ return token; }
}
