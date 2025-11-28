<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import type { Notice, NoticeCategory, TeamMember } from '../../types'

const props = defineProps<{
  notice?: Notice | null
  visible: boolean
  loading?: boolean
}>()

const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void
  (e: 'submit', data: Partial<Notice>): void
  (e: 'cancel'): void
}>()

const isEdit = computed(() => !!props.notice?.id)
const modalTitle = computed(() => isEdit.value ? 'Edit Notice' : 'New Notice')

const formState = ref({
  title: '',
  content: '',
  category: 'general' as NoticeCategory,
  priority: 'normal' as 'normal' | 'important' | 'urgent',
  isPinned: false,
  isPopup: false,
  popupStartDate: null as string | null,
  popupEndDate: null as string | null,
})

const categoryOptions = [
  { value: 'general', label: 'General' },
  { value: 'system', label: 'System' },
  { value: 'maintenance', label: 'Maintenance' },
  { value: 'event', label: 'Event' },
]

const priorityOptions = [
  { value: 'normal', label: 'Normal' },
  { value: 'important', label: 'Important' },
  { value: 'urgent', label: 'Urgent' },
]

watch(() => props.notice, (notice) => {
  if (notice) {
    formState.value = {
      title: notice.title,
      content: notice.content,
      category: notice.category,
      priority: notice.priority,
      isPinned: notice.isPinned,
      isPopup: notice.isPopup,
      popupStartDate: notice.popupStartDate || null,
      popupEndDate: notice.popupEndDate || null,
    }
  } else {
    resetForm()
  }
}, { immediate: true })

watch(() => props.visible, (visible) => {
  if (!visible) {
    resetForm()
  }
})

function resetForm() {
  formState.value = {
    title: '',
    content: '',
    category: 'general',
    priority: 'normal',
    isPinned: false,
    isPopup: false,
    popupStartDate: null,
    popupEndDate: null,
  }
}

function handleSubmit() {
  // Mock author for now
  const mockAuthor: TeamMember = {
    id: '101',
    name: 'Current User',
    email: 'user@company.com',
    role: 'TL',
  }

  const data: Partial<Notice> = {
    title: formState.value.title,
    content: formState.value.content,
    category: formState.value.category,
    priority: formState.value.priority,
    isPinned: formState.value.isPinned,
    isPopup: formState.value.isPopup,
    popupStartDate: formState.value.popupStartDate || '',
    popupEndDate: formState.value.popupEndDate || '',
    author: mockAuthor,
    attachments: [],
  }

  emit('submit', data)
}

function handleCancel() {
  emit('update:visible', false)
  emit('cancel')
}
</script>

<template>
  <a-modal
    :open="visible"
    :title="modalTitle"
    :confirmLoading="loading"
    width="700px"
    @ok="handleSubmit"
    @cancel="handleCancel"
  >
    <a-form
      :model="formState"
      layout="vertical"
      class="notice-form"
    >
      <a-form-item label="Title" required>
        <a-input
          v-model:value="formState.title"
          placeholder="Enter notice title"
          :maxlength="200"
          show-count
        />
      </a-form-item>

      <a-row :gutter="16">
        <a-col :span="12">
          <a-form-item label="Category" required>
            <a-select
              v-model:value="formState.category"
              :options="categoryOptions"
              placeholder="Select category"
            />
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item label="Priority" required>
            <a-select
              v-model:value="formState.priority"
              :options="priorityOptions"
              placeholder="Select priority"
            />
          </a-form-item>
        </a-col>
      </a-row>

      <a-form-item label="Content" required>
        <a-textarea
          v-model:value="formState.content"
          placeholder="Enter notice content"
          :rows="8"
          :maxlength="5000"
          show-count
        />
      </a-form-item>

      <a-row :gutter="16">
        <a-col :span="12">
          <a-form-item>
            <a-checkbox v-model:checked="formState.isPinned">
              Pin to top
            </a-checkbox>
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item>
            <a-checkbox v-model:checked="formState.isPopup">
              Show as popup
            </a-checkbox>
          </a-form-item>
        </a-col>
      </a-row>

      <template v-if="formState.isPopup">
        <a-divider>Popup Settings</a-divider>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="Popup Start Date">
              <a-date-picker
                v-model:value="formState.popupStartDate"
                style="width: 100%"
                show-time
                format="YYYY-MM-DD HH:mm"
              />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="Popup End Date">
              <a-date-picker
                v-model:value="formState.popupEndDate"
                style="width: 100%"
                show-time
                format="YYYY-MM-DD HH:mm"
              />
            </a-form-item>
          </a-col>
        </a-row>
      </template>
    </a-form>
  </a-modal>
</template>

<style scoped>
.notice-form {
  padding: var(--spacing-md) 0;
}
</style>
