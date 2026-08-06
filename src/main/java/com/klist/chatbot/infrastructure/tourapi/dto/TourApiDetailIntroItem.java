package com.klist.chatbot.infrastructure.tourapi.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TourApiDetailIntroItem(
        @JsonProperty("contentid") String contentid,
        @JsonProperty("contenttypeid") String contenttypeid,
        @JsonProperty("usetime") String usetime,
        @JsonProperty("usetimeculture") String usetimeculture,
        @JsonProperty("usefee") String usefee,
        @JsonProperty("parkingfee") String parkingfee,
        @JsonProperty("eventstartdate") String eventstartdate,
        @JsonProperty("eventenddate") String eventenddate,
        @JsonProperty("playtime") String playtime,
        @JsonProperty("usetimefestival") String usetimefestival,
        @JsonProperty("bookingplace") String bookingplace,
        @JsonProperty("eventhomepage") String eventhomepage,
        @JsonProperty("schedule") String schedule,
        @JsonProperty("taketime") String taketime,
        @JsonProperty("openperiod") String openperiod,
        @JsonProperty("usetimeleports") String usetimeleports,
        @JsonProperty("usefeeleports") String usefeeleports,
        @JsonProperty("parkingfeeleports") String parkingfeeleports,
        @JsonProperty("reservation") String reservation,
        @JsonProperty("checkintime") String checkintime,
        @JsonProperty("checkouttime") String checkouttime,
        @JsonProperty("reservationlodging") String reservationlodging,
        @JsonProperty("reservationurl") String reservationurl,
        @JsonProperty("opentime") String opentime,
        @JsonProperty("saleitemcost") String saleitemcost,
        @JsonProperty("opentimefood") String opentimefood,
        @JsonProperty("reservationfood") String reservationfood
) {
}
