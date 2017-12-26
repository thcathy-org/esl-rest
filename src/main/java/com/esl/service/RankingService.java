package com.esl.service;

import com.esl.dao.repository.MemberScoreRepository;
import com.esl.entity.practice.MemberScore;
import com.esl.entity.practice.MemberScoreRanking;
import com.esl.model.Member;
import com.esl.util.CollectionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

@Service
public class RankingService {
    private static Logger log = LoggerFactory.getLogger(RankingService.class);

    @Autowired
    MemberScoreRepository memberScoreRepository;

    @Value("${RankingService.numOfScore}") int numOfScoreInRank;

    private volatile MemberScoreRanking scheduledRandomRanking;

    @Async
    public CompletableFuture<MemberScoreRanking> myScoreRanking(Member member, int scoreYearMonth) {
        Optional<MemberScore> memberScore = memberScoreRepository.findByMemberAndScoreYearMonth(member, scoreYearMonth);
        if (memberScore.isPresent())
            return myScoreRanking(memberScore.get());
        else
            return CompletableFuture.completedFuture(null);
    }

    @Async
    public CompletableFuture<MemberScoreRanking> myScoreRanking(MemberScore base) {
        log.debug("start create score ranking for member: {}", base.getMember());

        CompletableFuture<List<MemberScore>> highersFutures = memberScoreRepository.findTop5HigherScore
                (base.getScoreYearMonth(), base.getScore(), base.getLastUpdatedDate());
        CompletableFuture<List<MemberScore>> lowersFutures = memberScoreRepository.findTop5LowerScore(base.getScoreYearMonth(), base.getScore(), base.getLastUpdatedDate());

        List<MemberScore> scores = CollectionUtil.concatLists(numOfScoreInRank, base, lowersFutures.join(), highersFutures.join());
        Collections.reverse(scores);
        CompletableFuture<Long> positionFutures = memberScoreRepository.countHigherScore(base.getScoreYearMonth(), scores.get(0).getScore(), scores.get(0).getLastUpdatedDate());
        long position = positionFutures.join() + 1;

        return CompletableFuture.completedFuture(new MemberScoreRanking(base, scores, false, position));
    }

    @Async
    public CompletableFuture<MemberScoreRanking> topScore(int scoreYearMoth) {
        Pageable firstFive = new PageRequest(0, 5);
        return CompletableFuture.completedFuture(new MemberScoreRanking(null, memberScoreRepository.findTopScore(scoreYearMoth, firstFive).join(), true, 1));
    }

    @Async
    public CompletableFuture<MemberScoreRanking> randomTopScore() {
        int offset = new Random().nextInt(7);
        if (offset == 6) {
            return topScore(MemberScore.allTimesMonth());
        } else {
            //return topScore(MemberScore.lastMonthBy(offset));
            return topScore(MemberScore.allTimesMonth());
        }
    }

    @Scheduled(fixedRate=600000)
    public void changeRandomRanking() {
        log.info("changeRandomRanking");
        do {
            randomTopScore().thenAccept(r -> scheduledRandomRanking = r);
        } while (scheduledRandomRanking == null || scheduledRandomRanking.getScores().size() < 1);
    }

    public MemberScoreRanking getScheduledRandomRanking() {
        return scheduledRandomRanking;
    }
}
