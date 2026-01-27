package com.pharmacy.repository;

import com.pharmacy.entity.MemberPoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MemberPointRepository extends JpaRepository<MemberPoint, Integer> {

    List<MemberPoint> findByMemberId(String memberId);

    @Query("SELECT COALESCE(SUM(mp.point), 0) FROM MemberPoint mp WHERE mp.memberId = :memberId")
    Integer getTotalPointsByMemberId(@Param("memberId") String memberId);
}