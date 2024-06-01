/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tabaldi.api.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 *
 * @author Awais Waheed - Funavry Technologies
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetManyFileDataPayload {
    
    private List<FilePathObject> filePaths;


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class FilePathObject{
        private String id;
        private String path;
        
    }
}
