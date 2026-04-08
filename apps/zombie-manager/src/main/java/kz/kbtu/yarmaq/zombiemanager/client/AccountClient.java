package kz.kbtu.yarmaq.zombiemanager.client;

import kz.kbtu.yarmaq.zombiemanager.dto.AccountResponse;
import kz.kbtu.yarmaq.zombiemanager.dto.UpdateBalanceRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "accounts-service", url = "${services.accounts-service.url}")
public interface AccountClient {

    @GetMapping("/api/v1/internal/accounts/by-type-currency")
    AccountResponse getAccountByTypeAndCurrency(
        @RequestParam("type") String type,
        @RequestParam("currency") String currency
    );

    @PostMapping("/api/v1/internal/accounts/{id}/balance")
    void updateBalance(
        @PathVariable("id") String id,
        @RequestBody UpdateBalanceRequest request
    );
}
