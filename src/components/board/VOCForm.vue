<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import type { VOC, VOCCategory, VOCPriority, TeamMember } from '../../types'
import { projectApi } from '../../api'

const props = defineProps<{
  voc?: VOC | null
  visible: boolean
  loading?: boolean
}>()

const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void
  (e: 'submit', data: Partial<VOC>): void
  (e: 'cancel'): void
}>()

const isEdit = computed(() => !!props.voc?.id)
const modalTitle = computed(() => isEdit.value ? 'Edit VOC' : 'New VOC')

interface ProjectOption {
  value: string
  label: string
  groupId: string
  groupName: string
}

const projectOptions = ref<ProjectOption[]>([])
const loadingProjects = ref(false)

const formState = ref({
  title: '',
  content: '',
  category: 'feature_request' as VOCCategory,
  priority: 'medium' as VOCPriority,
  projectId: null as string | null,
  dueDate: null as string | null,
})

const categoryOptions = [
  { value: 'bug', label: 'Bug' },
  { value: 'feature_request', label: 'Feature Request' },
  { value: 'improvement', label: 'Improvement' },
  { value: 'question', label: 'Question' },
  { value: 'documentation', label: 'Documentation' },
  { value: 'other', label: 'Other' },
]

const priorityOptions = [
  { value: 'low', label: 'Low' },
  { value: 'medium', label: 'Medium' },
  { value: 'high', label: 'High' },
  { value: 'critical', label: 'Critical' },
]

onMounted(async () => {
  await loadProjects()
})

async function loadProjects() {
  loadingProjects.value = true
  try {
    const projects = await projectApi.getList()
    projectOptions.value = projects.map((p: { id: string; name: string; groupId: string }) => ({
      value: p.id,
      label: p.name,
      groupId: p.groupId,
      groupName: '', // Would need to fetch from task groups
    }))
  } catch (error) {
    console.error('Failed to load projects:', error)
  } finally {
    loadingProjects.value = false
  }
}

watch(() => props.voc, (voc) => {
  if (voc) {
    formState.value = {
      title: voc.title,
      content: voc.content,
      category: voc.category,
      priority: voc.priority,
      projectId: voc.projectId || null,
      dueDate: voc.dueDate || null,
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
    category: 'feature_request',
    priority: 'medium',
    projectId: null,
    dueDate: null,
  }
}

function handleSubmit() {
  // Mock requester for now
  const mockRequester: TeamMember = {
    id: '102',
    name: 'Current User',
    email: 'user@company.com',
    role: 'Developer',
  }

  const selectedProject = projectOptions.value.find(p => p.value === formState.value.projectId)

  const data: Partial<VOC> = {
    title: formState.value.title,
    content: formState.value.content,
    category: formState.value.category,
    priority: formState.value.priority,
    status: 'open',
    projectId: formState.value.projectId || undefined,
    projectName: selectedProject?.label || undefined,
    projectGroupId: selectedProject?.groupId || undefined,
    projectGroupName: selectedProject?.groupName || undefined,
    requester: mockRequester,
    dueDate: formState.value.dueDate || undefined,
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
      class="voc-form"
    >
      <a-form-item label="Title" required>
        <a-input
          v-model:value="formState.title"
          placeholder="Enter VOC title"
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

      <a-row :gutter="16">
        <a-col :span="12">
          <a-form-item label="Related Project">
            <a-select
              v-model:value="formState.projectId"
              :options="projectOptions"
              :loading="loadingProjects"
              placeholder="Select project (optional)"
              allowClear
              showSearch
              :filterOption="(input: string, option: ProjectOption) =>
                option.label.toLowerCase().includes(input.toLowerCase())
              "
            />
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item label="Due Date">
            <a-date-picker
              v-model:value="formState.dueDate"
              style="width: 100%"
              placeholder="Select due date (optional)"
            />
          </a-form-item>
        </a-col>
      </a-row>

      <a-form-item label="Description" required>
        <a-textarea
          v-model:value="formState.content"
          placeholder="Describe your request in detail..."
          :rows="8"
          :maxlength="5000"
          show-count
        />
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<style scoped>
.voc-form {
  padding: var(--spacing-md) 0;
}
</style>
