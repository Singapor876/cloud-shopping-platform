<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="150px">
      <el-form-item label="Promotion Name" prop="name">
        <el-input
          v-model="queryParams.name"
          placeholder="Please enter promotion name"
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
          v-hasPermi="['cms:promotion:add']"
        >Add</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="el-icon-edit"
          size="mini"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['cms:promotion:edit']"
        >Edit</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="el-icon-delete"
          size="mini"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['cms:promotion:remove']"
        >Delete</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['cms:promotion:export']"
        >Export</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="promotionList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="No." align="center" width="100">
        <template slot-scope="scope">
          {{ scope.$index + 1 }}
        </template>
      </el-table-column>
<!--      <el-table-column label="Promotion ID" align="center" prop="id" />-->
      <el-table-column label="Promotion Name" align="center" prop="name" />
      <el-table-column label="Promotion Type" align="center" prop="type">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.cms_promototion_type" :value="scope.row.type"/>
        </template>
      </el-table-column>
      <el-table-column label="Discount Rate" align="center" prop="discount" />
      <el-table-column label="Coupon Value" align="center" prop="couponValue" />
      <el-table-column label="Start Time" align="center" prop="startTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.startTime, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="End Time" align="center" prop="endTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.endTime, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="Operation" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['cms:promotion:edit']"
          >Edit</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['cms:promotion:remove']"
          >Delete</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-share"
            @click="handleDistributeProduct(scope.row)"
            v-hasPermi="['cms:promotion:distribute']"
          >Assign Products</el-button>
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

    <!-- Add or Edit Promotion Dialog -->
    <el-dialog :title="title" :visible.sync="open" width="500px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="150px">
        <el-form-item label="Promotion Name" prop="name">
          <el-input v-model="form.name" placeholder="Please enter promotion name" />
        </el-form-item>
        <el-form-item label="Promotion Type" prop="type">
          <el-select v-model="form.type" placeholder="Please select promotion type">
            <el-option
              v-for="dict in dict.type.cms_promototion_type"
              :key="dict.value"
              :label="dict.label"
              :value="parseInt(dict.value)"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="Discount Rate" prop="discount">
          <el-input v-model="form.discount" placeholder="Please enter discount rate" />
        </el-form-item>
        <el-form-item label="Coupon Value" prop="couponValue">
          <el-input v-model="form.couponValue" placeholder="Please enter coupon value" />
        </el-form-item>
        <el-form-item label="Start Time" prop="startTime">
          <el-date-picker clearable
                          v-model="form.startTime"
                          type="date"
                          value-format="yyyy-MM-dd"
                          placeholder="Please select start time">
          </el-date-picker>
        </el-form-item>
        <el-form-item label="End Time" prop="endTime">
          <el-date-picker clearable
                          v-model="form.endTime"
                          type="date"
                          value-format="yyyy-MM-dd"
                          placeholder="Please select end time">
          </el-date-picker>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">OK</el-button>
        <el-button @click="cancel">Cancel</el-button>
      </div>
    </el-dialog>

    <!-- Assign Products Dialog 1 -->
    <el-dialog title="Assign Products" :visible.sync="distributeDialogVisible" width="800px" append-to-body>
      <el-row :gutter="10" class="mb8">
        <el-col :span="1.5">
          <el-button
            type="primary"
            plain
            icon="el-icon-plus"
            size="mini"
            @click="openAddProductDialog"
          >Add</el-button>
        </el-col>
      </el-row>
      <el-table :data="currentProductList" v-loading="productLoading">
        <el-table-column label="Product Image" align="center" width="100">
          <template slot-scope="scope">
            <el-image
              :src="scope.row.imageUrl"
              style="width: 50px; height: 50px"
              fit="cover"
            />
          </template>
        </el-table-column>
        <el-table-column label="Product Name" align="center" prop="name" />
        <el-table-column label="Product Description" align="center" prop="description" />
        <el-table-column label="Product Price" align="center" prop="price" />
        <el-table-column label="Operation" align="center" width="100">
          <template slot-scope="scope">
            <el-button
              size="mini"
              type="text"
              icon="el-icon-delete"
              @click="removeProduct(scope.row)"
            >Delete</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div slot="footer" class="dialog-footer">
        <el-button @click="distributeDialogVisible = false">Cancel</el-button>
        <el-button type="primary" @click="submitDistributeProduct">OK</el-button>
      </div>
    </el-dialog>

    <!-- Add Products Dialog 2 -->
    <el-dialog title="Select Products" :visible.sync="addProductDialogVisible" width="800px" append-to-body>
      <el-table
        v-loading="addProductLoading"
        :data="productList"
        @selection-change="handleProductSelectionChange"
      >
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column label="Product Image" align="center" width="100">
          <template slot-scope="scope">
            <el-image
              :src="scope.row.imageUrl"
              style="width: 50px; height: 50px"
              fit="cover"
            />
          </template>
        </el-table-column>
        <el-table-column label="Product Name" align="center" prop="name" />
        <el-table-column label="Product Description" align="center" prop="description" />
        <el-table-column label="Product Price" align="center" prop="price" />
      </el-table>
      <pagination
        v-show="productTotal>0"
        :total="productTotal"
        :page.sync="productQueryParams.pageNum"
        :limit.sync="productQueryParams.pageSize"
        @pagination="getProductList"
      />
      <div slot="footer" class="dialog-footer">
        <el-button @click="addProductDialogVisible = false">Cancel</el-button>
        <el-button type="primary" @click="addSelectedProducts">OK</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listPromotion, getPromotion, delPromotion, addPromotion, updatePromotion, distributeProductByPromotionId } from "@/api/cms/promotion";
