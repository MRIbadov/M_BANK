package com.MMCBank.repository;

import com.MMCBank.entity.Account;
import com.MMCBank.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    @Query("SELECT t FROM Transaction t WHERE t.fromAccount = :acc OR t.toAccount = :acc " +
           "ORDER BY t.createdAt DESC")
    List<Transaction> findByAccount(@Param("acc") Account account);

    @Query("SELECT t FROM Transaction t WHERE t.fromAccount IN :accounts OR t.toAccount IN :accounts " +
           "ORDER BY t.createdAt DESC")
    List<Transaction> findByAccounts(@Param("accounts") List<Account> accounts);
}
