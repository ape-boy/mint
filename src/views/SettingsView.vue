<script setup lang="ts">
import { ref } from 'vue'
import {
  UserOutlined,
  BellOutlined,
  SafetyOutlined,
  GlobalOutlined,
  SaveOutlined
} from '@ant-design/icons-vue'
import { PageHeader } from '@/components'

// Profile Settings
const profile = ref({
  name: 'John Doe',
  email: 'john.doe@company.com',
  department: 'SSD Firmware Development',
  role: 'Developer'
})

// Notification Settings
const notifications = ref({
  buildSuccess: true,
  buildFailure: true,
  trApproval: true,
  releaseAlert: false,
  emailNotifications: true
})

// Display Settings
const display = ref({
  theme: 'dark',
  language: 'en',
  timezone: 'Asia/Seoul',
  dateFormat: 'YYYY-MM-DD'
})

function saveSettings() {
  console.log('Saving settings:', {
    profile: profile.value,
    notifications: notifications.value,
    display: display.value
  })
}
</script>

<template>
  <div class="settings-view">
    <PageHeader title="Settings" description="Manage your account and preferences" />

    <div class="settings-container">
      <!-- Profile Section -->
      <div class="settings-section">
        <h3><UserOutlined /> Profile</h3>
        <div class="settings-form">
          <div class="form-row">
            <div class="form-group">
              <label>Name</label>
              <a-input v-model:value="profile.name" />
            </div>
            <div class="form-group">
              <label>Email</label>
              <a-input v-model:value="profile.email" disabled />
            </div>
          </div>
          <div class="form-row">
            <div class="form-group">
              <label>Department</label>
              <a-input v-model:value="profile.department" />
            </div>
            <div class="form-group">
              <label>Role</label>
              <a-input v-model:value="profile.role" disabled />
            </div>
          </div>
        </div>
      </div>

      <!-- Notification Section -->
      <div class="settings-section">
        <h3><BellOutlined /> Notifications</h3>
        <div class="settings-form">
          <div class="toggle-group">
            <div class="toggle-item">
              <div class="toggle-info">
                <span class="toggle-label">Build Success</span>
                <span class="toggle-description">Notify when builds complete successfully</span>
              </div>
              <a-switch v-model:checked="notifications.buildSuccess" />
            </div>
            <div class="toggle-item">
              <div class="toggle-info">
                <span class="toggle-label">Build Failure</span>
                <span class="toggle-description">Notify when builds fail</span>
              </div>
              <a-switch v-model:checked="notifications.buildFailure" />
            </div>
            <div class="toggle-item">
              <div class="toggle-info">
                <span class="toggle-label">TR Approval</span>
                <span class="toggle-description">Notify when TR requires approval</span>
              </div>
              <a-switch v-model:checked="notifications.trApproval" />
            </div>
            <div class="toggle-item">
              <div class="toggle-info">
                <span class="toggle-label">Release Alert</span>
                <span class="toggle-description">Notify on new releases</span>
              </div>
              <a-switch v-model:checked="notifications.releaseAlert" />
            </div>
            <div class="toggle-item">
              <div class="toggle-info">
                <span class="toggle-label">Email Notifications</span>
                <span class="toggle-description">Receive notifications via email</span>
              </div>
              <a-switch v-model:checked="notifications.emailNotifications" />
            </div>
          </div>
        </div>
      </div>

      <!-- Security Section -->
      <div class="settings-section">
        <h3><SafetyOutlined /> Security</h3>
        <div class="settings-form">
          <div class="action-buttons">
            <a-button>Change Password</a-button>
            <a-button>Manage API Keys</a-button>
            <a-button>Two-Factor Authentication</a-button>
          </div>
        </div>
      </div>

      <!-- Display Section -->
      <div class="settings-section">
        <h3><GlobalOutlined /> Display</h3>
        <div class="settings-form">
          <div class="form-row">
            <div class="form-group">
              <label>Theme</label>
              <a-select v-model:value="display.theme" style="width: 100%">
                <a-select-option value="dark">Dark</a-select-option>
                <a-select-option value="light">Light</a-select-option>
                <a-select-option value="system">System</a-select-option>
              </a-select>
            </div>
            <div class="form-group">
              <label>Language</label>
              <a-select v-model:value="display.language" style="width: 100%">
                <a-select-option value="en">English</a-select-option>
                <a-select-option value="ko">Korean</a-select-option>
              </a-select>
            </div>
          </div>
          <div class="form-row">
            <div class="form-group">
              <label>Timezone</label>
              <a-select v-model:value="display.timezone" style="width: 100%">
                <a-select-option value="Asia/Seoul">Asia/Seoul (KST)</a-select-option>
                <a-select-option value="UTC">UTC</a-select-option>
                <a-select-option value="America/Los_Angeles">America/Los Angeles (PST)</a-select-option>
              </a-select>
            </div>
            <div class="form-group">
              <label>Date Format</label>
              <a-select v-model:value="display.dateFormat" style="width: 100%">
                <a-select-option value="YYYY-MM-DD">YYYY-MM-DD</a-select-option>
                <a-select-option value="MM/DD/YYYY">MM/DD/YYYY</a-select-option>
                <a-select-option value="DD/MM/YYYY">DD/MM/YYYY</a-select-option>
              </a-select>
            </div>
          </div>
        </div>
      </div>

      <!-- Save Button -->
      <div class="settings-actions">
        <a-button type="primary" size="large" @click="saveSettings">
          <template #icon><SaveOutlined /></template>
          Save Changes
        </a-button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.settings-view {
  max-width: 800px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: var(--spacing-lg);
}

.settings-container {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-lg);
}

.settings-section {
  background: var(--color-bg-secondary);
  border: 1px solid var(--color-border-light);
  border-radius: var(--radius-lg);
  padding: var(--spacing-lg);
}

.settings-section h3 {
  margin: 0 0 var(--spacing-lg);
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-semibold);
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  color: var(--color-text-primary);
  border-bottom: 1px solid var(--color-border-light);
  padding-bottom: var(--spacing-sm);
}

.settings-form {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
}

.form-row {
  display: flex;
  gap: var(--spacing-lg);
}

.form-group {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: var(--spacing-xs);
}

.form-group label {
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-medium);
  color: var(--color-text-secondary);
}

.toggle-group {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-sm);
}

.toggle-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--spacing-sm);
  background: rgba(255, 255, 255, 0.02);
  border-radius: var(--radius-md);
  border: 1px solid var(--color-border-light);
}

.toggle-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.toggle-label {
  font-weight: var(--font-weight-medium);
  color: var(--color-text-primary);
}

.toggle-description {
  font-size: var(--font-size-xs);
  color: var(--color-text-muted);
}

.action-buttons {
  display: flex;
  gap: var(--spacing-sm);
  flex-wrap: wrap;
}

.settings-actions {
  display: flex;
  justify-content: flex-end;
}
</style>
