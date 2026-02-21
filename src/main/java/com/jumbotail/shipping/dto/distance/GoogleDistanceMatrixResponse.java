package com.jumbotail.shipping.dto.distance;

import lombok.Data;
import java.util.List;

@Data
public class GoogleDistanceMatrixResponse {
    private List<Row> rows;
    private String status;

    @Data
    public static class Row {
        private List<Element> elements;
    }

    @Data
    public static class Element {
        private Distance distance;
        private Duration duration;
        private String status;
    }

    @Data
    public static class Distance {
        private Integer value; // meters
        private String text;
    }

    @Data
    public static class Duration {
        private Integer value; // seconds
        private String text;
    }
}
