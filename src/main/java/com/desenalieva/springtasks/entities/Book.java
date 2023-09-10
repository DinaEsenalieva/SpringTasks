package com.desenalieva.springtasks.entities;

import lombok.*;
import org.hibernate.Hibernate;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Objects;

/**
 * Книга.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Book {
    /**
     * Идентификатор
     */
    @Id
    private Long id;

    /**
     * Название
     */
    private String name;

    /**
     * Автор
     */
    private String author;
}