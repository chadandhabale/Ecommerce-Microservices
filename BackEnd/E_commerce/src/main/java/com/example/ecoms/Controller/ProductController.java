package com.example.ecoms.Controller;

import com.example.ecoms.DTO.ProductRequestDTO;
import com.example.ecoms.DTO.ProductResponseDTO;
import com.example.ecoms.Service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    /** ✅ Get all available products */
    @GetMapping
    public ResponseEntity<List<ProductResponseDTO>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    /** ✅ Get a product by ID */
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> getProductById(@PathVariable Long id) {
        ProductResponseDTO product = productService.findProductById(id);
        return ResponseEntity.ok(product);
    }

    /** ✅ Add a new product */
    @PostMapping
    public ResponseEntity<ProductResponseDTO> addProduct(@Valid @RequestBody ProductRequestDTO productRequestDTO) {
        ProductResponseDTO savedProduct = productService.addProduct(productRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedProduct);
    }

    /** ✅ Update an existing product */
    @PutMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> updateProduct(@PathVariable Long id, @Valid @RequestBody ProductRequestDTO productRequestDTO) {
        ProductResponseDTO updatedProduct = productService.updateProduct(id, productRequestDTO);
        return ResponseEntity.ok(updatedProduct);
    }

    /** ✅ Delete a product by ID */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok("✅ Product with ID " + id + " deleted successfully.");
    }

    /** ✅ Get products by category */
    @GetMapping("/category/{category}")
    public ResponseEntity<List<ProductResponseDTO>> getProductsByCategory(@PathVariable String category) {
        List<ProductResponseDTO> products = productService.getProductsByCategory(category);
        return ResponseEntity.ok(products);
    }

    /** ✅ Search products by keyword */
    @GetMapping("/search")
    public ResponseEntity<List<ProductResponseDTO>> searchProducts(@RequestParam String keyword) {
        List<ProductResponseDTO> products = productService.searchProducts(keyword);
        return ResponseEntity.ok(products);
    }
}