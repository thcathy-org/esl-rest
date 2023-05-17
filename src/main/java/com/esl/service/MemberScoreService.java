package com.esl.service;

import com.esl.dao.repository.MemberScoreRepository;
import com.esl.entity.practice.MemberScore;
import com.esl.model.Member;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.Optional;

@Transactional
@Service
public class MemberScoreService {
    private static Logger log = LoggerFactory.getLogger(MemberScoreService.class);

    @Resource private MemberScoreRepository memberScoreRepository;

    public void addScoreToMember(Member member, int score) {
        updateMemberScore(member, score, MemberScore.allTimesMonth());
        updateMemberScore(member, score, MemberScore.thisMonth());
    }

    private void updateMemberScore(Member member, int score, int yearMonth) {
        MemberScore memberScore = findOrCreateMemberScore(member, yearMonth);
        memberScore.setLastUpdatedDate(new Date());
        memberScore.addScore(score);

        memberScoreRepository.save(memberScore);
        log.info("Updated member score: {}", score);
    }

    public MemberScore findOrCreateMemberScore(Member member, int yearMonth) {
        Optional<MemberScore> score = memberScoreRepository.findByMemberAndScoreYearMonth(member, yearMonth);
        return score.orElseGet(() -> {
            MemberScore newScore = new MemberScore(member, yearMonth);
            newScore.setCreatedDate(new Date());
            return newScore;
        });
    }

    public void deleteByMember(Member member) {
        log.info("delete all member score by member {}:{}", member.getId(), member.getEmailAddress());
        memberScoreRepository.deleteByMember(member);
    }

}
