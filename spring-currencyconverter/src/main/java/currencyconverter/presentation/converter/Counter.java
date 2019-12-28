package currencyconverter.presentation.converter;

import org.springframework.stereotype.Service;

@Service
public class Counter {
    public Integer counter;

    public void setCounter(Integer newCounter) {
        this.counter = newCounter;
    }

    public int getCounter() {
        return this.counter;
    }
}
