package com.fxlso.objects;

import java.util.UUID;

public record RefreshToken(String value, UUID jti) {}
