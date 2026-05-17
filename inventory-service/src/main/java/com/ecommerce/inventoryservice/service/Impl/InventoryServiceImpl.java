package com.ecommerce.inventoryservice.service.Impl;

import com.ecommerce.inventoryservice.dto.InventoryRequest;
import com.ecommerce.inventoryservice.dto.InventoryResponse;
import com.ecommerce.inventoryservice.exception.ResourceNotFoundException;
import com.ecommerce.inventoryservice.mapper.InventoryMapper;
import com.ecommerce.inventoryservice.model.Inventory;
import com.ecommerce.inventoryservice.repository.InventoryRepository;
import com.ecommerce.inventoryservice.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@RefreshScope
public class InventoryServiceImpl implements InventoryService {
    private final InventoryRepository inventoryRepository;

    private final InventoryMapper inventoryMapper;

    @Value("${inventory.allow-backorders:false}")
    private boolean allowBackorders;

    @Override
    @Transactional(readOnly = true)
    public boolean isInStock(String sku, Integer quantity) {

        if (allowBackorders) {
            log.warn("MODO BACKORDERS ACTIVO: Autorizando stock para SKU: {}", sku);
            return true;
        }

        return inventoryRepository.findBySku(sku)
                .map(inventory -> inventory.getQuantity() >= quantity)
                .orElse(false);
    }

    @Override
    @Transactional
    public InventoryResponse createInventory(InventoryRequest inventoryRequest) {
        boolean exists = inventoryRepository.existsBySku(inventoryRequest.getSku());
        if (exists) {
            throw new RuntimeException("El inventario para el SKU " + inventoryRequest.getSku() + " ya existe");
        }

        Inventory inventory = inventoryMapper.toModel(inventoryRequest);
        Inventory savedInventory = inventoryRepository.save(inventory);

        log.info("Inventario creado para el SKU: {}", savedInventory);

        return inventoryMapper.toInventoryResponseDTO(inventory);
    }

    @Override
    public List<InventoryResponse> getAllInventory() {
        return inventoryRepository.findAll().stream()
                .map(inventoryMapper::toInventoryResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryResponse> getInventoryByProductId(String productId) {
        return List.of();
    }

    @Override
    @Transactional
    public InventoryResponse updateInventory(Long id, InventoryRequest inventoryRequest) {
        Inventory inventory = inventoryRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Inventario", "id", id));
        inventory.setSku(inventoryRequest.getSku());
        inventory.setQuantity(inventoryRequest.getQuantity());
        inventoryRepository.save(inventory);

        log.info("Inventario actualizado para el ID: {}", id);

        return inventoryMapper.toInventoryResponseDTO(inventory);
    }

    @Override
    @Transactional
    public void deleteInventory(Long id) {
        if(!inventoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Inventario", "id", id);
        }
        inventoryRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void reduceStock(String sku, Integer quantity) {
        Inventory inventory = inventoryRepository.findBySku(sku).orElseThrow(() -> new ResourceNotFoundException("Inventario", "sku", sku));

        if(inventory.getQuantity() < quantity) {
            throw new RuntimeException("Stok insuficiente para el SKU " + inventory.getSku());
        }
        inventory.setQuantity(inventory.getQuantity() - quantity);

        inventoryRepository.save(inventory);
    }
}
