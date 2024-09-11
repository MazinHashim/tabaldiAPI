package com.tabaldi.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.List;

@Entity
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "categories")
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id", unique = true, nullable = false)
    private long categoryId;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private String arName;
    @Column(nullable = false, columnDefinition = "boolean default true")
    private boolean isPublished;
//    @OneToMany(mappedBy = "category")
//    private List<Product> products;
    @JoinColumn(name = "vendor_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @ManyToOne
    @JsonIgnore
    private Vendor vendor;

}
