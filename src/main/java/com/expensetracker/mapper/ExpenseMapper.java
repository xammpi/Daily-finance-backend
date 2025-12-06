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

    // No toEntity - use constructor: new Expense(amount, date, description, user, category)
    // No updateEntityFromRequest - use behavior method: expense.updateDetails(date, description, category)

    @Mapping(source = "category.id", target = "categoryId")
    ExpenseResponse toResponse(Expense expense);

}
