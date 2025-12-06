package com.expensetracker.controller;

import com.expensetracker.dto.currency.CurrencyResponse;
import com.expensetracker.service.CurrencyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/currencies")
@RequiredArgsConstructor
@Tag(name = "Currencies", description = "Currency management endpoints")
public class CurrencyController {

    private final CurrencyService currencyService;

    @GetMapping
    @Operation(
            summary = "Get all currencies",
            description = "Returns a list of all available currencies with their codes, names, and symbols"
    )
    public ResponseEntity<List<CurrencyResponse>> getAllCurrencies() {
        return ResponseEntity.ok(currencyService.getAllCurrencies());
    }
}
