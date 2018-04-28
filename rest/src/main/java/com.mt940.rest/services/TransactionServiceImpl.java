package com.mt940.rest.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mt940.dao.jpa.MT940StatementDao;
import com.mt940.dao.jpa.MT940TransactionDao;
import com.mt940.domain.mt940.MT940Transaction;
import com.mt940.domain.mt940.MT940TransactionSearchRequest;
import com.mt940.rest.dto.TransactionView;
import lombok.extern.slf4j.Slf4j;
import ma.glasnost.orika.MapperFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

@Slf4j
@Service
public class TransactionServiceImpl implements TransactionService {

    @Autowired
    MT940TransactionDao mt940TransactionDao;

    @Autowired
    MT940StatementDao mt940StatementDao;

    @Autowired
    MapperFacade mapper;

    @Autowired
    ObjectMapper objectMapper;

    @Override
    public TransactionView findById(long transactionId) {
        MT940Transaction transaction = mt940TransactionDao.findById(transactionId);
        TransactionView transactionView = mapper.map(transaction, TransactionView.class);

        transactionView.setStatementId(transaction.getStatement().getId());
        return transactionView;
    }

    @Override
    public void addTransaction(TransactionView transactionView) {
        MT940Transaction transaction = mapper.map(transactionView, MT940Transaction.class);
        transaction.setStatement(mt940StatementDao.findById(transactionView.getStatementId()));
        mt940TransactionDao.save(transaction);
    }

    @Override
    public void updateTransaction(TransactionView transactionView) {
        MT940Transaction transaction = mt940TransactionDao.findById(transactionView.getId());
        mapper.map(transactionView, transaction);
        if (transactionView.getStatementId() != null)
            transaction.setStatement(mt940StatementDao.findById(transactionView.getStatementId()));
        transaction.setId(transactionView.getId());
        mt940TransactionDao.save(transaction);
    }

    @Override
    @Transactional
    public void deleteTransaction(long transactionId) {
        mt940TransactionDao.delete(mt940TransactionDao.findById(transactionId));
    }

    @Override
    public Page<TransactionView> listAllByPage(Pageable pageable, String filter) {
        MT940TransactionSearchRequest searchRequest = null;
        try {
            searchRequest = objectMapper.readValue(filter, MT940TransactionSearchRequest.class);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        //log.error(searchRequest.status.toString());

        Page<MT940Transaction> transactionList = mt940TransactionDao.findByAllNullable(searchRequest, pageable);
        return transactionList.map(source -> mapper.map(source, TransactionView.class));
    }
}