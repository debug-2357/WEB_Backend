package com.debug.api.entity.recruit;

import com.debug.api.entity.BaseTimeEntity;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "recruit_period")
public class RecruitPeriod extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "year_of", length = 100, unique = true, nullable = false)
    private String yearOf;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    @ElementCollection
    @CollectionTable(name = "recruit_questions", joinColumns = @JoinColumn(name = "recruit_period_id"))
    @Column(columnDefinition = "MEDIUMTEXT")
    private List<String> questions;

    @OneToMany(targetEntity = RecruitApply.class, cascade = CascadeType.REMOVE)
    List<RecruitApply> recruitApplies;

    @Builder
    public RecruitPeriod(String yearOf, LocalDateTime startDate, LocalDateTime endDate, List<String> questions) {
        this.yearOf = yearOf;
        this.startDate = startDate;
        this.endDate = endDate;
        this.questions = questions;
    }

    public void updateQuestions(List<String> questions) {
        this.questions = questions;
    }
}