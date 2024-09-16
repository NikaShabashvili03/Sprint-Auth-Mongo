package com.example.demo.service;

import com.example.demo.models.Account.Account;
import com.example.demo.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


@Service
public class AccountService implements UserDetailsService {
    @Autowired
    private AccountRepository repo;
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Account account = repo.findByEmail(email);

        if(account != null){
            var springUser = User.withUsername(account.getEmail())
                    .password(account.getPassword())
                    .roles(account.getRole())
                    .build();

            return springUser;
        }

        return null;
    }

    public Account getAccountByEmail(String email) {
        return repo.findByEmail(email);
    }

}
