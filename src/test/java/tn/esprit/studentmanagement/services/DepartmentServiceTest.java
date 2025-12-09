package tn.esprit.studentmanagement.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.studentmanagement.entities.Department;
import tn.esprit.studentmanagement.repositories.DepartmentRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DepartmentServiceTest {

    @Mock
    private DepartmentRepository departmentRepository;

    @InjectMocks
    private DepartmentService departmentService;

    private Department department;

    @BeforeEach
    void setUp() {
        department = new Department();
        department.setIdDepartment(1L);
        department.setName("Computer Science");
        department.setLocation("Building A");
        department.setPhone("123-456-7890");
        department.setHead("Dr. Smith");
    }

    @Test
    void testGetAllDepartments() {
        List<Department> departments = Arrays.asList(department);
        when(departmentRepository.findAll()).thenReturn(departments);

        List<Department> result = departmentService.getAllDepartments();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Computer Science", result.get(0).getName());
        verify(departmentRepository, times(1)).findAll();
    }

    @Test
    void testGetDepartmentById_Success() {
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));

        Department result = departmentService.getDepartmentById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getIdDepartment());
        assertEquals("Computer Science", result.getName());
        verify(departmentRepository, times(1)).findById(1L);
    }

    @Test
    void testGetDepartmentById_NotFound() {
        when(departmentRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> departmentService.getDepartmentById(999L));
        verify(departmentRepository, times(1)).findById(999L);
    }

    @Test
    void testSaveDepartment() {
        when(departmentRepository.save(any(Department.class))).thenReturn(department);

        Department result = departmentService.saveDepartment(department);

        assertNotNull(result);
        assertEquals("Computer Science", result.getName());
        verify(departmentRepository, times(1)).save(any(Department.class));
    }

    @Test
    void testDeleteDepartment() {
        doNothing().when(departmentRepository).deleteById(anyLong());

        assertDoesNotThrow(() -> departmentService.deleteDepartment(1L));
        verify(departmentRepository, times(1)).deleteById(1L);
    }
}
