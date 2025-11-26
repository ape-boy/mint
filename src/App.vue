<script setup lang="ts">
import { RouterView } from 'vue-router'
import { theme } from 'ant-design-vue'
</script>

<template>
  <a-config-provider
    :theme="{
      algorithm: theme.darkAlgorithm,
      token: {
        colorPrimary: '#00c896',
        fontFamily: 'Inter, sans-serif',
        borderRadius: 8,
        colorBgContainer: '#1e293b',
        colorBgElevated: '#334155',
        colorBgLayout: '#0f172a',
        colorText: '#f1f5f9',
        colorTextSecondary: '#cbd5e1',
      }
    }"
  >
    <RouterView />
  </a-config-provider>
</template>
