package ucr.proyectoalgoritmos.Domain.Archivos;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import ucr.proyectoalgoritmos.Domain.list.ListException;
import ucr.proyectoalgoritmos.Domain.passenger.PassengerManager;
import ucr.proyectoalgoritmos.util.Utility;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class PassengerData {

    private PassengerManager passengerManager;
    private int minTotalPassengers;



    public PassengerData(PassengerManager passengerManager , int minTotalPassengers){
        this.passengerManager = passengerManager;
        this.minTotalPassengers = minTotalPassengers;
    }

    //Cargar los pasajeros
    public void loadPassengersFromJson(String filePath , int Id){

        System.out.println("Loading passengers");
        boolean loadedFromJson = false;
        int passengersLoaded = 0;

        try{

            String content = new String(Files.readAllBytes(Paths.get(filePath)));
            JsonElement parsedElement = JsonParser.parseString(content);
            JsonObject jsonObject = parsedElement.getAsJsonObject();
            JsonArray passengersArray = jsonObject.getAsJsonArray("passengers");

            if (passengersArray != null){

                for (int i = 0; i < passengersArray.size(); i++) {

                    JsonObject passengerJson = passengersArray.get(i).getAsJsonObject();
                    String id = passengerJson.has("id") ? passengerJson.get("id").getAsString() : Utility.RandomId();
                    String name = passengerJson.get("name").getAsString();
                    String nationality = passengerJson.get("nationality").getAsString();

                    passengerManager.registerPassenger(id , name , nationality);
                    passengersLoaded ++;

                }

                loadedFromJson = true;

            }

        } catch (IOException | ListException e) {
            throw new RuntimeException(e);
        }

        int currentPassengerCount = passengerManager.getPassengerCount();
        int passengersToGenerate = minTotalPassengers -currentPassengerCount;

        if (passengersToGenerate > 0) {

            for (int i = 0 ;i<passengersToGenerate; i++){

                String id = Utility.RandomId();
                String name = Utility.RandomNames();
                String nationality = Utility.RandomNationalities();

                try {
                    passengerManager.registerPassenger(id , name , nationality);
                } catch (ListException e) {
                    throw new RuntimeException(e);
                }
            }
        }

    }


}
