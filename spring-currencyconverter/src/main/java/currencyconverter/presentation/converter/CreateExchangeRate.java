package currencyconverter.presentation.converter;

public class CreateExchangeRate {
    public Double amount;

    public void Converter(Double amount, Double rate) {
        this.amount = amount * rate;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }
}
