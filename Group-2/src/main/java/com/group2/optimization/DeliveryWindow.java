package com.group2.optimization;

/** A candidate delivery slot a single rider could carry out, from {@code startTime} to {@code endTime}. */
public record DeliveryWindow(String orderId, double startTime, double endTime) {
}
