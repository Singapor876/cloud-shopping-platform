<template>
  <div class="personal-page">
    <div class="personal-shell">
      <div class="page-banner">
        <h3 class="page-title">Browse History</h3>
      </div>

      <div class="history-card">
        <el-table :data="history" style="width: 100%" v-loading="loading" class="history-table">
          <el-table-column label="No." align="center" width="100">
            <template slot-scope="scope">{{ scope.$index + 1 }}</template>
          </el-table-column>
          <el-table-column label="Product Image" width="120">
            <template #default="scope">
              <div class="image-wrap">
                <img :src="scope.row.productImage" alt="Product Image" class="product-image" @click="goToDetail(scope.row.productId)" />
              </div>
            </template>
          </el-table-column>
          <el-table-column label="Product Name">
            <template #default="scope">
              <span class="product-name" @click="goToDetail(scope.row.productId)">{{ scope.row.productName }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="browseTime" label="Browse Time" width="180" />
          <el-table-column label="Action" width="100" align="center">
            <template #default="scope">
              <el-button type="text" class="delete-link" @click="deleteHistory(scope.row)">Delete</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>
  </div>
</template>

<script>
import { listHistory, delHistory } from "@/api/ums/history";

export default {
  data() {
    return {
      history: [],
      loading: false,
    };
  },
  created() {
    this.fetchHistory();
  },
  methods: {
    async fetchHistory() {
      this.loading = true;
      try {
        const response = await listHistory();
        this.history = response.rows;
      } catch (error) {
        console.error("Failed to fetch browse history:", error);
      } finally {
        this.loading = false;
      }
    },
    async deleteHistory(item) {
      this.loading = true;
      try {
        await delHistory(item.id);
        this.$message.success("Browse history deleted!");
        this.fetchHistory();
      } catch (error) {
        console.error("Failed to delete browse history:", error);
      } finally {
        this.loading = false;
      }
    },
    goToDetail(productId) {
      this.$router.push({ name: "ProductDetail", params: { id: productId } });
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

.history-card {
  background: #fff;
  border: 1px solid rgba(255, 77, 79, 0.08);
  border-top: none;
  border-radius: 0 0 18px 18px;
  box-shadow: 0 10px 28px rgba(15, 23, 42, 0.06);
  overflow: hidden;
  padding: 0 18px 18px;
}

.page-title {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  color: #1f2937;
}

.history-table :deep(.el-table__header-wrapper th) {
  background: #fff6f6;
  color: #1f2937;
  font-weight: 700;
}

.history-table :deep(.el-table__body tr:hover > td) {
  background-color: rgba(255, 77, 79, 0.04);
}

.history-table :deep(.el-table__body td),
.history-table :deep(.el-table__header th) {
  border-bottom-color: #eef2f7;
}

.image-wrap {
  width: 58px;
  height: 58px;
  margin: 0 auto;
  padding: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 14px;
  background: linear-gradient(135deg, #fff6f6 0%, #ffffff 100%);
  border: 1px solid rgba(255, 77, 79, 0.08);
}

.product-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 10px;
  cursor: pointer;
}

.product-name {
  cursor: pointer;
  color: #1f2937;
  font-weight: 600;
  transition: color 0.2s ease;
}

.product-name:hover,
.product-name:focus {
  color: #ff4d4f;
}

.delete-link {
  color: #ff4d4f;
  font-weight: 700;
}

.delete-link:hover,
.delete-link:focus {
  color: #e6393c;
}
</style>
