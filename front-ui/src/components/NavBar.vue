<template>
  <nav class="navbar">
    <div class="navbar-shell shell">
      <div class="navbar-left">
        <div class="navbar-logo" @click="goToHome">
          <svg viewBox="0 0 64 64" class="navbar-logo-svg" role="img" aria-label="E-commerce mall logo">
            <defs>
              <linearGradient id="navbarLogoGradient" x1="0%" y1="0%" x2="100%" y2="100%">
                <stop offset="0%" stop-color="#ff4d4f" />
                <stop offset="100%" stop-color="#ff8a3d" />
              </linearGradient>
            </defs>
            <rect x="6" y="6" width="52" height="52" rx="16" fill="url(#navbarLogoGradient)" />
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
        <div class="navbar-title">Spring Cloud Mall</div>
      </div>

      <div class="navbar-right">
        <template v-if="token">
          <span class="inline-link" @click="goToPage('personal-center')">Personal Center</span>
          <span class="inline-link" @click="goToPage('my-cart')">Shopping Cart</span>
          <span class="inline-link" @click="goToPage('favorites')">Favorites</span>
        </template>

        <div
          v-if="token"
          class="navbar-user"
          @mouseenter="showUserMenu = true"
          @mouseleave="showUserMenu = false"
        >
          <img :src="avatar" alt="Avatar" class="user-avatar" />
          <span class="user-icon">▼</span>
          <div v-if="showUserMenu" class="user-dropdown">
            <ul>
              <li @click="goToPage('my-orders')">My Orders</li>
              <li @click="goToPage('my-reviews')">My Reviews</li>
              <li @click="goToPage('browse-history')">Browse History</li>
              <li @click="handleLogout">Logout</li>
            </ul>
          </div>
        </div>

        <div v-else class="navbar-auth">
          <span class="auth-link" @click="goToLogin">Login</span>
          <span class="auth-sep">|</span>
          <span class="auth-link" @click="goToRegister">Register</span>
        </div>
      </div>
    </div>
  </nav>
</template>

<script>
import avatar from '@/assets/avatar.png';
import { getToken, getUser } from "@/utils/auth";

export default {
  data() {
    return {
      showUserMenu: false,
      token: getToken() || '',
      avatar: getUser().userAvatar || avatar,
    };
  },
  methods: {
    goToHome() {
      if (this.$route.path !== "/") {
        this.$router.push("/");
      }
    },
    goToProductList() {
      if (this.$route.path !== "/product") {
        this.$router.push("/product");
      }
    },
    goToLogin() {
      if (this.$route.path !== "/login") {
        this.$router.push("/login");
      }
    },
    goToRegister() {
      this.$router.push({ path: "/login", query: { mode: "register" } });
    },
    goToPage(pageName) {
      if (this.$route.name !== pageName) {
        this.$router.push({ name: pageName });
      }
    },
    handleLogout() {
      localStorage.removeItem("Front-Token");
      localStorage.removeItem("Avatar");
      this.token = "";
      this.avatar = "";
      this.$router.push("/login");
    },
    syncUserState() {
      this.token = getToken() || "";
      this.avatar = getUser().userAvatar || avatar;
    },
  },
  watch: {
    $route() {
      this.syncUserState();
    },
  },
  created() {
    this.syncUserState();
  },
};
</script>

<style scoped>
.navbar {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 50px;
  background-color: #fff;
  border-bottom: 2px solid #ff4d4f;
  z-index: 1000;
}

.shell {
  width: 1200px;
  margin: 0 auto;
}

.navbar-shell {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.navbar-left,
.navbar-center,
.navbar-right {
  display: flex;
  align-items: center;
}

.navbar-left {
  flex: 0 0 auto;
}

.navbar-title {
  margin-left: 10px;
  font-size: 16px;
  font-weight: 700;
  color: #ff4d4f;
  white-space: nowrap;
}

.navbar-center {
  flex: 1;
  justify-content: center;
  max-width: 620px;
  gap: 18px;
}

.navbar-right {
  flex: 0 0 auto;
}

.navbar-logo img {
  height: 30px;
  cursor: pointer;
}

.navbar-logo-svg {
  width: 30px;
  height: 30px;
  display: block;
  cursor: pointer;
  filter: drop-shadow(0 4px 10px rgba(255, 77, 79, 0.18));
}

.nav-button {
  padding: 4px 12px;
  cursor: pointer;
  font-size: 14px;
  color: #515a6e;
  transition: all 0.3s ease;
  border-radius: 14px;
}

.nav-button:hover {
  color: #ff4d4f;
  background: rgba(255, 77, 79, 0.08);
}

.navbar-user {
  position: relative;
  display: flex;
  align-items: center;
  cursor: pointer;
}

.inline-link {
  margin-right: 8px;
  cursor: pointer;
  color: #515a6e;
  font-size: 13px;
  white-space: nowrap;
  transition: color 0.3s ease;
}

.inline-link:hover {
  color: #ff4d4f;
}

.user-avatar {
  width: 26px;
  height: 26px;
  border-radius: 50%;
}

.user-icon {
  margin-left: 4px;
  font-size: 12px;
}

.user-dropdown {
  position: absolute;
  top: 100%;
  right: 0;
  background-color: #fff;
  border: 1px solid #ddd;
  border-radius: 5px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  z-index: 1001;
  min-width: 150px;
}

.user-dropdown ul {
  list-style: none;
  padding: 0;
  margin: 0;
}

.user-dropdown li {
  padding: 10px 20px;
  cursor: pointer;
  color: #333;
}

.user-dropdown li:hover {
  background-color: #f0f0f0;
}

.navbar-auth {
  display: flex;
  align-items: center;
  gap: 10px;
  color: #515a6e;
}

.auth-link {
  cursor: pointer;
}

.auth-link:hover {
  color: #ff7e00;
}

.auth-sep {
  color: #c0c4cc;
}
</style>
