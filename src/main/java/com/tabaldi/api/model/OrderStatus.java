package com.tabaldi.api.model;

public enum OrderStatus {WAITING, CONFIRMED, PROCESSING, DELIVERED, CANCELED}
// WAITING : Customer Ordered Successfully.
// CONFIRMED : Vendor Accept Customer Order and check all required items is available and then confirm the order.
// PROCESSING : When Order is under preparing to be delivered.
// DELIVERED : When The Order has been Delivered and received by the customer.
// CANCELED : When the order canceled by customer before its confirmed.
