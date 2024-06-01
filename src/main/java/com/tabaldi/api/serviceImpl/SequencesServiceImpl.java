package com.tabaldi.api.serviceImpl;

import com.tabaldi.api.exception.TabaldiGenericException;
import com.tabaldi.api.model.Option;
import com.tabaldi.api.model.Product;
import com.tabaldi.api.payload.OptionPayload;
import com.tabaldi.api.repository.OptionRepository;
import com.tabaldi.api.service.OptionService;
import com.tabaldi.api.service.ProductService;
import com.tabaldi.api.service.SequencesService;
import com.tabaldi.api.utils.MessagesUtils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SequencesServiceImpl implements SequencesService {

    @PersistenceContext
    EntityManager entityManager;


    @Transactional
    public Long createSequenceFor(String target, long startFrom, Long id) {
        String query = "INSERT INTO sequences_"+target+" (reference_id, last_number) VALUES (:referenceId, :startFrom)";
        if(target.equals("orders")) query = "INSERT INTO sequences_orders (last_number) VALUES (:startFrom)";

         Query runQuery = entityManager.createNativeQuery(query);
        runQuery.setParameter("startFrom", startFrom);
        if(!target.equals("orders")){
            runQuery.setParameter("referenceId", id);
        }
        int result = runQuery.executeUpdate();
        System.out.println(result);
        return startFrom;
    }
    @Transactional
    public Long resetSequenceFor(String target, long startFrom, Long id) {
        String query = "UPDATE sequences_"+target+" SET last_number=:startFrom WHERE reference_id=:referenceId";
        if(target.equals("orders")) query = "UPDATE sequences_orders SET last_number=:startFrom";
        Query runQuery = entityManager.createNativeQuery(query);
        runQuery.setParameter("startFrom", startFrom);
        if(!target.equals("orders")) {
            runQuery.setParameter("referenceId", id);
        }
        int result = runQuery.executeUpdate();
        System.out.println(result);
        return startFrom;
    }
    @Transactional
    public Long getNextSequenceFor(String target, Long id) {

        String query = "SELECT last_number from sequences_"+target+" where reference_id=:referenceId";
        if(target.equals("orders")) query = "SELECT last_number from sequences_orders";
        Query runQuery = entityManager.createNativeQuery(query);
        if(!target.equals("orders")) runQuery.setParameter("referenceId", id);
        long lastNumber = Integer.parseInt(runQuery.getSingleResult().toString());

        String updateQuery = "UPDATE sequences_"+target+" SET last_number=:nextNumber WHERE reference_id=:referenceId";
        if(target.equals("orders"))
            updateQuery = "UPDATE sequences_orders SET last_number=:nextNumber";
        Query runUpdateQuery = entityManager.createNativeQuery(updateQuery);
        runUpdateQuery.setParameter("nextNumber", lastNumber+1);
        if(!target.equals("orders")) runUpdateQuery.setParameter("referenceId", id);
        int result =runUpdateQuery.executeUpdate();
        System.out.println(result);
        return lastNumber+1;
    }
}
