package com.library.library_system.model;

import jakarta.persistence.*;
import java.util.Date;
import java.util.List;

/*
 * Model for the Loan Management View
 * Handles the management of loans in the library system
 * Includes CRUD operations for loans
 */
@Entity
@Table(name = "loan")
public class Loan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "loan_id")
    private int loanId;

    @Temporal(TemporalType.DATE)
    private Date borrowDate;

    @Temporal(TemporalType.DATE)
    private Date dueDate;

    @Temporal(TemporalType.DATE)
    private Date returnDate;

    @ManyToOne
    @JoinColumn(name = "bronco_id")
    private Student student;

    @ManyToMany
    @JoinTable(
        name = "loan_bookcopy",
        joinColumns = @JoinColumn(name = "loan_id"),
        inverseJoinColumns = @JoinColumn(name = "copy_id")
    )
    private List<BookCopy> bookCopies;

    public Loan() {}
     
    /*
     * Checks if a loan is overdue
     */
    public boolean isOverdue() {
		return returnDate == null && dueDate != null && new Date().after(dueDate);
    	
    }
    
    /*
     * Marks a loan as returned
     */
    public void markAsReturned() {
    	this.returnDate = new Date();
    	for(BookCopy copy: bookCopies) {
    		copy.markAsReturned();
    	}
    	
    }
    
    /*
     * Generates a receipt for a loan
     */
    public String generateReceipt() {
    	return "Loan #" + loanId + " | Borrowed: " + borrowDate + " | Due: " + dueDate;
    }
    
    /*
     * Checks if a loan is returned
     */
    public boolean isReturned() {
        return returnDate != null;
    }
    

    /*
     * Getters and Setters
     */
    
    public Student getStudent() {
        return student;
    }
    
    
    public Date getBorrowDate() {
    	return borrowDate;
    }
    
    public void setBorrowDate(Date borrowDate) {
    	this.borrowDate = borrowDate;
    }

	public Date getDueDate() {
		return dueDate;
	}

	public void setDueDate(Date dueDate) {
		this.dueDate = dueDate;
	}

	public Date getReturnDate() {
		return returnDate;
	}

	public void setReturnDate(Date returnDate) {
		this.returnDate = returnDate;
	}


	public void setStudent(Student student) {
        this.student = student;
    }

    public void setBookCopies(List<BookCopy> copies) {
        this.bookCopies = copies;
    }

    public List<BookCopy> getBookCopies() {
        return bookCopies;
    }


    public int getLoanId() {
        return loanId;
    }
    
    
}
