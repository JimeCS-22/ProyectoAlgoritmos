package ucr.proyectoalgoritmos.Domain.route;


import java.util.List;

public class RouteListWrapper {
    private List<Route> routes;

    public RouteListWrapper() {}

    public RouteListWrapper(List<Route> routes) {
        this.routes = routes;
    }

    public List<Route> getRoutes() {
        return routes;
    }

    public void setRoutes(List<Route> routes) {
        this.routes = routes;
    }

    @Override
    public String toString() {
        return "RouteListWrapper{" +
                "routes=" + routes +
                '}';
    }
}