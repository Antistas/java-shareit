package ru.practicum.shareit.booking;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByBooker_Id(Long userId, Sort sort);

    List<Booking> findByBooker_IdAndStartBeforeAndEndAfter(Long userId, LocalDateTime now1,
                                                         LocalDateTime now2, Sort sort);

    List<Booking> findByBooker_IdAndEndBefore(Long userId, LocalDateTime now, Sort sort);

    List<Booking> findByBooker_IdAndStartAfter(Long userId, LocalDateTime now, Sort sort);

    List<Booking> findByBooker_IdAndStatus(Long userId, BookingStatus status, Sort sort);

    List<Booking> findByItem_Owner_Id(Long ownerId, Sort sort);

    List<Booking> findByItem_Owner_IdAndStatus(Long ownerId, BookingStatus status, Sort sort);

    // брал ли пользователь эту вещь в аренду и завершилась ли аренда ?
    boolean existsByItem_IdAndBooker_IdAndStatusAndEndBefore(
            Long itemId,
            Long bookerId,
            BookingStatus status,
            LocalDateTime now
    );

    // поиск по бронированиям, принимает на вход список вещей, статус, дату, сортируем по дате убыв.
    List<Booking> findByItem_IdInAndStatusAndEndBeforeOrderByEndDesc(
            List<Long> itemIds,
            BookingStatus status,
            LocalDateTime now
    );

    // аналогично по возр.
    List<Booking> findByItem_IdInAndStatusAndStartAfterOrderByStartAsc(
            List<Long> itemIds,
            BookingStatus status,
            LocalDateTime now
    );
}
