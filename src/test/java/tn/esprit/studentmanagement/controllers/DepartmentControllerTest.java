package tn.esprit.studentmanagement.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tn.esprit.studentmanagement.entities.Department;
import tn.esprit.studentmanagement.services.IDepartmentService;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DepartmentController.class)
class DepartmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IDepartmentService departmentService;

    private Department department1;
    private Department department2;

    @BeforeEach
    void setUp() {
        department1 = new Department();
        department1.setIdDepartment(1L);
        department1.setName("Computer Science");

        department2 = new Department();
        department2.setIdDepartment(2L);
        department2.setName("Mathematics");
    }

    @Test
    void testGetAllDepartments() throws Exception {
        List<Department> departments = Arrays.asList(department1, department2);
        when(departmentService.getAllDepartments()).thenReturn(departments);

        mockMvc.perform(get("/Depatment/getAllDepartment"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name").value("Computer Science"))
                .andExpect(jsonPath("$[1].name").value("Mathematics"));
    }

    @Test
    void testGetDepartmentById() throws Exception {
        when(departmentService.getDepartmentById(1L)).thenReturn(department1);

        mockMvc.perform(get("/Depatment/getDepartment/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idDepartment").value(1))
                .andExpect(jsonPath("$.name").value("Computer Science"));
    }
}
