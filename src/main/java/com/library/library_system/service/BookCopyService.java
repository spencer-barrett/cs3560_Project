package com.library.library_system.service;


import com.library.library_system.model.BookCopy;
import com.library.library_system.model.Loan;
import com.library.library_system.util.SessionFactoryProvider;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import java.util.List;

/*
 * Service for the Book Copy Management View
 * Handles the management of book copies in the library system
 * Includes CRUD operations for book copies
 */
public class BookCopyService {

    /*
     * Creates a new book copy and adds it to the database
     */
    public void createBookCopy(BookCopy copy) {
        Transaction tx = null;
        try (Session session = SessionFactoryProvider.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.persist(copy);
            tx.commit();

        // handle errors
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            System.err.println("Error creating book copy: " + e.getMessage());
            throw new RuntimeException("Failed to create book copy", e);
        }
    }

    /*
     * Retrieves a book copy by its ID
     */
    public BookCopy readBookCopy(int copyId) {
        try (Session session = SessionFactoryProvider.getSessionFactory().openSession()) {
            return session.get(BookCopy.class, copyId);

        // handle errors
        } catch (Exception e) {
            System.err.println("Error reading book copy with ID " + copyId + ": " + e.getMessage());
            throw new RuntimeException("Failed to read book copy", e);
        }
    }

    /*
     * Updates a book copy in the database
     */
    public void updateBookCopy(BookCopy copy) {
        Transaction tx = null;
        try (Session session = SessionFactoryProvider.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.merge(copy);
            tx.commit();

        // handle errors
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            System.err.println("Error updating book copy: " + e.getMessage());
            throw new RuntimeException("Failed to update book copy", e);
        }
    }

    /*
     * Deletes a book copy from the database
     */
    public boolean deleteBookCopy(int copyId) {
        Transaction tx = null;
        try (Session session = SessionFactoryProvider.getSessionFactory().openSession()) {
            BookCopy copy = session.get(BookCopy.class, copyId);
            if (copy == null) return false;

            if (copy.isBorrowed()) {
                System.out.println("Cannot delete: copy is currently borrowed.");
                return false;
            }

            // check for loan history
            Query<Long> loanCountQuery = session.createQuery(
                "SELECT COUNT(l) FROM Loan l JOIN l.bookCopies bc WHERE bc.copyId = :id",
                Long.class);
            loanCountQuery.setParameter("id", copyId);
            Long loanCount = loanCountQuery.uniqueResult();
            if (loanCount != null && loanCount > 0) {
                System.out.println("Cannot delete: This copy has loan history.");
                return false;
            }

            tx = session.beginTransaction();
            session.remove(copy);
            tx.commit();
            return true;

        // handle errors
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            System.err.println("Error deleting book copy with ID " + copyId + ": " + e.getMessage());
            throw new RuntimeException("Failed to delete book copy", e);
        }
    }

    /*
     * Retrieves the availability and due date of a book copy for a loan
     */
    public String fetchAvailabilityAndDueDate(int copyId) {
        try (Session session = SessionFactoryProvider.getSessionFactory().openSession()) {
            BookCopy copy = session.get(BookCopy.class, copyId);
            if (copy == null) return "Copy not found.";

            if (!copy.isBorrowed()) return "Available";

            Query<Loan> loanQuery = session.createQuery(
                "SELECT l FROM Loan l JOIN l.bookCopies bc WHERE bc.copyId = :id AND l.returnDate IS NULL",
                Loan.class);
            loanQuery.setParameter("id", copyId);
            Loan loan = loanQuery.uniqueResult();

            if (loan != null) {
                return "Borrowed. Due on: " + loan.getDueDate();
            } else {
                return "Borrowed but no active loan found.";
            }

        // handle errors
        } catch (Exception e) {
            System.err.println("Error fetching availability for copy ID " + copyId + ": " + e.getMessage());
            throw new RuntimeException("Failed to fetch availability", e);
        }
    }

    /*
     * Retrieves all book copies from the database
     */
    public List<BookCopy> getAllBookCopies() {
        try (Session session = SessionFactoryProvider.getSessionFactory().openSession()) {
            return session.createQuery("FROM BookCopy", BookCopy.class).list();

        // handle errors
        } catch (Exception e) {
            System.err.println("Error retrieving all book copies: " + e.getMessage());
            throw new RuntimeException("Failed to retrieve book copies", e);
        }
    }
}
