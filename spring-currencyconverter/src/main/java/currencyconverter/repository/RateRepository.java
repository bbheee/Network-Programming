package currencyconverter.repository;

import currencyconverter.domain.ExchangeRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional(propagation = Propagation.MANDATORY)
public interface RateRepository extends JpaRepository<ExchangeRate, Integer> {
    ExchangeRate findByFromCountryAndToCountry(int from, int to);

}
