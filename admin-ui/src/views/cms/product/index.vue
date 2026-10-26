<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="150px">
      <el-form-item label="Product Name" prop="name">
        <el-input
          v-model="queryParams.name"
          placeholder="Please enter product name"
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
          v-hasPermi="['cms:product:add']"
        >Add New</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="el-icon-edit"
          size="mini"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['cms:product:edit']"
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
          v-hasPermi="['cms:product:remove']"
        >Delete</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['cms:product:export']"
        >Export</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="productList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="Serial No." align="center" width="100">
        <template slot-scope="scope">
          {{ scope.$index + 1 }}
        </template>
      </el-table-column>
<!--      <el-table-column label="Product ID" align="center" prop="id" />-->
      <el-table-column label="Product Name" align="center" prop="name" />
      <el-table-column label="Product Description" align="center" prop="description" />
      <el-table-column label="Product Price" align="center" prop="price" />
      <el-table-column label="Stock Quantity" align="center" prop="stock" />
      <el-table-column label="Product Category" align="center" prop="categoryName" />
      <el-table-column label="Product Status" align="center" prop="productStatus">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.cms_product_status" :value="scope.row.productStatus"/>
        </template>
      </el-table-column>
      <el-table-column label="Product Image" align="center" prop="imageUrl" width="100">
        <template slot-scope="scope">
          <image-preview :src="scope.row.imageUrl" :width="50" :height="50"/>
        </template>
      </el-table-column>
      <el-table-column label="Actions" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['cms:product:edit']"
          >Edit</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['cms:product:remove']"
          >Delete</el-button>
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

    <!-- Add or Edit Product Dialog -->
    <el-dialog :title="title" :visible.sync="open" width="500px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="150px">
        <el-form-item label="Product Name" prop="name">
          <el-input v-model="form.name" placeholder="Please enter product name" />
        </el-form-item>
        <el-form-item label="Product Description" prop="description">
          <el-input v-model="form.description" type="textarea" placeholder="Please enter content" />
        </el-form-item>
        <el-form-item label="Product Price" prop="price">
          <el-input v-model="form.price" placeholder="Please enter product price" />
        </el-form-item>
        <el-form-item label="Stock Quantity" prop="stock">
          <el-input v-model="form.stock" placeholder="Please enter stock quantity" />
        </el-form-item>
        <el-form-item label="Product Category" prop="categoryId">
          <el-select v-model="form.categoryId" placeholder="Please select product category">
            <el-option
              v-for="item in categoryOptions"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="Product Status" prop="productStatus">
          <el-select v-model="form.productStatus" placeholder="Please select product status">
            <el-option
              v-for="dict in dict.type.cms_product_status"
              :key="dict.value"
              :label="dict.label"
              :value="parseInt(dict.value)"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="Product Image" prop="imageUrl">
          <image-upload v-model="form.imageUrl"/>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">OK</el-button>
        <el-button @click="cancel">Cancel</el-button>
      </div>
    </el-dialog>
  </div>
</template>
<script>
import { listProduct, getProduct, delProduct, addProduct, updateProduct } from "@/api/cms/product";
import { listLeafNodes } from "@/api/cms/category";
export default {
  name: "Product",
  dicts: ['cms_product_status'],
  data() {
    return {
      // Mask layer
      loading: true,
      // Selected array
      ids: [],
      // Not single disable
      single: true,
      // Not multiple disable
      multiple: true,
      // Show search conditions
      showSearch: true,
      // Total number of records
      total: 0,
      // Product table data
      productList: [],
      // Category options
      categoryOptions: [],
      // Dialog title
      title: "",
      // Show dialog or not
      open: false,
      // Query parameters
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        name: null,
        description: null,
        price: null,
        stock: null,
        categoryId: null,
        productStatus: null,
        imageUrl: null,
      },
      // Form parameters
      form: {},
      // Form validation
      rules: {
        name: [
          { required: true, message: "Product name cannot be empty", trigger: "blur" }
        ],
        price: [
          { required: true, message: "Product price cannot be empty", trigger: "blur" }
        ],
        stock: [
          { required: true, message: "Stock quantity cannot be empty", trigger: "blur" }
        ],
        categoryId: [
          { required: true, message: "Product category ID cannot be empty", trigger: "blur" }
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
    this.getCategoryList();
  },
  methods: {
    /** Query product list */
    getList() {
      this.loading = true;
      listProduct(this.queryParams).then(response => {
        this.productList = response.rows;
        this.total = response.total;
        this.loading = false;
      });
    },
    /** Get category list */
    getCategoryList() {
      listLeafNodes().then(response => {
        this.categoryOptions = response.data;
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
        name: null,
        description: null,
        price: null,
        stock: null,
        categoryId: null,
        productStatus: null,
        imageUrl: null,
        createTime: null,
        updateTime: null
      };
      this.resetForm("form");
    },
    /** Search button action */
    handleQuery() {
      this.queryParams.pageNum = 1;
      this.getList();
    },
    /** Reset button action */
    resetQuery() {
      this.resetForm("queryForm");
      this.handleQuery();
    },
    // Checkbox selected data
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.id)
      this.single = selection.length!==1
      this.multiple = !selection.length
    },
    /** Add button action */
    handleAdd() {
      this.reset();
      this.open = true;
      this.title = "Add Product";
    },
    /** Edit button action */
    handleUpdate(row) {
      this.reset();
      const id = row.id || this.ids
      getProduct(id).then(response => {
        this.form = response.data;
        this.open = true;
        this.title = "Edit Product";
      });
    },
    /** Submit button */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.id != null) {
            updateProduct(this.form).then(response => {
              this.$modal.msgSuccess("Update successful");
              this.open = false;
              this.getList();
            });
          } else {
            addProduct(this.form).then(response => {
              this.$modal.msgSuccess("Add successful");
              this.open = false;
              this.getList();
            });
          }
        }
      });
    },
    /** Delete button action */
    handleDelete(row) {
      const ids = row.id || this.ids;
      this.$modal.confirm('Are you sure you want to delete the product with ID "' + ids + '"?').then(function() {
        return delProduct(ids);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("Delete successful");
      }).catch(() => {});
    },
    /** Export button action */
    handleExport() {
      this.download('cms/product/export', {
        ...this.queryParams
      }, `product_${new Date().getTime()}.xlsx`)
    }
  }
};
</script>
