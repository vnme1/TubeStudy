package com.tubestudy.tracker.service;

import com.tubestudy.tracker.entity.VideoProgress;
import com.tubestudy.tracker.repository.VideoProgressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CsvExportService {
    private final VideoProgressRepository videoProgressRepository;

    /**
     * 모든 학습 기록을 CSV 형식으로 내보냅니다. (UTF-8 BOM 포함)
     * Excel에서 한글이 깨지지 않도록 BOM을 추가합니다.
     */
    @Transactional(readOnly = true)
    public byte[] exportStudyRecordsAsCsv() throws IOException {
        List<VideoProgress> records = videoProgressRepository.findAll();

        StringWriter stringWriter = new StringWriter();

        // CSV 헤더
        stringWriter.append("영상ID,제목,채널명,학습시간(분),마지막 진도,동기화시간\n");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        // 데이터 행
        for (VideoProgress record : records) {
            stringWriter.append(escapeCsv(record.getVideoId())).append(",");
            stringWriter.append(escapeCsv(record.getTitle())).append(",");
            stringWriter.append(escapeCsv(record.getChannel())).append(",");
            long watchMinutes = Math.round(record.getStudyTimeSeconds() / 60.0);
            stringWriter.append(String.valueOf(watchMinutes)).append(",");
            stringWriter.append(String.valueOf(Math.round(record.getLastProgressSeconds()))).append(",");
            stringWriter.append(record.getLastSyncedAt() != null ? record.getLastSyncedAt().format(formatter) : "")
                    .append("\n");
        }

        // UTF-8 BOM 추가 (Excel에서 한글 인식)
        byte[] bom = new byte[] { (byte) 0xEF, (byte) 0xBB, (byte) 0xBF };
        byte[] content = stringWriter.toString().getBytes(StandardCharsets.UTF_8);
        
        byte[] result = new byte[bom.length + content.length];
        System.arraycopy(bom, 0, result, 0, bom.length);
        System.arraycopy(content, 0, result, bom.length, content.length);
        
        return result;
    }

    /**
     * CSV에서 특수문자를 처리합니다.
     */
    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
