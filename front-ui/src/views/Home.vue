<template>
  <div class="home-page">
    <div class="header shell">
      <div class="search-box">
        <el-input
          v-model="searchContent"
          placeholder="Please enter product name or description"
          class="search-input"
          @keyup.enter.native="handleSearch"
        />
        <el-button type="primary" class="search-btn" @click="handleSearch">Search</el-button>
        <el-button class="reset-btn" @click="handleReset">Reset</el-button>
      </div>
    </div>

    <div class="main shell">
      <div class="category-panel">
        <div class="panel-title">All Categories</div>
        <el-menu class="category-menu" @select="handleCategorySelect">
          <template v-for="category in categories">
            <el-submenu v-if="category.children && category.children.length" :key="category.id" :index="category.id">
              <template slot="title">{{ category.name }}</template>
              <template v-for="child in category.children">
                <el-menu-item v-if="child.children && child.children.length" :key="child.id" :index="child.id">
                  {{ child.name }}
                </el-menu-item>
                <el-menu-item v-else :key="child.id" :index="child.id">{{ child.name }}</el-menu-item>
              </template>
            </el-submenu>
            <el-menu-item v-else :key="category.id" :index="category.id">
              {{ category.name }}
            </el-menu-item>
          </template>
        </el-menu>
      </div>

      <div class="content-panel">
        <div class="banner-block">
          <div class="section-title">Recommended Products</div>
          <el-carousel :interval="5000" arrow="always" height="360px" v-loading="loading">
            <el-carousel-item v-for="product in recommendProducts" :key="product.id">
              <div class="banner-item" @click="goToDetail(product.id)">
                <img :src="product.imageUrl" :alt="product.name" />
              </div>
            </el-carousel-item>
          </el-carousel>
        </div>

        <div class="activity-block">
          <div class="section-title">Promotional Products</div>
          <div class="promotion-list" v-loading="loading">
            <div
              v-for="promotion in promotionList"
              :key="promotion.id"
              class="promotion-card"
              @click="goToPromotionDetail(promotion.id)"
            >
              <div class="promotion-name">{{ promotion.name }}</div>
              <div class="promotion-type">
                {{ promotion.type === 0 ? `Discount: ${promotion.discount}% off` : `Discount: ¥${promotion.couponValue}` }}
              </div>
              <div class="promotion-time">{{ formatDate(promotion.startTime) }} - {{ formatDate(promotion.endTime) }}</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { listCategory } from "@/api/cms/category";
import { listPromotion } from "@/api/cms/promotion";
import { getRecommendProducts } from "@/api/cms/recommend";

export default {
  name: "Home",
  data() {
    return {
      searchContent: "",
      selectedCategoryId: "",
      categories: [],
      promotionList: [],
      recommendProducts: [],
      loading: false,
    };
  },
  created() {
    this.loadCategories();
    this.loadPromotions();
    this.loadRecommendProducts();
  },
  methods: {
    async loadCategories() {
      try {
        const response = await listCategory();
        if (response.code === 0) {
          this.categories = response.data || [];
        }
      } catch (error) {
        console.error("Failed to fetch categories:", error);
      }
    },
    async loadPromotions() {
      this.loading = true;
      try {
        const response = await listPromotion();
        if (response.code === 0) {
          this.promotionList = response.data || [];
        }
      } catch (error) {
        console.error("Failed to fetch promotions:", error);
      } finally {
        this.loading = false;
      }
    },
    async loadRecommendProducts() {
      try {
        const response = await getRecommendProducts();
        if (response.code === 0) {
          this.recommendProducts = response.data || [];
        }
      } catch (error) {
        console.error("Failed to fetch recommended products:", error);
      }
    },
    handleSearch() {
      const route = this.$router.resolve({
        name: "Product",
        query: {
          searchContent: this.searchContent,
          categoryId: this.selectedCategoryId,
        },
      });
      window.open(route.href, "_blank");
    },
    handleReset() {
      this.searchContent = "";
      this.selectedCategoryId = "";
    },
    handleCategorySelect(categoryId) {
      this.selectedCategoryId = categoryId;
      const route = this.$router.resolve({
        name: "Product",
        query: {
          categoryId,
        },
      });
      window.open(route.href, "_blank");
    },
    goToDetail(productId) {
      this.$router.push({ name: "ProductDetail", params: { id: productId } });
    },
    goToPromotionDetail(promotionId) {
      this.$router.push({ name: "PromotionDetail", params: { id: promotionId } });
    },
    formatDate(date) {
      return date ? new Date(date).toLocaleDateString() : "";
    },
  },
};
</script>

