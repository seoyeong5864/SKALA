package com.skala.ai.lab.tool;

/**
 * 이번 요청(스레드)에서 도구가 실제로 호출됐는지 추적한다.
 *
 * <p>이 질문이 "규정 질문"이었는지를 벡터 유사도 점수만으로 판단하면 신뢰할 수 없다 — 이 문서
 * 집합에서는 "12345 어디예요?" 같은 순수 주문 질문도 배송·취소 정책 문서와 0.3~0.4대 유사도가
 * 나와, 임계값을 아무리 조정해도 진짜 규정 질문(0.3대)과 깔끔하게 갈리지 않는다.
 * 그래서 "도구가 실제로 불렸는가"라는 더 확실한 신호로 출처 첨부 여부를 가른다 — 도구가 답을
 * 만들었다면 그 답은 근거 문서가 아니라 도구 실행 결과에서 나온 것이므로 출처를 붙이지 않는다.
 */
public final class ToolInvocationTracker {

    private static final ThreadLocal<Boolean> INVOKED = ThreadLocal.withInitial(() -> false);

    private ToolInvocationTracker() {
    }

    public static void markInvoked() {
        INVOKED.set(true);
    }

    public static boolean wasInvoked() {
        return INVOKED.get();
    }

    public static void reset() {
        INVOKED.remove();
    }
}
