package com.nnenna.portal.billing;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CustomerView(Long id, String fullName, String email) {
}
