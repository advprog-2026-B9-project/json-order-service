package com.b9.json.jsonplatform.order.application.external;

import org.springframework.stereotype.Service;

@Service
public class InventoryServiceDummy {
    public boolean isStockAvailable(Long productId, Integer quantity) {
        return true;
    }

    public void reserveStock(Long productId, Integer quantity) {
        System.out.println("Dummy: Memotong stok untuk produk ID " + productId + " sebanyak " + quantity);
    }
}