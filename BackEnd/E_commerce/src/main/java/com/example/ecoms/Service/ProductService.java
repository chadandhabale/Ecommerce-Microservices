package com.example.ecoms.Service;

import com.example.ecoms.DTO.ProductRequestDTO;
import com.example.ecoms.DTO.ProductResponseDTO;

import java.util.List;

public interface ProductService {
    
    List<ProductResponseDTO> getAllProducts();
    ProductResponseDTO findProductById(Long productId);
    ProductResponseDTO addProduct(ProductRequestDTO productRequestDTO);
    ProductResponseDTO updateProduct(Long productId, ProductRequestDTO productRequestDTO);
    void deleteProduct(Long productId);
    List<ProductResponseDTO> getProductsByCategory(String category);
    List<ProductResponseDTO> searchProducts(String keyword);
}