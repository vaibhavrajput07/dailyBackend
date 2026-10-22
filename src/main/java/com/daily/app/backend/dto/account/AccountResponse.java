package com.daily.app.backend.dto.account;

/** @param email the verified recovery email, or null if none has been added yet */
public record AccountResponse(String ownerName, String mobileNumber, String email) {
}
