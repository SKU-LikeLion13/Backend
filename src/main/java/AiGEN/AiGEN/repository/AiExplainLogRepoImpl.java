//package AiGEN.AiGEN.repository;
//
//import AiGEN.AiGEN.domain.AiExplainLog;
//import jakarta.persistence.EntityManager;
//import org.springframework.stereotype.Repository;
//import org.springframework.transaction.annotation.Transactional;
//
//@Repository
//public class AiExplainLogRepoImpl implements AiExplainLogRepo {
//    private EntityManager em;
//
//    @Override
//    @Transactional
//    public AiExplainLog save(AiExplainLog log) {
//        em.persist(log);
//        return log;
//    }
//
//    @Override
//    @Transactional
//    public void updateStatusAndOutput(Long id, String status, String outputJson) {
//        AiExplainLog found = em.find(AiExplainLog.class, id);
//        if (found == null) return;
//
//        found.updateStatusAndOutput(status, outputJson);
//    }
//}
