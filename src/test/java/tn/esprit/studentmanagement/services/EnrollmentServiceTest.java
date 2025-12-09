package tn.esprit.studentmanagement.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.studentmanagement.entities.Enrollment;
import tn.esprit.studentmanagement.entities.Status;
import tn.esprit.studentmanagement.repositories.EnrollmentRepository;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EnrollmentServiceTest {

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @InjectMocks
    private EnrollmentService enrollmentService;

    private Enrollment enrollment;

    @BeforeEach
    void setUp() {
        enrollment = new Enrollment();
        enrollment.setIdEnrollment(1L);
        enrollment.setEnrollmentDate(LocalDate.of(2025, 1, 1));
        enrollment.setGrade(85.5);
        enrollment.setStatus(Status.ACTIVE);
    }

    @Test
    void testGetAllEnrollments() {
        List<Enrollment> enrollments = Arrays.asList(enrollment);
        when(enrollmentRepository.findAll()).thenReturn(enrollments);

        List<Enrollment> result = enrollmentService.getAllEnrollments();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(Status.ACTIVE, result.get(0).getStatus());
        verify(enrollmentRepository, times(1)).findAll();
    }

    @Test
    void testGetEnrollmentById_Success() {
        when(enrollmentRepository.findById(1L)).thenReturn(Optional.of(enrollment));

        Enrollment result = enrollmentService.getEnrollmentById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getIdEnrollment());
        assertEquals(85.5, result.getGrade());
        verify(enrollmentRepository, times(1)).findById(1L);
    }

    @Test
    void testGetEnrollmentById_NotFound() {
        when(enrollmentRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> enrollmentService.getEnrollmentById(999L));
        verify(enrollmentRepository, times(1)).findById(999L);
    }

    @Test
    void testSaveEnrollment() {
        when(enrollmentRepository.save(any(Enrollment.class))).thenReturn(enrollment);

        Enrollment result = enrollmentService.saveEnrollment(enrollment);

        assertNotNull(result);
        assertEquals(Status.ACTIVE, result.getStatus());
        verify(enrollmentRepository, times(1)).save(any(Enrollment.class));
    }

    @Test
    void testDeleteEnrollment() {
        doNothing().when(enrollmentRepository).deleteById(anyLong());

        assertDoesNotThrow(() -> enrollmentService.deleteEnrollment(1L));
        verify(enrollmentRepository, times(1)).deleteById(1L);
    }
}
