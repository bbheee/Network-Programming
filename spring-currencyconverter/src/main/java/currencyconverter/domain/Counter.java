package currencyconverter.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "COUNTER")
public class Counter implements CounterDTO {

    @Id
    @Column(name = "id")
    private Integer id;
    @NotNull
    @Column(name = "counter")
    private Integer counter;

    @Override
    public Integer getCounter() {
        return this.counter;
    }

    public void counterIncrease() {
        this.counter = this.counter + 1;
    }

    public Counter() {
    }

}