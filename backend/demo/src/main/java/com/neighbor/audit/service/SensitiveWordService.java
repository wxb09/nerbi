package com.neighbor.audit.service;

import com.github.houbb.sensitive.word.api.IWordDeny;
import com.github.houbb.sensitive.word.bs.SensitiveWordBs;
import com.neighbor.audit.dto.AuditResult;
import com.neighbor.audit.dto.CreateSensitiveWordRequest;
import com.neighbor.audit.dto.SensitiveWordDTO;
import com.neighbor.audit.entity.AuditLog;
import com.neighbor.audit.entity.SensitiveWord;
import com.neighbor.audit.repository.AuditLogRepository;
import com.neighbor.audit.repository.SensitiveWordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SensitiveWordService {

    private static final Logger log = LoggerFactory.getLogger(SensitiveWordService.class);

    private final SensitiveWordRepository sensitiveWordRepository;
    private final AuditLogRepository auditLogRepository;
    private volatile SensitiveWordBs sensitiveWordBs;
    private volatile boolean auditEnabled = true;

    public SensitiveWordService(SensitiveWordRepository sensitiveWordRepository,
                                AuditLogRepository auditLogRepository) {
        this.sensitiveWordRepository = sensitiveWordRepository;
        this.auditLogRepository = auditLogRepository;
        refreshWordBuffer();
    }

    @Scheduled(fixedRate = 60000)
    public synchronized void refreshWordBuffer() {
        List<String> words = sensitiveWordRepository.findAllEnabledWords();
        
        IWordDeny wordDeny = new IWordDeny() {
            @Override
            public List<String> deny() {
                return words;
            }
        };
        
        sensitiveWordBs = SensitiveWordBs.newInstance()
                .wordDeny(wordDeny)
                .init();
        log.info("[SensitiveWord] 敏感词库已刷新，共 {} 个敏感词", words.size());
    }

    public boolean isAuditEnabled() {
        return auditEnabled;
    }

    public void setAuditEnabled(boolean enabled) {
        this.auditEnabled = enabled;
        log.info("[SensitiveWord] 审核开关已设置为: {}", enabled);
    }

    public AuditResult auditText(String text, String targetType, Long targetId) {
        if (!auditEnabled) {
            return AuditResult.pass();
        }

        if (text == null || text.isBlank()) {
            return AuditResult.pass();
        }

        List<String> foundWords = sensitiveWordBs.findAll(text);
        
        if (foundWords.isEmpty()) {
            return AuditResult.pass();
        }

        String wordsStr = String.join(",", foundWords);
        log.info("[Audit] 发现敏感词: {} - 目标: {}#{}", wordsStr, targetType, targetId);

        saveAuditLog(targetType, targetId, text, "BLOCK", wordsStr);

        return AuditResult.block("内容包含敏感词", wordsStr);
    }

    public boolean containsSensitiveWord(String text) {
        if (!auditEnabled || text == null || text.isBlank()) {
            return false;
        }
        return sensitiveWordBs.contains(text);
    }

    public String filterText(String text) {
        if (text == null || text.isBlank()) {
            return text;
        }
        return sensitiveWordBs.replace(text);
    }

    private void saveAuditLog(String targetType, Long targetId, String content, 
                              String result, String sensitiveWords) {
        AuditLog log = new AuditLog();
        log.setTargetType(targetType);
        log.setTargetId(targetId);
        log.setContentSnapshot(content.length() > 500 ? content.substring(0, 500) : content);
        log.setAuditResult(result);
        log.setSensitiveWords(sensitiveWords);
        auditLogRepository.save(log);
    }

    @Transactional
    public SensitiveWordDTO addWord(CreateSensitiveWordRequest request) {
        if (sensitiveWordRepository.existsByWord(request.word())) {
            throw new IllegalArgumentException("敏感词已存在: " + request.word());
        }

        SensitiveWord word = new SensitiveWord();
        word.setWord(request.word());
        word.setCategory(request.category());
        word.setSeverity(request.severity());
        word.setStatus(1);

        SensitiveWord saved = sensitiveWordRepository.save(word);
        refreshWordBuffer();

        return toDTO(saved);
    }

    @Transactional
    public void deleteWord(Long id) {
        sensitiveWordRepository.deleteById(id);
        refreshWordBuffer();
    }

    @Transactional
    public SensitiveWordDTO toggleWordStatus(Long id) {
        SensitiveWord word = sensitiveWordRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("敏感词不存在: " + id));
        word.setStatus(word.getStatus() == 1 ? 0 : 1);
        SensitiveWord saved = sensitiveWordRepository.save(word);
        refreshWordBuffer();
        return toDTO(saved);
    }

    public Page<SensitiveWordDTO> getWords(String keyword, String category, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<SensitiveWord> words;

        if (keyword != null && !keyword.isBlank()) {
            words = sensitiveWordRepository.findByWordContaining(keyword, pageable);
        } else if (category != null && !category.isBlank()) {
            words = sensitiveWordRepository.findByCategory(category, pageable);
        } else {
            words = sensitiveWordRepository.findAll(pageable);
        }

        return words.map(this::toDTO);
    }

    public List<String> getCategories() {
        return List.of("INSULT", "PORN", "POLITICAL", "AD", "OTHER");
    }

    private SensitiveWordDTO toDTO(SensitiveWord word) {
        return new SensitiveWordDTO(
                word.getId(),
                word.getWord(),
                word.getCategory(),
                word.getSeverity(),
                word.getStatus(),
                word.getCreatedAt() != null ? word.getCreatedAt().toString() : null
        );
    }
}
