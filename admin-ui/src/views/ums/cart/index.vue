<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="150px">
      <el-form-item label="Username" prop="userName">
        <el-input
          v-model="queryParams.userName"
          placeholder="Please enter username"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>

      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">Search</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">Reset</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="el-icon-plus"
          size="mini"
          @click="handleAdd"
          v-hasPermi="['ums:cart:add']"
        >Add</el-button>
      </el-col>
      <el-col :span="1.5">
<!--        <el-button-->
<!--          type="success"-->
<!--          plain-->
<!--          icon="el-icon-edit"-->
<!--          size="mini"-->
<!--          :disabled="single"-->
<!--          @click="handleUpdate"-->
<!--          v-hasPermi="['ums:cart:edit']"-->
<!--        >Edit</el-button>-->
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="el-icon-delete"
          size="mini"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['ums:cart:remove']"
        >Delete</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['ums:cart:export']"
        >Export</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="cartList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="No." align="center" width="100">
        <template slot-scope="scope">
          {{ scope.$index + 1 }}
        </template>
      </el-table-column>
      <el-table-column label="User" align="center" prop="umsUser.userName" />
      <el-table-column label="Products" align="center">
        <template slot-scope="scope">
          <div v-for="product in scope.row.cmsProductList" :key="product.id">
            {{ product.name }} × {{ product.quantity }}
          </div>
        </template>
      </el-table-column>
      <el-table-column label="Actions" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
<!--          <el-button-->
<!--            size="mini"-->
<!--            type="text"-->
<!--            icon="el-icon-edit"-->
<!--            @click="handleUpdate(scope.row)"-->
<!--            v-hasPermi="['ums:cart:edit']"-->
<!--          >Edit</el-button>-->
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['ums:cart:remove']"
          >Delete</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-info"
            @click="handleProductDetail(scope.row)"
            v-hasPermi="['ums:cart:detail']"
          >Product Details</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total>0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- Add or modify cart dialog -->
    <el-dialog :title="title" :visible.sync="open" width="600px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="150px">
        <el-form-item label="User" prop="userId">
          <el-select v-model="form.userId" placeholder="Please select user" filterable clearable>
            <el-option
              v-for="user in userList"
              :key="user.userId"
              :label="user.userName"
              :value="user.userId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="" prop="userId">
          <el-button type="primary" size="mini" @click="openProductDialog">Add Product</el-button>
        </el-form-item>

        <el-form-item label="Product List" prop="productList">
          <el-table :data="form.productList" border>
            <el-table-column label="Product Name" prop="name" align="center"/>
            <el-table-column label="Quantity" width="250" align="center">
              <template slot-scope="scope">
                <el-input-number
                  v-model="scope.row.quantity"
                  :min="1"
                  :max="scope.row.stock"
                  controls-position="right"
                />
              </template>
            </el-table-column>
            <el-table-column label="Actions" width="100" align="center">
              <template slot-scope="scope">
                <el-button type="danger" size="mini" @click="removeProduct(scope.$index)">Delete</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">OK</el-button>
        <el-button @click="cancel">Cancel</el-button>
      </div>
    </el-dialog>
    <!-- All products dialog -->
    <el-dialog title="Select Product" :visible.sync="productDialogVisible" width="800px" append-to-body>
      <el-table :data="productList" @selection-change="handleProductSelectionChange">
        <el-table-column type="selection" width="55" />
        <el-table-column label="Product Name" prop="name" />
        <el-table-column label="Description" prop="description" />
        <el-table-column label="Stock" prop="stock" />
      </el-table>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="addSelectedProducts">OK</el-button>
        <el-button @click="productDialogVisible = false">Cancel</el-button>
      </div>
    </el-dialog>

    <!-- Product details dialog -->
    <el-dialog title="Product Details" :visible.sync="productDetailVisible" width="800px" append-to-body>
      <el-table :data="productDetailList" v-loading="productDetailLoading">
        <el-table-column label="Product Image" align="center" width="120">
          <template slot-scope="scope">
            <el-image :src="scope.row.imageUrl" style="width: 100px; height: 100px" fit="cover"></el-image>
          </template>
        </el-table-column>
        <el-table-column label="Product Name" align="center" prop="name" />
        <el-table-column label="Description" align="center" prop="description" />
        <el-table-column label="Quantity" align="center" prop="quantity" />
      </el-table>
    </el-dialog>
  </div>
</template>

<script>
import { listCart, getCart, delCart, addCart, updateCart } from "@/api/ums/cart";
import { listUser } from "@/api/ums/user";
import { listProduct } from "@/api/cms/product";

