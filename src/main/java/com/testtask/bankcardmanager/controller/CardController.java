package com.testtask.bankcardmanager.controller;

import com.testtask.bankcardmanager.dto.request.CreateCardRequest;
import com.testtask.bankcardmanager.dto.response.CardResponse;
import com.testtask.bankcardmanager.service.CardService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cards")
public class CardController {

    private final CardService cardService;

    public CardController(CardService cardService) {
        this.cardService = cardService;
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
}