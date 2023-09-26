# Задание №1 

### Вопрос:
@Transactional по умолчанию откатывается на unchecked исключения.
Но есть некоторое множество исключений (например, DataAccessException), которые всё равно пометят транзакцию как rollback-only, даже если их поймать в try-catch.
Определить это "некоторое множество", понять, почему так происходит, и написать различные тесты.

### Ответ:
см. **CustomSerialServiceTest**

#### Правило 1
Если один транзакционный метод (метод1), вызывает другой транзакционный метод (метод2) с propagation,
предполагающим использование существующей транзакции,
в случае если метод 2 отметит транзакцию как rollbackOnly, то соответственно вся транзакция будет откачена
даже в случае, если в методе1 исключение будет обернуто в try/catch, или у метода1 задан noRollbackFor

#### Правило 2
Если один транзакционный метод (метод1), вызывает другой транзакционный метод (метод2) с propagation,
предполагающим использование существующей транзакции,
в случае если метод 2 НЕ отметит транзакцию как rollbackOnly (благодаря тому, что у метода2 задан noRollbackFor),
а просто прокинет исключение вверх по стеку, то дальнейшее поведение зависит от метода1
обернуто ли исключение в try/catch, задан ли noRollbackFor

#### Правило 3
Если один транзакционный метод (метод1), вызывает другой транзакционный метод (метод2) с propagation, 
предполагающим создание новой транзакции, то соответственно откат каждой транзакции зависит 
от поведения соответствующего метода.

Что можно посмотреть: [@Transactional в Spring и исключения](https://habr.com/ru/articles/725064/)


# Задание №2

### Вопрос:
Понять разницу между @Transactional из разных пакетов: спрингового и javax/jakarta

### Ответ:
см. **TransactionalTest**

Аннотация @Transactional из пакета org.springframework.transaction.annotation имеет следующие параметры:
* **transactionManager** - позволяет указать используемый менеджер транзакций
* **label** - позволяет указать наименование транзакции
* **propagation** - позволяет указать тип распространения транзакции
* **isolation** - позволяет указать уровень изоляции транзакции
* **timeout/timeoutString** - позволяет указать таймаут транзакции (в секундах)
* **readOnly** - логический флаг, которому может быть присвоено значение true, 
если транзакция фактически доступна только для чтения, что позволяет 
проводить соответствующие оптимизации во время выполнения
* **rollbackFor/rollbackForClassName** - позволяет указать типы исключений, которые должны вызывать откат транзакции
* **noRollbackFor/noRollbackForClassName** - позволяет указать типы исключений, которые НЕ должны вызывать откат транзакции

Аннотация @Transactional из пакета javax.transaction имеет следующие параметры:
* **dontRollbackOn** - позволяет указать типы исключений, которые НЕ должны вызывать откат транзакции
* **rollbackOn** - позволяет указать типы исключений, которые должны вызывать откат транзакции
* **value** - позволяет указать тип распространения транзакции 
(можно указать одно из значений перечисления Transactional.TxType, в котором в отличии от перечисления Propagation отсутствует значение NESTED)


# Задание №3

### Вопрос:
Разобраться, в какой момент происходит flush изменений, и как на это влияет FlushMode

### Ответ:
см. **FlushModeTypeTest.java**

FlushModeTypes:
* ALWAYS
* AUTO
* COMMIT
* MANUAL

Спецификация JPA определяет только FlushModeTypes AUTO и COMMIT. 
Hibernate добавляет специфичные FlushModeTypes ALWAYS и MANUAL.

* **FlushModeType.ALWAYS** (Hibernate), flush происходит:
    * перед каждым запросом
    * перед фиксацией транзакции
    * при прямом вызове flush()
* **FlushModeType.AUTO** (JPA & Hibernate) - default, flush происходит:
    * перед выполнением запроса, использующего любую таблицу БД, для которой контекст персистентности содержит любые изменения
    * перед фиксацией транзакции
    * при прямом вызове flush()
* **FlushModeType.COMMIT** (JPA & Hibernate), flush происходит:
    * перед фиксацией транзакции
    * при прямом вызове flush()
* **FlushModeType.MANUAL** (Hibernate), flush происходит:
    * при прямом вызове flush()

Что можно посмотреть:
* [FlushMode in JPA and Hibernate – What it is and how to change it](https://thorben-janssen.com/flushmode-in-jpa-and-hibernate/#flushmodetypeauto-jpa--hibernate)
* [Как работает Flush в Hibernate](https://sysout.ru/kak-rabotaet-flush-v-hibernate/)


# Задание №4

### Вопрос:
Как среагировать на завершение транзакции именно с помощью механизмов спринга.

### Ответ:
см. **SerialServiceTest**

В Spring 4.2 появилась аннотация @TransactionalEventListener, которая является расширением @EventListener
и позволяет привязывать слушатель событий к фазе транзакции.

Привязка возможна к следующим фазам транзакции:
* **AFTER_COMMIT** - (по умолчанию) используется для запуска события, если транзакция завершилась успешно.
* **AFTER_ROLLBACK** – если транзакция откатилась
* **AFTER_COMPLETION** — если транзакция завершена (комбинация AFTER_ROLLBACK и AFTER_COMMIT)
* **BEFORE_COMMIT** - используется для запуска события непосредственно перед фиксацией транзакции.

Что можно посмотреть:
* [Spring Data — Power of Domain Events](https://dev.to/kirekov/spring-data-power-of-domain-events-2okm)
* [Spring Events](https://www.baeldung.com/spring-events)


# Задание №5

### Вопрос:
Что произойдет, если в методе аннотированном @Transactional(readOnly = true) вызвать flush()?

### Ответ:
см. **ReadOnlyServiceTest.java#testReadOnlyMethod**

Исключение не возникает, изменения попадают в бд.


# Задание №6

### Вопрос:
Необходимо проверить поведение при вызове из метода с readOnly = true метода с readOnly = false и наоборот 
на факт сохранения изменений в бд.
Примечание: параметр readOnly аннотации @Transactional имеет значение, только если создается новая транзакция,
если используется существующая транзакция, то для нее не меняется параметр readOnly.

### Ответ:
см. **ReadOnlyServiceTest.java**

Изменения сделанные в методе с readOnly = false попадают в бд, изменения сделанные в методе с readOnly = true НЕ попадают в бд