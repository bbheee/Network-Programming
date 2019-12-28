package currencyconverter.presentation.converter;

import currencyconverter.application.ExchangeService;
import currencyconverter.domain.IllegalBankTransactionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.Valid;

/**
 * Handles all HTTP requests to context root.
 */
@Controller
@Scope("session")
public class ConverterController {
    static final String DEFAULT_PAGE_URL = "/";
    static final String SELECT_CONVERTER_URL = "converter";
    static final String CREATE_CONVERTER_URL = "create-converter";
    static final String MAKE_RATE_URL = "make-exchangerate";
    static final String ADMIN_URL = "admin";

    @Autowired
    private ExchangeService service;

    @GetMapping(DEFAULT_PAGE_URL)
    public String showDefaultView() {
        return "redirect:" + SELECT_CONVERTER_URL;
    }

    @GetMapping("/" + SELECT_CONVERTER_URL)
    public String showConversion(CreateConverter createConverter, BindingResult bindingResults, Model model) {
        if (bindingResults.hasErrors()) {
            model.addAttribute("createConverter", new CreateConverter());
        }

        return SELECT_CONVERTER_URL;
    }

    @PostMapping("/" + CREATE_CONVERTER_URL)
    public String createConverter(@Valid CreateConverter createConverter, BindingResult bindingResult, Model model) throws IllegalBankTransactionException {
        if (bindingResult.hasErrors()) {

            return SELECT_CONVERTER_URL;
        }
        if (createConverter.getFromCurrency().equalsIgnoreCase(createConverter.getToCurrency())) {
            CreateExchangeRate createExchangeRate = new CreateExchangeRate();
            createExchangeRate.setAmount(createConverter.getAmount());
            model.addAttribute("createExchangeRate", createExchangeRate);
            return SELECT_CONVERTER_URL;
        }
        CreateExchangeRate createExchangeRate = new CreateExchangeRate();

        service.increaseNumOfConverter();
        double rate = service.getExchangeRate(createConverter.getFromCurrency(), createConverter.getToCurrency());
        createExchangeRate.Converter(createConverter.getAmount(), rate);
        model.addAttribute("createExchangeRate", createExchangeRate);
        return SELECT_CONVERTER_URL;
    }

    @GetMapping("/" + ADMIN_URL)
    public String showAdminView(CurrencyRate currencyRate, Model model) {
        Counter counter = new Counter();
        counter.setCounter(service.getNumberConverter());
        model.addAttribute("counter", counter);
        return ADMIN_URL;
    }

    @PostMapping("/" + MAKE_RATE_URL)
    public String setNewRate(@Valid CurrencyRate currencyRate, BindingResult bindingResult, Model model) {
        Counter counter = new Counter();
        counter.setCounter(service.getNumberConverter());
        if (bindingResult.hasErrors()) {
            model.addAttribute("exchangeRate", currencyRate);
            model.addAttribute("counter", counter);
            return ADMIN_URL;

        } else if (currencyRate.getFromCurrency().equalsIgnoreCase(currencyRate.getToCurrency())) {
            model.addAttribute("exchangeRate", currencyRate);
            model.addAttribute("counter", counter);
            return ADMIN_URL;
        }

        service.setExchangeRate(currencyRate.getFromCurrency(), currencyRate.getToCurrency(), currencyRate.getNewExchangeRate());
        model.addAttribute("counter", counter);
        model.addAttribute("exchangeRate", currencyRate);
        return ADMIN_URL;
    }

}
