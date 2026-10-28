
package com.tikit.seat.repository;

import com.tikit.seat.entity.TemplateSeat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TemplateSeatRepository extends JpaRepository<TemplateSeat, Long> {

    List<TemplateSeat> findByTemplateId(Long templateId);
}
