package com.pharmacy.service;

import com.pharmacy.entity.Member;
import com.pharmacy.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class MemberService {

    @Autowired
    private MemberRepository memberRepository;

    // 创建新会员
    public Member createMember(String memberId, String name, String phone) {
        // 检查手机号是否已存在
        if (memberRepository.findByPhone(phone).isPresent()) {
            throw new RuntimeException("手机号已存在: " + phone);
        }

        Member member = new Member(memberId, name, phone);
        member.setCreateTime(LocalDateTime.now());
        return memberRepository.save(member);
    }

    // 根据ID查找会员
    public Optional<Member> findById(String memberId) {
        return memberRepository.findById(memberId);
    }

    // 根据手机号查找会员
    public Optional<Member> findByPhone(String phone) {
        return memberRepository.findByPhone(phone);
    }

    // 获取所有会员
    public List<Member> findAll() {
        return memberRepository.findAll();
    }

    // 更新会员信息
    public Member updateMember(Member member) {
        if (!memberRepository.existsById(member.getMemberId())) {
            throw new RuntimeException("会员不存在: " + member.getMemberId());
        }
        return memberRepository.save(member);
    }

    // 删除会员
    public void deleteMember(String memberId) {
        memberRepository.deleteById(memberId);
    }

    // 增加积分
    public boolean addPoints(String memberId, int points) {
        Optional<Member> optionalMember = memberRepository.findById(memberId);
        if (optionalMember.isPresent()) {
            Member member = optionalMember.get();
            member.addPoints(points);
            memberRepository.save(member);
            return true;
        }
        return false;
    }

    // 使用积分
    public boolean usePoints(String memberId, int points) {
        Optional<Member> optionalMember = memberRepository.findById(memberId);
        if (optionalMember.isPresent()) {
            Member member = optionalMember.get();
            boolean success = member.usePoints(points);
            if (success) {
                memberRepository.save(member);
            }
            return success;
        }
        return false;
    }

    // 检查手机号是否存在
    public boolean isPhoneExists(String phone) {
        return memberRepository.findByPhone(phone).isPresent();
    }

    // 生成下一个会员ID（简单实现）
    public String generateNextMemberId() {
        // 这里可以根据业务规则实现更复杂的ID生成逻辑
        List<Member> members = memberRepository.findAll();
        if (members.isEmpty()) {
            return "M00001";
        }

        // 获取最大的会员ID并递增
        String maxId = members.stream()
                .map(Member::getMemberId)
                .max(String::compareTo)
                .orElse("M00000");

        // 提取数字部分并递增
        int number = Integer.parseInt(maxId.substring(1)) + 1;
        return String.format("M%05d", number);
    }
}