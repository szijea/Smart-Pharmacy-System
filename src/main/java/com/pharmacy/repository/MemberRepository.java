package com.pharmacy.repository;

import com.pharmacy.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, String> {

    // 根据手机号查找会员
    Optional<Member> findByPhone(String phone);

    // 根据会员卡号查找
    Optional<Member> findByCardNo(String cardNo);

    // 根据等级查找会员
    List<Member> findByLevel(Integer level);

    // 查找积分大于指定值的会员
    List<Member> findByPointsGreaterThan(Integer points);

    // 根据姓名模糊搜索
    // 修复: 原 @Query("SELECT m FROM Member m WHERE m.name LIKE %:name%") 在某些情况下不会正确匹配, 改为派生查询或 CONCAT 形式
    // 使用派生查询自动生成: LIKE %name%
    List<Member> findByNameContaining(String name);

    // 备用显式 JPQL（如需强制使用，可改调用此方法）
    @Query("SELECT m FROM Member m WHERE m.name LIKE CONCAT('%', :kw, '%')")
    List<Member> findByNameLikeKeyword(@Param("kw") String kw);

    // 统计各等级会员数量
    @Query("SELECT m.level, COUNT(m) FROM Member m GROUP BY m.level")
    List<Object[]> countMembersByLevel();

    // 在现有的 MemberRepository 中添加以下方法：

    // 统计总会员数
    @Query("SELECT COUNT(m) FROM Member m")
    Long countTotalMembers();

    // 统计最近30天新增会员
    @Query("SELECT COUNT(m) FROM Member m WHERE m.createTime >= :startDate")
    Long countNewMembersSince(@Param("startDate") LocalDateTime startDate);

    // 查找最近注册的会员
    List<Member> findTop10ByOrderByCreateTimeDesc();

    // 根据姓名或手机号模糊搜索
    List<Member> findByNameContainingOrPhoneContaining(String name, String phone);
}