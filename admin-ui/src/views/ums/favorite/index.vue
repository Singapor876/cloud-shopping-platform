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
          v-hasPermi="['ums:favorite:add']"
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
          v-hasPermi="['ums:favorite:edit']"
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
          v-hasPermi="['ums:favorite:remove']"
        >Delete</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['ums:favorite:export']"
        >Export</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="favoriteList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="No." align="center" width="100">
        <template slot-scope="scope">
          {{ scope.$index + 1 }}
        </template>
      </el-table-column>
<!--      <el-table-column label="Favorite ID" align="center" prop="id" />-->
      <el-table-column label="User" align="center" prop="userName" />
      <el-table-column label="Product Image" align="center" prop="productImage" width="100">
        <template slot-scope="scope">
          <image-preview :src="scope.row.productImage" :width="50" :height="50"/>
        </template>
      </el-table-column>
      <el-table-column label="Product" align="center" prop="productName" />
      <el-table-column label="Actions" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['ums:favorite:edit']"
          >Edit</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['ums:favorite:remove']"
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

    <!-- Add or modify favorite dialog -->
    <el-dialog :title="title" :visible.sync="open" width="500px" append-to-body>
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
        <el-form-item label="Product" prop="userId">
          <el-select v-model="form.productId" placeholder="Please select product" filterable clearable>
            <el-option
              v-for="product in productList"
              :key="product.id"
              :label="product.name"
              :value="product.id"
            />
          </el-select>
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
import { listFavorite, getFavorite, delFavorite, addFavorite, updateFavorite } from "@/api/ums/favorite";
import { listUser } from "@/api/ums/user";
import { listProduct } from "@/api/cms/product";

export default {
  name: "Favorite",
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
      // Favorite table data
      favoriteList: [],
      // User list
      userList: [],
      // Product list
      productList: [],
      // Dialog title
      title: "",
      // Dialog visibility
      open: false,
      // Query parameters
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        userId: null,
        userName: null,
        productId: null,
      },
      // Form parameters
      form: {
        userId: null,
        productId: null,
      },
      // Form validation
      rules: {
        userId: [
          { required: true, message: "User ID cannot be empty", trigger: "blur" }
        ],
        productId: [
          { required: true, message: "Product ID cannot be empty", trigger: "blur" }
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
    /** Query favorite list */
    getList() {
      this.loading = true;
      listFavorite(this.queryParams).then(response => {
        this.favoriteList = response.rows;
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
      this.ids = selection.map(item => item.id)
      this.single = selection.length!==1
      this.multiple = !selection.length
    },
    /** Add button operation */
    handleAdd() {
      this.reset();
      this.open = true;
      this.title = "Add Favorite";
    },
    /** Edit button operation */
    handleUpdate(row) {
      this.reset();
      const id = row.id || this.ids
      getFavorite(id).then(response => {
        this.form = response.data;
        this.open = true;
        this.title = "Edit Favorite";
      });
    },
    /** Submit button */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.id != null) {
            updateFavorite(this.form).then(response => {
              this.$modal.msgSuccess("Edit successful");
              this.open = false;
              this.getList();
            });
          } else {
            addFavorite(this.form).then(response => {
              this.$modal.msgSuccess("Add successful");
              this.open = false;
              this.getList();
            });
          }
        }
      });
    },
    /** Delete button operation */
    handleDelete(row) {
      const ids = row.id || this.ids;
      this.$modal.confirm('Are you sure you want to delete the favorite data with ID "' + ids + '"?').then(function() {
        return delFavorite(ids);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("Delete successful");
      }).catch(() => {});
    },
    /** Export button operation */
    handleExport() {
      this.download('ums/favorite/export', {
        ...this.queryParams
      }, `favorite_${new Date().getTime()}.xlsx`)
    }
  }
};
</script>
