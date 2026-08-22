package com.skala.helpdesk.tools;

import java.util.ArrayList;
import java.util.List;

// 이번 턴에서 어떤 @Tool 메서드가 실제로 호출됐는지 이름으로 표시
public final class ToolInvocationTracker {

    private static final ThreadLocal<List<String>> INVOKED = ThreadLocal.withInitial(ArrayList::new);

    private ToolInvocationTracker() {}

    public static void markInvoked(String toolName) {
        INVOKED.get().add(toolName);
    }

    public static List<String> invoked() {
        return List.copyOf(INVOKED.get());
    }

    public static boolean wasInvoked() {
        return !INVOKED.get().isEmpty();
    }

    public static void reset() {
        INVOKED.remove();
    }
}
