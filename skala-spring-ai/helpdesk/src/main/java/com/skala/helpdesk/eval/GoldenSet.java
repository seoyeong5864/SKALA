package com.skala.helpdesk.eval;

import java.util.List;

public record GoldenSet(String q, List<String> must, String src) {}
