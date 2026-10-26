<template>
  <div class="promotion-page">
    <div class="promotion-shell">
      <div class="page-banner">
        <div class="banner-copy">
          <p class="page-kicker">Promotion</p>
          <h2 class="page-title">{{ promotion.name || "Promotion Details" }}</h2>
          <div class="banner-meta">
            <span class="meta-pill type-pill">
              {{ promotion.type === 0 ? `Discount ${promotion.discount * 10}% off` : `Discount ¥${promotion.couponValue}` }}
            </span>
            <span class="meta-pill time-pill">
              {{ formatDate(promotion.startTime) }} - {{ formatDate(promotion.endTime) }}
            </span>
          </div>
        </div>
      </div>

      <div class="content-card">
        <div class="section-head">
          <h3>Promotion Products</h3>
          <span class="section-subtitle">Click product to view details</span>
        </div>

        <el-row v-if="promotion.productList && promotion.productList.length > 0" :gutter="20" class="product-grid">
          <el-col v-for="product in promotion.productList" :key="product.id" :xs="24" :sm="12" :md="8" :lg="8">
            <el-card class="product-card" shadow="never" @click.native="goProduct(product.id)">
              <div class="image-frame">
                <img :src="product.imageUrl" class="product-image" alt="Product Image" />
              </div>
              <div class="product-name">{{ product.name }}</div>
              <div class="product-price">
                <span class="original-price">¥{{ product.price }}</span>
                <span v-if="promotion.type === 0" class="discounted-price">¥{{ (product.price * promotion.discount).toFixed(2) }}</span>
                <span v-else class="discounted-price">¥{{ (product.price - promotion.couponValue).toFixed(2) }}</span>
              </div>
              <div class="card-hint">Click to view product details</div>
            </el-card>
          </el-col>
        </el-row>

        <div v-else class="empty-state">No promotion products</div>
      </div>
    </div>
  </div>
</template>

<script>
import { getPromotion } from "@/api/cms/promotion";

export default {
  name: "PromotionDetail",
  data() {
    return {
      promotion: {
        id: "",
        name: "",
        type: 0,
        discount: 0,
        couponValue: 0,
        startTime: "",
        endTime: "",
        productList: [],
      },
    };
  },
  created() {
    this.loadPromotionDetail();
  },
  methods: {
    async loadPromotionDetail() {
      const promotionId = this.$route.params.id;
      const response = await getPromotion(promotionId);
      if (response.code === 0) {
        this.promotion = response.data || this.promotion;
      }
    },
    formatDate(date) {
      if (!date) {
        return "-";
      }
      return new Date(date).toLocaleDateString();
    },
    goProduct(productId) {
      this.$router.push({ name: "ProductDetail", params: { id: productId } });
    },
  },
};
</script>

<style scoped>
.promotion-page {
  min-height: calc(100vh - 70px);
  padding: 20px 18px 28px;
  background:
    radial-gradient(circle at top right, rgba(255, 122, 89, 0.12), transparent 28%),
    radial-gradient(circle at top left, rgba(255, 77, 79, 0.08), transparent 20%),
    linear-gradient(180deg, #fff7f7 0%, #f7f9fc 28%, #f5f7fb 100%);
}

.promotion-shell {
  max-width: 1180px;
  margin: 0 auto;
}

.page-banner {
  position: relative;
  padding: 24px 28px 22px;
  border-radius: 22px 22px 0 0;
  border: 1px solid rgba(255, 77, 79, 0.08);
  border-bottom: none;
  background:
    linear-gradient(135deg, rgba(255, 255, 255, 0.94), rgba(255, 246, 246, 0.98)),
    linear-gradient(135deg, #fff6f6 0%, #ffffff 55%, #f9fbff 100%);
  box-shadow: 0 12px 30px rgba(15, 23, 42, 0.06);
  overflow: hidden;
}

.page-banner::after {
  content: "";
  position: absolute;
  inset: auto -40px -40px auto;
  width: 180px;
  height: 180px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(255, 77, 79, 0.12), transparent 68%);
}

.banner-copy {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.page-kicker {
  margin: 0;
  font-size: 13px;
  font-weight: 700;
  letter-spacing: 0.12em;
  color: #ff4d4f;
  text-transform: uppercase;
}

.page-title {
  margin: 0;
  font-size: 32px;
  line-height: 1.15;
  font-weight: 800;
  color: #111827;
}

.banner-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.meta-pill {
  display: inline-flex;
  align-items: center;
  padding: 8px 14px;
  border-radius: 999px;
  font-size: 13px;
  font-weight: 700;
}

.type-pill {
  color: #ff4d4f;
  background: #fff6f6;
  border: 1px solid rgba(255, 77, 79, 0.12);
}

.time-pill {
  color: #475569;
  background: #f8fafc;
  border: 1px solid #e5e7eb;
}

.content-card {
  padding: 22px 22px 26px;
  background: #fff;
  border: 1px solid rgba(255, 77, 79, 0.08);
  border-top: none;
  border-radius: 0 0 22px 22px;
  box-shadow: 0 12px 30px rgba(15, 23, 42, 0.06);
}

.section-head {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  gap: 12px;
  margin-bottom: 18px;
}

.section-head h3 {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  color: #111827;
}

.section-subtitle {
  color: #6b7280;
  font-size: 13px;
}

.product-grid {
  margin-top: 4px;
}

.product-card {
  height: 100%;
  border: 1px solid #eef2f7;
  border-radius: 18px;
  background: linear-gradient(180deg, #ffffff 0%, #fffdfd 100%);
  box-shadow: 0 10px 22px rgba(15, 23, 42, 0.05);
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease, border-color 0.2s ease;
  overflow: hidden;
}

.product-card:hover {
  transform: translateY(-4px);
  border-color: rgba(255, 77, 79, 0.18);
  box-shadow: 0 16px 28px rgba(255, 77, 79, 0.12);
}

.image-frame {
  padding: 14px;
  background: linear-gradient(135deg, #fff6f6 0%, #ffffff 100%);
  border-radius: 16px;
  border: 1px solid rgba(255, 77, 79, 0.08);
}

.product-image {
  width: 100%;
  height: 260px;
  object-fit: cover;
  border-radius: 12px;
  display: block;
}

.product-name {
  margin-top: 14px;
  font-size: 16px;
  font-weight: 700;
  color: #1f2937;
  line-height: 1.4;
}

.product-price {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-top: 10px;
  flex-wrap: wrap;
}

.original-price {
  text-decoration: line-through;
  color: #9ca3af;
  font-size: 13px;
}

.discounted-price {
  color: #ff4d4f;
  font-weight: 800;
  font-size: 18px;
}

.card-hint {
  margin-top: 12px;
  color: #6b7280;
  font-size: 12px;
}

.empty-state {
  margin-top: 18px;
  padding: 18px;
  text-align: center;
  border-radius: 16px;
  background: #f8fafc;
  border: 1px dashed #dbe4ee;
  color: #6b7280;
}

@media (max-width: 768px) {
  .page-title {
    font-size: 26px;
  }

  .content-card,
  .page-banner {
    padding-left: 18px;
    padding-right: 18px;
  }

  .section-head {
    flex-direction: column;
    align-items: flex-start;
  }

  .product-image {
    height: 220px;
  }
}
</style>