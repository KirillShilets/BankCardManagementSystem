package com.testtask.bankcardmanager.controller;

import com.testtask.bankcardmanager.dto.request.CreateCardRequest;
import com.testtask.bankcardmanager.dto.request.UpdateCardRequest;
import com.testtask.bankcardmanager.dto.response.CardResponse;
import com.testtask.bankcardmanager.dto.response.TransactionResponse;
import com.testtask.bankcardmanager.model.Card;
import com.testtask.bankcardmanager.model.enums.CardStatus;
import com.testtask.bankcardmanager.service.CardService;
import com.testtask.bankcardmanager.service.TransactionService;
import jakarta.persistence.criteria.Predicate;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/cards")
public class CardController {

    private final CardService cardService;
    private final TransactionService transactionService;

    public CardController(CardService cardService, TransactionService transactionService) {
        this.cardService = cardService;
        this.transactionService = transactionService;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<CardResponse> createCard(@Valid @RequestBody CreateCardRequest request) {
        CardResponse createdCard = cardService.createCard(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCard);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or @cardSecurityService.isOwner(authentication, #id)")
    public ResponseEntity<CardResponse> getCardById(@PathVariable Long id) {
        CardResponse cardDto = cardService.getCardById(id);
        return ResponseEntity.ok(cardDto);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Page<CardResponse>> getAllCards(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) CardStatus status,
            @RequestParam(required = false) String cardHolder,
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {

        Specification<Card> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (userId != null) {
                predicates.add(criteriaBuilder.equal(root.get("user").get("id"), userId));
            }
            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }
            if (cardHolder != null && !cardHolder.isBlank()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("cardHolder")), "%" + cardHolder.toLowerCase() + "%"));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        Page<CardResponse> cardPage = cardService.getAllCards(spec, pageable);
        return ResponseEntity.ok(cardPage);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<CardResponse> updateCard(@PathVariable Long id, @Valid @RequestBody UpdateCardRequest request) {
        CardResponse updatedCard = cardService.updateCard(id, request);
        return ResponseEntity.ok(updatedCard);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteCard(@PathVariable Long id) {
        cardService.deleteCard(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{cardId}/transactions")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<List<TransactionResponse>> getCardTransactions(@PathVariable Long cardId) {
        List<TransactionResponse> transactions = transactionService.getAllTransactionsByCardId(cardId);
        return ResponseEntity.ok(transactions);
    }
}