<template>
  <div class="app-container">
    <div class="page-shell">
      <div class="page-banner">
        <div class="banner-copy">
          <p class="page-kicker">Recommendation Settings</p>
          <p class="page-desc">Tune browse, favorite, and review weights to adjust how products are ranked in recommendations.</p>
        </div>
      </div>

      <div class="config-card">
        <el-form ref="form" :model="form" :rules="rules" label-width="200px" class="weight-form">
          <el-form-item label="Browse Weight" prop="browseWeight">
            <el-input-number v-model="form.browseWeight" :min="0" :step="1" :precision="2" controls-position="right" />
          </el-form-item>

          <el-form-item label="Favorite Weight" prop="favoriteWeight">
            <el-input-number v-model="form.favoriteWeight" :min="0" :step="1" :precision="2" controls-position="right" />
          </el-form-item>

          <el-form-item label="1 Star Review Weight" prop="reviewStar1Weight">
            <el-input-number v-model="form.reviewStar1Weight" :min="0" :step="1" :precision="2" controls-position="right" />
          </el-form-item>

          <el-form-item label="2 Star Review Weight" prop="reviewStar2Weight">
            <el-input-number v-model="form.reviewStar2Weight" :min="0" :step="1" :precision="2" controls-position="right" />
          </el-form-item>

          <el-form-item label="3 Star Review Weight" prop="reviewStar3Weight">
            <el-input-number v-model="form.reviewStar3Weight" :min="0" :step="1" :precision="2" controls-position="right" />
          </el-form-item>

          <el-form-item label="4 Star Review Weight" prop="reviewStar4Weight">
            <el-input-number v-model="form.reviewStar4Weight" :min="0" :step="1" :precision="2" controls-position="right" />
          </el-form-item>

          <el-form-item label="5 Star Review Weight" prop="reviewStar5Weight">
            <el-input-number v-model="form.reviewStar5Weight" :min="0" :step="1" :precision="2" controls-position="right" />
          </el-form-item>

          <div class="footer">
            <el-button type="primary" class="save-btn" @click="submitForm">Save</el-button>
          </div>
        </el-form>
      </div>

      <div class="tip-box">
        <p>Note: Browse, favorite, and review weights directly affect recommendation results; reviews are configured by star rating, with higher stars having greater weight.</p>
      </div>
    </div>
  </div>
</template>

<script>
import { getRecommendWeight, updateRecommendWeight } from "@/api/cms/recommendWeight";

export default {
  name: "RecommendWeightConfig",
  data() {
    return {
      form: {
        id: "1",
        browseWeight: 1,
        favoriteWeight: 3,
        reviewStar1Weight: 1,
        reviewStar2Weight: 2,
        reviewStar3Weight: 3,
        reviewStar4Weight: 4,
        reviewStar5Weight: 5,
      },
      rules: {
        browseWeight: [{ required: true, message: "Please enter browse weight", trigger: "blur" }],
        favoriteWeight: [{ required: true, message: "Please enter favorite weight", trigger: "blur" }],
        reviewStar1Weight: [{ required: true, message: "Please enter 1 star review weight", trigger: "blur" }],
        reviewStar2Weight: [{ required: true, message: "Please enter 2 star review weight", trigger: "blur" }],
        reviewStar3Weight: [{ required: true, message: "Please enter 3 star review weight", trigger: "blur" }],
        reviewStar4Weight: [{ required: true, message: "Please enter 4 star review weight", trigger: "blur" }],
        reviewStar5Weight: [{ required: true, message: "Please enter 5 star review weight", trigger: "blur" }],
      },
    };
  },
  created() {
    this.loadData();
  },
  methods: {
    loadData() {
      getRecommendWeight().then((response) => {
        if (response.code === 0) {
          this.form = response.data || this.form;
          this.form.id = "1";
        }
      });
    },
    submitForm() {
      this.$refs.form.validate((valid) => {
        if (!valid) {
          return;
        }
        updateRecommendWeight(this.form).then((response) => {
          if (response.code === 0) {
            this.$modal.msgSuccess("Saved successfully");
          } else {
            this.$message.error(response.msg || "Save failed");
          }
        });
      });
    },
  },
};
</script>

<style scoped>
.app-container {
  padding: 20px;
  background:
    radial-gradient(circle at top right, rgba(255, 122, 89, 0.08), transparent 28%),
    radial-gradient(circle at top left, rgba(255, 77, 79, 0.06), transparent 22%),
    linear-gradient(180deg, #fff8f8 0%, #f7f9fc 26%, #f5f7fb 100%);
}

.page-shell {
  max-width: 860px;
  margin: 0 auto;
}

.page-banner,
.config-card,
.tip-box {
  border: 1px solid rgba(255, 77, 79, 0.08);
  box-shadow: 0 10px 28px rgba(15, 23, 42, 0.06);
}

.page-banner {
  padding: 18px 22px 10px;
  border-radius: 18px 18px 0 0;
  border-bottom: none;
  background: linear-gradient(135deg, #fff6f6 0%, #ffffff 55%, #f9fbff 100%);
}

.banner-copy {
  max-width: 680px;
}

.page-kicker {
  margin: 0 0 6px;
  font-size: 12px;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  color: #ff4d4f;
  font-weight: 700;
}

.page-title {
  margin: 0;
  font-size: 24px;
  font-weight: 700;
  color: #1f2937;
}

.page-desc {
  margin: 8px 0 0;
  color: #6b7280;
  line-height: 1.6;
}

.config-card {
  padding: 24px 24px 10px;
  border-radius: 0 0 18px 18px;
  background: #fff;
}

.weight-form {
  max-width: 760px;
}

.weight-form :deep(.el-form-item__label) {
  white-space: nowrap;
  font-weight: 600;
  color: #374151;
}

.weight-form :deep(.el-input-number) {
  width: 100%;
  max-width: 260px;
}

.weight-form :deep(.el-input-number .el-input__inner) {
  border-radius: 12px;
}

.weight-form :deep(.el-input-number__decrease),
.weight-form :deep(.el-input-number__increase) {
  border-color: rgba(255, 77, 79, 0.12);
}

.tip-box {
  margin-top: 16px;
  padding: 14px 18px;
  background: #fff;
  border-radius: 16px;
  color: #4b5563;
  max-width: 860px;
}

.tip-box p {
  margin: 0;
  line-height: 1.7;
}

.footer {
  margin-top: 18px;
  display: flex;
  justify-content: flex-end;
}

.save-btn {
  min-width: 120px;
  border-radius: 12px;
  background: linear-gradient(135deg, #ff4d4f 0%, #ff7a59 100%);
  border-color: #ff4d4f;
  box-shadow: 0 8px 18px rgba(255, 77, 79, 0.18);
}

.save-btn:hover,
.save-btn:focus {
  background: linear-gradient(135deg, #ff6b6d 0%, #ff8a3d 100%);
  border-color: #ff6b6d;
}
</style>
