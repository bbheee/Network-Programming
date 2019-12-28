package currencyconverter.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "CURRENCY")

public class Currency implements CurrencyDTO {
    @Id
    @Column(name = "id")
    private Integer id;

    @Column(name = "country")
    private String country;

    @Override
    public int getId() {
        return this.id;
    }

    @Override
    public String getCountry() {
        return this.country;
    }

    public Currency() {
    }
}
