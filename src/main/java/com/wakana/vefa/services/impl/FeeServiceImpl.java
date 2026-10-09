package com.wakana.vefa.services.impl;
import com.wakana.vefa.enums.FeeEnum;
import com.wakana.vefa.model.Fee;
import com.wakana.vefa.repository.FeeRepository;
import com.wakana.vefa.services.FeeService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class FeeServiceImpl implements FeeService {
    private final FeeRepository feeRepository;

    @Override
    public Fee create(FeeEnum name, double fee) {
        Fee feeobj = new Fee();
        feeobj.setName(name);
        feeobj.setFee(fee);
        return feeRepository.save(feeobj);
    }

    @Override
    public Fee getPlanByName(FeeEnum name) {
        return feeRepository.findByName(name).orElse(null);
    }

    @Override
    public List<Fee> getAll() {

        return feeRepository.findAll();
    }

    @Override
    public Fee update(Long id, double fee) {
        Fee existedFee = getById(id);
        existedFee.setFee(fee);
        return feeRepository.save(existedFee);
    }

    @Override
    public Fee getById(Long id) {
        return feeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Fee not found with id: " + id));
    }

}
