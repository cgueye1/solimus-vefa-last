package com.wakana.vefa.dto.response;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MeetStatsResponse {
    private long upcoming;
    private long past;
    private long totalParticipants;
}
