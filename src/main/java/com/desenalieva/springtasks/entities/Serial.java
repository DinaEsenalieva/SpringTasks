package com.desenalieva.springtasks.entities;

import lombok.*;
import org.hibernate.Hibernate;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.util.Objects;

/**
 * Сериал.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Serial {
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
     * Рейтинг
     */
    @Min(0)
    @Max(10)
    private Integer rating;

    public Serial(Long id) {
        this.id = id;
    }
}