package com.eventmanagement.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.eventmanagement.dto.CategoryDto;
import com.eventmanagement.dto.CreateCategoryRequest;
import com.eventmanagement.dto.MoveRequest;
import com.eventmanagement.model.Category;
import com.eventmanagement.service.CategoryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CategoryControllerTest {

    private  static final String BASE_URL="/api/v1/categories";

    @MockitoBean
    private CategoryService categoryService;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    private final CreateCategoryRequest.CreateCategoryRequestBuilder createCategoryRequestBuilder = CreateCategoryRequest.builder();
    private final CategoryDto.CategoryDtoBuilder categoryDtoBuilder = CategoryDto.builder();
    private final Category.CategoryBuilder categoryBuilder = Category.builder();

    @BeforeEach
    void setup() {
        createCategoryRequestBuilder
                .label("Category 1");

        categoryBuilder
                .label("Category 1")
                .parent(null)
                .children(new ArrayList<>());

        categoryDtoBuilder
                .label("Category 1")
                .parentId(null)
                .children(new ArrayList<>());
    }

    @Test
    @DisplayName("POST " + BASE_URL + " - Create Category")
    void testCreateCategory() throws Exception
    {
        // Arrange
        CreateCategoryRequest request = createCategoryRequestBuilder.build();
        CategoryDto categoryDto = categoryDtoBuilder.build();

        // Act
        when(categoryService.createCategory(any(CreateCategoryRequest.class), anyLong()))
                .thenReturn(categoryDto);

        performPost(BASE_URL , request)
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("GET " + BASE_URL + " - Get Category Subtree")
    void testGetSubtree() throws Exception
    {
        // Arrange
        CategoryDto categoryDto = categoryDtoBuilder
                .parentId(1L)
                .build();

        when(categoryService.getSubtree(anyLong()))
                .thenReturn(categoryDto);

        // Act && Assert
        performGet(BASE_URL + "/1/subtree")
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT " + BASE_URL + " - Move Category Subtree")
    void testMoveSubtree() throws Exception
    {
        // Arrange
        MoveRequest request = MoveRequest.builder()
                .newParentId(2L)
                .build();

        CategoryDto categoryDto = categoryDtoBuilder
                .parentId(1L)
                .build();

        when(categoryService.getSubtree(anyLong()))
                .thenReturn(categoryDto);

        // Act && Assert
        performPut(BASE_URL + "/1/move", request)
                .andExpect(status().isOk());
    }


    @Test
    @DisplayName("DELETE " + BASE_URL + " - Delete Category")
    void testDeleteCategory() throws Exception
    {
        // Arrange
        doNothing().when(categoryService).deleteCategory(anyLong());

        // Act and Assert
        performDelete(BASE_URL + "/1")
                .andExpect(status().isNoContent());
    }

    protected ResultActions performPost(String url, Object dto) throws Exception {
        return mockMvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(dto)));
    }

    protected ResultActions performPut(String url, Object dto) throws Exception {
        return mockMvc.perform(put(url)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(dto)));
    }

    protected ResultActions performDelete(String url) throws Exception {
        return mockMvc.perform(delete(url)
                .contentType(MediaType.APPLICATION_JSON_VALUE));
    }

    protected ResultActions performGet(String url) throws Exception {
        return mockMvc.perform(get(url));
    }
}
