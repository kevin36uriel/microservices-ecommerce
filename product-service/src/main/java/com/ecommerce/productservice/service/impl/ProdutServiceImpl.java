package com.ecommerce.productservice.service.impl;

import com.ecommerce.productservice.dto.ProductRequestDTO;
import com.ecommerce.productservice.dto.ProductResponseDTO;
import com.ecommerce.productservice.exception.ResourceNotFoundException;
import com.ecommerce.productservice.model.Product;
import com.ecommerce.productservice.repository.ProductRepository;
import com.ecommerce.productservice.service.ProductService;
import lombok.RequiredArgsConstructor;
import com.ecommerce.productservice.mapper.ProductMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProdutServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Override
    public ProductResponseDTO createProduct(ProductRequestDTO requestDTO) {
        Product product = productMapper.toProduct(requestDTO);
        Product savedProduct= productRepository.save(product);

        log.info("Producto {} guardado", savedProduct.getName());

        return productMapper.toProductResponseDTO(savedProduct);
    }

    @Override
    public List<ProductResponseDTO> getAllProducts() {
        return productRepository.findAll()
                .stream()
                .map(productMapper::toProductResponseDTO)
                .toList();
    }

    @Override
    public ProductResponseDTO getProductById(String id) {
        Product product = productRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Producto ", "id", id));
        return productMapper.toProductResponseDTO(product);
    }

    @Override
    public ProductResponseDTO updateProduct(String id, ProductRequestDTO requestDTO) {
        Product product = productRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Producto ", "id", id));
        productMapper.updateProduct(requestDTO, product);
        Product updatedProduct = productRepository.save(product);

        log.info("Producto {} actualizado", updatedProduct.getName());

        return productMapper.toProductResponseDTO(updatedProduct);
    }

    @Override
    public void deleteProductById(String id) {
        if(!productRepository.existsById(id)){
            throw  new ResourceNotFoundException("Producto ", "id", id);
        }
        productRepository.deleteById(id);
        log.info("Producto con el id:{} fue eliminado", id);
    }
}
