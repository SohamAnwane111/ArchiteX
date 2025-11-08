package com.systemsimulator.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@Document(collection = "architectures")
public class Architecture {
    @Id
    private String id;
    private String name;
    private List<Component> components = new ArrayList<>();
    private List<Link> links = new ArrayList<>();

    // Foreign keys
    private Integer userId;
    private Integer questionId;

    // Metadata
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean submitted = false;

    public Architecture() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Architecture(String id, String name) {
        this.id = id;
        this.name = name;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void addComponent(Component component) {
        this.components.add(component);
        this.updatedAt = LocalDateTime.now();
    }

    public void addLink(Link link) {
        this.links.add(link);
        this.updatedAt = LocalDateTime.now();
    }
}

