package AiGEN.AiGEN.repository;

import AiGEN.AiGEN.domain.CreativeRequest;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;


@Repository
@RequiredArgsConstructor
public class CreativeRequestRepoImpl implements CreativeRequestRepo {

    private final EntityManager em;

    @Override
    public CreativeRequest save(CreativeRequest request) {
        if (request.getId() == null) {
            em.persist(request);
            return request;
        } else {
            return em.merge(request);
        }
    }
}
