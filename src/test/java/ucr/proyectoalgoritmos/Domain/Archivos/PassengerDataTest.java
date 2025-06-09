package ucr.proyectoalgoritmos.Domain.Archivos;

import org.junit.jupiter.api.Test;
import ucr.proyectoalgoritmos.Domain.list.ListException;
import ucr.proyectoalgoritmos.Domain.passanger.PassengerManager;
import ucr.proyectoalgoritmos.util.Utility;

import static org.junit.jupiter.api.Assertions.*;

class PassengerDataTest {

    private static final int minTotalPassengers = 25 ;

    @Test
    void test(){

        PassengerManager passengerManager =new PassengerManager();
        PassengerData passengerData = new PassengerData(passengerManager , minTotalPassengers);


        try {

            for (int i = 0; i < 5; i++) {

                passengerManager.registerPassenger(Utility.RandomId() , Utility.RandomNames() , Utility.RandomNationalities());

            }

        } catch (ListException e) {
            throw new RuntimeException(e);
        }


        System.out.println("PASSENGER MANAGEMENT");
        passengerData.loadPassengersFromJson("data/passengers.json" , minTotalPassengers);
        System.out.println(passengerManager.passengers.inOrder());


    }

}