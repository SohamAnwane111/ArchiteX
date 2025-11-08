package com.systemsimulator.repository;

import com.systemsimulator.model.Architecture;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArchitectureRepository extends MongoRepository<Architecture, String> {

    List<Architecture> findByUserId(Integer userId);

    List<Architecture> findByQuestionId(Integer questionId);

    List<Architecture> findByUserIdAndQuestionId(Integer userId, Integer questionId);

    List<Architecture> findBySubmittedTrue();
}

