<script setup lang="ts">
import { RouterView } from 'vue-router'
import { theme } from 'ant-design-vue'
</script>

<template>
  <a-config-provider
    :theme="{
      algorithm: theme.darkAlgorithm,
      token: {
        colorPrimary: '#00d4aa',
        colorSuccess: '#4caf50',
        colorWarning: '#ffb74d',
        colorError: '#f44336',
        colorInfo: '#29b6f6',
        fontFamily: 'Inter, -apple-system, BlinkMacSystemFont, sans-serif',
        fontSize: 14,
        borderRadius: 4,
        colorBgContainer: '#232328',
        colorBgElevated: '#2e2e33',
        colorBgLayout: '#1a1a1f',
        colorBgSpotlight: '#36363e',
        colorText: '#ffffff',
        colorTextSecondary: '#b5b5b5',
        colorTextTertiary: '#8b8b8b',
        colorTextQuaternary: '#6b6b6b',
        colorBorder: '#3a3a40',
        colorBorderSecondary: 'rgba(255, 255, 255, 0.08)',
        colorFill: 'rgba(255, 255, 255, 0.04)',
        colorFillSecondary: 'rgba(255, 255, 255, 0.06)',
        colorFillTertiary: 'rgba(255, 255, 255, 0.08)',
      }
    }"
  >
    <RouterView />
  </a-config-provider>
</template>
