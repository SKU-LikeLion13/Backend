package AiGEN.AiGEN.repository;

import AiGEN.AiGEN.domain.UploadBatch;
import AiGEN.AiGEN.domain.UserSession;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UploadBatchRepoImpl implements UploadBatchRepo {
    private final EntityManager em;

    @Override
    public UploadBatch save(UploadBatch entity) {
        if (entity.getId() == null) {
            em.persist(entity);
            return entity;
        } else {
            return em.merge(entity);
        }
    }

    @Override
    public Optional<UploadBatch> findById(Long id) {
        return Optional.ofNullable(em.find(UploadBatch.class, id));
    }

    @Override
    public List<UploadBatch> findBySession(UserSession session) {
        return em.createQuery("""
                select b
                from UploadBatch b
                where b.userSession = :session
                order by b.uploadedAt desc
                """, UploadBatch.class)
                .setParameter("session", session)
                .getResultList();
    }

    @Override
    public List<String> findAllFilenames(String anonId, int limit) {
        return em.createQuery("""
            select b.filename
            from UploadBatch b
            where b.userSession.anonId = :anonId
            order by b.uploadedAt desc
            """, String.class)
                .setParameter("anonId", anonId)
                .setMaxResults(Math.max(1, limit))
                .getResultList();
    }

    @Override
    public Optional<Long> findLatestIdBySession(String anonId) {
        var list = em.createQuery("""
            select b.id
            from UploadBatch b
            where b.userSession.anonId = :anonId
            order by b.uploadedAt desc
            """, Long.class)
                .setParameter("anonId", anonId)
                .setMaxResults(1)
                .getResultList();
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

}
