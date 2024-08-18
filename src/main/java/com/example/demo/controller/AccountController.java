package com.example.demo.controller;

import com.example.demo.models.AppUser;
import com.example.demo.models.SignIn;
import com.example.demo.models.SignUp;
import com.example.demo.repository.AppUserRepository;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Date;
import java.util.HashMap;

@RestController
@RequestMapping("/api/v1/auth")
public class AccountController {
    @Value("${security.jwt.secret-key}")
    private String jwtSecretKey;

    @Value("${security.jwt.issuer}")
    private String jwtIssuer;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private AuthenticationManager authenticationManager;


    @GetMapping("/profile")
    public ResponseEntity<Object> profile(Authentication auth) {
        var response = new HashMap<String, Object>();
        response.put("Username", auth.getName());
        response.put("Authorities", auth.getAuthorities());

        var appUser = appUserRepository.findByUsername(auth.getName());
        response.put("User", appUser);

        return ResponseEntity.ok(response);
    }
    @PostMapping("/signin")
    public ResponseEntity<Object> signin  (@Valid @RequestBody SignIn signIn, BindingResult result) {
        // Validation

        if(result.hasErrors()){
            var errorsList = result.getAllErrors();
            var errorsMap = new HashMap<String, String>();

            for(int i = 0; i < errorsList.size(); i++){
                var error = (FieldError) errorsList.get(i);
                errorsMap.put(error.getField(), error.getDefaultMessage());
            }

            return ResponseEntity.badRequest().body(errorsMap);
        }

        //  login
        try{
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            signIn.getUsername(),
                            signIn.getPassword()
                    )
            );

            AppUser appUser = appUserRepository.findByUsername(signIn.getUsername());

            String jwtToken = createJwtToken(appUser);

            var response = new HashMap<String, Object>();
            response.put("token", jwtToken);
            response.put("user", appUser);

            return ResponseEntity.ok(response);
        }
        catch (Exception e) {
            System.out.println("There is an Exception : ");
            e.printStackTrace();
        }

        return ResponseEntity.badRequest().body("Bad username or password");
    }

    @PostMapping("/signup")
    public ResponseEntity<Object> signup (@Valid @RequestBody SignUp signUp, BindingResult result){
        // Validation

        if(result.hasErrors()){
            var errorsList = result.getAllErrors();
            var errorsMap = new HashMap<String, String>();

            for(int i = 0; i < errorsList.size(); i++){
                var error = (FieldError) errorsList.get(i);
                errorsMap.put(error.getField(), error.getDefaultMessage());
            }

            return ResponseEntity.badRequest().body(errorsMap);
        }

        //  Register
        var bCryptEncoder = new BCryptPasswordEncoder();

        AppUser appUser = new AppUser();
        appUser.setFirstName(signUp.getFirstName());
        appUser.setLastName(signUp.getLastName());
        appUser.setUsername(signUp.getUsername());
        appUser.setEmail(signUp.getEmail());
        appUser.setRole("client");
        appUser.setCreatedAt(new Date());
        appUser.setPassword(bCryptEncoder.encode(signUp.getPassword()));

        //  save user into database
        try {
            var otherUser = appUserRepository.findByUsername(signUp.getUsername());
            if(otherUser != null){
                return ResponseEntity.badRequest().body("Username already taken");
            }

            otherUser = appUserRepository.findByEmail(signUp.getEmail());
            if(otherUser != null){
                return ResponseEntity.badRequest().body("Email already taken");
            }

            appUserRepository.save(appUser);

            String jwtToken = createJwtToken(appUser);
            var response = new HashMap<String, Object>();
            response.put("token", jwtToken);
            response.put("user", appUser);

            return ResponseEntity.ok(response);
        }
        catch (Exception e) {
            System.out.println("There is an Exception : ");
            e.printStackTrace();
        }

        return ResponseEntity.badRequest().body("Error");
    }
    private String createJwtToken(AppUser appUser){
        Instant now = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(jwtIssuer)
                .issuedAt(now)
                .expiresAt(now.plusSeconds(24 * 3600))
                .subject(appUser.getUsername())
                .claim("role", appUser.getRole())
                .build();

        var encoder = new NimbusJwtEncoder(
                new ImmutableSecret<>(jwtSecretKey.getBytes()));
        var params = JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(), claims);

        return encoder.encode(params).getTokenValue();
    }
}
