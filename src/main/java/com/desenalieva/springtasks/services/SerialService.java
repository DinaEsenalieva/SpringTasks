package com.desenalieva.springtasks.services;

import com.desenalieva.springtasks.events.CreateSerialEvent;
import com.desenalieva.springtasks.entities.Serial;
import com.desenalieva.springtasks.repositories.SerialRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.transaction.event.TransactionPhase.AFTER_COMMIT;

/**
 * Сервис сериалов.
 */
@Service
@RequiredArgsConstructor
public class SerialService {
    private final static Logger log = LoggerFactory.getLogger(CustomSerialService.class);
    /**
     * Путь до файла, в который нужно записать информацию о созданном сериале.
     */
    @Value("${serial.info.filepath}")
    public String SERIAL_INFO_FILE_PATH;

    /**
     * Репозиторий сериалов.
     */
    private final SerialRepository serialRepository;

    /**
     * Сервис публикации событий.
     */
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Создание сериала.
     * @param id     идентификатор сериала
     * @param name   название сериала
     * @param rating рейтинг сериала
     * @return сериал
     */
    @Transactional
    public Serial create(Long id, String name, Integer rating) {
        Serial serial = new Serial(id, name, rating);
        eventPublisher.publishEvent(new CreateSerialEvent(this, serial));
        return serialRepository.save(serial);
    }

    /**
     * Обработка события успешного коммита транзакции после создания сериала.
     * Запись в файл информации о созданном сериале.
     * @param createSerialEvent событие создания сериала
     */
    @TransactionalEventListener(phase = AFTER_COMMIT)
    public void writeSerialInfo(CreateSerialEvent createSerialEvent) {
        Serial serial = createSerialEvent.getSerial();
        try (BufferedWriter bw = Files.newBufferedWriter(Path.of(SERIAL_INFO_FILE_PATH), UTF_8)) {
            bw.write(String.format("%1$s, %2$d", serial.getName(), serial.getRating()));
        } catch (IOException ex) {
            log.error("Произошла ошибка при записи информации о сериале в файл {}: ", SERIAL_INFO_FILE_PATH, ex);
            throw new RuntimeException(ex);
        }
    }

    /**
     * Обновление рейтинга сериала.
     * @param id     идентификатор сериала
     * @param rating рейтинг сериала
     */
    @Transactional
    public void updateRatingById(Long id, Integer rating) {
        serialRepository.findById(id)
                .ifPresent(serial -> {
                    serial.setRating(rating);
                    serialRepository.save(serial);
                });
    }

    /**
     * Удаление сериала с переданным идентификатором.
     * (использую данный метод, так как он выкидывает EmptyResultDataAccessException)
     * @param id идентификатор сериала
     */
    @Transactional
    public void deleteById(Long id) {
        serialRepository.deleteById(id);
    }
}