<template>
  <div class="personal-page">
    <div class="personal-shell">
      <div class="page-banner">
        <h3 class="page-title">Personal Information</h3>
      </div>

      <el-form class="personal-form" :model="form" label-width="120px" v-loading="loading">
        <el-form-item label="Avatar" class="avatar-row">
          <div class="avatar-card">
            <el-upload
                class="avatar-uploader"
                action="#"
                :show-file-list="false"
                :before-upload="beforeAvatarUpload"
                :http-request="handleAvatarUpload"
            >
              <img v-if="form.userAvatar" :src="form.userAvatar" class="avatar" alt="User Avatar" />
              <i v-else class="el-icon-plus avatar-uploader-icon"></i>
            </el-upload>
            <div class="user-balance">
              Balance: {{ form.userBalance }}
              <el-button type="text" class="balance-action" @click="openRechargeDialog">Recharge</el-button>
            </div>
          </div>
        </el-form-item>

        <el-form-item label="Username">
          <el-input v-model="form.userName" />
        </el-form-item>
        <el-form-item label="Phone">
          <el-input v-model="form.userPhone" />
        </el-form-item>

        <el-form-item label="Address">
          <el-button type="primary" class="theme-button" @click="openAddressDialog">Manage Address</el-button>
        </el-form-item>

        <el-form-item label="Reset">
          <el-button type="primary" class="theme-button" @click="openChangePasswordDialog">Change Password</el-button>
        </el-form-item>

        <el-form-item class="submit-row">
          <el-button type="primary" class="theme-button theme-button--save" @click="handleSubmit">Save</el-button>
        </el-form-item>
      </el-form>
    </div>

    <el-dialog title="Manage Address" :visible.sync="addressDialogVisible" width="600px" custom-class="personal-dialog">
      <el-table :data="addressList" border>
        <el-table-column label="Phone" prop="phone" />
        <el-table-column label="Address" prop="address" />
        <el-table-column label="Action" width="120">
          <template slot-scope="scope">
            <el-button type="text" @click="editAddress(scope.row)">Edit</el-button>
            <el-button type="text" @click="deleteAddress(scope.row.phone)">Delete</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-form :model="currentAddress" label-width="100px" class="address-form">
        <el-form-item label="Phone">
          <el-input v-model="currentAddress.phone" />
        </el-form-item>
        <el-form-item label="Address">
          <el-input v-model="currentAddress.address" />
        </el-form-item>
      </el-form>

      <div slot="footer" class="dialog-footer">
        <el-button @click="addressDialogVisible = false">Cancel</el-button>
        <el-button type="primary" class="theme-button" @click="saveAddress">Save Address</el-button>
      </div>
    </el-dialog>

    <el-dialog title="Recharge" :visible.sync="rechargeDialogVisible" width="400px" custom-class="personal-dialog">
      <el-form :model="rechargeForm" label-width="100px">
        <el-form-item label="Current Balance">
          <el-input v-model="form.userBalance" disabled />
        </el-form-item>
        <el-form-item label="Recharge Amount">
          <el-input v-model="rechargeForm.amount" type="number" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="rechargeDialogVisible = false">Cancel</el-button>
        <el-button type="primary" class="theme-button" @click="handleRecharge">Save</el-button>
      </div>
    </el-dialog>

    <el-dialog title="Change Password" :visible.sync="changePasswordDialogVisible" width="400px" custom-class="personal-dialog">
      <el-form :model="passwordForm" label-width="100px" placeholder="Enter new password">
        <el-form-item label="New">
          <el-input v-model="passwordForm.newPassword" type="password" placeholder="Enter new password"/>
        </el-form-item>
        <el-form-item label="Confirm" placeholder="Enter password again">
          <el-input v-model="passwordForm.confirmPassword" type="password" placeholder="Enter password again"/>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="changePasswordDialogVisible = false">Cancel</el-button>
        <el-button type="primary" class="theme-button" @click="handleChangePassword">Save</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getById, updatePersonalData } from "@/api/ums/user";
import { fileUpload } from "@/api/publish/file";

