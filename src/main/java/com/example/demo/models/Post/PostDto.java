package com.example.demo.models.Post;

import jakarta.validation.constraints.NotEmpty;

public class PostDto {
    @NotEmpty
    private String title;
    @NotEmpty
    private String description;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
