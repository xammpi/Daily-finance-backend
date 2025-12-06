package com.expensetracker.mapper;

import com.expensetracker.config.CentralMappingConfig;
import com.expensetracker.dto.category.CategoryRequest;
import com.expensetracker.dto.category.CategoryResponse;
import com.expensetracker.entity.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = CentralMappingConfig.class)
public interface CategoryMapper {

    CategoryResponse toResponse(Category category);
}
