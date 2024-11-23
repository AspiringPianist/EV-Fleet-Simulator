public class ChargingStation extends GameObject {
    /*
     * Charging Station Class -
     * Access to EV properties like battery level, stops the EV there until battery
     * is full
     * EV always has route to nearest charging station
     * It is not an obstacle, vehicle can pass through it
     * Should have access to the current EV object, and EV also has access to it
     */
    //TODO: getDetails()
    public ChargingStation(int x, int y) { 
        super(x, y);
    }

    @Override
    public String getDetails() {
        return location.x + " " + location.y; //pr something..this is TODO
    }

    public void chargeVehicle(EV vehicle) {
        System.out.println("Received EV Type : " + vehicle.type + " | Location (" + location.x + ", " + location.y + ")");
        // charge over time (time concept)
        while (!vehicle.fullCharge())
            vehicle.charge();
    }
}
