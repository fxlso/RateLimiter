package com.fxlso.objects;

public record ApiKey(String key, String userId, int requestLimit, int limitResetTimeMs) {
}
