package ucr.proyectoalgoritmos.Domain.Archivos;

import org.junit.jupiter.api.Test;
import ucr.proyectoalgoritmos.Domain.aeropuetos.Airport;
import ucr.proyectoalgoritmos.Domain.aeropuetos.AirportManager;
import ucr.proyectoalgoritmos.Domain.list.ListException;
import ucr.proyectoalgoritmos.route.RouteManager;

import java.io.IOException;

class DataLoaderTest {

    @Test
    void test (){

        AirportManager airportManager = new AirportManager();
        RouteManager routeManager = new RouteManager();
        DataLoader dataLoader = new DataLoader(airportManager , routeManager);

        try {

            System.out.println("Iniciando carga de aeropuertos");
            dataLoader.loadAirportFromJson("C:\\Users\\jimen\\OneDrive\\Escritorio\\UCR\\Estructuras\\Archivos proyecto\\airports.json.txt");
            System.out.println("Airport loading completed");

            airportManager.createAirport("CDG" , "Charles de Gaulle Airport" , "France");
            airportManager.createAirport("LHR" , "Heatrow Airport" , "United Kindom");
            airportManager.createAirport("DXB", "Dubai International Airport", "United Arab Emirates");
            airportManager.createAirport("IST", "Istanbul Airport", "Turkey");

            System.out.println("Synchronizing airports with the route graph");

            if (airportManager.getAirportCount()>0){

                for (int i = 0; i < airportManager.getAirportCount(); i++) {

                    Airport airport = (Airport) airportManager.getAllAirports().get(i);
                    routeManager.getGraph().addVertex(airport.getCode());

                }

                System.out.println("AirportManager Airports");

            }else {

                System.out.println("No airports were loaded.");
            }

            System.out.println("List of Airports");
            airportManager.listAirports(false , true);
            airportManager.setAirportStatus("MAD" , Airport.AirportStatus.INACTIVE);
            airportManager.listAirports(false , true);

            System.out.println("Shortest route");
            String originCode1 = "SJO";
            String destinaonCode1 = "PTY";
            int shortesDistance1 = routeManager.calculateShortestRoute(originCode1 , destinaonCode1);

            if (shortesDistance1 != Integer.MAX_VALUE){

                System.out.println("Distance " + shortesDistance1 + "km");

            }else {

                System.out.println("No route found " + originCode1 + "to" + destinaonCode1);

            }

        } catch (ListException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }

}