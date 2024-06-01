package com.tabaldi.api.response;

import com.tabaldi.api.model.Option;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class OptionResponse extends GenericResponse{
    private String event;
    private Option option;
}
