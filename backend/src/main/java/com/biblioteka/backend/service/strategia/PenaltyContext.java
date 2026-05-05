package com.biblioteka.backend.service.strategia;

import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Service
public class PenaltyContext {

    private final Map<String, PenaltyStrategy> strategies = new HashMap<>();

    public PenaltyContext(List<PenaltyStrategy> strategyList) {
        for (PenaltyStrategy strategy : strategyList) {
            strategies.put(strategy.getName().toUpperCase(), strategy);
        }
    }
    public PenaltyStrategy getStrategy(String role) {
        if (role == null) {
            return strategies.get("STANDARD");
        }
        String searchKey = role.toUpperCase();
        searchKey = switch (searchKey) {
            case "BIBLIOTEKARZ", "ADMIN" -> "STAFF";
            default -> "STANDARD";
        };
        return strategies.getOrDefault(searchKey, strategies.get("STANDARD"));
    }
}