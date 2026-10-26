<template>
  <div class="product-page">
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
        <el-menu class="category-menu" :default-active="selectedCategoryId" @select="handleMenuSelect">
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
            <el-menu-item v-else :key="category.id" :index="category.id">{{ category.name }}</el-menu-item>
          </template>
        </el-menu>
      </div>

      <div class="product-panel">
        <div class="result-tip" v-if="searchContent || selectedCategoryId">
          Current Filter: <span v-if="searchContent">Keyword "{{ searchContent }}"</span><span v-if="searchContent && selectedCategoryId">, </span><span v-if="selectedCategoryId">Category "{{ selectedCategoryName }}"</span>
        </div>

        <div v-loading="loading" class="product-grid">
          <div
            v-for="product in products"
            :key="product.id"
            class="product-card"
            @click="goToDetail(product.id)"
          >
            <div class="image-box">
              <el-image :src="product.imageUrl" fit="contain" class="product-image" lazy>
                <div slot="error" class="image-fallback">
                  <i class="el-icon-picture-outline" />
                </div>
              </el-image>
            </div>
            <div class="product-meta">
              <h3 class="product-title">{{ product.name }}</h3>
              <p class="product-desc">{{ product.description }}</p>
              <div class="price-info">
                <span class="price-current">￥{{ product.price }}</span>
                <el-tag v-if="product.stock < 10" type="danger" size="mini" class="stock-tag">Only {{ product.stock }} left</el-tag>
              </div>
              <div class="status-info">
                <el-tag v-if="product.product_status === 0" type="info" size="mini">Sold Out</el-tag>
                <el-tag v-else type="success" size="mini">Available</el-tag>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { listCategory } from "@/api/cms/category";
import { listProduct, searchProduct } from "@/api/cms/product";

export default {
  data() {
    return {
      searchContent: "",
      selectedCategoryId: "",
      categories: [],
      products: [],
      loading: false,
    };
  },
  computed: {
    selectedCategoryName() {
      const flatCategories = [];
      const walk = (items) => {
        (items || []).forEach((item) => {
          flatCategories.push(item);
          if (item.children && item.children.length) {
            walk(item.children);
          }
        });
      };
      walk(this.categories);
      const target = flatCategories.find((item) => item.id === this.selectedCategoryId);
      return target ? target.name : "";
    },
  },
  created() {
    this.searchContent = this.$route.query.searchContent || "";
    this.selectedCategoryId = this.$route.query.categoryId || "";
    this.fetchCategories();
    this.fetchProducts();
  },
  watch: {
    "$route.query": {
      deep: true,
      handler() {
        this.searchContent = this.$route.query.searchContent || "";
        this.selectedCategoryId = this.$route.query.categoryId || "";
        this.fetchProducts();
      },
    },
  },
  methods: {
    async fetchCategories() {
      try {
        const response = await listCategory();
        if (response.code === 0) {
          this.categories = response.data || [];
        }
      } catch (error) {
        this.handleApiError(error);
      }
    },
    async fetchProducts() {
      this.loading = true;
      try {
        const params = {
          searchContent: this.searchContent,
          categoryId: this.selectedCategoryId,
        };
        const response = this.searchContent || this.selectedCategoryId ? await searchProduct(params) : await listProduct();
        if (response.code === 0) {
          this.products = response.data || [];
        }
      } catch (error) {
        this.handleApiError(error);
      } finally {
        this.loading = false;
      }
    },
    navigateProduct(query) {
      const normalizedCurrentQuery = {
        searchContent: this.$route.query.searchContent || "",
        categoryId: this.$route.query.categoryId || "",
      };
      const normalizedNextQuery = {
        searchContent: query.searchContent || "",
        categoryId: query.categoryId || "",
      };

      if (
        normalizedCurrentQuery.searchContent === normalizedNextQuery.searchContent &&
        normalizedCurrentQuery.categoryId === normalizedNextQuery.categoryId
      ) {
        return false;
      }

      this.$router.replace({
        name: "Product",
        query: normalizedNextQuery,
      });
      return true;
    },
    handleMenuSelect(categoryId) {
      this.selectedCategoryId = categoryId;
      this.navigateProduct({
        searchContent: this.searchContent,
        categoryId: this.selectedCategoryId,
      });
    },
    handleReset() {
      this.searchContent = "";
      this.selectedCategoryId = "";
      this.navigateProduct({
        searchContent: "",
        categoryId: "",
      });
    },
    handleSearch() {
      this.navigateProduct({
        searchContent: this.searchContent,
        categoryId: this.selectedCategoryId,
      });
    },
    handleApiError(error) {
      if (error.response) {
        switch (error.response.status) {
          case 401:
            alert("Unauthorized, please login!");
            this.$router.push({ name: "/login" });
            break;
          case 500:
            alert("Server internal error, please try again later!");
            break;
          default:
            alert("Request failed, please check network or contact administrator!");
        }
      } else {
        alert("Network error, please check your network connection!");
      }
    },
    goToDetail(productId) {
      this.$router.push({ name: "ProductDetail", params: { id: productId } });
    },
  },
};
</script>

<style scoped>
.product-page {
  min-height: 100vh;
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
  width: 100%;
  min-width: 0;
}

.search-btn,
.reset-btn {
  margin-left: 20px;
}

.search-btn {
  background: #ff4d4f;
  border-color: #ff4d4f;
  border-radius: 0 12px 12px 0;
}

.reset-btn {
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

.product-panel {
  flex: 1;
  background: #fff;
  padding: 15px;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.result-tip {
  margin-bottom: 12px;
  color: #666;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 15px;
}

.product-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  border: 1px solid #eee;
}

.product-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);
}

.image-box {
  width: 100%;
  height: 180px;
}

.product-image {
  width: 100%;
  height: 180px;
  object-fit: cover;
}

.image-fallback {
  width: 100%;
  height: 180px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
  color: #909399;
}

.product-meta {
  padding: 10px;
}

.product-title {
  font-size: 14px;
  margin: 0 0 8px;
}

.product-desc {
  color: #666;
  font-size: 12px;
  height: 36px;
  overflow: hidden;
  margin: 0 0 10px;
}

.price-info {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.price-current {
  color: #ff4d4f;
  font-size: 16px;
  font-weight: 700;
}
</style>

.image-box {
  height: 300px; /* 固定高度 */
  display: flex;
  justify-content: center;
  align-items: center;
  overflow: hidden;
  background: #f5f7fa; /* 背景色 */
}

.product-image {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain; /* 保持图片比例，完整显示 */
  transition: transform 0.3s;
}

.product-card:hover .product-image {
  transform: scale(1.05);
}

.image-fallback {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
  color: #909399;
}

.product-meta {
  padding: 16px;
}

.product-title {
  font-size: 15px;
  color: #303133;
  margin: 0 0 8px;
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-desc {
  font-size: 13px;
  color: #606266;
  height: 40px;
  line-height: 1.5;
  margin: 0 0 12px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.price-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.price-current {
  color: #ff6600;
  font-size: 18px;
  font-weight: 600;
}

.stock-tag {
  margin-left: 8px;
}

.status-info {
  margin-top: 8px;
  text-align: right;
}
</style>
