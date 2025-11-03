const BASE_URL = "http://localhost:333";

// Enhanced fetch with CORS handling
async function loadProducts() {
    try {
        console.log("🔄 Fetching products from:", `${BASE_URL}/api/products`);
        
        const response = await fetch(`${BASE_URL}/api/products`, {
            method: 'GET',
            mode: 'cors', // Explicitly set CORS mode
            credentials: 'include', // Include credentials if needed
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            }
        });
        
        console.log("📡 Response status:", response.status);
        
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        
        const products = await response.json();
        console.log("✅ Loaded products:", products);
        
        displayProductsByCategory(products);

    } catch (error) {
        console.log("❌ Error fetching products:", error);
        showFallbackProducts();
        
        // Show specific error message
        const containers = ['trending-products', 'clothing-products', 'electronics-products'];
        containers.forEach(containerId => {
            const container = document.getElementById(containerId);
            if (container) {
                container.innerHTML = `
                    <div class="col-12 text-center">
                        <div class="alert alert-warning">
                            <h5>🚨 Connection Issue</h5>
                            <p>${error.message}</p>
                            <small class="text-muted">Make sure your backend services are running on ports 333, 8081, 9091</small>
                            <br>
                            <button class="btn btn-outline-primary btn-sm mt-2" onclick="loadProducts()">
                                🔄 Retry
                            </button>
                        </div>
                    </div>
                `;
            }
        });
    }
}

function displayProductsByCategory(products) {
    const trendingList = document.getElementById("trending-products");
    const clothingList = document.getElementById("clothing-products");
    const electronicsList = document.getElementById("electronics-products");

    // Clear containers
    trendingList.innerHTML = "";
    clothingList.innerHTML = "";
    electronicsList.innerHTML = "";

    if (!products || products.length === 0) {
        showFallbackProducts();
        return;
    }

    // Group products by category
    const trending = products.filter(p => !p.category || p.category.toLowerCase().includes('trending'));
    const clothing = products.filter(p => p.category && p.category.toLowerCase().includes('clothing'));
    const electronics = products.filter(p => p.category && (
        p.category.toLowerCase().includes('electronics') || 
        p.category.toLowerCase().includes('computers') ||
        p.category.toLowerCase().includes('audio')
    ));

    // Display products
    displayProductGrid(trending.length > 0 ? trending : products.slice(0, 3), trendingList, "🌟 No trending products found");
    displayProductGrid(clothing, clothingList, "👗 No clothing products available");
    displayProductGrid(electronics, electronicsList, "💻 No electronics products available");
}

function displayProductGrid(products, container, emptyMessage) {
    if (!products || products.length === 0) {
        container.innerHTML = `
            <div class="col-12">
                <div class="text-center py-4">
                    <i class="fas fa-box-open fa-2x text-muted mb-3"></i>
                    <p class="text-muted">${emptyMessage}</p>
                </div>
            </div>
        `;
        return;
    }

    container.innerHTML = products.map(product => `
        <div class="col-lg-4 col-md-6 mb-4">
            <div class="card h-100 product-card shadow-sm">
                <img src="${product.imageUrl || 'img/img1.png'}" 
                     class="card-img-top product-image" 
                     alt="${product.name}"
                     style="height: 250px; object-fit: cover;"
                     onerror="this.src='img/img1.png'">
                <div class="card-body d-flex flex-column">
                    <h5 class="card-title">${product.name}</h5>
                    <p class="card-text text-muted small">${product.description || 'Premium quality product'}</p>
                    <div class="mt-auto">
                        <p class="price fw-bold text-primary mb-2">₹${product.price}</p>
                        <button class="btn btn-primary w-100 add-to-cart-btn" 
                            onclick="addToCart(${product.id}, '${escapeHtml(product.name)}', ${product.price}, '${product.imageUrl || 'img/img1.png'}')">
                            <i class="fas fa-cart-plus me-2"></i>Add to Cart
                        </button>
                    </div>
                </div>
            </div>
        </div>
    `).join('');
}

function escapeHtml(text) {
    if (!text) return "";
    const map = {
        '&': '&amp;',
        '<': '&lt;', 
        '>': '&gt;',
        '"': '&quot;',
        "'": '&#039;'
    };
    return String(text).replace(/[&<>"']/g, function(m) { return map[m]; });
}

function showFallbackProducts() {
    const fallbackProducts = [
        {
            id: 1,
            name: "Classic Cotton T-Shirt",
            price: 599,
            category: "Clothing",
            imageUrl: "img/img1.png",
            description: "Comfortable 100% cotton t-shirt for everyday wear"
        },
        {
            id: 2, 
            name: "Wireless Headphones",
            price: 2499,
            category: "Electronics",
            imageUrl: "img/img2.png",
            description: "High-quality wireless headphones with noise cancellation"
        },
        {
            id: 3,
            name: "Running Sports Shoes",
            price: 2999, 
            category: "Trending",
            imageUrl: "img/img3.png",
            description: "Lightweight running shoes with extra cushioning"
        }
    ];
    
    displayProductsByCategory(fallbackProducts);
    
    // Show warning message
    const containers = ['trending-products', 'clothing-products', 'electronics-products'];
    setTimeout(() => {
        containers.forEach(containerId => {
            const container = document.getElementById(containerId);
            if (container) {
                const alertDiv = document.createElement('div');
                alertDiv.className = 'col-12';
                alertDiv.innerHTML = `
                    <div class="alert alert-info mt-3">
                        <small>
                            <i class="fas fa-info-circle me-1"></i>
                            Showing demo products. Backend connection required for real data.
                        </small>
                    </div>
                `;
                container.appendChild(alertDiv);
            }
        });
    }, 100);
}

// Test backend connection on page load
async function testBackendConnection() {
    try {
        const response = await fetch(`${BASE_URL}/api/products`, {
            method: 'GET',
            mode: 'cors'
        });
        
        if (response.ok) {
            console.log("✅ Backend connection successful");
            return true;
        } else {
            console.log("❌ Backend connection failed");
            return false;
        }
    } catch (error) {
        console.log("❌ Backend connection error:", error);
        return false;
    }
}

// Initialize when page loads
document.addEventListener('DOMContentLoaded', function() {
    console.log("🔄 Testing backend connection...");
    testBackendConnection().then(isConnected => {
        if (isConnected) {
            loadProducts();
        } else {
            console.log("🚨 Using fallback products - backend not connected");
            showFallbackProducts();
        }
    });
});