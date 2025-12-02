package com.expensetracker.mapper;

import com.expensetracker.config.CentralMappingConfig;
import com.expensetracker.dto.expense.ExpenseRequest;
import com.expensetracker.dto.expense.ExpenseResponse;
import com.expensetracker.entity.Expense;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = CentralMappingConfig.class)
public interface ExpenseMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "category", ignore = true)
    Expense toEntity(ExpenseRequest request);

    @Mapping(source = "category.id", target = "categoryId")
    ExpenseResponse toResponse(Expense expense);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "category", ignore = true)
    void updateEntityFromRequest(ExpenseRequest request, @MappingTarget Expense expense);

}
