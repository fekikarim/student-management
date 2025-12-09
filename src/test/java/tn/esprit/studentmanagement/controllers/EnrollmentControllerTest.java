package tn.esprit.studentmanagement.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tn.esprit.studentmanagement.entities.Enrollment;
import tn.esprit.studentmanagement.entities.Status;
import tn.esprit.studentmanagement.services.IEnrollment;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EnrollmentController.class)
class EnrollmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IEnrollment enrollmentService;

    private Enrollment enrollment1;
    private Enrollment enrollment2;

    @BeforeEach
    void setUp() {
        enrollment1 = new Enrollment();
        enrollment1.setIdEnrollment(1L);
        enrollment1.setStatus(Status.ACTIVE);

        enrollment2 = new Enrollment();
        enrollment2.setIdEnrollment(2L);
        enrollment2.setStatus(Status.COMPLETED);
    }

    @Test
    void testGetAllEnrollments() throws Exception {
        List<Enrollment> enrollments = Arrays.asList(enrollment1, enrollment2);
        when(enrollmentService.getAllEnrollments()).thenReturn(enrollments);

        mockMvc.perform(get("/Enrollment/getAllEnrollment"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"))
                .andExpect(jsonPath("$[1].status").value("COMPLETED"));
    }

    @Test
    void testGetEnrollmentById() throws Exception {
        when(enrollmentService.getEnrollmentById(1L)).thenReturn(enrollment1);

        mockMvc.perform(get("/Enrollment/getEnrollment/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idEnrollment").value(1))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }
}
