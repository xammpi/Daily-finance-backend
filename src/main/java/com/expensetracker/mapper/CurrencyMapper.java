package com.expensetracker.mapper;

import com.expensetracker.config.CentralMappingConfig;
import com.expensetracker.dto.currency.CurrencyResponse;
import com.expensetracker.entity.Currency;
import org.mapstruct.Mapper;

@Mapper(config = CentralMappingConfig.class)
public interface CurrencyMapper {

    CurrencyResponse toResponse(Currency currency);
}
