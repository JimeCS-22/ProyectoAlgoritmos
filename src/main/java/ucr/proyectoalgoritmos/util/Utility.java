package ucr.proyectoalgoritmos.util;

import ucr.proyectoalgoritmos.Domain.passanger.Passenger; // Import your Passenger class

import java.text.DecimalFormat;
import java.util.Random;

public class Utility {

    static {
        // static initialization block, currently empty
    }

    public static String format(double value){
        return new DecimalFormat("###,###,###.##").format(value);
    }
    public static String $format(double value){
        return new DecimalFormat("$###,###,###.##").format(value);
    }

    public static void fill(int[] a, int bound) {
        for (int i = 0; i < a.length; i++) {
            a[i] = new Random().nextInt(bound);
        }
    }

    public static int random(int bound) {
        return new Random().nextInt(bound);
    }

    public static int compare(Object a, Object b) {
        // Handle null cases explicitly for robustness
        if (a == null && b == null) return 0;
        if (a == null) return -1; // a is "less" than b if a is null
        if (b == null) return 1;  // a is "greater" than b if b is null

        switch (instanceOf(a, b)){
            case "Integer":
                Integer int1 = (Integer)a; Integer int2 = (Integer)b;
                return int1 < int2 ? -1 : int1 > int2 ? 1 : 0; //0 == equal
            case "String":
                String st1 = (String)a; String st2 = (String)b;
                return st1.compareTo(st2); // compareTo already returns -1, 0, or 1
            case "Character":
                Character c1 = (Character)a; Character c2 = (Character)b;
                return c1.compareTo(c2); // compareTo already returns -1, 0, or 1
            case "Passenger": // --- ADDED THIS CASE ---
                Passenger p1 = (Passenger) a;
                Passenger p2 = (Passenger) b;
                return p1.getId().compareTo(p2.getId()); // Compare passengers by their ID
            // case "EdgeWeight":
            //    EdgeWeight ew1 = (EdgeWeight) a ; EdgeWeight ew2 = (EdgeWeight) b;
            //    return compare(ew1.getEdge(), ew2.getEdge());
        }
        return 2; // Unknown type comparison - indicates a problem
    }

    private static String instanceOf(Object a, Object b) {
        if(a instanceof Integer && b instanceof Integer) return "Integer";
        if(a instanceof String && b instanceof String) return "String";
        if(a instanceof Character && b instanceof Character) return "Character";
        if (a instanceof Passenger && b instanceof Passenger) return "Passenger"; // --- ADDED THIS LINE ---
        //if (a instanceof EdgeWeight && b instanceof EdgeWeight) return "EdgeWeight";
        return "Unknown";
    }

    public static int maxArray(int[] a) {
        int max = a[0]; //first element
        for (int i = 1; i < a.length; i++) {
            if(a[i]>max){
                max=a[i];
            }
        }
        return max;
    }

    public static int[] getIntegerArray(int n) {
        int[] newArray = new int[n];
        for (int i = 0; i < n; i++) {
            newArray[i] = random(9999);
        }
        return newArray;
    }


    public static int[] copyArray(int[] a) {
        int n = a.length;
        int[] newArray = new int[n];
        for (int i = 0; i < n; i++) {
            newArray[i] = a[i];
        }
        return newArray;
    }

    public static String show(int[] a, int n) {
        String result="";
        for (int i = 0; i < n; i++) {
            result+=a[i]+" ";
        }
        return result;
    }

    public static String RandomAlphabet() {
        String[] Alphabet = {
                "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M",
                "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"
        };

        Random random = new Random();
        int randomIndex = random.nextInt(Alphabet.length);
        return Alphabet[randomIndex];
    }
}