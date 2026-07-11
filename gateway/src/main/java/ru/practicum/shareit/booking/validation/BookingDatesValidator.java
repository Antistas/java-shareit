package ru.practicum.shareit.booking.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;

public class BookingDatesValidator implements ConstraintValidator<ValidBookingDates, BookItemRequestDto> {

    @Override
    public boolean isValid(BookItemRequestDto dto, ConstraintValidatorContext context) {

        if (dto == null) {
            return true;
        }

        if (dto.getStart() == null || dto.getEnd() == null) {
            return true;
        }

        return dto.getEnd().isAfter(dto.getStart());
    }
}
