<template>
  <div class="app-container">
    <el-form ref="form" :model="form" :rules="rules" label-width="150px">
      <el-form-item label="Carousel" prop="imageUrl">
        <el-button type="primary" @click="openProductDialog">Select Products</el-button>
        <span v-if="form.imageUrl" style="margin-left: 10px;">Selected {{ parsedImageUrl.length }} products</span>
        <div v-if="form.imageUrl" class="selected-products">
          <el-image
            v-for="(product, index) in parsedImageUrl"
            :key="index"
            :src="product.imageUrl"
            fit="cover"
            style="width: 100px; height: 100px; margin-right: 10px; margin-top: 10px;"
          >
            <div slot="error" class="image-fallback">
              <i class="el-icon-picture-outline" />
            </div>
          </el-image>
        </div>
      </el-form-item>
      <el-form-item label="Content Description">
        <editor v-model="form.description" :min-height="192" />
      </el-form-item>
    </el-form>

    <div class="footer">
      <el-button type="primary" @click="submitForm">Save</el-button>
    </div>

    <el-dialog
      title="Select Products"
      :visible.sync="productDialogVisible"
      width="80%"
      :before-close="handleDialogClose"
    >
      <el-table
        ref="productTable"
        :data="productList"
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column label="Product Image" width="120">
          <template slot-scope="scope">
            <el-image
              :src="scope.row.imageUrl"
              fit="cover"
              style="width: 100px; height: 100px;"
            >
              <div slot="error" class="image-fallback">
                <i class="el-icon-picture-outline" />
              </div>
            </el-image>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="Product Name" />
        <el-table-column prop="description" label="Product Description" />
      </el-table>
      <el-pagination
        style="margin-top: 20px; text-align: right;"
        :current-page="pagination.currentPage"
        :page-size="pagination.pageSize"
        :total="pagination.total"
        @current-change="handlePageChange"
        layout="prev, pager, next"
      />
      <div slot="footer" class="dialog-footer">
        <el-button @click="productDialogVisible = false">Cancel</el-button>
        <el-button type="primary" @click="confirmSelection">Confirm</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getContent, updateContent } from "@/api/cms/content";
import { listProduct } from "@/api/cms/product";

export default {
  name: "ContentEditor",
  data() {
    return {
      form: {
        id: null,
        imageUrl: null,
        description: null,
      },
      rules: {
        imageUrl: [
          { required: true, message: "Carousel cannot be empty", trigger: "blur" },
        ],
        description: [
          { required: true, message: "Content description cannot be empty", trigger: "blur" },
        ],
      },
      productDialogVisible: false,
      productList: [],
      selectedProducts: [],
      pagination: {
        currentPage: 1,
        pageSize: 10,
        total: 0,
      },
    };
  },
  computed: {
    parsedImageUrl() {
      if (this.form.imageUrl) {
        try {
          return JSON.parse(this.form.imageUrl);
        } catch (e) {
          console.error("Failed to parse imageUrl", e);
          return [];
        }
      }
      return [];
    },
    selectedProductIds() {
      return this.parsedImageUrl.map((product) => product.id);
    },
  },
  created() {
    this.loadData();
  },
  methods: {
    loadData() {
      getContent().then((response) => {
        this.form = response.data;
      });
    },
    submitForm() {
      this.$refs["form"].validate((valid) => {
        if (valid) {
          updateContent(this.form).then(() => {
            this.$modal.msgSuccess("Saved successfully");
          });
        }
      });
    },
    openProductDialog() {
      this.productDialogVisible = true;
      this.loadProductList();
    },
    loadProductList() {
      const params = {
        pageNum: this.pagination.currentPage,
        pageSize: this.pagination.pageSize,
      };
      listProduct(params).then((response) => {
        if (response.code === 200) {
          this.productList = response.rows;
          this.pagination.total = response.total;
          this.$nextTick(() => {
            this.setDefaultSelection();
          });
        } else {
          this.$message.error(response.msg || "Failed to load product list");
        }
      });
    },
    setDefaultSelection() {
      if (this.productList.length > 0 && this.selectedProductIds.length > 0) {
        this.productList.forEach((product) => {
          if (this.selectedProductIds.includes(product.id)) {
            this.$refs.productTable.toggleRowSelection(product, true);
          }
        });
      }
    },
    handleSelectionChange(selection) {
      this.selectedProducts = selection;
    },
    confirmSelection() {
      if (this.selectedProducts.length === 0) {
        this.$message.warning("Please select at least one product");
        return;
      }
      const selectedData = this.selectedProducts.map((product) => ({
        id: product.id,
        imageUrl: product.imageUrl,
      }));
      this.form.imageUrl = JSON.stringify(selectedData);
      this.productDialogVisible = false;
    },
    handleDialogClose(done) {
      this.selectedProducts = [];
      done();
    },
    handlePageChange(page) {
      this.pagination.currentPage = page;
      this.loadProductList();
    },
  },
};
</script>

<style scoped>
.app-container {
  padding: 20px;
}

.footer {
  margin-top: 20px;
  text-align: center;
}

.image-fallback {
  width: 100px;
  height: 100px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
  color: #909399;
}

.selected-products {
  display: flex;
  flex-wrap: wrap;
  margin-top: 10px;
}
</style>
