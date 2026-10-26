<template>
  <div class="app-container dashboard-page" v-loading="loading">
    <div class="hero-panel">
      <div>
        <div class="hero-title">Mall Business Data Dashboard</div>
      </div>
    </div>

    <div class="metric-grid">
      <div v-for="item in summary.metrics" :key="item.title" class="metric-card">
        <div class="metric-title">{{ item.title }}</div>
        <div class="metric-value-row">
          <span class="metric-value">{{ item.value }}</span>
          <span class="metric-unit">{{ item.unit }}</span>
        </div>
        <div class="metric-note">{{ item.note }}</div>
      </div>
    </div>

    <div class="chart-grid">
      <div class="chart-card chart-card--wide">
        <div class="chart-header">
          <span>Product Category Distribution</span>
          <span class="chart-tip">By product quantity</span>
        </div>
        <div ref="productCategoryChart" class="chart-box chart-box--large"></div>
      </div>

      <div class="chart-card">
        <div class="chart-header">
          <span>Product Status Structure</span>
          <span class="chart-tip">On sale vs sold out</span>
        </div>
        <div ref="productStatusChart" class="chart-box"></div>
      </div>

      <div class="chart-card">
        <div class="chart-header">
          <span>Order Status Structure</span>
          <span class="chart-tip">Order fulfillment</span>
        </div>
        <div ref="orderStatusChart" class="chart-box"></div>
      </div>

      <div class="chart-card chart-card--wide">
        <div class="chart-header">
          <span>User Behavior Composition</span>
          <span class="chart-tip">Browse, favorite, comment, order</span>
        </div>
        <div ref="userBehaviorChart" class="chart-box chart-box--large"></div>
      </div>


    </div>
  </div>
</template>

<script>
import echarts from 'echarts'
require('echarts/theme/macarons')
import { getDashboardSummary } from '@/api/cms/dashboard'

export default {
  name: "Index",
  data() {
    return {
      loading: false,
      summary: {
        metrics: [],
        productCategoryDistribution: [],
        productStatusDistribution: [],
        orderStatusDistribution: [],
        userBehaviorDistribution: [],
        paymentMethodDistribution: []
      },
      charts: {
        productCategory: null,
        productStatus: null,
        orderStatus: null,
        userBehavior: null,
        paymentMethod: null,
      },
    };
  },
  mounted() {
    this.fetchSummary();
    window.addEventListener('resize', this.handleResize);
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.handleResize);
    this.disposeCharts();
  },
  methods: {
    async fetchSummary() {
      this.loading = true;
      try {
        const response = await getDashboardSummary();
        if (response && (response.code === 0 || response.code === 200 || typeof response.code === 'undefined')) {
          this.summary = response.data || this.summary;
          this.$nextTick(() => {
            this.renderCharts();
          });
        }
      } catch (error) {
        console.error('Failed to load dashboard summary:', error);
      } finally {
        this.loading = false;
      }
    },
    renderCharts() {
      this.renderProductCategoryChart();
      this.renderProductStatusChart();
      this.renderOrderStatusChart();
      this.renderUserBehaviorChart();
      this.renderPaymentMethodChart();
    },
    renderProductCategoryChart() {
      const chart = this.initChart('productCategoryChart', 'productCategory');
      if (!chart) {
        return;
      }
      const optionData = this.summary.productCategoryDistribution.map(item => ({ name: item.name, value: Number(item.value || 0) }));
      chart.setOption({
        color: ['#ff7a59', '#ff4d4f', '#2f7ed8', '#52c41a', '#f59e0b', '#7c3aed'],
        tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
        grid: { left: 90, right: 20, top: 30, bottom: 20 },
        xAxis: { type: 'value' },
        yAxis: {
          type: 'category',
          data: optionData.map(item => item.name),
        },
        series: [
          {
            type: 'bar',
            data: optionData.map(item => item.value),
            barWidth: 18,
            label: { show: true, position: 'right' },
          },
        ],
      }, true);
    },
    renderProductStatusChart() {
      const chart = this.initChart('productStatusChart', 'productStatus');
      if (!chart) {
        return;
      }
      const optionData = this.summary.productStatusDistribution.map(item => ({ name: item.name, value: Number(item.value || 0) }));
      chart.setOption({
        color: ['#10b981', '#ef4444'],
        tooltip: { trigger: 'item' },
        legend: { bottom: 0, left: 'center' },
        series: [
          {
            type: 'pie',
            radius: ['45%', '72%'],
            center: ['50%', '46%'],
            avoidLabelOverlap: false,
            label: { formatter: '{b}\n{d}%' },
            data: optionData,
          },
        ],
      }, true);
    },
    renderOrderStatusChart() {
      const chart = this.initChart('orderStatusChart', 'orderStatus');
      if (!chart) {
        return;
      }
      const optionData = this.summary.orderStatusDistribution.map(item => ({ name: item.name, value: Number(item.value || 0) }));
      chart.setOption({
        color: ['#ff4d4f', '#5470c6', '#91cc75', '#fac858'],
        tooltip: { trigger: 'item' },
        legend: { bottom: 0, left: 'center' },
        series: [
          {
            type: 'pie',
            radius: ['45%', '72%'],
            center: ['50%', '46%'],
            avoidLabelOverlap: false,
            label: { formatter: '{b}\n{d}%' },
            data: optionData,
          },
        ],
      }, true);
    },
    renderUserBehaviorChart() {
      const chart = this.initChart('userBehaviorChart', 'userBehavior');
      if (!chart) {
        return;
      }
      const optionData = this.summary.userBehaviorDistribution.map(item => ({ name: item.name, value: Number(item.value || 0) }));
      chart.setOption({
        color: ['#ff4d4f', '#f59e0b', '#5470c6', '#52c41a'],
        tooltip: { trigger: 'item' },
        legend: { bottom: 0, left: 'center' },
        series: [
          {
            type: 'pie',
            radius: ['40%', '70%'],
            center: ['50%', '45%'],
            label: { formatter: '{b}: {c}' },
            data: optionData,
          },
        ],
      }, true);
    },
    renderPaymentMethodChart() {
      const chart = this.initChart('paymentMethodChart', 'paymentMethod');
      if (!chart) {
        return;
      }
      const optionData = this.summary.paymentMethodDistribution.map(item => ({ name: item.name, value: Number(item.value || 0) }));
      chart.setOption({
        color: ['#f59e0b', '#2f7ed8', '#10b981', '#ef4444', '#7c3aed'],
        tooltip: { trigger: 'item' },
        legend: { bottom: 0, left: 'center' },
        series: [
          {
            type: 'pie',
            radius: ['40%', '70%'],
            center: ['50%', '45%'],
            label: { formatter: '{b}: {c}' },
            data: optionData,
          },
        ],
      }, true);
    },
    initChart(refName, chartKey) {
      const dom = this.$refs[refName];
      if (!dom) {
        return null;
      }
      if (this.charts[chartKey]) {
        this.charts[chartKey].dispose();
      }
      this.charts[chartKey] = echarts.init(dom, 'macarons');
      return this.charts[chartKey];
    },
    handleResize() {
      Object.values(this.charts).forEach(chart => {
        if (chart) {
          chart.resize();
        }
      });
    },
    disposeCharts() {
      Object.keys(this.charts).forEach(key => {
        if (this.charts[key]) {
          this.charts[key].dispose();
          this.charts[key] = null;
        }
      });
    },
    goTarget(href) {
      window.open(href, "_blank");
    },
  },
};
</script>

