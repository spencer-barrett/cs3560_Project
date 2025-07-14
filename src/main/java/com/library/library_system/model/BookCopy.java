package com.library.library_system.model;

import jakarta.persistence.*;
import java.util.List;

/*
 * Model for the Book Copy Management View
 * Handles the management of book copies in the library system
 * Includes CRUD operations for book copies
 */
@Entity
@Table(name = "bookcopy")
public class BookCopy {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "copy_id")
    private int copyId;

    private String barcode;
    private String location;
    private boolean isBorrowed;

    @ManyToOne
    @JoinColumn(name = "book_id")
    private Book book;

    @ManyToMany(mappedBy = "bookCopies")
    private List<Loan> loans;

    public BookCopy() {}
    
    /*
     * Marks a book copy as borrowed
     */
    public void markAsBorrowed() {
        this.isBorrowed = true;
    }

    /*
     * Marks a book copy as returned
     */
    public void markAsReturned() {
        this.isBorrowed = false;
    }

    /*
     * Checks if a book copy is available
     */
    public boolean isAvailable() {
        return !isBorrowed;
    }
    

    /*
     * Getters and Setters
     */

    public String getStatus() {
        return isBorrowed ? "Borrowed" : "Available";
    }

    public void setStatus(String status) {
        this.isBorrowed = "Borrowed".equalsIgnoreCase(status);
    }

	public int getCopyId() {
		return copyId;
	}

	public String getBarcode() {
		return barcode;
	}

	public void setBarcode(String barcode) {
		this.barcode = barcode;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public boolean isBorrowed() {
		return isBorrowed;
	}

	public void setBorrowed(boolean isBorrowed) {
		this.isBorrowed = isBorrowed;
	}



	public void setBook(Book book) {
		this.book = book;
		
	}

    public Book getBook() {
        return book;
    }
    
    

}
