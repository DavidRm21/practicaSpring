package com.prueba.dataservices.repository;

import com.prueba.dataservices.entity.ErrorDatabase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IErroreDatabaseRepo extends JpaRepository<ErrorDatabase, Long> {


}