export default {
  name: "Cart",
  data() {
    return {
      // Loading layer
      loading: true,
      // Selected array
      ids: [],
      // Not single disabled
      single: true,
      // Not multiple disabled
      multiple: true,
      // Show search conditions
      showSearch: true,
      // Total number of items
      total: 0,
      // Cart table data
      cartList: [],
      // Product details dialog visibility
      productDetailVisible: false,
      // Product details data
      productDetailList: [],
      // User list
      userList: [],
      // Product list
      productList: [],
      // Selected product list (for dialog)
      selectedProducts: [],
      // Product dialog visibility
      productDialogVisible: false,
      // Form parameters
      form: {
        userId: null,
        productList: [], // Product list containing product ID, name, quantity, and stock
      },
      // Product details loading state
      productDetailLoading: false,
      // Dialog title
      title: "",
      // Dialog visibility
      open: false,
      // Query parameters
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        userId: null,
        productId: null,
        quantity: null,
        userName: null,
      },
      // Form validation
      rules: {
        userId: [
          { required: true, message: "User ID cannot be empty", trigger: "blur" }
        ],
        productId: [
          { required: true, message: "Product ID cannot be empty", trigger: "blur" }
        ],
        quantity: [
          { required: true, message: "Product quantity cannot be empty", trigger: "blur" }
        ],
        createTime: [
          { required: true, message: "Creation time cannot be empty", trigger: "blur" }
        ],
        updateTime: [
          { required: true, message: "Update time cannot be empty", trigger: "blur" }
        ]
      }
    };
  },
  created() {
    this.getList();
    this.loadUserList();
    this.loadProductList();
  },
  computed: {
    // Get max stock for currently selected product
    selectedProductStock() {
      const product = this.productList.find(p => p.id === this.form.productId);
      return product ? product.stock : 1;
    }
  },
  methods: {
    /** Load user list */
    loadUserList() {
      listUser().then(response => {
        this.userList = response.rows || [];
      });
    },
    /** Load product list */
    loadProductList() {
      listProduct().then(response => {
        this.productList = response.rows || [];
      });
    },
    /** Open product selection dialog */
    openProductDialog() {
      this.productDialogVisible = true;
    },
    /** Handle product selection */
    handleProductSelectionChange(selection) {
      this.selectedProducts = selection;
    },
    /** Add selected products to list */
    addSelectedProducts() {
      this.selectedProducts.forEach(product => {
        // Check if already exists
        const exists = this.form.productList.some(p => p.id === product.id);
        if (!exists) {
          this.form.productList.push({
            id: product.id,
            name: product.name,
            stock: product.stock,
            quantity: 1, // Default quantity is 1
          });
        }
      });
      this.productDialogVisible = false;
    },
    /** Delete product */
    removeProduct(index) {
      this.form.productList.splice(index, 1);
    },
    /** Query cart list */
    getList() {
      this.loading = true;
      listCart(this.queryParams).then(response => {
        this.cartList = response.rows;
        this.total = response.total;
        this.loading = false;
      });
    },
    // Cancel button
    cancel() {
      this.open = false;
      this.reset();
    },
    // Form reset
    reset() {
      this.form = {
        id: null,
        userId: null,
        productId: null,
        quantity: null,
        createTime: null,
        updateTime: null
      };
      this.resetForm("form");
    },
    /** Search button operation */
    handleQuery() {
      this.queryParams.pageNum = 1;
      this.getList();
    },
    /** Reset button operation */
    resetQuery() {
      this.resetForm("queryForm");
      this.handleQuery();
    },
    // Checkbox selected data
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.userId)
      this.single = selection.length!==1
      this.multiple = !selection.length
    },
    /** Add button operation */
    handleAdd() {
      this.reset();
      this.form = {
        userId: null,
        productList: [], // Initialize as empty array
      };
      this.open = true;
      this.title = "Add Cart";
    },
    /** Edit button operation */
    handleUpdate(row) {
      this.reset();
      const id = row.userId || this.ids
      getCart(id).then(response => {
        this.form = response.data;
        this.open = true;
        this.title = "Edit Cart";
      });
    },
    /** Product details button operation */
    handleProductDetail(row) {
      this.productDetailVisible = true;
      this.productDetailList = row.cmsProductList || [];
    },
    /** Submit button */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          // Iterate productList, split each product
          this.form.productList.forEach(product => {
            const cartItem = {
              userId: this.form.userId,
              productId: product.id,
              quantity: product.quantity
            };

            if (this.form.id != null) {
              // Edit operation
              updateCart(cartItem).then(response => {
                this.$modal.msgSuccess("Edit successful");
                this.open = false;
                this.getList();
              });
            } else {
              // Add operation
              addCart(cartItem).then(response => {
                this.$modal.msgSuccess("Add successful");
                this.open = false;
                this.getList();
              });
            }
          });
        }
      });
    },

    /** Delete button operation */
    handleDelete(row) {
      const ids = row.userId || this.ids;
      this.$modal.confirm('Are you sure you want to delete the cart data with ID "' + ids + '"?').then(function() {
        return delCart(ids);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("Delete successful");
      }).catch(() => {});
    },
    /** Export button operation */
    handleExport() {
      this.download('ums/cart/export', {
        ...this.queryParams
      }, `cart_${new Date().getTime()}.xlsx`)
    }
  }
};
</script>
