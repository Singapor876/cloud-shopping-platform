<template>
  <div class="login-page">
    <div class="login-shell">
      <el-card class="login-card" shadow="never">
        <div class="card-head">
          <div class="card-brand">
            <div class="hero-logo">
              <svg viewBox="0 0 64 64" class="hero-logo-svg" role="img" aria-label="Shop UF logo">
                <defs>
                  <linearGradient id="loginLogoGradient" x1="0%" y1="0%" x2="100%" y2="100%">
                    <stop offset="0%" stop-color="#ff4d4f" />
                    <stop offset="100%" stop-color="#ff8a3d" />
                  </linearGradient>
                </defs>
                <rect x="6" y="6" width="52" height="52" rx="16" fill="url(#loginLogoGradient)" />
                <path
                  d="M18 26.5h28l-2.4 15.5c-.3 2-1.8 3.5-3.8 3.5H24.2c-2 0-3.6-1.5-3.9-3.5L18 26.5Z"
                  fill="#fff"
                  opacity="0.96"
                />
                <path
                  d="M22 26.5v-2.2c0-5.4 4.4-9.8 10-9.8s10 4.4 10 9.8v2.2"
                  fill="none"
                  stroke="#fff"
                  stroke-width="3.4"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                />
                <path
                  d="M26.5 26.5v-2.1c0-2.9 2.4-5.3 5.5-5.3s5.5 2.4 5.5 5.3v2.1"
                  fill="none"
                  stroke="#ff4d4f"
                  stroke-width="2.6"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                />
                <circle cx="25" cy="46" r="3.2" fill="#ff4d4f" />
                <circle cx="39" cy="46" r="3.2" fill="#ff4d4f" />
              </svg>
            </div>
            <div class="card-brand-copy">
          <span class="card-badge">{{ isLogin ? 'Login' : 'Register' }}</span>
              <h1>Spring Cloud Mall</h1>
              <p class="hero-text">Unified shopping entry, one step in.</p>
            </div>
          </div>
          <h2 class="title">{{ isLogin ? 'User Login' : 'User Register' }}</h2>
          <p class="subtitle">{{ isLogin ? 'Please enter username and password to access the system' : 'After creating an account, you can start using it' }}</p>
        </div>

        <el-form :model="formData" :rules="rules" ref="formRef" class="form">
          <el-form-item prop="username">
            <el-input v-model="formData.username" placeholder="Please enter username" prefix-icon="el-icon-user" />
          </el-form-item>
          <el-form-item prop="password">
            <el-input v-model="formData.password" type="password" placeholder="Please enter password" show-password prefix-icon="el-icon-lock" />
          </el-form-item>
          <el-form-item v-if="!isLogin" prop="confirmPassword">
            <el-input v-model="formData.confirmPassword" type="password" placeholder="Confirm password" show-password prefix-icon="el-icon-lock" />
          </el-form-item>
          <el-button type="primary" class="submit-btn" @click="handleSubmit" :loading="loading">
            {{ isLogin ? 'Login' : 'Register' }}
          </el-button>
        </el-form>

        <p class="toggle-text" @click="toggleForm">
          {{ isLogin ? 'No account? Click to register' : 'Already have an account? Click to login' }}
        </p>
      </el-card>
    </div>
  </div>
</template>

<script>
import { login, register } from '@/api/ums/user';
import {setToken, setUser} from '@/utils/auth';

export default {
  data() {
    return {
      isLogin: true,
      loading: false,
      formData: {
        username: '',
        password: '',
        confirmPassword: '',
      },
      rules: {
        username: [{ required: true, message: 'Please enter username', trigger: 'blur' }],
        password: [{ required: true, message: 'Please enter password', trigger: 'blur' }],
        confirmPassword: [{ required: true, message: 'Please confirm password', trigger: 'blur' }],
      },
    };
  },
    created() {
      if (this.$route.query.mode === 'register') {
        this.isLogin = false;
      }
    },
  methods: {
    toggleForm() {
      this.isLogin = !this.isLogin;
    },
    async handleSubmit() {
      this.loading = true;
      try {
        let res;
        if (this.isLogin) {
          res = await login(this.formData);
          setToken(res.data.token)
          setUser(res.data.user)
          this.$message.success('Login successful!');
          this.$router.push('/').then(() => {
            window.location.reload();
          });
        } else {
          if (this.formData.password !== this.formData.confirmPassword) {
            this.$message.error('The two passwords do not match');
            return;
          }
          res = await register(this.formData);
          this.$message.success('Registration successful, please login!');
          this.isLogin = true;
        }
      } catch (error) {
        this.$message.error(error.message || 'Operation failed');
      } finally {
        this.loading = false;
      }
    },
  },
};
</script>

