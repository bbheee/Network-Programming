package currencyconverter.presentation.converter;

import javax.validation.constraints.*;

public class CurrencyRate {
    @NotBlank(message = "Please specify the following currency: SEK, EURO, USD, DKK")
    @Pattern(regexp = "^[\\p{L}\\p{M}*]*$", message = "Only letters are allowed")
    @Size(min = 3, max = 3, message = "Currency only valid for 3 letters")
    private String fromCurrency;

    @NotNull(message = "Please specify the currency you want to convert: SEK, EURO, USD, DKK ")
    @Pattern(regexp = "^[\\p{L}\\p{M}*]*$", message = "Only letters are allowed")
    @Size(min = 3, max = 3, message = "Currency only valid for 3 letters")
    private String toCurrency;

    @NotNull(message = "Put the amount")
    @PositiveOrZero(message = "Amount must be zero or greater")
    private Double newExchangeRate;

    public String getFromCurrency() {
        return this.fromCurrency;
    }

    public String getToCurrency() {
        return this.toCurrency;
    }

    public void setFromCurrency(String fromCurrency) {
        this.fromCurrency = fromCurrency;
    }

    public void setToCurrency(String toCurrency) {
        this.toCurrency = toCurrency;
    }

    public Double getNewExchangeRate() {
        return this.newExchangeRate;
    }

    public void setNewExchangeRate(Double newExchangeRate) {
        this.newExchangeRate = newExchangeRate;
    }
}
