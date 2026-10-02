package com.fxlso.requests;

// using Integer because this request is technically optional
public record ApiKeyGenRequest(Integer requestLimit, Integer limitResetMs) {}
