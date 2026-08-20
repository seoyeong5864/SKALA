package com.skala.ai.lab.advisor;

/**
 * 아주 단순한 마스킹 예시 — 실제로는 도메인에 맞는 규칙이 필요하다.
 * 로그에 남는 모든 곳(질문·도구 인자)에서 공통으로 쓴다.
 */
public final class PiiMasker {

    private PiiMasker() {
    }

    public static String mask(String raw) {
        if (raw == null) {
            return null;
        }
        return raw
                .replaceAll("\\d{6}-\\d{7}", "******-*******")      // 주민등록번호 형태
                .replaceAll("\\d{4}-\\d{4}-\\d{4}-\\d{4}", "****-****-****-****") // 카드번호 형태
                .replaceAll("[\\w.+-]+@[\\w-]+\\.[\\w.]+", "***@***"); // 이메일
    }
}
