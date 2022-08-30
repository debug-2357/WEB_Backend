package com.debug.domain.entity.recruit;

import com.debug.domain.entity.BaseTimeEntity;
import com.debug.domain.entity.user.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import javax.persistence.*;
import java.util.Map;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "recruit_apply")
public class RecruitApply extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(targetEntity = User.class)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(targetEntity = RecruitPeriod.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "recruit_period_id")
    private RecruitPeriod recruitPeriod;

    @Convert(converter = MapToStringConverter.class)
    @Column(name = "content", columnDefinition = "MEDIUMTEXT", nullable = false)
    private Map<String, String> content;

    @Column(name = "pass")
    @Convert(converter = BooleanToYNConverter.class)
    private Boolean isPass;

    @Builder
    public RecruitApply(User user, RecruitPeriod recruitPeriod, Map<String, String> content, Boolean isPass) {
        this.user = user;
        this.recruitPeriod = recruitPeriod;
        this.content = content;
        this.isPass = isPass;
    }

    public void updateContent(Map<String, String> content) {
        this.content = content;
    }
}

@Converter
class BooleanToYNConverter implements AttributeConverter<Boolean, String>{
    @Override
    public String convertToDatabaseColumn(Boolean attribute){
        if (attribute == null) return "";

        return attribute ? "Y" : "N";
    }

    @Override
    public Boolean convertToEntityAttribute(String dbData){
        if (dbData.equals("")) return null;

        return "Y".equals(dbData);
    }
}

@Converter
class MapToStringConverter implements AttributeConverter<Map<String, String>, String>{

    private final ObjectMapper objectMapper = Jackson2ObjectMapperBuilder.json().build();

    @Override
    public String convertToDatabaseColumn(Map<String, String> attribute) {
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Map<String, String> convertToEntityAttribute(String dbData) {
        try {
            return objectMapper.readValue(dbData, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}