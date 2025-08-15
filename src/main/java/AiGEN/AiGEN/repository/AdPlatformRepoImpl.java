package AiGEN.AiGEN.repository;

import AiGEN.AiGEN.domain.AdPlatform;
import AiGEN.AiGEN.util.PlatformNormalizer;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class AdPlatformRepoImpl implements AdPlatformRepo {

    private final EntityManager em;

    @Override
    public AdPlatform save(AdPlatform entity) {
        if (entity.getCode() == null) throw new IllegalArgumentException("code is null");
        AdPlatform found = em.find(AdPlatform.class, entity.getCode());
        if (found == null) {
            em.persist(entity);
            return entity;
        }
        // merge 이름/active 갱신
        found = new AdPlatform(entity.getCode(), entity.getName(), entity.isActive());
        return em.merge(found);
    }

    @Override
    public Optional<AdPlatform> findByCode(String code) {
        if (code == null) return Optional.empty();
        return Optional.ofNullable(em.find(AdPlatform.class, code));
    }

    @Override
    public AdPlatform getRefOrThrow(String code) {
        AdPlatform found = em.find(AdPlatform.class, code);
        if (found == null) throw new EntityNotFoundException("ad_platform not found: " + code);
        return em.getReference(AdPlatform.class, code);
    }

    /**  표준화 → 없으면 INSERT(name=code, active=true) → getReference 반환 */
    @Override
    public AdPlatform ensureAndGetRef(String rawCode) {
        String norm = PlatformNormalizer.normalize(rawCode);
        if (norm == null || norm.isBlank()) throw new IllegalArgumentException("platformCode required");
        AdPlatform found = em.find(AdPlatform.class, norm);
        if (found == null) {
            em.persist(new AdPlatform(norm, norm, true));
        }
        return em.getReference(AdPlatform.class, norm);
    }

    @Override
    public List<AdPlatform> findAllActive() {
        return em.createQuery("select p from AdPlatform p where p.active = true order by p.code", AdPlatform.class)
                .getResultList();
    }

    @Override
    public List<AdPlatform> searchByName(String keyword, int limit, int offset) {
        String q = (keyword == null) ? "" : keyword.trim();
        return em.createQuery("select p from AdPlatform p where upper(p.name) like concat('%', upper(:q), '%') order by p.name", AdPlatform.class)
                .setParameter("q", q)
                .setFirstResult(Math.max(0, offset))
                .setMaxResults(limit > 0 ? limit : 50)
                .getResultList();
    }

    @Override
    public int setActive(String code, boolean active) {
        return em.createQuery("""
                            update AdPlatform p
                            set p.active = :active
                            where p.code = :code
                        """)
                .setParameter("active", active)
                .setParameter("code", code)
                .executeUpdate();
    }
}

