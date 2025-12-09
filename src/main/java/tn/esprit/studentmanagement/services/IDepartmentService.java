package tn.esprit.studentmanagement.services;

import tn.esprit.studentmanagement.entities.Department;

import java.util.List;

public interface IDepartmentService {
    public List<Department> getAllDepartments();
    public Department getDepartmentById(Long idDepartment);
    public Department saveDepartment(Department department);
    public void deleteDepartment(Long idDepartment);
}
