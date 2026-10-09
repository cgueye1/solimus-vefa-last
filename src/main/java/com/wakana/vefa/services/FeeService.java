package com.wakana.vefa.services;
import com.wakana.vefa.enums.FeeEnum;
import com.wakana.vefa.model.Fee;

import java.util.List;

public interface FeeService { 
                             
    Fee create(FeeEnum name, double fee);
    Fee getById(Long id)     ;      
    Fee getPlanByName(FeeEnum name);
    List<Fee> getAll();
    Fee update(Long id, double fee);


}
