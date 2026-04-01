package com.huynh.personal_expense_be.modules.category.presentation;

import com.huynh.personal_expense_be.modules.category.application.dto.CategoryAnalysisResponse;
import com.huynh.personal_expense_be.modules.category.application.dto.CategoryResponse;
import com.huynh.personal_expense_be.modules.category.application.dto.CreateCategoryCommand;
import com.huynh.personal_expense_be.modules.category.application.dto.UpdateCategoryCommand;
import com.huynh.personal_expense_be.modules.category.application.port.in.*;
import com.huynh.personal_expense_be.modules.category.presentation.request.CreateCategoryRequest;
import com.huynh.personal_expense_be.modules.category.presentation.request.UpdateCategoryRequest;

import tools.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class CategoryControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CreateCategoryUseCase createCategoryUseCase;

    @Mock
    private GetCategoryUseCase getCategoryUseCase;

    @Mock
    private GetCategoryAnalysisUseCase getCategoryAnalysisUseCase;

    @Mock
    private UpdateCategoryUseCase updateCategoryUseCase;

    @Mock
    private DeleteCategoryUseCase deleteCategoryUseCase;

    @InjectMocks
    private CategoryController categoryController;

    private ObjectMapper objectMapper;
    private Principal mockPrincipal;

    private UUID categoryId;
    private CategoryResponse categoryResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(categoryController).build();
        objectMapper = new ObjectMapper();

        mockPrincipal = mock(Principal.class);
        lenient().when(mockPrincipal.getName()).thenReturn("user1");

        categoryId = UUID.randomUUID();
        categoryResponse = new CategoryResponse(
                categoryId,
                "Food",
                "user1",
                null,
                Instant.now(),
                Instant.now());
    }

    @Test
    void create_ShouldReturnCreatedCategory() throws Exception {
        CreateCategoryRequest request = new CreateCategoryRequest("Food", null);

        when(createCategoryUseCase.createCategory(any(CreateCategoryCommand.class))).thenReturn(categoryResponse);

        mockMvc.perform(post("/api/v1/categories")
                .principal(mockPrincipal)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Category created successfully"))
                .andExpect(jsonPath("$.data.id").value(categoryId.toString()))
                .andExpect(jsonPath("$.data.name").value("Food"));
    }

    @Test
    void getById_ShouldReturnCategory() throws Exception {
        when(getCategoryUseCase.getCategoryById(categoryId)).thenReturn(categoryResponse);

        mockMvc.perform(get("/api/v1/categories/{id}", categoryId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Category retrieved successfully"))
                .andExpect(jsonPath("$.data.id").value(categoryId.toString()))
                .andExpect(jsonPath("$.data.name").value("Food"));
    }

    @Test
    void getAll_ShouldReturnListOfCategories() throws Exception {
        when(getCategoryUseCase.getAllCategories()).thenReturn(List.of(categoryResponse));

        mockMvc.perform(get("/api/v1/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Categories retrieved successfully"))
                .andExpect(jsonPath("$.data[0].id").value(categoryId.toString()))
                .andExpect(jsonPath("$.data[0].name").value("Food"));
    }

    @Test
    void getCategoryAnalysis_ShouldReturnAnalysisList() throws Exception {
        CategoryAnalysisResponse analysisResponse = new CategoryAnalysisResponse(
                10, 2023, BigDecimal.TEN, 5L, categoryId, "Food");

        when(getCategoryAnalysisUseCase.getCategoryAnalysis("user1")).thenReturn(List.of(analysisResponse));

        mockMvc.perform(get("/api/v1/categories/analysis")
                .principal(mockPrincipal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Category analysis retrieved successfully"))
                .andExpect(jsonPath("$.data[0].id").value(categoryId.toString()))
                .andExpect(jsonPath("$.data[0].name").value("Food"))
                .andExpect(jsonPath("$.data[0].totalAmount").value(10))
                .andExpect(jsonPath("$.data[0].month").value(10))
                .andExpect(jsonPath("$.data[0].year").value(2023));
    }

    @Test
    void update_ShouldReturnUpdatedCategory() throws Exception {
        UpdateCategoryRequest request = new UpdateCategoryRequest("Updated Food", null);
        CategoryResponse updatedResponse = new CategoryResponse(
                categoryId,
                "Updated Food",
                "user1",
                null,
                Instant.now(),
                Instant.now());

        when(updateCategoryUseCase.updateCategory(eq(categoryId), any(UpdateCategoryCommand.class)))
                .thenReturn(updatedResponse);

        mockMvc.perform(put("/api/v1/categories/{id}", categoryId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Category updated successfully"))
                .andExpect(jsonPath("$.data.id").value(categoryId.toString()))
                .andExpect(jsonPath("$.data.name").value("Updated Food"));
    }

    @Test
    void delete_ShouldReturnAccepted() throws Exception {
        doNothing().when(deleteCategoryUseCase).deleteCategory(categoryId);

        mockMvc.perform(delete("/api/v1/categories/{id}", categoryId))
                .andExpect(status().isAccepted());

        verify(deleteCategoryUseCase, times(1)).deleteCategory(categoryId);
    }
}
