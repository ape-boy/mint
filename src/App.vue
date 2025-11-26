<script setup lang="ts">
import { RouterView } from 'vue-router'
import { theme } from 'ant-design-vue'
</script>

<template>
  <a-config-provider
    :theme="{
      algorithm: theme.darkAlgorithm,
      token: {
        colorPrimary: '#3b82f6',
        fontFamily: 'Inter, sans-serif',
        borderRadius: 6,
        colorBgContainer: '#1e293b',
        colorBgElevated: '#334155',
      }
    }"
  >
    <RouterView />
  </a-config-provider>
</template>
