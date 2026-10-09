package com.wakana.vefa.repository;
import com.wakana.vefa.enums.FeeEnum;
import com.wakana.vefa.model.Fee;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface FeeRepository extends JpaRepository<Fee, Long> {
    Optional<Fee> findByName(FeeEnum name);
    
}
