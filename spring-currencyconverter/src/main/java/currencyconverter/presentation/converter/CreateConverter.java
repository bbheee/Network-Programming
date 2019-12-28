package currencyconverter.presentation.converter;

import javax.validation.constraints.*;

public class CreateConverter {
    @NotBlank(message = "Please specify the following currency: SEK, EURO, USD, DKK")
    // The regex below should permit only characters, but asterisk is
    // unfortunately also valid.
    @Pattern(regexp = "^[\\p{L}\\p{M}*]*$", message = "Only letters are allowed")
    @Size(min = 3, max = 3, message = "Currency only valid for 3 letters")
    private String fromCurrency;

    @NotNull(message = "Please specify the currency you want to convert: SEK, EURO, USD, DKK ")
    @Pattern(regexp = "^[\\p{L}\\p{M}*]*$", message = "Only letters are allowed")
    @Size(min = 3, max = 3, message = "Currency only valid for 3 letters")
    private String toCurrency;

    @NotNull(message = "Please specify the amount ")
    @PositiveOrZero(message = "Balance must be zero or greater")
    private Double amount;

    public void setFromCurrency(String fromCurrency) {
        this.fromCurrency = fromCurrency;
    }

    public void setToCurrency(String toCurrency) {
        this.toCurrency = toCurrency;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getFromCurrency() {
        return fromCurrency;
    }

    public String getToCurrency() {
        return toCurrency;
    }

    public Double getAmount() {
        return amount;
    }
}
