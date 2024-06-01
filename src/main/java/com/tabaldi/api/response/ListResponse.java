package com.tabaldi.api.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ListResponse<T> extends GenericResponse{
    private List<T> list;

    @Builder(builderMethodName = "genericBuilder")
    public ListResponse(String message, List<T> list) {
        super(message);
        this.list = list;
    }
}
