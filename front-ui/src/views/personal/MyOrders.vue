<template>
  <div class="personal-page">
    <div class="personal-shell">
      <div class="page-banner">
        <h3 class="title">My Orders</h3>
      </div>

      <div class="orders-card">
        <el-table :data="orders" style="width: 100%" v-loading="loading" class="orders-table">
          <el-table-column label="No." align="center" width="100">
            <template slot-scope="scope">
              {{ scope.$index + 1 }}
            </template>
          </el-table-column>
          <el-table-column label="Total">
            <template #default="scope">
              ￥{{ scope.row.totalAmount }}
            </template>
          </el-table-column>
          <el-table-column label="Payment Method" prop="paymentMethod" />
          <el-table-column label="Order Status">
            <template #default="scope">
              <el-tag :type="getOrderStatusTagType(scope.row.orderStatus)">
                {{ getOrderStatusText(scope.row.orderStatus) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="Created Time" prop="createTime" />
          <el-table-column label="Action" width="120" align="center">
            <template #default="scope">
              <el-button type="text" class="detail-link" @click="goToOrderDetail(scope.row.id)">View Details</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>
  </div>
</template>

<script>
import { listOrder } from "@/api/oms/order";

export default {
  data() {
    return {
      orders: [],
      loading: false,
    };
  },
  created() {
    this.fetchOrders();
  },
  methods: {
    async fetchOrders() {
      this.loading = true;
      try {
        const response = await listOrder();
        this.orders = response.data;
      } catch (error) {
        console.error("Failed to fetch orders:", error);
      } finally {
        this.loading = false;
      }
    },
    goToOrderDetail(orderId) {
      this.$router.push({
        path: "/order-detail/" + orderId,
      });
    },
    getOrderStatusText(status) {
      switch (status) {
        case 0:
          return "Pending Payment";
        case 1:
          return "Paid";
        case 2:
          return "Cancelled";
        case 3:
          return "Completed";
        case 4:
          return "Refunded";
        default:
          return "Unknown";
      }
    },
    getOrderStatusTagType(status) {
      switch (status) {
        case 0:
          return "warning";
        case 1:
          return "success";
        case 2:
          return "info";
        case 3:
          return "";
        case 4:
          return "danger";
        default:
          return "info";
      }
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

.orders-card {
  background: #fff;
  border: 1px solid rgba(255, 77, 79, 0.08);
  border-top: none;
  border-radius: 0 0 18px 18px;
  box-shadow: 0 10px 28px rgba(15, 23, 42, 0.06);
  overflow: hidden;
}

.title {
  font-size: 24px;
  font-weight: 600;
  margin: 0;
  color: #1f2937;
}

.orders-table :deep(.el-table__header-wrapper th) {
  background: #fff6f6;
  color: #1f2937;
  font-weight: 700;
}

.orders-table :deep(.el-table__body tr:hover > td) {
  background-color: rgba(255, 77, 79, 0.04);
}

.orders-table :deep(.el-tag) {
  font-size: 13px;
  padding: 4px 10px;
  border-radius: 999px;
}

.detail-link {
  color: #ff4d4f;
  font-weight: 700;
}

.detail-link:hover,
.detail-link:focus {
  color: #e6393c;
}

.orders-table :deep(.el-button--text.is-disabled) {
  color: #c0c4cc;
}

.orders-table :deep(.el-table__body td),
.orders-table :deep(.el-table__header th) {
  border-bottom-color: #eef2f7;
}
</style>
