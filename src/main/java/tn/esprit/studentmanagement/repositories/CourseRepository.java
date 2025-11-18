package tn.esprit.studentmanagement.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.studentmanagement.entities.Course;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {}
