package com.skala.helpdesk.advisor;

// 민감 정보 마스킹 처리
// 로그에 남는 모든 곳에서 공통으로 사용
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
