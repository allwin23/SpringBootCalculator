package com.jumbotail.shipping.strategy.distance;

import com.jumbotail.shipping.dto.distance.DistanceResponse;

public interface DistanceStrategy {
    
    /**
     * Calculates the distance and estimated duration between two geographic coordinates.
     *
     * @param srcLat  Source Latitude
     * @param srcLng  Source Longitude
     * @param destLat Destination Latitude
     * @param destLng Destination Longitude
     * @return DistanceResponse containing distance in km and duration in minutes
     */
    DistanceResponse calculate(double srcLat, double srcLng, double destLat, double destLng);
}
