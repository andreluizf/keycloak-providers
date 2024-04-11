package com.matera.keycloak.api;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
@Slf4j
public class EventEntityRepository {

    public static SessionDetailsDTO getClientIdBySessionId(String sessionId, EntityManager em) {
        Query query = em.createNativeQuery(
                "select \n" +
                        " ue.USERNAME, \n" +
                        " ee.CLIENT_ID, ee.SESSION_ID  from EVENT_ENTITY ee \n" +
                        " INNER JOIN USER_ENTITY ue  ON ue.ID = ee.USER_ID\n" +
                        "where ee.SESSION_ID = :session AND ee.`TYPE` ='LOGIN' LIMIT 1", SessionDetailsDTO.class);
        query.setParameter("session",  sessionId);
        SessionDetailsDTO dto = (SessionDetailsDTO) query.getSingleResult();
  log.info("+++++++++++++ ¨¨¨¨¨¨¨ ================ busca ::{}",dto.toString());
        return dto;
    }
}