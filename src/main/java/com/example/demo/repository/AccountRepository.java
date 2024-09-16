package com.example.demo.repository;

import com.example.demo.models.Account.Account;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface AccountRepository extends MongoRepository<Account, String> {

    @Query(value = "{ 'email' : ?0 }")
    Account findByEmail(String email);
}