export default {
  data() {
    return {
      form: {
        userId: "",
        userName: "",
        userAvatar: "",
        userPhone: "",
        userBalance: 0,
        userAddress: "[]",
        userPassword: "",
      },
      loading: false,
      addressDialogVisible: false,
      addressList: [],
      currentAddress: { id: null, phone: "", address: "" },
      nextAddressId: 1,
      rechargeDialogVisible: false,
      rechargeForm: { amount: 0 },
      changePasswordDialogVisible: false,
      passwordForm: { newPassword: "", confirmPassword: "" },
    };
  },
  created() {
    this.fetchUserData();
  },
  methods: {
    async fetchUserData() {
      this.loading = true;
      try {
        const response = await getById();
        this.form = {
          ...response.data,
        };
        this.addressList = JSON.parse(this.form.userAddress || "[]");
        if (this.addressList.length > 0) {
          this.nextAddressId = Math.max(...this.addressList.map((item) => item.id)) + 1;
        }
      } catch (error) {
        console.error("Failed to fetch user data:", error);
      } finally {
        this.loading = false;
      }
    },
    openAddressDialog() {
      this.addressDialogVisible = true;
    },
    editAddress(address) {
      this.currentAddress = { ...address };
    },
    deleteAddress(phone) {
      this.addressList = this.addressList.filter((item) => item.phone !== phone);
    },
    saveAddress() {
      if (this.currentAddress.id !== null) {
        const index = this.addressList.findIndex((item) => item.id === this.currentAddress.id);
        this.addressList.splice(index, 1, { ...this.currentAddress });
      } else {
        this.addressList.push({
          id: this.nextAddressId++,
          ...this.currentAddress,
        });
      }
      this.currentAddress = { id: null, phone: "", address: "" };
    },
    openRechargeDialog() {
      this.rechargeForm.amount = 0;
      this.rechargeDialogVisible = true;
    },
    async handleRecharge() {
      try {
        this.form.userBalance = (parseFloat(this.form.userBalance) + parseFloat(this.rechargeForm.amount)).toFixed(2);
        await updatePersonalData(this.form);
        this.$message.success("Recharge successful!");
        this.rechargeDialogVisible = false;
      } catch (error) {
        this.$message.error("Recharge failed!");
      }
    },
    openChangePasswordDialog() {
      this.passwordForm.newPassword = "";
      this.passwordForm.confirmPassword = "";
      this.changePasswordDialogVisible = true;
    },
    async handleChangePassword() {
      if (this.passwordForm.newPassword !== this.passwordForm.confirmPassword) {
        this.$message.error("The two passwords do not match!");
        return;
      }
      try {
        this.form.userPassword = this.passwordForm.newPassword;
        await updatePersonalData(this.form);
        this.$message.success("Password changed successfully!");
        this.changePasswordDialogVisible = false;
      } catch (error) {
        this.$message.error("Password change failed!");
      }
    },
    beforeAvatarUpload(file) {
      const isImage = file.type.startsWith("image/");
      const isLt2M = file.size / 1024 / 1024 < 2;

      if (!isImage) {
        this.$message.error("Only image files can be uploaded!");
      }
      if (!isLt2M) {
        this.$message.error("Image size cannot exceed 2MB!");
      }

      return isImage && isLt2M;
    },
    async handleAvatarUpload({ file }) {
      const formData = new FormData();
      formData.append("file", file);
      try {
        const response = await fileUpload(formData);
        this.form.userAvatar = response.data.url;
        this.$message.success("Avatar uploaded successfully!");
      } catch (error) {
        this.$message.error("Avatar upload failed!");
        console.error("Upload failed:", error);
      }
    },
    async handleSubmit() {
      try {
        this.form.userAddress = JSON.stringify(this.addressList);
        await updatePersonalData(this.form);
        this.$message.success("User information updated successfully!");
        this.fetchUserData();
      } catch (error) {
        this.$message.error("Failed to update user information!");
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
  max-width: 980px;
  margin: 0 auto;
}

.page-banner,
.personal-form {
  background: #fff;
  border: 1px solid rgba(255, 77, 79, 0.08);
  box-shadow: 0 10px 28px rgba(15, 23, 42, 0.06);
}

.page-banner {
  padding: 18px 24px 8px;
  border-radius: 18px 18px 0 0;
  border-bottom: none;
  background: linear-gradient(135deg, #fff6f6 0%, #ffffff 55%, #f9fbff 100%);
}

.personal-form {
  padding: 22px 24px 10px;
  border-radius: 0 0 18px 18px;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  margin: 0;
  color: #1f2937;
}

.personal-form :deep(.el-form-item__label) {
  color: #4b5563;
  font-weight: 600;
}

.personal-form :deep(.el-input__inner) {
  border-radius: 12px;
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
}

.personal-form :deep(.el-input__inner:focus) {
  border-color: #ff4d4f;
  box-shadow: 0 0 0 2px rgba(255, 77, 79, 0.12);
}

.avatar-row {
  margin-bottom: 22px;
}

.avatar-uploader {
  display: inline-block;
}

.avatar-uploader :deep(.el-upload) {
  border: 1px dashed rgba(255, 77, 79, 0.35);
  border-radius: 50%;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  width: 120px;
  height: 120px;
  background: #fff;
  box-shadow: 0 8px 18px rgba(255, 77, 79, 0.08);
}

.avatar-uploader :deep(.el-upload:hover) {
  border-color: #ff4d4f;
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 120px;
  height: 120px;
  line-height: 120px;
  text-align: center;
}

.avatar {
  width: 100%;
  height: 100%;
  display: block;
  border-radius: 50%;
}

.user-balance {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
}

.balance-action {
  color: #ff4d4f;
  font-weight: 700;
}

.balance-action:hover,
.balance-action:focus {
  color: #e6393c;
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

.theme-button--save {
  min-width: 120px;
}

.submit-row {
  margin-top: 6px;
}

.submit-row :deep(.el-form-item__content) {
  text-align: right;
}

.dialog-footer {
  text-align: right;
}

.address-form {
  margin-top: 20px;
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

:deep(.personal-dialog .el-table__header-wrapper th) {
  background: #fafbfc;
  color: #374151;
}

:deep(.personal-dialog .el-table__body tr:hover > td) {
  background-color: rgba(255, 77, 79, 0.04);
}
</style>
