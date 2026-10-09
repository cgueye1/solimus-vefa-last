package com.wakana.vefa.dto;

import lombok.Data;

@Data
public class PaiementRequest {
    private  String ref;
    private  String price;
    private  String itemName;
    private  String commandeName;
    
    private long subscriptionId;
    private int installmentCount;
    

}
