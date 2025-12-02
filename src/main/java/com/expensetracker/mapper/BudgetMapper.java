package com.expensetracker.mapper;

import com.expensetracker.config.CentralMappingConfig;
import com.expensetracker.dto.budget.BudgetRequest;
import com.expensetracker.dto.budget.BudgetResponse;
import com.expensetracker.entity.Budget;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = CentralMappingConfig.class)
public interface BudgetMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "user", ignore = true)
    Budget toEntity(BudgetRequest request);

    @Mapping(source = "category.id", target = "categoryId")
    @Mapping(source = "category.name", target = "categoryName")
    BudgetResponse toResponse(Budget budget);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "user", ignore = true)
    void updateEntityFromRequest(BudgetRequest request, @MappingTarget Budget budget);
}
