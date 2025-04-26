package com.testtask.bankcardmanager.controller;

import com.testtask.bankcardmanager.dto.request.TransferRequest;
import com.testtask.bankcardmanager.dto.request.WithdrawalRequest;
import com.testtask.bankcardmanager.dto.response.CardResponse;
import com.testtask.bankcardmanager.dto.response.TransactionResponse;
import com.testtask.bankcardmanager.service.CardService;
import com.testtask.bankcardmanager.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final CardService cardService;
    private final TransactionService transactionService;

    public UserController(CardService cardService, TransactionService transactionService) {
        this.cardService = cardService;
        this.transactionService = transactionService;
    }

    @GetMapping("/cards")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<CardResponse>> getCurrentUserCards(@PageableDefault(size = 20, sort = "id") Pageable pageable) {
        Page<CardResponse> cardPage = cardService.getCurrentUserCards(pageable);
        return ResponseEntity.ok(cardPage);
    }

    @GetMapping("/transactions")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<TransactionResponse>> getCurrentUserTransactions() {
        List<TransactionResponse> transactions = transactionService.getTransactionsForCurrentUser();
        return ResponseEntity.ok(transactions);
    }

    @PostMapping("/cards/{id}/block")
    @PreAuthorize("isAuthenticated() and @cardSecurityService.isOwner(authentication, #id)")
    public ResponseEntity<Void> blockCard(@PathVariable Long id) {
        cardService.blockCard(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/cards/transfer")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> transferFunds(@Valid @RequestBody TransferRequest request) {
        cardService.transferFunds(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/cards/{id}/withdraw")
    @PreAuthorize("isAuthenticated() and @cardSecurityService.isOwner(authentication, #id)")
    public ResponseEntity<TransactionResponse> withdrawFunds(@PathVariable Long id, @Valid @RequestBody WithdrawalRequest request) {
        TransactionResponse transaction = cardService.withdrawFunds(id, request);
        return ResponseEntity.ok(transaction);
    }
}