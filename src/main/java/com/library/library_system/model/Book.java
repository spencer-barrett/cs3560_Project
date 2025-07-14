package com.library.library_system.model;

import jakarta.persistence.*;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/*
 * Model for the Book Management View
 * Handles the management of books in the library system
 * Includes CRUD operations for books
 */
@Entity
@Table(name = "book")
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "book_id")
    private int bookId;

    private String isbn;
    private String title;
    private String description;
    private String authors;
    private int numberOfPages;
    private String publisher;
    @Temporal(TemporalType.DATE)
    private Date publicationDate;

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL)
    private List<BookCopy> copies;

    public Book() {}
    
    
    /*
	 * Returns a list of available book copies
	 */
    public List<BookCopy> getAvailableCopies(){
    	return copies.stream()
    			.filter(copy -> !copy.isBorrowed())
    			.collect(Collectors.toList());
    }
    

	/*
	 * Returns the total number of book copies
	 */
    public int getTotalCopies() {
    	return copies.size();
    }
    
	/*
	 * Getters and Setters
	 */

    public int getBookId() {
        return bookId;
    }

	public String getIsbn() {
		return isbn;
	}

	public void setIsbn(String isbn) {
		this.isbn = isbn;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getAuthors() {
		return authors;
	}

	public void setAuthors(String authors) {
		this.authors = authors;
	}

	public int getNumberOfPages() {
		return numberOfPages;
	}

	public void setNumberOfPages(int numberOfPages) {
		this.numberOfPages = numberOfPages;
	}

	public String getPublisher() {
		return publisher;
	}

	public void setPublisher(String publisher) {
		this.publisher = publisher;
	}

	public Date getPublicationDate() {
		return publicationDate;
	}

	public void setPublicationDate(Date publicationDate) {
		this.publicationDate = publicationDate;
	}

	/*
	 * toString method to display the book title and authors
	 */
    @Override
    public String toString() {
        return title + " by " + authors;
    }
   
}
