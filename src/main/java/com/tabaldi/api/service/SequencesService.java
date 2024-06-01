package com.tabaldi.api.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;

import java.math.BigInteger;

@Service
public interface SequencesService {
    Long createSequenceFor(String target, long startFrom,Long id);
    Long resetSequenceFor(String target, long startFrom, Long id);
    Long getNextSequenceFor(String target, Long id);
}
