package currencyconverter.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "EXCHANGE_RATE")
public class ExchangeRate implements ExchangeRateDTO {

    @Id
    @Column(name = "id")
    private Integer id;
    @NotNull
    @Column(name = "fromCountry")
    private Integer fromCountry;
    @NotNull
    @Column(name = "toCountry")
    private Integer toCountry;
    @NotNull
    @Column(name = "rate")
    private Double rate;

    @Override
    public Double getRate() {
        return this.rate;
    }

    public void setRate(Double newRate) {
        this.rate = newRate;
    }

    public ExchangeRate() {
    }

}