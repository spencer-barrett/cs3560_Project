package com.library.library_system.service;


import com.library.library_system.model.Student;
import com.library.library_system.util.SessionFactoryProvider;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;

/*
 * Service for the Student Management View
 * Handles the management of students in the library system
 * Includes CRUD operations for students
 */
public class StudentService {

    /*
     * Adds a student to the database
     */
    public void addStudent(Student student) {
        Transaction tx = null;
        try (Session session = SessionFactoryProvider.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.persist(student);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        }
    }

    /*
     * Retrieves a student by their broncoId
     */
    public Student getStudentById(int broncoId) {
        try (Session session = SessionFactoryProvider.getSessionFactory().openSession()) {
            return session.get(Student.class, broncoId);
        }
    }

    /*
     * Retrieves all students from the database
     */
    public List<Student> getAllStudents() {
        try (Session session = SessionFactoryProvider.getSessionFactory().openSession()) {
            return session.createQuery("FROM Student", Student.class).list();
        }
    }

    /*
     * Updates a student's address
     */
    public void updateStudentAddress(int broncoId, String newAddress) {
        Transaction tx = null;
        try (Session session = SessionFactoryProvider.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            Student student = session.get(Student.class, broncoId);
            if (student != null) {
                student.setAddress(newAddress);
                session.merge(student);
            }
            tx.commit();

        // handle errors
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        }
    }

    /*
     * Updates a student's degree
     */
    public void updateStudentDegree(int broncoId, String newDegree) {
        Transaction tx = null;
        try (Session session = SessionFactoryProvider.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            Student student = session.get(Student.class, broncoId);
            if (student != null) {
                student.setDegree(newDegree);
                session.merge(student);
            }
            tx.commit();

        // handle errors
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        }
    }
    
    /*
     * Updates a student's information to the database
     */
    public void updateStudent(Student student) {
        Transaction tx = null;
        try (Session session = SessionFactoryProvider.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.merge(student);
            tx.commit();

        // handle errors
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }
    
    /*
     * Deletes a student from the database
     */
    public boolean deleteStudent(int broncoId) {
        Transaction tx = null;
        try (Session session = SessionFactoryProvider.getSessionFactory().openSession()) {
            tx = session.beginTransaction();

            Student student = session.get(Student.class, broncoId);
            if (student == null) {
                System.out.println("Student not found.");
                return false;
            }

            // check loan dependency
            String hql = "SELECT count(l) FROM Loan l WHERE l.student.broncoId = :broncoId";
            Long loanCount = session.createQuery(hql, Long.class)
                                    .setParameter("broncoId", broncoId)
                                    .uniqueResult();

            if (loanCount != null && loanCount > 0) {
                System.out.println("Cannot delete student: existing loans found.");
                return false;
            }

            session.remove(student);
            tx.commit();
            System.out.println("Student deleted.");
            return true;

        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
            return false;
        }
    }

}