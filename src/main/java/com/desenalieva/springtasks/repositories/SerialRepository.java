package com.desenalieva.springtasks.repositories;

import com.desenalieva.springtasks.entities.Serial;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Репозиторий сериалов.
 */
public interface SerialRepository extends JpaRepository<Serial, Long> {
}
