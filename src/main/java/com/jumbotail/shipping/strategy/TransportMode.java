package com.jumbotail.shipping.strategy;

/**
 * Enum representing different transport modes
 */
public enum TransportMode {
    MINI_VAN("Mini Van", 0, 100, 3.0, 40.0),      // 40 km/h
    TRUCK("Truck", 100, 500, 2.0, 60.0),         // 60 km/h
    AEROPLANE("Aeroplane", 500, Double.MAX_VALUE, 1.0, 500.0); // 500 km/h
    
    private final String name;
    private final double minDistance;
    private final double maxDistance;
    private final double ratePerKmPerKg; // Rate in Rs per km per kg
    private final double averageSpeed;    // Average speed in km/h
    
    TransportMode(String name, double minDistance, double maxDistance, double ratePerKmPerKg, double averageSpeed) {
        this.name = name;
        this.minDistance = minDistance;
        this.maxDistance = maxDistance;
        this.ratePerKmPerKg = ratePerKmPerKg;
        this.averageSpeed = averageSpeed;
    }
    
    /**
     * Determine transport mode based on distance
     * 
     * @param distance Distance in kilometers
     * @return Appropriate TransportMode
     */
    public static TransportMode getTransportMode(double distance) {
        if (distance < MINI_VAN.maxDistance) {
            return MINI_VAN;
        } else if (distance < TRUCK.maxDistance) {
            return TRUCK;
        } else {
            return AEROPLANE;
        }
    }
    
    /**
     * Calculate shipping charge based on distance and weight
     * 
     * @param distance Distance in kilometers
     * @param weight Weight in kilograms
     * @return Shipping charge in rupees
     */
    public double calculateCharge(double distance, double weight) {
        return distance * weight * ratePerKmPerKg;
    }
    
    public String getName() {
        return name;
    }
    
    public double getMinDistance() {
        return minDistance;
    }
    
    public double getMaxDistance() {
        return maxDistance;
    }
    
    public double getRatePerKmPerKg() {
        return ratePerKmPerKg;
    }

    public double getAverageSpeed() {
        return averageSpeed;
    }
}
