<template>
  <div class="personal-page">
    <div class="personal-shell">
      <div class="page-banner">
        <h3 class="page-title">Product Details</h3>
      </div>

      <div class="detail-card" v-loading="loading">
        <div class="product-container">
          <div class="product-image">
            <img :src="product.imageUrl" alt="Product Image" />
          </div>
          <div class="product-info">
            <div class="title-row">
              <h2>{{ product.name }}</h2>
              <div v-if="product.isCollect" class="collected-text">Collected</div>
              <button v-else class="collect-button" @click="handleCollect">Collect</button>
            </div>
            <p class="description">{{ product.description }}</p>
            <div class="price-section">
              <p v-if="product.isDiscount !== null" class="original-price">Original Price: ￥{{ product.price }}</p>
              <p class="final-price">{{ product.isDiscount !== null ? "Sale Price: " : "Price: " }}￥{{ calculateFinalPrice(product) }}</p>
            </div>
            <p class="stock">Stock: {{ product.stock }}</p>
            <div class="button-group">
              <button class="cart-button" @click="addToCart">Add to Cart</button>
              <button class="order-button" @click="initOrder">Order Now</button>
            </div>
          </div>
        </div>
      </div>

      <div class="review-card" v-loading="loading">
        <h3 class="review-title">User Reviews</h3>
        <div v-if="product.productReviewList.length === 0" class="empty-text">No reviews yet</div>
        <div v-for="review in product.productReviewList" :key="review.id" class="review-item">
          <p class="review-user">{{ review.userName }} - {{ review.createTime }}</p>
          <p class="review-content">{{ review.content }}</p>
          <p class="review-rating">Rating: {{ review.rating }}⭐</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { getProduct } from "@/api/cms/product";
import { initOrder } from "@/api/oms/order";
import { addProductInCart } from "@/api/ums/cart";
import { addCollect } from "@/api/ums/favorite";

export default {
  data() {
    return {
      loading: false,
      product: {
        productReviewList: [],
        isCollect: false,
        isDiscount: null,
        discountNum: null,
      },
    };
  },
  created() {
    this.fetchProduct();
  },
  methods: {
    async fetchProduct() {
      this.loading = true;
      try {
        const response = await getProduct(this.$route.params.id);
        this.product = {
          productReviewList: [],
          isCollect: false,
          isDiscount: null,
          discountNum: null,
          ...response.data,
          productReviewList: response.data.productReviewList || [],
        };
      } catch (error) {
        console.error("Failed to fetch product details:", error);
      } finally {
        this.loading = false;
      }
    },
    async initOrder() {
      const orderData = {
        userId: "Current user ID",
        totalAmount: this.calculateFinalPrice(this.product),
        paymentMethod: "Online payment",
        userAddress: null,
        omsPlaceOrderProductItemList: [
          {
            productId: this.product.id,
            quantity: 1,
            price: this.product.price,
          },
        ],
      };
      try {
        const response = await initOrder(orderData);
        if (response.code === 0) {
          this.$message.success("Order initialized successfully!");
          this.$router.push({ path: `/order-detail/${response.data.id}` });
        } else {
          this.$message.error("Order initialization failed: " + response.msg);
        }
      } catch (error) {
        console.error("Order initialization failed:", error);
        this.$message.error("Order initialization failed, please try again!");
      }
    },
    async addToCart() {
      const cartData = {
        productId: this.product.id,
        quantity: 1,
      };
      try {
        const response = await addProductInCart(cartData);
        if (response.code === 0) {
          this.$message.success("Product added to cart successfully!");
        } else {
          this.$message.error("Failed to add to cart, please try again!");
        }
      } catch (error) {
        console.error("Failed to add to cart:", error);
        this.$message.error("Failed to add to cart, please try again!");
      }
    },
    async handleCollect() {
      try {
        const response = await addCollect({ productId: this.product.id });
        if (response.code === 0) {
          this.product.isCollect = true;
          this.$message.success("Collection successful!");
        } else {
          this.$message.error("Collection failed: " + response.msg);
        }
      } catch (error) {
        console.error("Collection failed:", error);
        this.$message.error("Collection failed, please try again!");
      }
    },
    calculateFinalPrice(product) {
      if (product.isDiscount === null) {
        return product.price;
      }
      if (product.isDiscount) {
        return (product.price * product.discountNum).toFixed(2);
      } else {
        return (product.price - product.discountNum).toFixed(2);
      }
    },
  },
};
</script>

