package com.MMCBank.dto;
public record UserProfileResponse(
    Long id, String firstName, String lastName,
    String email, String username, String phoneNumber
) {}
