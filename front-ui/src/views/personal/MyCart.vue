<template>
  <div class="personal-page">
    <div class="personal-shell">
      <div class="page-banner">
        <h3 class="page-title">My Shopping Cart</h3>
      </div>

      <div class="cart-card">
        <el-table :data="cartItems" style="width: 100%" @selection-change="handleSelectionChange" v-loading="loading" class="cart-table">
          <el-table-column type="selection" width="55" />
          <el-table-column label="No." align="center" width="100">
            <template slot-scope="scope">{{ scope.$index + 1 }}</template>
          </el-table-column>
          <el-table-column label="Product Image" align="center" width="140">
            <template #default="scope">
              <div class="image-wrap">
                <img :src="scope.row.imageUrl" alt="Product Image" class="product-image" />
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="name" label="Product Name" />
          <el-table-column label="Quantity">
            <template #default="scope">
              <el-input-number v-model="scope.row.quantity" :min="0" @change="handleQuantityChange(scope.row)" />
            </template>
          </el-table-column>
          <el-table-column prop="price" label="Original Price" />
          <el-table-column label="Discounted Price">
            <template #default="scope">{{ calculateDiscountedPrice(scope.row) }}</template>
          </el-table-column>
        </el-table>

        <div class="cart-total">
          <span class="total-text">Total: ￥{{ totalPrice }}</span>
          <el-button type="primary" @click="initOrder" class="checkout-btn">Checkout</el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { listCart, addProductInCart, removeProductInCart } from "@/api/ums/cart";
import { initOrder } from "@/api/oms/order";

export default {
  data() {
    return {
      cartItems: [],
      selectedItems: [],
      loading: false,
    };
  },
  computed: {
    totalPrice() {
      return this.selectedItems.reduce((total, item) => {
        const discountedPrice = this.calculateDiscountedPrice(item);
        const price = discountedPrice === "No discount" ? item.price : discountedPrice;
        return total + price * item.quantity;
      }, 0);
    },
  },
  created() {
    this.fetchCart();
  },
  methods: {
    async fetchCart() {
      this.loading = true;
      try {
        const response = await listCart();
        this.cartItems = response.rows[0].cmsProductList.map(item => ({
          ...item,
          oldQuantity: item.quantity,
          isDiscount: item.isDiscount || null,
          discountNum: item.discountNum || 0,
        }));
      } catch (error) {
        console.error("Failed to fetch cart data:", error);
      } finally {
        this.loading = false;
      }
    },
    calculateDiscountedPrice(item) {
      if (item.isDiscount === null) {
        return "No discount";
      } else if (item.isDiscount) {
        return Math.floor(item.price * item.discountNum);
      } else {
        return Math.floor(item.price - item.discountNum);
      }
    },
    async handleQuantityChange(item) {
      if (item.quantity === item.oldQuantity) {
        return;
      }
      if (item.quantity > item.oldQuantity) {
        await addProductInCart({
          productId: item.id,
          quantity: item.quantity,
        });
      } else if (item.quantity < item.oldQuantity) {
        var res = await removeProductInCart({
          productId: item.id,
          quantity: item.quantity,
        });
        if (res.msg === '删除成功' || res.msg === 'Delete successful') {
          this.fetchCart();
        }
      }
      item.oldQuantity = item.quantity;
      this.$message.success("Quantity updated successfully!");
    },
    handleSelectionChange(selection) {
      this.selectedItems = selection;
    },
    async initOrder() {
      const orderData = {
        userId: "Current user ID",
        totalAmount: this.totalPrice,
        paymentMethod: "Online payment",
        userAddress: null,
        omsPlaceOrderProductItemList: this.selectedItems.map(item => ({
          productId: item.id,
          quantity: item.quantity,
          price: item.price,
          isDiscount: item.isDiscount,
          discountNum: item.discountNum,
        })),
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

.cart-card {
  background: #fff;
  border: 1px solid rgba(255, 77, 79, 0.08);
  border-top: none;
  border-radius: 0 0 18px 18px;
  box-shadow: 0 10px 28px rgba(15, 23, 42, 0.06);
  overflow: hidden;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  margin: 0;
  color: #1f2937;
}

.cart-table :deep(.el-table__header-wrapper th) {
  background: #fff6f6;
  color: #1f2937;
  font-weight: 700;
}

.cart-table :deep(.el-table__body tr:hover > td) {
  background-color: rgba(255, 77, 79, 0.04);
}

.cart-table :deep(.el-input-number) {
  width: 140px;
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
}

.cart-total {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 18px;
  padding: 18px 20px 20px;
  border-top: 1px solid #eef2f7;
  background: linear-gradient(180deg, #ffffff 0%, #fff9f9 100%);
  text-align: right;
}

.total-text {
  font-size: 18px;
  font-weight: 700;
  color: #1f2937;
}

.checkout-btn {
  font-size: 16px;
  padding: 10px 22px;
  border-radius: 12px;
  background: linear-gradient(135deg, #ff4d4f 0%, #ff7a59 100%);
  border-color: #ff4d4f;
  box-shadow: 0 8px 18px rgba(255, 77, 79, 0.22);
}

.checkout-btn:hover,
.checkout-btn:focus {
  background: linear-gradient(135deg, #ff6b6d 0%, #ff8a3d 100%);
  border-color: #ff6b6d;
}

.cart-card :deep(.el-checkbox__input.is-checked .el-checkbox__inner),
.cart-card :deep(.el-radio__input.is-checked .el-radio__inner) {
  background-color: #ff4d4f;
  border-color: #ff4d4f;
}

.cart-card :deep(.el-button--text) {
  color: #ff4d4f;
}

.cart-card :deep(.el-button--text:hover),
.cart-card :deep(.el-button--text:focus) {
  color: #e6393c;
}

.cart-card :deep(.el-table__body td),
.cart-card :deep(.el-table__header th) {
  border-bottom-color: #eef2f7;
}
</style>
