const GATEWAY = "http://localhost:333";
let cart = JSON.parse(localStorage.getItem("shoplane_cart") || "[]");

function saveCart() {
    localStorage.setItem("shoplane_cart", JSON.stringify(cart));
    updateCartBadge();
}

function updateCartBadge() {
    document.querySelectorAll(".cart-badge").forEach(el => {
        if (el) el.textContent = cart.reduce((sum, item) => sum + item.quantity, 0);
    });
}

function addToCart(id, name, price, imageUrl) {
    const existingItem = cart.find(item => item.id === id);
    
    if (existingItem) {
        existingItem.quantity += 1;
    } else {
        cart.push({ 
            id, 
            name, 
            price: Number(price), 
            imageUrl: imageUrl || 'img/img1.png', 
            quantity: 1 
        });
    }
    
    saveCart();
    showAddToCartFeedback(event);
}

function showAddToCartFeedback(event) {
    const btn = event?.target;
    if (btn) {
        const originalText = btn.textContent;
        btn.textContent = "✓ Added";
        btn.classList.add('btn-success');
        btn.classList.remove('btn-primary');
        
        setTimeout(() => {
            btn.textContent = originalText;
            btn.classList.remove('btn-success');
            btn.classList.add('btn-primary');
        }, 1500);
    }
}

function renderCart() {
    const tbody = document.getElementById("cart-items");
    const note = document.getElementById("cart-empty-note");
    const totalAmountEl = document.getElementById("total-amount");
    const checkoutBtn = document.getElementById("checkout-btn");
    
    if (!tbody) return;

    tbody.innerHTML = "";
    
    if (cart.length === 0) {
        if (note) note.textContent = "Your cart is empty. Add products from the shop.";
        if (totalAmountEl) totalAmountEl.textContent = "₹0";
        if (checkoutBtn) checkoutBtn.disabled = true;
        return;
    }
    
    if (note) note.textContent = "";
    if (checkoutBtn) checkoutBtn.disabled = false;
    
    let total = 0;
    
    cart.forEach((item, index) => {
        const subtotal = item.price * item.quantity;
        total += subtotal;
        
        const tr = document.createElement("tr");
        tr.innerHTML = `
            <td>
                <img src="${item.imageUrl}" alt="${item.name}" 
                     style="width: 60px; height: 60px; object-fit: cover;" 
                     onerror="this.src='img/img1.png'">
            </td>
            <td>${item.name}</td>
            <td>₹${item.price.toFixed(2)}</td>
            <td>
                <div class="d-flex align-items-center gap-2">
                    <button class="btn btn-sm btn-outline-secondary" onclick="changeQuantity(${index}, -1)">-</button>
                    <span class="mx-2">${item.quantity}</span>
                    <button class="btn btn-sm btn-outline-secondary" onclick="changeQuantity(${index}, 1)">+</button>
                </div>
            </td>
            <td>₹${subtotal.toFixed(2)}</td>
            <td>
                <button class="btn btn-danger btn-sm" onclick="removeFromCart(${index})">
                    <i class="fas fa-trash"></i>
                </button>
            </td>
        `;
        tbody.appendChild(tr);
    });
    
    if (totalAmountEl) totalAmountEl.textContent = `₹${total.toFixed(2)}`;
}

function changeQuantity(index, delta) {
    cart[index].quantity += delta;
    if (cart[index].quantity <= 0) {
        cart.splice(index, 1);
    }
    saveCart();
    renderCart();
}

function removeFromCart(index) {
    cart.splice(index, 1);
    saveCart();
    renderCart();
}


async function checkout() {
    if (cart.length === 0) return alert("🛒 Your cart is empty!");

    const total = cart.reduce((sum, item) => sum + (item.price * item.quantity), 0);
    if (total <= 0) return alert("Invalid total amount!");

    const userEmail = localStorage.getItem("userEmail");
    const userId = localStorage.getItem("userId");
    const userName = localStorage.getItem("userName") || "Customer";
    const orderId = Date.now();

    if (!userEmail) {
        alert("🔐 Please login first before checkout!");
        window.location.href = "login.html";
        return;
    }

    try {
        console.log("🔄 Creating Razorpay order for amount:", total);

        const res = await fetch(`${GATEWAY}/api/payments/create`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                amount: total,
                userId: userId || 1,
                orderId,
                customerEmail: userEmail,
                customerName: userName,
                description: "Payment for shopping order"
            })
        });

        if (!res.ok) throw new Error(await res.text());
        const orderData = await res.json();
        console.log("✅ Razorpay order created:", orderData);

        const options = {
            key: orderData.keyId,
            amount: orderData.amount * 100,
            currency: orderData.currency,
            name: "Shoplane",
            description: "Order Payment",
            order_id: orderData.razorpayOrderId,
            handler: async response => {
                console.log("💳 Payment successful:", response);
                await confirmPayment(response, userEmail);
            },
            prefill: { name: userName, email: userEmail, contact: "9999999999" },
            notes: { address: "Shoplane Office" },
            theme: { color: "#ff6600" }
        };

        const razorpay = new Razorpay(options);
        razorpay.on('payment.failed', response => {
            console.error("❌ Payment failed:", response.error);
            alert(`❌ Payment failed: ${response.error.description}`);
        });

        razorpay.open();

    } catch (err) {
        console.error("❌ Checkout error:", err);
        alert("❌ Payment initialization failed: " + err.message);
    }
}

async function confirmPayment(response, userEmail) {
    try {
        console.log("🔄 Verifying payment with backend...");

        const verifyResponse = await fetch(`${GATEWAY}/api/payments/verify`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                razorpayOrderId: response.razorpay_order_id,
                razorpayPaymentId: response.razorpay_payment_id,
                razorpaySignature: response.razorpay_signature,
                customerEmail: userEmail
            })
        });

        if (verifyResponse.ok) {
            alert("✅ Payment successful! A confirmation email has been sent.");
            cart = [];
            saveCart();
            renderCart();
            setTimeout(() => (window.location.href = "index.html"), 2000);
        } else {
            alert("⚠️ Payment verified with issues.");
        }
    } catch (error) {
        console.error("❌ Verification error:", error);
        alert("✅ Payment successful, verification may be delayed.");
    }
}



// Initialize
document.addEventListener("DOMContentLoaded", () => {
    updateCartBadge();
    if (document.getElementById("cart-items")) {
        renderCart();
    }
    document.getElementById("checkout-btn")?.addEventListener("click", checkout);
});