<style scoped>
.login-page {
  position: fixed;
  inset: 0;
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  box-sizing: border-box;
  overflow: hidden;
  background:
    radial-gradient(circle at top right, rgba(255, 122, 89, 0.18), transparent 30%),
    radial-gradient(circle at bottom left, rgba(255, 77, 79, 0.12), transparent 26%),
    linear-gradient(135deg, #fff8f8 0%, #f7f9fc 48%, #f5f7fb 100%);
}

.login-shell {
  width: min(640px, 100%);
  transform: translateY(-14px);
}

.card-brand {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 18px;
}

.hero-logo {
  width: 88px;
  height: 88px;
  display: grid;
  place-items: center;
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.75);
  border: 1px solid rgba(255, 77, 79, 0.12);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.85), 0 16px 28px rgba(15, 23, 42, 0.08);
}

.hero-logo-svg {
  width: 64px;
  height: 64px;
}

.card-brand-copy {
  min-width: 0;
}

.card-brand-copy h1 {
  margin: 10px 0 0;
  font-size: 32px;
  line-height: 1.08;
  font-weight: 900;
  color: #111827;
  white-space: nowrap;
}

.hero-text {
  margin: 8px 0 0;
  max-width: none;
  color: #475569;
  font-size: 15px;
  line-height: 1.6;
}

.login-card {
  width: 100%;
  padding: 28px 28px 22px;
  border-radius: 28px;
  background: #fff;
  border: 1px solid rgba(255, 77, 79, 0.08);
  box-shadow: 0 18px 36px rgba(15, 23, 42, 0.08);
}

.card-head {
  margin-bottom: 22px;
}

.card-badge {
  display: inline-flex;
  align-items: center;
  padding: 7px 12px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 800;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: #ff4d4f;
  background: #fff6f6;
  border: 1px solid rgba(255, 77, 79, 0.12);
}

.title {
  margin: 14px 0 8px;
  font-size: 30px;
  line-height: 1.15;
  font-weight: 800;
  color: #111827;
}

.subtitle {
  margin: 0;
  color: #6b7280;
  line-height: 1.7;
}

.form {
  margin-top: 18px;
}

.form :deep(.el-input__inner) {
  height: 48px;
  border-radius: 14px;
  border-color: #e5e7eb;
  background: #f8fafc;
}

.form :deep(.el-input__inner:focus) {
  border-color: #ff4d4f;
}

.form :deep(.el-input__prefix) {
  color: #ff4d4f;
}

.submit-btn {
  width: 100%;
  margin-top: 8px;
  height: 48px;
  border: none;
  border-radius: 14px;
  background: linear-gradient(135deg, #ff4d4f 0%, #ff7a59 100%);
  box-shadow: 0 10px 20px rgba(255, 77, 79, 0.2);
}

.submit-btn:hover,
.submit-btn:focus {
  background: linear-gradient(135deg, #ff6b6d 0%, #ff8a3d 100%);
}

.toggle-text {
  margin: 18px 0 0;
  text-align: center;
  color: #ff4d4f;
  cursor: pointer;
  font-weight: 700;
}

.toggle-text:hover {
  color: #e6393c;
}

@media (max-width: 900px) {
  .login-shell {
    width: min(100%, 560px);
  }

  .card-brand {
    gap: 14px;
  }

  .hero-logo {
    width: 76px;
    height: 76px;
  }

  .hero-logo-svg {
    width: 56px;
    height: 56px;
  }

  .card-brand-copy h1 {
    font-size: 28px;
  }
}

@media (max-width: 520px) {
  .login-page {
    padding: 16px;
  }

  .login-shell {
    transform: translateY(-8px);
  }

  .login-card {
    border-radius: 22px;
  }

  .login-card {
    padding: 22px 18px 18px;
  }

  .card-brand {
    align-items: flex-start;
  }

  .card-brand-copy h1 {
    white-space: normal;
    font-size: 24px;
  }
}
</style>
