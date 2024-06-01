package com.tabaldi.api.response;

import com.tabaldi.api.model.Category;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class CategoryResponse extends GenericResponse{
    private String event;
    private Category category;
}
