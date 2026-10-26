<template>
  <div class="personal-page">
    <div class="personal-shell">
      <div class="page-banner">
        <h3 class="page-title">Order Details</h3>
      </div>

      <div class="detail-card">
        <el-form label-width="120px" class="order-form">
          <el-form-item label="User">
            <el-input v-model="userName" disabled />
          </el-form-item>
          <el-form-item label="Order Time">
            <el-input v-model="createTime" disabled />
          </el-form-item>
          <el-form-item label="Order Status">
            <el-input v-model="orderStatusText" disabled />
          </el-form-item>

          <el-form-item label="Product Info">
            <el-table :data="orderItems" border class="order-table">
              <el-table-column label="Product Image" width="100">
                <template #default="scope">
                  <div class="image-wrap">
                    <img :src="scope.row.cmsProduct.imageUrl" alt="Product Image" class="product-image" />
                  </div>
                </template>
              </el-table-column>
              <el-table-column prop="cmsProduct.name" label="Product Name" />
              <el-table-column prop="quantity" label="Quantity" width="100" />
              <el-table-column prop="price" label="Unit Price" width="120" />
            </el-table>
          </el-form-item>

          <el-form-item label="Shipping Address">
            <el-button v-if="orderStatus === 0" type="primary" class="theme-button" @click="showAddressDialog">Select Address</el-button>
            <div v-if="selectedAddress" class="address-info">
              <p>Contact: {{ selectedAddress.phone }}</p>
              <p>Address: {{ selectedAddress.address }}</p>
            </div>
          </el-form-item>

          <el-form-item label="Total Amount">
            <el-input v-model="totalAmount" disabled />
          </el-form-item>

          <el-form-item v-if="orderStatus === 0" class="action-row">
            <el-button type="primary" class="theme-button" @click="placeOrder">Pay</el-button>
            <el-button class="cancel-button" @click="cancelOrder">Cancel</el-button>
          </el-form-item>
        </el-form>
      </div>
    </div>

    <el-dialog title="Select Shipping Address" :visible.sync="addressDialogVisible" width="30%" custom-class="personal-dialog">
      <el-table :data="addressList" @row-click="handleAddressSelect" class="address-table">
        <el-table-column prop="phone" label="Contact" />
        <el-table-column prop="address" label="Address" />
      </el-table>
    </el-dialog>
  </div>
</template>

<script>
import { getById } from "@/api/ums/user";
import { getOrder, placeOrder, cancelOrder } from "@/api/oms/order";

