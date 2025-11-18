package tn.esprit.studentmanagement.services;

import tn.esprit.studentmanagement.entities.Enrollment;

import java.util.List;

public interface IEnrollment {
    public List<Enrollment> getAllEnrollments();
    public Enrollment getEnrollmentById(Long idEnrollment);
    public Enrollment saveEnrollment(Enrollment enrollment);
    public void deleteEnrollment(Long idEnrollment);

}
