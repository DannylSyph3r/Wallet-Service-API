package dev.slethware.walletservice.service.user;

import dev.slethware.walletservice.exception.UnauthorizedException;
import dev.slethware.walletservice.models.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    public static User getLoggedInUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("No authenticated user found");
        }

        return (User) authentication.getPrincipal();
    }

    public static UUID getCurrentUserId() {
        return getLoggedInUser().getId();
    }
}