import { listProduct } from "@/api/cms/product";

export default {
  name: "Promotion",
  dicts: ['cms_promototion_type'],
  data() {
    return {
      loading: true,
      ids: [],
      single: true,
      multiple: true,
      showSearch: true,
      total: 0,
      promotionList: [],
      title: "",
      open: false,
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        name: null,
        type: null,
        discount: null,
        couponCode: null,
        couponValue: null,
        startTime: null,
        endTime: null,
      },
      form: {},
      rules: {
        name: [
          { required: true, message: "Promotion name cannot be empty", trigger: "blur" }
        ],
        type: [
          { required: true, message: "Promotion type cannot be empty", trigger: "change" }
        ],
        startTime: [
          { required: true, message: "Start time cannot be empty", trigger: "blur" }
        ],
        endTime: [
          { required: true, message: "End time cannot be empty", trigger: "blur" }
        ],
        createTime: [
          { required: true, message: "Creation time cannot be empty", trigger: "blur" }
        ],
        updateTime: [
          { required: true, message: "Update time cannot be empty", trigger: "blur" }
        ]
      },
      distributeDialogVisible: false,
      productLoading: false,
      currentProductList: [],
      currentPromotionId: null,

      addProductDialogVisible: false,
      addProductLoading: false,
      productList: [],
      productTotal: 0,
      productQueryParams: {
        pageNum: 1,
        pageSize: 10,
      },
      selectedProducts: [],
    };
  },
  created() {
    this.getList();
  },
  methods: {
    getList() {
      this.loading = true;
      listPromotion(this.queryParams).then(response => {
        this.promotionList = response.rows;
        this.total = response.total;
        this.loading = false;
      });
    },
    cancel() {
      this.open = false;
      this.reset();
    },
    reset() {
      this.form = {
        id: null,
        name: null,
        type: null,
        discount: null,
        couponCode: null,
        couponValue: null,
        startTime: null,
        endTime: null,
        createTime: null,
        updateTime: null
      };
      this.resetForm("form");
    },
    handleQuery() {
      this.queryParams.pageNum = 1;
      this.getList();
    },
    resetQuery() {
      this.resetForm("queryForm");
      this.handleQuery();
    },
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.id)
      this.single = selection.length!==1
      this.multiple = !selection.length
    },
    handleAdd() {
      this.reset();
      this.open = true;
      this.title = "Add Promotion";
    },
    handleUpdate(row) {
      this.reset();
      const id = row.id || this.ids
      getPromotion(id).then(response => {
        this.form = response.data;
        this.open = true;
        this.title = "Edit Promotion";
      });
    },
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.id != null) {
            updatePromotion(this.form).then(response => {
              this.$modal.msgSuccess("Modified successfully");
              this.open = false;
              this.getList();
            });
          } else {
            addPromotion(this.form).then(response => {
              this.$modal.msgSuccess("Added successfully");
              this.open = false;
              this.getList();
            });
          }
        }
      });
    },
    handleDelete(row) {
      const ids = row.id || this.ids;
      this.$modal.confirm('Are you sure you want to delete promotion ID "' + ids + '"?').then(function() {
        return delPromotion(ids);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("Deleted successfully");
      }).catch(() => {});
    },
    handleExport() {
      this.download('cms/promotion/export', {
        ...this.queryParams
      }, `promotion_${new Date().getTime()}.xlsx`)
    },
    handleDistributeProduct(row) {
      this.currentPromotionId = row.id;
      this.currentProductList = row.productList || [];
      this.distributeDialogVisible = true;
    },
    openAddProductDialog() {
      this.addProductDialogVisible = true;
      this.getProductList();
    },
    getProductList() {
      this.addProductLoading = true;
      listProduct(this.productQueryParams).then(response => {
        this.productList = response.rows;
        this.productTotal = response.total;
        this.addProductLoading = false;
      });
    },
    handleProductSelectionChange(selection) {
      this.selectedProducts = selection;
    },
    addSelectedProducts() {
      this.selectedProducts.forEach(product => {
        if (!this.currentProductList.some(p => p.id === product.id)) {
          this.currentProductList.push(product);
        }
      });
      this.addProductDialogVisible = false;
    },
    removeProduct(product) {
      this.currentProductList = this.currentProductList.filter(p => p.id !== product.id);
    },
    submitDistributeProduct() {
      if (!this.currentPromotionId || this.currentProductList.length === 0) {
        this.$modal.msgError("Please select products to assign");
        return;
      }
      const data = {
        promotionId: this.currentPromotionId,
        productIds: this.currentProductList.map(p => p.id),
      };
      distributeProductByPromotionId(data).then(response => {
        this.$modal.msgSuccess("Assigned successfully");
        this.distributeDialogVisible = false;
        this.getList();
      });
    },
  },
};
</script>
