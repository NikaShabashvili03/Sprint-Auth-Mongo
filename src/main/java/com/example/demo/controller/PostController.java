package com.example.demo.controller;


import com.example.demo.models.Account.Account;
import com.example.demo.models.Post.Post;
import com.example.demo.models.Post.PostDto;
import com.example.demo.repository.AccountRepository;
import com.example.demo.repository.PostRepository;
import jakarta.validation.Valid;
import org.apache.tomcat.util.http.parser.Authorization;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Stream;

@RestController
@RequestMapping("/api/v1/posts")
public class PostController {

    @Autowired
    private PostRepository postRepository;
    @Autowired
    private AccountRepository accountRepository;
    @GetMapping("/all")
    public ResponseEntity<Object> all () {
        Object posts = postRepository.findAll();

        return ResponseEntity.ok(posts);
    }

    @GetMapping("/one/{id}")
    public ResponseEntity<Object> one (@PathVariable String id) {
        Optional<Post> post = postRepository.findById(id);

        if(!post.isPresent()){
            return ResponseEntity.badRequest().body("Post not found");
        }

        return ResponseEntity.ok(post);
    }

    @PostMapping("/create")
    public ResponseEntity<Object> create (@Valid @RequestBody PostDto postDto, BindingResult result, Authentication auth) {
        if(result.hasErrors()){
            var errorsList = result.getAllErrors();
            var errorsMap = new HashMap<String, String>();

            for(int i = 0; i < errorsList.size(); i++){
                var error = (FieldError) errorsList.get(i);
                errorsMap.put(error.getField(), error.getDefaultMessage());
            }

            return ResponseEntity.badRequest().body(errorsMap);
        }

        var account = accountRepository.findByEmail(auth.getName());

        if(account == null) {
            return ResponseEntity.notFound().build();
        }

        Post post = new Post();
        post.setDescription(postDto.getDescription());
        post.setTitle(postDto.getTitle());
        post.setAccount(account);
        post.setCreatedAt(new Date());

        postRepository.save(post);

        return ResponseEntity.ok("Sucess");
    }

    @GetMapping("/my")
    public ResponseEntity<Object> my(Authentication auth) {
        Account account = accountRepository.findByEmail(auth.getName());
        if(account != null){
            List<Post> posts = postRepository.findByAccountId(account.getId());

            return ResponseEntity.ok(posts);
        }



        return ResponseEntity.badRequest().body("Account not found");
    }

    @PostMapping("/{id}/like")
    public ResponseEntity<Object> like(@PathVariable String id, Authentication auth) {
        Account currentUser = accountRepository.findByEmail(auth.getName());

        if(currentUser == null) return ResponseEntity.badRequest().body("Account not found");

        Optional<Post> postOpt = postRepository.findById(id);

        if(!postOpt.isPresent()) return ResponseEntity.badRequest().body("Post not found");

        Post post = postOpt.get();

        if(post.getLikes() == null){
            post.setLikes(new ArrayList<>());
        }

        if(post.getLikes().contains(currentUser)){
            System.out.println("contains");
            post.getLikes().remove(currentUser);
        }else{
            System.out.println("not contains");
            post.getLikes().add(currentUser);
        }

        postRepository.save(post);
        return ResponseEntity.ok("Success");
    }
}
