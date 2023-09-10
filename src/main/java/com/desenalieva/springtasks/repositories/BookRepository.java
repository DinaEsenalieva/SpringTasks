package com.desenalieva.springtasks.repositories;

import com.desenalieva.springtasks.entities.Book;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Репозиторий книг.
 */
public interface BookRepository extends JpaRepository<Book, Long> {
}