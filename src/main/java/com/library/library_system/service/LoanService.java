package com.library.library_system.service;

import com.library.library_system.model.BookCopy;
import com.library.library_system.model.Loan;
import com.library.library_system.model.Student;
import com.library.library_system.util.SessionFactoryProvider;
import org.hibernate.Session;
import org.hibernate.Transaction;


import java.util.Date;
import java.util.List;

/*
 * Service for the Loan Management View
 * Handles the management of loans in the library system
 * Includes CRUD operations for loans
 */
public class LoanService {

    /*
     * Creates a new loan and adds it to the database
     */
    public void createLoan(Student student, List<BookCopy> copies, Date borrowDate, Date dueDate) {
        Transaction tx = null;
        try (Session session = SessionFactoryProvider.getSessionFactory().openSession()) {

            /*
             * validate user input
             */


            // max 5 book copies per student
            Long activeCopies = session.createQuery(
                "SELECT COUNT(bc) FROM Loan l JOIN l.bookCopies bc WHERE l.student.broncoId = :id AND l.returnDate IS NULL", Long.class)
                .setParameter("id", student.getBroncoId())
                .uniqueResult();
            if (activeCopies + copies.size() > 5) {
                throw new IllegalArgumentException("Student cannot borrow more than 5 book copies at a time.");
            }

            // no loans if student has overdue items
            Long overdueCount = session.createQuery(
                "SELECT COUNT(l) FROM Loan l WHERE l.student.broncoId = :id AND l.returnDate IS NULL AND l.dueDate < current_date", Long.class)
                .setParameter("id", student.getBroncoId())
                .uniqueResult();
            if (overdueCount > 0) {
                throw new IllegalArgumentException("Student has overdue items and cannot borrow more.");
            }

            // max loan duration of 180 days
            long diff = dueDate.getTime() - borrowDate.getTime();
            long days = diff / (1000 * 60 * 60 * 24);
            if (days > 180) {
                throw new IllegalArgumentException("Loan duration cannot exceed 180 days.");
            }

            tx = session.beginTransaction();

            Loan loan = new Loan();
            loan.setBorrowDate(borrowDate);
            loan.setDueDate(dueDate);
            loan.setStudent(student);
            loan.setBookCopies(copies);

            for (BookCopy copy : copies) {
                copy.setBorrowed(true);
                session.merge(copy);
            }

            session.persist(loan);
            tx.commit();

        // handle errors
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
            throw e;
        }
    }

    /*
     * Retrieves a loan by its ID
     */
    public Loan getLoanById(int id) {
        try (Session session = SessionFactoryProvider.getSessionFactory().openSession()) {
            return session.createQuery(
                "SELECT l FROM Loan l LEFT JOIN FETCH l.bookCopies WHERE l.loanId = :id", Loan.class)
                .setParameter("id", id)
                .uniqueResult();
        }
    }

    /*
     * Retrieves all loans from the database
     */
    public List<Loan> getAllLoans() {
        try (Session session = SessionFactoryProvider.getSessionFactory().openSession()) {
            return session.createQuery(
                "SELECT DISTINCT l FROM Loan l LEFT JOIN FETCH l.bookCopies", Loan.class)
                .list();
        }
    }

    /*
     * Deletes a loan from the database
     */
    public void deleteLoan(int loanId) {
        Transaction tx = null;
        try (Session session = SessionFactoryProvider.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            Loan loan = session.get(Loan.class, loanId);
            if (loan != null) {
                for (BookCopy copy : loan.getBookCopies()) {
                    copy.setBorrowed(false);
                    session.merge(copy);
                }
                session.remove(loan);
            }
            tx.commit();

        // handle errors
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        }
    }

    /*
     * Returns a loan and updates the database
     */
    public void returnLoan(int loanId, Date returnDate) {
        Transaction tx = null;
        try (Session session = SessionFactoryProvider.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            Loan loan = session.get(Loan.class, loanId);
            if (loan != null) {
                loan.setReturnDate(returnDate);
                for (BookCopy copy : loan.getBookCopies()) {
                    copy.setBorrowed(false);
                    session.merge(copy);
                }
                session.merge(loan);
            }
            tx.commit();

        // handle errors
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        }
    }

    /*
     * Helper method to generate a receipt for a loan
     */
    public String generateReceipt(int loanId) {
        try (Session session = SessionFactoryProvider.getSessionFactory().openSession()) {
            Loan loan = session.createQuery(
                "SELECT l FROM Loan l LEFT JOIN FETCH l.bookCopies WHERE l.loanId = :id", Loan.class)
                .setParameter("id", loanId)
                .uniqueResult();
            if (loan == null) return "Loan not found.";


            StringBuilder sb = new StringBuilder();
            sb.append("Receipt for Loan #").append(loan.getLoanId()).append("\n");
            sb.append("Borrowed on: ").append(loan.getBorrowDate() != null ? loan.getBorrowDate(): "").append("\n");
            sb.append("Due on: ").append(loan.getDueDate() != null ? loan.getDueDate() : "").append("\n");
            sb.append("Returned on: ").append(loan.getReturnDate() != null ? loan.getReturnDate() : "Not yet returned").append("\n");
            sb.append("Books:\n");
            for (BookCopy copy : loan.getBookCopies()) {
                sb.append("- ").append(copy.getBarcode()).append(" - ").append(copy.getBook().getTitle()).append("\n");
            }

            return sb.toString();
        }
    }

    /*
     * Retrieves all overdue loans from the database
     */
    public List<Loan> getOverdueLoans() {
        try (Session session = SessionFactoryProvider.getSessionFactory().openSession()) {
            return session.createQuery(
                "FROM Loan WHERE returnDate IS NULL AND dueDate < current_date", Loan.class).list();
        }
    }
}
