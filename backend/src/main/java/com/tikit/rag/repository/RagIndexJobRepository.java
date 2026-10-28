package com.tikit.rag.repository;

import com.tikit.rag.entity.RagIndexJob;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RagIndexJobRepository
        extends JpaRepository<RagIndexJob, Long> {

    @Query("""
            select j.id from RagIndexJob j
            where j.status = com.tikit.rag.entity.RagIndexJobStatus.QUEUED
            order by j.createdAt asc, j.id asc
            """)
    List<Long> findQueuedJobIds(Pageable pageable);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update RagIndexJob j
            set j.status = com.tikit.rag.entity.RagIndexJobStatus.RUNNING,
                j.attemptCount = j.attemptCount + 1,
                j.leaseOwner = :owner,
                j.failureCode = null,
                j.completedAt = null
            where j.id = :id and j.status = com.tikit.rag.entity.RagIndexJobStatus.QUEUED
            """)
    int claimQueuedJob(@Param("id") Long id, @Param("owner") String owner);
}
