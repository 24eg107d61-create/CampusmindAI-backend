
package com.campusmind.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.campusmind.backend.entity.Book;

public interface BookRepository extends JpaRepository<Book, Long> {
    boolean existsByIsbn(String isbn);
}
