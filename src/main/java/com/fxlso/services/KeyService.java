package com.fxlso.services;

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

    public String generateKey(User user, int maxRequests, int limitResetTimeMs) {
        return keyRepository.generateKey(user, maxRequests, limitResetTimeMs);
    }

    public String generateKey(User user) {
        return keyRepository.generateKey(user);
    }

    public void revokeKeys(User user) {
        keyRepository.revokeKeys(user);
    }

    public User getUserByApiKey(String apiKey) {
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