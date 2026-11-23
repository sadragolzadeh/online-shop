package org.example.model.valueobject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import org.example.model.exception.ValidationException;

/** A non-negative monetary amount (Toman) with two decimal digits of precision. */
public record Money(BigDecimal amount) implements Comparable<Money> {

    public static final Money ZERO = new Money(BigDecimal.ZERO);

    public Money {
        if (amount == null) {
            throw new ValidationException("Amount must not be empty.");
        }
        if (amount.signum() < 0) {
            throw new ValidationException("Amount must not be negative.");
        }
        amount = amount.setScale(2, RoundingMode.HALF_UP);
    }

    public static Money of(long value) {
        return new Money(BigDecimal.valueOf(value));
    }

    public Money add(Money other) {
        return new Money(amount.add(other.amount));
    }

    public Money multiply(int factor) {
        return new Money(amount.multiply(BigDecimal.valueOf(factor)));
    }

    public boolean isZero() {
        return amount.signum() == 0;
    }

    @Override
    public int compareTo(Money other) {
        return amount.compareTo(other.amount);
    }

    @Override
    public String toString() {
        DecimalFormat format = new DecimalFormat("#,##0.##", DecimalFormatSymbols.getInstance(Locale.US));
        return format.format(amount) + " Toman";
    }
}
