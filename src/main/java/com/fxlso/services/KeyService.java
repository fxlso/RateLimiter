package com.fxlso.services;

import com.fxlso.exceptions.ApiGenerationException;
import com.fxlso.exceptions.InvalidApiKeyException;
import com.fxlso.exceptions.NoActiveKeysException;
import com.fxlso.objects.User;
import com.fxlso.repositories.KeyRepository;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class KeyService {

    private KeyRepository keyRepository;

    public KeyService(KeyRepository keyRepository) {
        this.keyRepository = keyRepository;
    }

    public boolean isValidApiKey(String apiKey) {
        boolean valid = keyRepository.isKeyValid(apiKey);

        if (!valid) {
            throw new InvalidApiKeyException("The provided API key is invalid.");
        }

        return valid;
    }

    public Map<String, Object> generateKey(User user, Integer maxRequests, Integer limitResetTimeMs) {

        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }

        if (maxRequests == null) {
            maxRequests = KeyRepository.DEFAULT_REQUEST_LIMIT;
        }

        if (limitResetTimeMs == null) {
            limitResetTimeMs = KeyRepository.DEFAULT_RESET_INTERVAL_MS;
        }

        if (maxRequests <= 0 || limitResetTimeMs <= 0) {
            throw new ApiGenerationException("maxRequests and limitResetTimeMs must be non-negative.");
        }

        if (limitResetTimeMs < KeyRepository.DEFAULT_RESET_INTERVAL_MS) {
            throw new ApiGenerationException(String.format("limitResetTimeMs must be at least %d milliseconds.", KeyRepository.DEFAULT_RESET_INTERVAL_MS));
        }

        return keyRepository.generateKey(user, maxRequests, limitResetTimeMs);
    }

    public Map<String, Object> generateKey(User user) {

        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }

        return keyRepository.generateKey(user);
    }

    public void revokeKeys(User user) {

        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }

        keyRepository.revokeKeys(user);
    }

    public User getUserByApiKey(String apiKey) {
        if (apiKey == null || apiKey.isEmpty()) {
            throw new IllegalArgumentException("API key cannot be null or empty");
        }

        return keyRepository.getUserByKey(apiKey);
    }

    public Map<String, Object> getApiKeyDetails(User user) {
        boolean activeKeys = keyRepository.doesUserHaveActiveKey(user);

        if (!activeKeys) {
            throw new NoActiveKeysException("The user does not have an active API key.");
        }

        Map<String, Object> details = keyRepository.getApiKeyDetailsByUser(user);
        return details;
    }



}