package com.skala.ai.lab.service;

import com.skala.ai.lab.domain.Order;
import com.skala.ai.lab.dto.SummaryResponse;
import com.skala.ai.lab.repository.OrderRepository;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class OrderSummaryService {

    private final OrderRepository orders;
    private final ChatClient summaryChat;

    public OrderSummaryService(OrderRepository orders, ChatClient summChatClient){
        this.orders = orders;
        this.summaryChat = summChatClient;
    }

    public SummaryResponse summarize(String orderId, String userId){
        Order order = orders.findByIdAndOwnerId(orderId, userId)
            .orElseThrow(()-> new OrderNotFoundException(orderId));

        String summary;
        try {
            summary = callModel(order);
        } catch (Exception e) {
            summary = order.getItem() + " · " + order.getStatus().label();
        }
        return new SummaryResponse(order.getId(), summary);
    }

    private String callModel(Order order) {
        return summaryChat.prompt()
                .user(u -> u.text("주문번호 {id} · 상품 {item} · 상태 {status} · 도착예정 {eta}"
                        + "\n위 정보를 한 문장으로 요약해 줘.")
                    .param("id", order.getId()).param("item", order.getItem())
                    .param("status", order.getStatus().label()).param("eta", order.getEta()))
                    .call().content();
    }

}