<style scoped>
.personal-page {
  min-height: calc(100vh - 70px);
  padding: 20px 18px 28px;
  background:
    radial-gradient(circle at top right, rgba(255, 122, 89, 0.08), transparent 28%),
    radial-gradient(circle at top left, rgba(255, 77, 79, 0.06), transparent 22%),
    linear-gradient(180deg, #fff8f8 0%, #f7f9fc 26%, #f5f7fb 100%);
}

.personal-shell {
  max-width: 1180px;
  margin: 0 auto;
}

.page-banner {
  padding: 18px 24px 8px;
  border-radius: 18px 18px 0 0;
  border: 1px solid rgba(255, 77, 79, 0.08);
  border-bottom: none;
  background: linear-gradient(135deg, #fff6f6 0%, #ffffff 55%, #f9fbff 100%);
  box-shadow: 0 10px 28px rgba(15, 23, 42, 0.06);
}

.page-title {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  color: #1f2937;
}

.detail-card,
.review-card {
  background: #fff;
  border: 1px solid rgba(255, 77, 79, 0.08);
  box-shadow: 0 10px 28px rgba(15, 23, 42, 0.06);
  overflow: hidden;
}

.detail-card {
  border-top: none;
  border-radius: 0 0 18px 18px;
  padding: 18px;
}

.review-card {
  margin-top: 18px;
  border-radius: 18px;
  padding: 18px;
}

.product-container {
  display: flex;
  gap: 24px;
  align-items: flex-start;
}

.product-image {
  flex: 0 0 420px;
  display: flex;
  align-items: center;
  justify-content: center;
  min-width: 0;
  background: linear-gradient(135deg, #fff6f6 0%, #ffffff 100%);
  border-radius: 18px;
  padding: 14px;
  border: 1px solid rgba(255, 77, 79, 0.08);
}

.product-image img {
  width: 100%;
  max-width: 450px;
  max-height: 350px;
  object-fit: contain;
  border-radius: 12px;
  display: block;
}

.product-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.title-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  margin-bottom: 10px;
}

.title-row h2 {
  margin: 0;
  color: #1f2937;
  font-size: 28px;
  line-height: 1.2;
}

.collect-button,
.cart-button,
.order-button {
  padding: 10px 18px;
  font-size: 14px;
  color: #fff;
  border: none;
  border-radius: 12px;
  cursor: pointer;
  transition: background 0.2s ease, transform 0.2s ease;
}

.collect-button {
  background: linear-gradient(135deg, #ff4d4f 0%, #ff7a59 100%);
  box-shadow: 0 8px 18px rgba(255, 77, 79, 0.18);
}

.collect-button:hover {
  background: linear-gradient(135deg, #ff6b6d 0%, #ff8a3d 100%);
}

.collected-text {
  padding: 8px 14px;
  border-radius: 999px;
  font-size: 13px;
  color: #ff4d4f;
  background: #fff6f6;
  border: 1px solid rgba(255, 77, 79, 0.12);
}

.description {
  color: #6b7280;
  margin: 0 0 14px;
  line-height: 1.7;
}

.price-section {
  margin-bottom: 10px;
}

.original-price {
  font-size: 16px;
  color: #9ca3af;
  text-decoration: line-through;
  margin: 0 0 6px;
}

.final-price {
  font-size: 26px;
  color: #ff4d4f;
  font-weight: 700;
  margin: 0;
}

.stock {
  margin: 0 0 18px;
  color: #374151;
}

.button-group {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.cart-button {
  background: linear-gradient(135deg, #ff7a59 0%, #ff4d4f 100%);
}

.cart-button:hover {
  transform: translateY(-1px);
}

.order-button {
  background: linear-gradient(135deg, #ff4d4f 0%, #d94b4b 100%);
}

.order-button:hover {
  transform: translateY(-1px);
}

.review-title {
  margin: 0 0 14px;
  font-size: 22px;
  font-weight: 600;
  color: #1f2937;
}

.empty-text {
  color: #9ca3af;
  padding: 12px 0;
}

.review-item {
  padding: 14px 0;
  border-bottom: 1px solid #eef2f7;
}

.review-item:last-child {
  border-bottom: none;
}

.review-user {
  margin: 0 0 8px;
  font-weight: 700;
  color: #1f2937;
}

.review-content {
  margin: 0 0 8px;
  color: #4b5563;
  line-height: 1.7;
}

.review-rating {
  margin: 0;
  color: #ff4d4f;
  font-weight: 700;
}

@media (max-width: 960px) {
  .product-container {
    flex-direction: column;
  }

  .product-image {
    flex-basis: auto;
    width: 100%;
  }

  .title-row {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
