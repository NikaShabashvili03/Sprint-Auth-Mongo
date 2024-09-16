package com.example.demo.repository;

import com.example.demo.models.Post.Post;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface PostRepository extends MongoRepository<Post, String> {

//    @Query("{ 'accountId' : ?0 }")
    List<Post> findByAccountId(ObjectId accountId);
}
