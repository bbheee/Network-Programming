package currencyconverter.application;

import currencyconverter.domain.Currency;
import currencyconverter.domain.ExchangeRate;
import currencyconverter.repository.CounterRepository;
import currencyconverter.repository.CurrencyRepository;
import currencyconverter.repository.RateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
@Service
public class ExchangeService {
    @Autowired
    private CounterRepository counterRepository;
    @Autowired
    private CurrencyRepository currencyRepository;
    @Autowired
    private RateRepository rateRepository;

    private Currency from;
    private Currency to;
    private ExchangeRate rate;

    public double getExchangeRate(String rateFrom, String rateTo) {
        setCurrencyId(rateFrom, rateTo);
        getConverterRate();
        return this.rate.getRate();
    }

    public void setCurrencyId(String rateFrom, String rateTo) {
        this.from = currencyRepository.findCurrencyByCountry(rateFrom);
        this.to = currencyRepository.findCurrencyByCountry(rateTo);
    }

    public void getConverterRate() {
        this.rate = rateRepository.findByFromCountryAndToCountry(from.getId(), to.getId());
    }

    public void setExchangeRate(String rateFrom, String rateTo, Double newRate) {
        setCurrencyId(rateFrom, rateTo);
        this.rate = rateRepository.findByFromCountryAndToCountry(from.getId(), to.getId());
        this.rate.setRate(newRate);
    }

    public Integer getNumberConverter() {
        return counterRepository.findCounterById(0).getCounter();
    }

    public void increaseNumOfConverter() {
        counterRepository.findCounterById(0).counterIncrease();
    }

}
