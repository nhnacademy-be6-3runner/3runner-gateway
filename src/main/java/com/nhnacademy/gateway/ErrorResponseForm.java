package com.nhnacademy.gateway;

import lombok.Builder;

@Builder
public record ErrorResponseForm(String title, int status, String timestamp) {
}

