package com.library.library_system.service;


import com.library.library_system.model.Book;
import com.library.library_system.util.SessionFactoryProvider;
import com.library.library_system.model.Loan;
import com.library.library_system.model.BookCopy;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;

/*
 * Service for the Book Management View
 * Handles the management of books in the library system
 * Includes CRUD operations for books
 */
public class BookService {

    /*
     * Creates a new book and adds it to the database
     */
    public void createBook(Book book) {
        Transaction tx = null;
        try (Session session = SessionFactoryProvider.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.persist(book);
            tx.commit();

        // handle errors
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            System.err.println("Error creating book: " + e.getMessage());
            throw new RuntimeException("Failed to create book", e);
        }
    }

    /*
     * Retrieves a book by its ID
     */
    public Book readBook(int bookId) {
        try (Session session = SessionFactoryProvider.getSessionFactory().openSession()) {
            return session.get(Book.class, bookId);

        // handle errors
        } catch (Exception e) {
            System.err.println("Error reading book with ID " + bookId + ": " + e.getMessage());
            throw new RuntimeException("Failed to read book", e);
        }
    }

    /*
     * Updates a book in the database
     */
    public void updateBook(Book book) {
        Transaction tx = null;
        try (Session session = SessionFactoryProvider.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.merge(book);
            tx.commit();

        // handle errors
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            System.err.println("Error updating book: " + e.getMessage());
            throw new RuntimeException("Failed to update book", e);
        }
    }

    /*
     * Deletes a book from the database
     */
    public boolean deleteBook(int bookId) {
        Transaction tx = null;
        try (Session session = SessionFactoryProvider.getSessionFactory().openSession()) {
            Book book = session.get(Book.class, bookId);
            if (book == null) return false;

            // Check dependencies
            Query<BookCopy> copyQuery = session.createQuery("FROM BookCopy WHERE book.bookId = :id", BookCopy.class);
            copyQuery.setParameter("id", bookId);
            List<BookCopy> copies = copyQuery.list();

            for (BookCopy copy : copies) {
                if (copy.isBorrowed()) {
                    System.out.println("Cannot delete: Copies still on loan.");
                    return false;
                }
            }

            tx = session.beginTransaction();
            for (BookCopy copy : copies) {
                session.remove(copy);
            }
            session.remove(book);
            tx.commit();
            return true;

        // handle errors
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            System.err.println("Error deleting book with ID " + bookId + ": " + e.getMessage());
            throw new RuntimeException("Failed to delete book", e);
        }
    }

    /*
     * Retrieves all book copies for a book
     */
    public List<BookCopy> listBookCopies(int bookId) {
        try (Session session = SessionFactoryProvider.getSessionFactory().openSession()) {
            Query<BookCopy> query = session.createQuery("FROM BookCopy WHERE book.bookId = :id", BookCopy.class);
            query.setParameter("id", bookId);
            return query.list();

        // handle errors
        } catch (Exception e) {
            System.err.println("Error listing book copies for book ID " + bookId + ": " + e.getMessage());
            throw new RuntimeException("Failed to list book copies", e);
        }
    }

    /*
     * Retrieves the availability and due date of a book copy
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
                return "Borrowed, but no active loan found.";
            }

        // handle errors
        } catch (Exception e) {
            System.err.println("Error fetching availability for copy ID " + copyId + ": " + e.getMessage());
            throw new RuntimeException("Failed to fetch availability", e);
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
                System.out.println("Cannot delete copy: It's currently on loan.");
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
     * Retrieves all books from the database
     */
    public List<Book> getAllBooks() {
        try (Session session = SessionFactoryProvider.getSessionFactory().openSession()) {
            return session.createQuery("FROM Book", Book.class).list();

        // handle errors
        } catch (Exception e) {
            System.err.println("Error retrieving all books: " + e.getMessage());
            throw new RuntimeException("Failed to retrieve books", e);
        }
    }
}