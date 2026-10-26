<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="150px">
      <el-form-item label="Delivery Address" prop="userAddress">
        <el-input
          v-model="queryParams.userAddress"
          placeholder="Please enter delivery address"
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
          v-hasPermi="['oms:order:add']"
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
          v-hasPermi="['oms:order:edit']"
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
          v-hasPermi="['oms:order:remove']"
        >Delete</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['oms:order:export']"
        >Export</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="orderList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="No." align="center" width="100">
        <template slot-scope="scope">
          {{ scope.$index + 1 }}
        </template>
      </el-table-column>
<!--      <el-table-column label="订单ID" align="center" prop="id" />-->
      <el-table-column label="User" align="center" prop="userName" />
      <el-table-column label="Order Total Amount" align="center" prop="totalAmount" />
      <el-table-column label="Order Status" align="center" prop="orderStatus">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.oms_order_status" :value="scope.row.orderStatus"/>
        </template>
      </el-table-column>
      <el-table-column label="Logistics Status" align="center" prop="logisticsStatus">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.oms_logistics_status" :value="scope.row.logisticsStatus"/>
        </template>
      </el-table-column>
      <el-table-column label="Operation" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['oms:order:edit']"
          >Edit</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['oms:order:remove']"
          >Delete</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-info"
            @click="handleOrderItemDetail(scope.row)"
            v-hasPermi="['oms:order:detail']"
          >Order Item Details</el-button>
        </template>
      </el-table-column>

      <!-- 订单商品详情弹框 -->
      <el-dialog title="Order Item Details" :visible.sync="orderItemDetailVisible" width="800px" append-to-body>
        <el-table :data="orderItemDetailList" v-loading="orderItemDetailLoading">
          <el-table-column label="Product Image" align="center" width="120">
            <template slot-scope="scope">
              <el-image :src="scope.row.cmsProduct.imageUrl" style="width: 100px; height: 100px" fit="cover"></el-image>
            </template>
          </el-table-column>
          <el-table-column label="Product Name" align="center" prop="cmsProduct.name" />
          <el-table-column label="Product Quantity" align="center" prop="quantity" />
          <el-table-column label="Product Price" align="center" prop="price" />
          <el-table-column label="Product Total Price" align="center">
            <template slot-scope="scope">
              {{ (scope.row.quantity * scope.row.price).toFixed(2) }}
            </template>
          </el-table-column>
        </el-table>
      </el-dialog>

    </el-table>

    <pagination
      v-show="total>0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- 添加或修改订单对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="500px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="150px">
        <el-form-item label="User ID" prop="userId">
          <el-select v-model="form.userId" placeholder="Please select product category">
            <el-option
              v-for="item in userListOptions"
              :key="item.userId"
              :label="item.userName"
              :value="item.userId"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="Total Amount" prop="totalAmount">
          <el-input v-model="form.totalAmount" placeholder="Please enter order total amount" />
        </el-form-item>
        <el-form-item label="Order Status" prop="orderStatus">
          <el-select v-model="form.orderStatus" placeholder="Please select order status">
            <el-option
              v-for="dict in dict.type.oms_order_status"
              :key="dict.value"
              :label="dict.label"
              :value="parseInt(dict.value)"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="Logistics Status" prop="logisticsStatus">
          <el-select v-model="form.logisticsStatus" placeholder="Please select logistics status">
            <el-option
              v-for="dict in dict.type.oms_logistics_status"
              :key="dict.value"
              :label="dict.label"
              :value="parseInt(dict.value)"
            ></el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listOrder, getOrder, delOrder, addOrder, updateOrder, listUser } from "@/api/oms/order";
import {listLeafNodes} from "@/api/cms/category";

export default {
  name: "Order",
  dicts: ['oms_pay_type', 'oms_order_status', 'oms_logistics_status'],
  data() {
    return {
      // 遮罩层
      loading: true,
      // 选中数组
      ids: [],
      // 非单个禁用
      single: true,
      // 非多个禁用
      multiple: true,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 订单表格数据
      orderList: [],
      // 订单表格数据
      userListOptions: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 订单商品详情弹框是否显示
      orderItemDetailVisible: false,
      // 订单商品详情数据
      orderItemDetailList: [],
      // 订单商品详情加载状态
      orderItemDetailLoading: false,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        userId: null,
        totalAmount: null,
        orderStatus: null,
        logisticsStatus: null,
        paymentMethod: null,
        userAddress: null,
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        userId: [
          { required: true, message: "用户ID不能为空", trigger: "blur" }
        ],
        totalAmount: [
          { required: true, message: "订单总金额不能为空", trigger: "blur" }
        ],
        userAddress: [
          { required: true, message: "用户收货地址不能为空", trigger: "blur" }
        ],
        createTime: [
          { required: true, message: "创建时间不能为空", trigger: "blur" }
        ],
        updateTime: [
          { required: true, message: "更新时间不能为空", trigger: "blur" }
        ]
      }
    };
  },
  created() {
    this.getList();
    this.getUserListOptions();
  },
  methods: {
    /** 查询订单列表 */
    getList() {
      this.loading = true;
      listOrder(this.queryParams).then(response => {
        this.orderList = response.rows;
        this.total = response.total;
        this.loading = false;
      });
    },
    // 取消按钮
    cancel() {
      this.open = false;
      this.reset();
    },
    // 表单重置
    reset() {
      this.form = {
        id: null,
        userId: null,
        totalAmount: null,
        orderStatus: null,
        logisticsStatus: null,
        paymentMethod: null,
        userAddress: null,
        createTime: null,
        updateTime: null
      };
      this.resetForm("form");
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParams.pageNum = 1;
      this.getList();
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.resetForm("queryForm");
      this.handleQuery();
    },
    // 多选框选中数据
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.id)
      this.single = selection.length!==1
      this.multiple = !selection.length
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.reset();
      this.open = true;
      this.title = "添加订单";
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset();
      const id = row.id || this.ids
      getOrder(id).then(response => {
        this.form = response.data;
        this.open = true;
        this.title = "修改订单";
      });
    },
    /** 获取分类列表 */
    getUserListOptions() {
      listUser().then(response => {
        this.userListOptions = response.data;
      });
    },
    /** 订单商品详情按钮操作 */
    handleOrderItemDetail(row) {
      this.orderItemDetailLoading = true;
      this.orderItemDetailVisible = true;
      // 获取订单商品详情数据
      this.orderItemDetailList = row.omsOrderItemList || [];
      this.orderItemDetailLoading = false;
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.id != null) {
            updateOrder(this.form).then(response => {
              this.$modal.msgSuccess("修改成功");
              this.open = false;
              this.getList();
            });
          } else {
            addOrder(this.form).then(response => {
              this.$modal.msgSuccess("新增成功");
              this.open = false;
              this.getList();
            });
          }
        }
      });
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      const ids = row.id || this.ids;
      this.$modal.confirm('是否确认删除订单编号为"' + ids + '"的数据项？').then(function() {
        return delOrder(ids);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {});
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('oms/order/export', {
        ...this.queryParams
      }, `order_${new Date().getTime()}.xlsx`)
    }
  }
};
</script>
