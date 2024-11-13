package com.investmetic.domain.strategy.repository;

import com.investmetic.domain.strategy.model.entity.StockType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StockTypeRepository extends JpaRepository<StockType, Long> {

}
