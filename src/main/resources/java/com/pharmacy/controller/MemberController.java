package com.pharmacy.controller;

import com.pharmacy.entity.Member;
import com.pharmacy.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/members")
public class MemberController {

    @Autowired
    private MemberService memberService;

    @GetMapping
    public List<Member> getAllMembers() {
        return memberService.findAll();
    }

    @GetMapping("/{memberId}")
    public ResponseEntity<Member> getMemberById(@PathVariable String memberId) {
        Optional<Member> member = memberService.findById(memberId);
        return member.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/phone/{phone}")
    public ResponseEntity<Member> getMemberByPhone(@PathVariable String phone) {
        Optional<Member> member = memberService.findByPhone(phone);
        return member.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createMember(@RequestBody Member member) {
        try {
            // 如果前端没有提供memberId，自动生成
            if (member.getMemberId() == null || member.getMemberId().trim().isEmpty()) {
                member.setMemberId(memberService.generateNextMemberId());
            }

            Member savedMember = memberService.createMember(
                    member.getMemberId(),
                    member.getName(),
                    member.getPhone()
            );

            // 设置其他可选字段
            if (member.getCardNo() != null) {
                savedMember.setCardNo(member.getCardNo());
            }
            if (member.getAllergicHistory() != null) {
                savedMember.setAllergicHistory(member.getAllergicHistory());
            }
            if (member.getMedicalCardNo() != null) {
                savedMember.setMedicalCardNo(member.getMedicalCardNo());
            }

            return ResponseEntity.ok(savedMember);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{memberId}")
    public ResponseEntity<?> updateMember(@PathVariable String memberId, @RequestBody Member member) {
        try {
            if (!memberId.equals(member.getMemberId())) {
                return ResponseEntity.badRequest().body("会员ID不匹配");
            }

            Member updatedMember = memberService.updateMember(member);
            return ResponseEntity.ok(updatedMember);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{memberId}")
    public ResponseEntity<?> deleteMember(@PathVariable String memberId) {
        try {
            memberService.deleteMember(memberId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{memberId}/points/add")
    public ResponseEntity<?> addPoints(@PathVariable String memberId, @RequestParam int points) {
        boolean success = memberService.addPoints(memberId, points);
        if (success) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{memberId}/points/use")
    public ResponseEntity<?> usePoints(@PathVariable String memberId, @RequestParam int points) {
        boolean success = memberService.usePoints(memberId, points);
        if (success) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.badRequest().body("积分不足或会员不存在");
        }
    }

    @GetMapping("/check-phone")
    public ResponseEntity<Boolean> checkPhoneExists(@RequestParam String phone) {
        boolean exists = memberService.isPhoneExists(phone);
        return ResponseEntity.ok(exists);
    }
}