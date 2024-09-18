package com.example.demo.repository;

import com.example.demo.models.Account.Account;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

public interface AccountRepository extends MongoRepository<Account, String> {

    @Query(value = "{ 'email' : ?0 }")
    Account findByEmail(String email);

    @Query("{ 'firstName': { $regex: '[abc]', $options: 'i' } }")
    List<Account> findByRegex();
}