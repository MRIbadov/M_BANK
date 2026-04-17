package com.MMCBank.repository;

import com.MMCBank.entity.Account;
import com.MMCBank.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {
    List<Account> findByUserAndActiveTrue(User user);
    Optional<Account> findByIdAndUser(Long id, User user);

    Optional<Account> findByAccountNumber(String accountNumber);

    Boolean existsByAccountNumber(String accountNumber);
}
