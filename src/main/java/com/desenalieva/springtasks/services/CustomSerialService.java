package com.desenalieva.springtasks.services;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.RecoverableDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomSerialService {
    private final static Logger log = LoggerFactory.getLogger(CustomSerialService.class);
    /**
     * Сервис сериалов.
     */
    private final SerialService serialService;
    /**
     * Вспомогательный сервис.
     */
    private final HelperService helperService;

    @Transactional
    public void updateRatingAndDelete1(Long updateId, Integer rating, Long deleteId) {
        updateRatingAndDelete(updateId, rating, deleteId);
    }

    @Transactional
    public void updateRatingAndDelete2(Long updateId, Integer rating, Long deleteId) {
        try {
            updateRatingAndDelete(updateId, rating, deleteId);
        } catch (Exception ex) {
            log.error("Ошибка", ex);
        }
    }

    @Transactional(noRollbackFor = EmptyResultDataAccessException.class)
    public void updateRatingAndDelete3(Long updateId, Integer rating, Long deleteId) {
        updateRatingAndDelete(updateId, rating, deleteId);
    }

    /**
     * Обновляет рейтинг сериала c id = updateId и удаляет сериал с id = deleteId
     * @param updateId идентификатор сериала, рейтинг которого нужно обновить
     * @param rating   рейтинг сериала
     * @param deleteId идентификатор сериала, который нужно удалить
     */
    public void updateRatingAndDelete(Long updateId, Integer rating, Long deleteId) {
        serialService.updateRatingById(updateId, rating);
        serialService.deleteById(deleteId);
    }

    @Transactional
    public void updateRating1(Long id, Integer rating) {
        updateRatingWithException(id, rating);
    }

    @Transactional
    public void updateRating2(Long id, Integer rating) {
        try {
            updateRatingWithException(id, rating);
        } catch (Exception ex) {
            log.error("", ex);
        }
    }

    @Transactional(noRollbackFor = RecoverableDataAccessException.class)
    public void updateRating3(Long id, Integer rating) {
        updateRatingWithException(id, rating);
    }

    private void updateRatingWithException(Long id, Integer rating) {
        serialService.updateRatingById(id, rating);
        throw new RecoverableDataAccessException("Message");
    }

    // start region Правило 1
    @Transactional
    public void updateRating4(Long id, Integer rating) {
        updateRatingWihHelpMethod1(id, rating);
    }

    @Transactional
    public void updateRating5(Long id, Integer rating) {
        try {
            updateRatingWihHelpMethod1(id, rating);
        } catch (Exception ex) {
            log.error("Ошибка", ex);
        }
    }

    @Transactional(noRollbackFor = EmptyResultDataAccessException.class)
    public void updateRating6(Long id, Integer rating) {
        updateRatingWihHelpMethod1(id, rating);
    }

    private void updateRatingWihHelpMethod1(Long id, Integer rating) {
        serialService.updateRatingById(id, rating);
        helperService.helpMethod1();
    }
    // end region Правило 1

    // start region Правило 2
    @Transactional
    public void updateRating7(Long id, Integer rating) {
        updateRatingWithHelpMethod2(id, rating);
    }

    @Transactional
    public void updateRating8(Long id, Integer rating) {
        try {
            updateRatingWithHelpMethod2(id, rating);
        } catch (Exception ex) {
            log.error("Ошибка", ex);
        }
    }

    @Transactional(noRollbackFor = RecoverableDataAccessException.class)
    public void updateRating9(Long id, Integer rating) {
        updateRatingWithHelpMethod2(id, rating);
    }

    private void updateRatingWithHelpMethod2(Long id, Integer rating) {
        serialService.updateRatingById(id, rating);
        helperService.helpMethod2();
    }
    // end region Правило 2

    // start Правило 3
    @Transactional
    public void updateRating10(Long id, Integer rating) {
        updateRatingWihHelpMethod3(id, rating);
    }

    @Transactional
    public void updateRating11(Long id, Integer rating) {
        try {
            updateRatingWihHelpMethod3(id, rating);
        } catch (Exception ex) {
            log.error("Ошибка", ex);
        }
    }

    @Transactional(noRollbackFor = RecoverableDataAccessException.class)
    public void updateRating12(Long id, Integer rating) {
        updateRatingWihHelpMethod3(id, rating);
    }

    private void updateRatingWihHelpMethod3(Long id, Integer rating) {
        serialService.updateRatingById(id, rating);
        helperService.helpMethod3();
    }
    // end region Правило 3
}