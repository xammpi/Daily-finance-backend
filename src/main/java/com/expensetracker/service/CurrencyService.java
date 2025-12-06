package com.expensetracker.service;

import com.expensetracker.dto.currency.CurrencyResponse;
import com.expensetracker.mapper.CurrencyMapper;
import com.expensetracker.repository.CurrencyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CurrencyService {

    private final CurrencyRepository currencyRepository;
    private final CurrencyMapper currencyMapper;

    /**
     * Get all available currencies
     * @return list of all currencies
     */
    @Transactional(readOnly = true)
    public List<CurrencyResponse> getAllCurrencies() {
        return currencyRepository.findAll()
                .stream()
                .map(currencyMapper::toResponse)
                .collect(Collectors.toList());
    }
}