<style scoped lang="scss">
.dashboard-page {
  font-family: "open sans", "Helvetica Neue", Helvetica, Arial, sans-serif;
  color: #4b5563;
}

.hero-panel {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  gap: 20px;
  padding: 22px 24px;
  border-radius: 18px;
  background: linear-gradient(135deg, #fff6f6 0%, #ffffff 45%, #f9fbff 100%);
  box-shadow: 0 10px 30px rgba(15, 23, 42, 0.06);
  border: 1px solid rgba(255, 77, 79, 0.08);
}

.hero-title {
  font-size: 28px;
  font-weight: 800;
  color: #1f2937;
}

.hero-subtitle {
  margin-top: 8px;
  color: #6b7280;
}

.hero-meta {
  color: #ff4d4f;
  font-weight: 700;
  white-space: nowrap;
}

.metric-grid {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 14px;
  margin-top: 16px;
}

.metric-card,
.chart-card {
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 8px 24px rgba(15, 23, 42, 0.06);
  border: 1px solid #eef2f7;
}

.metric-card {
  padding: 16px;
}

.metric-title {
  color: #6b7280;
  font-size: 13px;
}

.metric-value-row {
  display: flex;
  align-items: baseline;
  gap: 6px;
  margin-top: 12px;
}

.metric-value {
  font-size: 28px;
  font-weight: 800;
  color: #111827;
}

.metric-unit {
  color: #ff4d4f;
  font-weight: 700;
}

.metric-note {
  margin-top: 10px;
  line-height: 1.5;
  color: #9ca3af;
  font-size: 12px;
}

.chart-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
  margin-top: 16px;
}

.chart-card {
  padding: 16px 16px 12px;
}

.chart-card--wide {
  grid-column: span 2;
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  font-weight: 700;
  color: #111827;
}

.chart-tip {
  font-size: 12px;
  color: #9ca3af;
  font-weight: 400;
}

.chart-box {
  width: 100%;
  height: 320px;
}

.chart-box--large {
  height: 360px;
}
</style>