export default {
  data() {
    return {
      orderId: this.$route.params.orderId,
      orderItems: [],
      addressList: [],
      selectedAddress: null,
      totalAmount: 0,
      userName: "",
      createTime: "",
      orderStatus: 0,
      addressDialogVisible: false,
    };
  },
  computed: {
    orderStatusText() {
      const statusMap = {
        0: "Pending Payment",
        1: "Paid",
        2: "Cancelled",
        3: "Completed",
        4: "Refunded",
      };
      return statusMap[this.orderStatus] || "Unknown";
    },
  },
  created() {
    this.fetchOrderDetail();
    this.fetchUserAddress();
  },
  methods: {
    async fetchOrderDetail() {
      try {
        const response = await getOrder(this.orderId);
        if (response.code === 0) {
          this.orderItems = response.data.omsOrderItemList;
          this.totalAmount = response.data.totalAmount;
          this.userName = response.data.userName;
          this.createTime = response.data.createTime;
          this.orderStatus = response.data.orderStatus;

          if (this.orderStatus !== 0 && response.data.userAddress) {
            this.selectedAddress = JSON.parse(response.data.userAddress);
          }
        } else {
          this.$message.error("Failed to fetch order details: " + response.msg);
        }
      } catch (error) {
        console.error("Failed to fetch order details:", error);
        this.$message.error("Failed to fetch order details, please try again!");
      }
    },
    async fetchUserAddress() {
      try {
        const response = await getById();
        if (response.code === 0) {
          this.addressList = JSON.parse(response.data.userAddress || "[]");
        }
      } catch (error) {
        console.error("Failed to fetch user address:", error);
        this.$message.error("Failed to fetch user address, please try again!");
      }
    },
    showAddressDialog() {
      this.addressDialogVisible = true;
    },
    handleAddressSelect(address) {
      this.selectedAddress = address;
      this.addressDialogVisible = false;
    },
    async placeOrder() {
      if (!this.selectedAddress) {
        this.$message.error("Please select a shipping address!");
        return;
      }

      const orderData = {
        orderId: this.orderId,
        omsPlaceOrderProductItemList: this.orderItems.map(item => ({
          productId: item.productId,
          quantity: item.quantity,
          price: item.price,
        })),
        totalAmount: this.totalAmount,
        paymentMethod: "Online payment",
        userAddress: JSON.stringify(this.selectedAddress),
        isCar: false,
      };

      try {
        const response = await placeOrder(orderData);
        if (response.code === 0) {
          this.$message.success("Payment successful!");
          this.$router.push({ path: "/my-orders" });
        } else {
          this.$message.error("Payment failed: " + response.msg);
        }
      } catch (error) {
        console.error("Payment failed:", error);
        this.$message.error("Payment failed, please try again!");
      }
    },
    async cancelOrder() {
      try {
        const response = await cancelOrder({
          id: this.orderId,
          orderStatus: 2,
        });

        if (response.code === 0) {
          this.$message.success("Order cancelled!");
          this.$router.push({ path: "/my-orders" });
        } else {
          this.$message.error("Cancel order failed: " + response.msg);
        }
      } catch (error) {
        console.error("Cancel order failed:", error);
        this.$message.error("Cancel order failed, please try again!");
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

.detail-card {
  background: #fff;
  border: 1px solid rgba(255, 77, 79, 0.08);
  border-top: none;
  border-radius: 0 0 18px 18px;
  box-shadow: 0 10px 28px rgba(15, 23, 42, 0.06);
  overflow: hidden;
  padding: 22px 18px 18px;
}

.page-title {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  color: #1f2937;
}

.order-form :deep(.el-form-item__label) {
  color: #374151;
  font-weight: 600;
}

.order-form :deep(.el-input.is-disabled .el-input__inner) {
  color: #374151;
  background-color: #f8fafc;
}

.order-table :deep(.el-table__header-wrapper th) {
  background: #fff6f6;
  color: #1f2937;
  font-weight: 700;
}

.order-table :deep(.el-table__body tr:hover > td) {
  background-color: rgba(255, 77, 79, 0.04);
}

.order-table :deep(.el-table__body td),
.order-table :deep(.el-table__header th) {
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
}

.address-info {
  margin-top: 12px;
  padding: 12px 14px;
  border-radius: 14px;
  background: #fff6f6;
  border: 1px solid rgba(255, 77, 79, 0.12);
  color: #374151;
}

.address-info p {
  margin: 0;
}

.address-info p + p {
  margin-top: 6px;
}

.theme-button,
.cancel-button {
  border-radius: 12px;
}

.theme-button {
  background: linear-gradient(135deg, #ff4d4f 0%, #ff7a59 100%);
  border-color: #ff4d4f;
  box-shadow: 0 8px 18px rgba(255, 77, 79, 0.18);
}

.theme-button:hover,
.theme-button:focus {
  background: linear-gradient(135deg, #ff6b6d 0%, #ff8a3d 100%);
  border-color: #ff6b6d;
}

.cancel-button {
  color: #374151;
  border: 1px solid #d1d5db;
}

.action-row :deep(.el-form-item__content) {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
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

.address-table :deep(.el-table__header-wrapper th) {
  background: #fff6f6;
  color: #1f2937;
  font-weight: 700;
}

.address-table :deep(.el-table__body tr:hover > td) {
  background-color: rgba(255, 77, 79, 0.04);
}
</style>
