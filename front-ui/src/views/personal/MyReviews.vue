<template>
  <div class="personal-page">
    <div class="personal-shell">
      <div class="page-banner">
        <h3 class="page-title">Review Management</h3>
      </div>

      <div class="review-card">
        <el-tabs v-model="activeTab" class="review-tabs">
          <el-tab-pane label="Pending Reviews" name="notReviewed">
            <el-card v-for="(order, index) in notReviewList" :key="index" class="order-card">
              <div class="order-header">
                <span>Order ID: {{ order.orderId }}</span>
              </div>
              <el-table :data="order.cmsProductList || []" style="width: 100%" v-loading="loading" class="review-table">
                <el-table-column label="Product Image">
                  <template slot-scope="scope">
                    <div class="image-wrap">
                      <img :src="scope.row.imageUrl" alt="Product Image" class="product-image" @click="goToDetail(scope.row.id)" />
                    </div>
                  </template>
                </el-table-column>
                <el-table-column label="Product Name">
                  <template slot-scope="scope">
                    <span class="product-name" @click="goToDetail(scope.row.id)">{{ scope.row.name }}</span>
                  </template>
                </el-table-column>
                <el-table-column label="Price" prop="price" />
                <el-table-column label="Action" width="120">
                  <template slot-scope="scope">
                    <el-button type="primary" size="small" class="theme-button" @click="openReviewDialog(scope.row, order.orderId)">Review</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </el-card>
          </el-tab-pane>

          <el-tab-pane label="My Reviews" name="myReviews">
            <el-table :data="reviews" style="width: 100%" v-loading="loading" class="review-table review-table--flat">
              <el-table-column label="No." align="center" width="100">
                <template slot-scope="scope">{{ scope.$index + 1 }}</template>
              </el-table-column>
              <el-table-column label="Product Image" width="120">
                <template slot-scope="scope">
                  <div class="image-wrap">
                    <img :src="scope.row.productImage" alt="Product Image" class="product-image" @click="goToDetail(scope.row.productId)" />
                  </div>
                </template>
              </el-table-column>
              <el-table-column label="Product Name">
                <template slot-scope="scope">
                  <span class="product-name" @click="goToDetail(scope.row.productId)">{{ scope.row.productName }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="content" label="Review Content" min-width="220" />
              <el-table-column prop="rating" label="Rating" width="100" />
              <el-table-column prop="createTime" label="Review Time" width="180" />
              <el-table-column label="Action" width="100">
                <template slot-scope="scope">
                  <el-button type="text" class="delete-link" @click="deleteReview(scope.row)">Delete</el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>
        </el-tabs>
      </div>
    </div>

    <el-dialog title="Product Review" :visible.sync="reviewDialogVisible" width="30%" custom-class="personal-dialog">
      <el-form :model="reviewForm" label-width="80px">
        <el-form-item label="Rating">
          <el-rate v-model="reviewForm.rating" :max="5" show-text />
        </el-form-item>
        <el-form-item label="Review Content">
          <el-input v-model="reviewForm.content" type="textarea" :rows="4" placeholder="Please enter your review" />
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="reviewDialogVisible = false">Cancel</el-button>
        <el-button type="primary" class="theme-button" @click="submitReview">Submit</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { listNotReview, listReview, addReview, delReview } from "@/api/ums/review";

export default {
  data() {
    return {
      activeTab: "notReviewed",
      notReviewList: [],
      reviews: [],
      reviewDialogVisible: false,
      reviewForm: {
        productId: "",
        orderId: "",
        content: "",
        rating: 0,
      },
      loading: false,
    };
  },
  created() {
    this.fetchNotReviewList();
    this.fetchReviews();
  },
  methods: {
    async fetchNotReviewList() {
      this.loading = true;
      try {
        const response = await listNotReview();
        this.notReviewList = response.rows || [];
      } catch (error) {
        this.$message.error("Failed to fetch pending reviews");
      } finally {
        this.loading = false;
      }
    },
    async fetchReviews() {
      this.loading = true;
      try {
        const response = await listReview();
        this.reviews = response.rows || [];
      } catch (error) {
        this.$message.error("Failed to fetch reviews");
      } finally {
        this.loading = false;
      }
    },
    openReviewDialog(product, orderId) {
      this.reviewForm = {
        productId: product.id,
        orderId,
        content: "",
        rating: 0,
      };
      this.reviewDialogVisible = true;
    },
    async submitReview() {
      try {
        await addReview(this.reviewForm);
        this.$message.success("Review submitted successfully");
        this.reviewDialogVisible = false;
        await this.fetchNotReviewList();
        await this.fetchReviews();
      } catch (error) {
        this.$message.error("Failed to submit review, please try again");
      }
    },
    async deleteReview(row) {
      try {
        await delReview(row.id);
        this.$message.success("Review deleted");
        await this.fetchReviews();
      } catch (error) {
        this.$message.error("Delete failed, please try again");
      }
    },
    goToDetail(productId) {
      this.$router.push({ name: "ProductDetail", params: { id: productId } });
    },
  },
};
</script>

<style scoped lang="scss">
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

.review-card {
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

.review-tabs {
  padding-top: 12px;
}

.review-tabs :deep(.el-tabs__nav-wrap::after) {
  height: 1px;
  background-color: #eef2f7;
}

.review-tabs :deep(.el-tabs__item) {
  color: #6b7280;
  font-weight: 600;
}

.review-tabs :deep(.el-tabs__item.is-active) {
  color: #ff4d4f;
}

.review-tabs :deep(.el-tabs__active-bar) {
  background-color: #ff4d4f;
}

.order-card {
  margin-bottom: 16px;
  border-radius: 16px;
  box-shadow: 0 8px 22px rgba(15, 23, 42, 0.06);
  border: 1px solid #eef2f7;
  overflow: hidden;
}

.order-header {
  padding: 14px 16px;
  font-weight: 700;
  color: #1f2937;
  background: linear-gradient(180deg, #fff6f6 0%, #ffffff 100%);
  border-bottom: 1px solid #eef2f7;
}

.review-table :deep(.el-table__header-wrapper th) {
  background: #fff6f6;
  color: #1f2937;
  font-weight: 700;
}

.review-table :deep(.el-table__body tr:hover > td) {
  background-color: rgba(255, 77, 79, 0.04);
}

.review-table :deep(.el-table__body td),
.review-table :deep(.el-table__header th) {
  border-bottom-color: #eef2f7;
}

.review-table--flat {
  margin-top: 10px;
}

.image-wrap {
  width: 62px;
  height: 62px;
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

.theme-button {
  background: linear-gradient(135deg, #ff4d4f 0%, #ff7a59 100%);
  border-color: #ff4d4f;
  box-shadow: 0 8px 18px rgba(255, 77, 79, 0.18);
  border-radius: 12px;
}

.theme-button:hover,
.theme-button:focus {
  background: linear-gradient(135deg, #ff6b6d 0%, #ff8a3d 100%);
  border-color: #ff6b6d;
}

.delete-link {
  color: #ff4d4f;
  font-weight: 700;
}

.delete-link:hover,
.delete-link:focus {
  color: #e6393c;
}

:deep(.personal-dialog) {
  border-radius: 18px;
  overflow: hidden;
}

:deep(.personal-dialog .el-dialog__header) {
  padding: 18px 20px 14px;
  border-bottom: 1px solid #eef2f7;
  background: linear-gradient(180deg, #fff6f6 0%, #ffffff 100%);
}

:deep(.personal-dialog .el-dialog__title) {
  font-weight: 700;
  color: #1f2937;
}

:deep(.personal-dialog .el-dialog__body) {
  padding: 20px;
}

:deep(.personal-dialog .el-dialog__footer) {
  padding: 14px 20px 20px;
  border-top: 1px solid #eef2f7;
}

:deep(.personal-dialog .el-button) {
  border-radius: 10px;
}

:deep(.personal-dialog .el-button--primary) {
  background: linear-gradient(135deg, #ff4d4f 0%, #ff7a59 100%);
  border-color: #ff4d4f;
  box-shadow: 0 8px 18px rgba(255, 77, 79, 0.18);
}

:deep(.personal-dialog .el-button--primary:hover),
:deep(.personal-dialog .el-button--primary:focus) {
  background: linear-gradient(135deg, #ff6b6d 0%, #ff8a3d 100%);
  border-color: #ff6b6d;
}
</style>