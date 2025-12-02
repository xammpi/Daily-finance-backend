package com.expensetracker.controller;

import com.expensetracker.dto.category.CategoryRequest;
import com.expensetracker.dto.category.CategoryResponse;
import com.expensetracker.security.JwtAuthenticationFilter;
import com.expensetracker.security.JwtTokenProvider;
import com.expensetracker.service.CategoryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoryController.class)
@AutoConfigureMockMvc(addFilters = false)
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CategoryService categoryService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    void createCategory_WithValidRequest_ReturnsCreatedCategory() throws Exception {
        // Given
        CategoryRequest request = new CategoryRequest();
        request.setName("Food");
        request.setDescription("Food expenses");

        CategoryResponse response = new CategoryResponse(
                1L,
                "Food",
                "Food expenses",
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(categoryService.createCategory(any(CategoryRequest.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Food"))
                .andExpect(jsonPath("$.description").value("Food expenses"));
    }

    @Test
    void createCategory_WithBlankName_ReturnsBadRequest() throws Exception {
        // Given
        CategoryRequest request = new CategoryRequest();
        request.setName("");
        request.setDescription("Food expenses");

        // When & Then
        mockMvc.perform(post("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createCategory_WithNullName_ReturnsBadRequest() throws Exception {
        // Given
        CategoryRequest request = new CategoryRequest();
        request.setName(null);
        request.setDescription("Food expenses");

        // When & Then
        mockMvc.perform(post("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getUserCategories_ReturnsListOfCategories() throws Exception {
        // Given
        List<CategoryResponse> categories = Arrays.asList(
                new CategoryResponse(1L, "Food", "Food expenses", LocalDateTime.now(), LocalDateTime.now()),
                new CategoryResponse(2L, "Transport", "Transport expenses", LocalDateTime.now(), LocalDateTime.now())
        );

        when(categoryService.getUserCategories()).thenReturn(categories);

        // When & Then
        mockMvc.perform(get("/api/v1/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Food"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("Transport"));
    }

    @Test
    void getCategoryById_WithValidId_ReturnsCategory() throws Exception {
        // Given
        CategoryResponse response = new CategoryResponse(
                1L,
                "Food",
                "Food expenses",
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(categoryService.getCategoryById(1L)).thenReturn(response);

        // When & Then
        mockMvc.perform(get("/api/v1/categories/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Food"))
                .andExpect(jsonPath("$.description").value("Food expenses"));
    }

    @Test
    void updateCategory_WithValidRequest_ReturnsUpdatedCategory() throws Exception {
        // Given
        CategoryRequest request = new CategoryRequest();
        request.setName("Updated Food");
        request.setDescription("Updated description");

        CategoryResponse response = new CategoryResponse(
                1L,
                "Updated Food",
                "Updated description",
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(categoryService.updateCategory(eq(1L), any(CategoryRequest.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(put("/api/v1/categories/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Updated Food"))
                .andExpect(jsonPath("$.description").value("Updated description"));
    }

    @Test
    void updateCategory_WithBlankName_ReturnsBadRequest() throws Exception {
        // Given
        CategoryRequest request = new CategoryRequest();
        request.setName("");
        request.setDescription("Updated description");

        // When & Then
        mockMvc.perform(put("/api/v1/categories/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteCategory_WithValidId_ReturnsNoContent() throws Exception {
        // Given
        doNothing().when(categoryService).deleteCategory(1L);

        // When & Then
        mockMvc.perform(delete("/api/v1/categories/1"))
                .andExpect(status().isNoContent());

        verify(categoryService, times(1)).deleteCategory(1L);
    }
}
