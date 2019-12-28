package currencyconverter.repository;

import currencyconverter.domain.Currency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


@Repository
@Transactional(propagation = Propagation.MANDATORY)
public interface CurrencyRepository extends JpaRepository<Currency, String> {
    Currency findCurrencyByCountry(String countrycode);
}
