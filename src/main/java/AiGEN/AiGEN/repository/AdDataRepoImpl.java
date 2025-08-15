package AiGEN.AiGEN.repository;

import AiGEN.AiGEN.domain.AdData;
import AiGEN.AiGEN.domain.AdPlatform;
import AiGEN.AiGEN.domain.UploadBatch;
import AiGEN.AiGEN.domain.UserSession;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class AdDataRepoImpl implements AdDataRepo {
    private final EntityManager em;

    @Override
    public AdData save(AdData adData) {
        em.persist(adData);
        return adData;
    }

    @Override
    public List<Object[]> sumByPlatformForBatchOfSession(UserSession session, Long batchId) {
        return em.createQuery("""
            select p.code,
                   coalesce(sum(a.cost),0),
                   coalesce(sum(a.clicks),0),
                   coalesce(sum(a.conversions),0),
                   coalesce(sum(a.revenue),0)
            from AdData a
            join a.platform p
            where a.userSession = :session
              and a.batch.id = :batchId
            group by p.code
            order by p.code
        """, Object[].class)
                .setParameter("session", session)
                .setParameter("batchId", batchId)
                .getResultList();
    }

    @Override
    public List<Object[]> sumByMonthForBatchAndPlatformOfSession(UserSession session, Long batchId, String platformCode) {
        return em.createQuery("""
            select function('YEAR', a.date),
                   function('MONTH', a.date),
                   coalesce(sum(a.cost),0),
                   coalesce(sum(a.clicks),0),
                   coalesce(sum(a.conversions),0),
                   coalesce(sum(a.revenue),0)
            from AdData a
            join a.platform p
            where a.userSession = :session
              and a.batch.id = :batchId
              and upper(p.code) = upper(:code)
            group by function('YEAR', a.date), function('MONTH', a.date)
            order by function('YEAR', a.date), function('MONTH', a.date)
        """, Object[].class)
                .setParameter("session", session)
                .setParameter("batchId", batchId)
                .setParameter("code", platformCode)
                .getResultList();
    }

    @Override
    public List<AdPlatform> findDistinctPlatforms(UserSession session, Long batchId) {
        return em.createQuery("""
            select distinct p
            from AdData a
            join a.platform p
            where a.userSession = :session
              and a.batch.id    = :batchId
            order by p.code
        """, AdPlatform.class)
                .setParameter("session", session)
                .setParameter("batchId", batchId)
                .getResultList();
    }
}