<style scoped>
.home-page {
  min-height: calc(100vh - 70px);
  background: #f5f5f5;
  padding-top: 20px;
}

.shell {
  width: 1200px;
  margin: 0 auto;
}

.header {
  height: 50px;
  display: flex;
  align-items: center;
  background: transparent;
  box-shadow: none;
  border-radius: 0;
  padding: 0;
}

.logo {
  width: 140px;
  font-size: 18px;
  font-weight: 700;
  color: #ff4d4f;
}

.search-box {
  width: 860px;
  margin: 0 auto;
  transform: translateX(95px);
  display: flex;
  align-items: center;
  justify-content: center;
}

.search-input {
  flex: 1;
  min-width: 0;
}

.search-btn {
  margin-left: 20px;
  border-radius: 0 12px 12px 0;
  background: #ff4d4f;
  border-color: #ff4d4f;
}

.reset-btn {
  margin-left: 20px;
  border-radius: 12px;
  color: #ff4d4f;
  border-color: #ff4d4f;
  background: #fff;
}

.search-btn:hover,
.search-btn:focus {
  background: #ff6b6d;
  border-color: #ff6b6d;
}

.reset-btn:hover,
.reset-btn:focus {
  color: #ff6b6d;
  border-color: #ff6b6d;
  background: rgba(255, 77, 79, 0.06);
}

.search-input {
  width: 100%;
  min-width: 0;
}

:deep(.search-input .el-input__inner) {
  border-radius: 12px;
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
}

:deep(.search-input .el-input__inner:focus) {
  border-color: #ff4d4f;
  box-shadow: 0 0 0 2px rgba(255, 77, 79, 0.12);
}

.main {
  display: flex;
  gap: 12px;
  margin-top: 12px;
}

.category-panel {
  width: 200px;
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.panel-title {
  height: 42px;
  line-height: 42px;
  text-align: center;
  background: linear-gradient(135deg, #ff4d4f, #ff7a59);
  color: #fff;
  font-weight: 700;
}

.category-menu {
  border-right: none;
}

.category-menu :deep(.el-menu-item),
.category-menu :deep(.el-submenu__title) {
  color: #515a6e;
}

.category-menu :deep(.el-menu-item:hover),
.category-menu :deep(.el-submenu__title:hover) {
  color: #ff4d4f;
  background-color: rgba(255, 77, 79, 0.06);
}

.category-menu :deep(.el-menu-item.is-active) {
  color: #ff4d4f;
  background-color: rgba(255, 77, 79, 0.1);
  font-weight: 600;
}

.category-menu :deep(.el-submenu.is-active > .el-submenu__title) {
  color: #ff4d4f;
}

.category-menu :deep(.el-submenu__title i) {
  color: inherit;
}

.content-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.banner-block,
.activity-block,
.product-block {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.banner-block .section-title {
  margin-top: 0;
}

.banner-item {
  width: 100%;
  height: 260px;
  cursor: pointer;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
}

.banner-item img {
  width: auto;
  height: auto;
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
  object-position: center center;
  display: block;
}

.section-title {
  font-size: 18px;
  font-weight: 700;
  padding: 15px 15px 0;
  border-left: 4px solid #ff4d4f;
  margin-left: 15px;
}

.promotion-list {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
  padding: 15px;
}

.promotion-card {
  padding: 15px;
  border: 1px solid #eee;
  border-radius: 12px;
  cursor: pointer;
  background: #fff;
}

.promotion-card:hover,
.product-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.08);
}

.promotion-name {
  font-size: 16px;
  margin-bottom: 8px;
}

.promotion-type {
  color: #ff4d4f;
}

.promotion-time {
  color: #999;
  font-size: 12px;
  margin-top: 8px;
}

</style>
