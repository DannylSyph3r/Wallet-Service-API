package dev.slethware.walletservice.service.user;

import dev.slethware.walletservice.models.entity.User;

import java.util.UUID;

public interface UserService {

    static User getLoggedInUser() {
        return UserServiceImpl.getLoggedInUser();
    }

    static UUID getCurrentUserId() {
        return UserServiceImpl.getCurrentUserId();
    }
}