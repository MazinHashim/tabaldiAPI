/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tabaldi.api.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 *
 * @author Awais Waheed - Funavry Technologies
 */
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class GetManyFileDataResponse extends GenericResponse{
    private List<FDObject> filesData;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class FDObject{
        private String id;
        private String data;
    }
}
