package com.expensetracker.service;

import com.expensetracker.dto.category.CategoryRequest;
import com.expensetracker.dto.category.CategoryResponse;
import com.expensetracker.entity.Category;
import com.expensetracker.entity.User;
import com.expensetracker.exception.BadRequestException;
import com.expensetracker.exception.ResourceNotFoundException;
import com.expensetracker.mapper.CategoryMapper;
import com.expensetracker.repository.CategoryRepository;
import com.expensetracker.repository.UserRepository;
import com.expensetracker.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CategoryMapper categoryMapper;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @Mock
    private UserPrincipal userPrincipal;

    @InjectMocks
    private CategoryService categoryService;

    private User user;
    private Category category;
    private CategoryRequest categoryRequest;
    private CategoryResponse categoryResponse;
    private final Long userId = 1L;
    private final Long categoryId = 1L;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(userId);
        user.setUsername("testuser");

        category = new Category();
        category.setId(categoryId);
        category.setName("Groceries");
        category.setDescription("Food and household items");
        category.setUser(user);

        categoryRequest = new CategoryRequest();
        categoryRequest.setName("Groceries");
        categoryRequest.setDescription("Food and household items");

        categoryResponse = new CategoryResponse(
                categoryId,
                "Groceries",
                "Food and household items",
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        // Mock security context
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userPrincipal);
        when(userPrincipal.getId()).thenReturn(userId);
    }

    @Test
    void createCategory_Success() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(categoryMapper.toEntity(categoryRequest)).thenReturn(category);
        when(categoryRepository.save(any(Category.class))).thenReturn(category);
        when(categoryMapper.toResponse(category)).thenReturn(categoryResponse);

        // Act
        CategoryResponse response = categoryService.createCategory(categoryRequest);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(categoryId);
        assertThat(response.name()).isEqualTo("Groceries");
        assertThat(response.description()).isEqualTo("Food and household items");

        verify(userRepository).findById(userId);
        verify(categoryMapper).toEntity(categoryRequest);
        verify(categoryRepository).save(any(Category.class));
        verify(categoryMapper).toResponse(category);
    }

    @Test
    void createCategory_UserNotFound_ThrowsResourceNotFoundException() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> categoryService.createCategory(categoryRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User not found");

        verify(userRepository).findById(userId);
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void getUserCategories_Success() {
        // Arrange
        Category category2 = new Category();
        category2.setId(2L);
        category2.setName("Transport");
        category2.setDescription("Car and public transport");
        category2.setUser(user);

        List<Category> categories = Arrays.asList(category, category2);

        CategoryResponse categoryResponse2 = new CategoryResponse(
                2L,
                "Transport",
                "Car and public transport",
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(categoryRepository.findByUserId(userId)).thenReturn(categories);
        when(categoryMapper.toResponse(category)).thenReturn(categoryResponse);
        when(categoryMapper.toResponse(category2)).thenReturn(categoryResponse2);

        // Act
        List<CategoryResponse> responses = categoryService.getUserCategories();

        // Assert
        assertThat(responses).isNotNull();
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).name()).isEqualTo("Groceries");
        assertThat(responses.get(1).name()).isEqualTo("Transport");

        verify(categoryRepository).findByUserId(userId);
        verify(categoryMapper, times(2)).toResponse(any(Category.class));
    }

    @Test
    void getUserCategories_EmptyList_Success() {
        // Arrange
        when(categoryRepository.findByUserId(userId)).thenReturn(Collections.emptyList());

        // Act
        List<CategoryResponse> responses = categoryService.getUserCategories();

        // Assert
        assertThat(responses).isNotNull();
        assertThat(responses).isEmpty();

        verify(categoryRepository).findByUserId(userId);
        verify(categoryMapper, never()).toResponse(any(Category.class));
    }

    @Test
    void getCategoryById_Success() {
        // Arrange
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(categoryMapper.toResponse(category)).thenReturn(categoryResponse);

        // Act
        CategoryResponse response = categoryService.getCategoryById(categoryId);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(categoryId);
        assertThat(response.name()).isEqualTo("Groceries");

        verify(categoryRepository).findById(categoryId);
        verify(categoryMapper).toResponse(category);
    }

    @Test
    void getCategoryById_CategoryNotFound_ThrowsResourceNotFoundException() {
        // Arrange
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> categoryService.getCategoryById(categoryId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Category not found");

        verify(categoryRepository).findById(categoryId);
        verify(categoryMapper, never()).toResponse(any(Category.class));
    }

    @Test
    void getCategoryById_CategoryDoesNotBelongToUser_ThrowsBadRequestException() {
        // Arrange
        User otherUser = new User();
        otherUser.setId(2L);
        category.setUser(otherUser);

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));

        // Act & Assert
        assertThatThrownBy(() -> categoryService.getCategoryById(categoryId))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Category does not belong to current user");

        verify(categoryRepository).findById(categoryId);
        verify(categoryMapper, never()).toResponse(any(Category.class));
    }

    @Test
    void updateCategory_Success() {
        // Arrange
        CategoryRequest updateRequest = new CategoryRequest();
        updateRequest.setName("Updated Groceries");
        updateRequest.setDescription("Updated description");

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        doNothing().when(categoryMapper).updateEntityFromRequest(updateRequest, category);
        when(categoryRepository.save(category)).thenReturn(category);
        when(categoryMapper.toResponse(category)).thenReturn(categoryResponse);

        // Act
        CategoryResponse response = categoryService.updateCategory(categoryId, updateRequest);

        // Assert
        assertThat(response).isNotNull();

        verify(categoryRepository).findById(categoryId);
        verify(categoryMapper).updateEntityFromRequest(updateRequest, category);
        verify(categoryRepository).save(category);
        verify(categoryMapper).toResponse(category);
    }

    @Test
    void updateCategory_CategoryNotFound_ThrowsResourceNotFoundException() {
        // Arrange
        CategoryRequest updateRequest = new CategoryRequest();
        updateRequest.setName("Updated Groceries");

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> categoryService.updateCategory(categoryId, updateRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Category not found");

        verify(categoryRepository).findById(categoryId);
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void updateCategory_CategoryDoesNotBelongToUser_ThrowsBadRequestException() {
        // Arrange
        User otherUser = new User();
        otherUser.setId(2L);
        category.setUser(otherUser);

        CategoryRequest updateRequest = new CategoryRequest();
        updateRequest.setName("Updated Groceries");

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));

        // Act & Assert
        assertThatThrownBy(() -> categoryService.updateCategory(categoryId, updateRequest))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Category does not belong to current user");

        verify(categoryRepository).findById(categoryId);
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void deleteCategory_Success() {
        // Arrange
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        doNothing().when(categoryRepository).delete(category);

        // Act
        categoryService.deleteCategory(categoryId);

        // Assert
        verify(categoryRepository).findById(categoryId);
        verify(categoryRepository).delete(category);
    }

    @Test
    void deleteCategory_CategoryNotFound_ThrowsResourceNotFoundException() {
        // Arrange
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> categoryService.deleteCategory(categoryId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Category not found");

        verify(categoryRepository).findById(categoryId);
        verify(categoryRepository, never()).delete(any(Category.class));
    }

    @Test
    void deleteCategory_CategoryDoesNotBelongToUser_ThrowsBadRequestException() {
        // Arrange
        User otherUser = new User();
        otherUser.setId(2L);
        category.setUser(otherUser);

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));

        // Act & Assert
        assertThatThrownBy(() -> categoryService.deleteCategory(categoryId))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Category does not belong to current user");

        verify(categoryRepository).findById(categoryId);
        verify(categoryRepository, never()).delete(any(Category.class));
    }
}
