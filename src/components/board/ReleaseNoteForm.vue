<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import { PlusOutlined, MinusCircleOutlined } from '@ant-design/icons-vue'
import type { ReleaseNote, ReleaseChange, TeamMember } from '../../types'
import { projectApi } from '../../api'

const props = defineProps<{
  releaseNote?: ReleaseNote | null
  visible: boolean
  loading?: boolean
}>()

const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void
  (e: 'submit', data: Partial<ReleaseNote>): void
  (e: 'cancel'): void
}>()

const isEdit = computed(() => !!props.releaseNote?.id)
const modalTitle = computed(() => isEdit.value ? 'Edit Release Note' : 'New Release Note')

interface ProjectOption {
  value: string
  label: string
}

const projectOptions = ref<ProjectOption[]>([])
const loadingProjects = ref(false)

const formState = ref({
  title: '',
  content: '',
  version: '',
  releaseType: 'minor' as 'major' | 'minor' | 'patch' | 'hotfix',
  projectId: null as string | null,
  isPopup: false,
  popupStartDate: null as string | null,
  popupEndDate: null as string | null,
  releasedAt: new Date().toISOString(),
})

const changes = ref<ReleaseChange[]>([])

const releaseTypeOptions = [
  { value: 'major', label: 'Major' },
  { value: 'minor', label: 'Minor' },
  { value: 'patch', label: 'Patch' },
  { value: 'hotfix', label: 'Hotfix' },
]

const changeTypeOptions = [
  { value: 'feature', label: 'Feature' },
  { value: 'bugfix', label: 'Bug Fix' },
  { value: 'improvement', label: 'Improvement' },
  { value: 'breaking', label: 'Breaking Change' },
]

onMounted(async () => {
  await loadProjects()
})

async function loadProjects() {
  loadingProjects.value = true
  try {
    const projects = await projectApi.getList()
    projectOptions.value = [
      { value: '', label: 'System Release (No Project)' },
      ...projects.map((p: { id: string; name: string }) => ({
        value: p.id,
        label: p.name,
      }))
    ]
  } catch (error) {
    console.error('Failed to load projects:', error)
  } finally {
    loadingProjects.value = false
  }
}

watch(() => props.releaseNote, (releaseNote) => {
  if (releaseNote) {
    formState.value = {
      title: releaseNote.title,
      content: releaseNote.content,
      version: releaseNote.version,
      releaseType: releaseNote.releaseType,
      projectId: releaseNote.projectId || null,
      isPopup: releaseNote.isPopup,
      popupStartDate: releaseNote.popupStartDate || null,
      popupEndDate: releaseNote.popupEndDate || null,
      releasedAt: releaseNote.releasedAt,
    }
    changes.value = [...(releaseNote.changes || [])]
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
    version: '',
    releaseType: 'minor',
    projectId: null,
    isPopup: false,
    popupStartDate: null,
    popupEndDate: null,
    releasedAt: new Date().toISOString(),
  }
  changes.value = []
}

function addChange() {
  changes.value.push({ type: 'feature', description: '' })
}

function removeChange(index: number) {
  changes.value.splice(index, 1)
}

function handleSubmit() {
  // Mock author for now
  const mockAuthor: TeamMember = {
    id: '101',
    name: 'Current User',
    email: 'user@company.com',
    role: 'TL',
  }

  const selectedProject = projectOptions.value.find(p => p.value === formState.value.projectId)

  const data: Partial<ReleaseNote> = {
    title: formState.value.title,
    content: formState.value.content,
    version: formState.value.version,
    releaseType: formState.value.releaseType,
    projectId: formState.value.projectId || undefined,
    projectName: selectedProject?.value ? selectedProject.label : undefined,
    changes: changes.value.filter(c => c.description.trim()),
    isPopup: formState.value.isPopup,
    popupStartDate: formState.value.popupStartDate || undefined,
    popupEndDate: formState.value.popupEndDate || undefined,
    releasedAt: formState.value.releasedAt,
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
    width="800px"
    @ok="handleSubmit"
    @cancel="handleCancel"
  >
    <a-form
      :model="formState"
      layout="vertical"
      class="release-note-form"
    >
      <a-form-item label="Title" required>
        <a-input
          v-model:value="formState.title"
          placeholder="Enter release note title"
          :maxlength="200"
          show-count
        />
      </a-form-item>

      <a-row :gutter="16">
        <a-col :span="8">
          <a-form-item label="Version" required>
            <a-input
              v-model:value="formState.version"
              placeholder="e.g., 1.2.3"
            />
          </a-form-item>
        </a-col>
        <a-col :span="8">
          <a-form-item label="Release Type" required>
            <a-select
              v-model:value="formState.releaseType"
              :options="releaseTypeOptions"
              placeholder="Select type"
            />
          </a-form-item>
        </a-col>
        <a-col :span="8">
          <a-form-item label="Project">
            <a-select
              v-model:value="formState.projectId"
              :options="projectOptions"
              :loading="loadingProjects"
              placeholder="Select project"
              allowClear
            />
          </a-form-item>
        </a-col>
      </a-row>

      <a-form-item label="Description">
        <a-textarea
          v-model:value="formState.content"
          placeholder="Enter release description..."
          :rows="4"
          :maxlength="2000"
          show-count
        />
      </a-form-item>

      <a-divider>Changes</a-divider>

      <div class="changes-list">
        <div v-for="(change, index) in changes" :key="index" class="change-item">
          <a-select
            v-model:value="change.type"
            :options="changeTypeOptions"
            style="width: 140px"
          />
          <a-input
            v-model:value="change.description"
            placeholder="Describe the change..."
            style="flex: 1"
          />
          <a-button type="text" danger @click="removeChange(index)">
            <MinusCircleOutlined />
          </a-button>
        </div>
        <a-button type="dashed" block @click="addChange">
          <PlusOutlined />
          Add Change
        </a-button>
      </div>

      <a-divider>Popup Settings</a-divider>

      <a-form-item>
        <a-checkbox v-model:checked="formState.isPopup">
          Show as popup
        </a-checkbox>
      </a-form-item>

      <template v-if="formState.isPopup">
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
.release-note-form {
  padding: var(--spacing-md) 0;
}

.changes-list {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-sm);
}

.change-item {
  display: flex;
  gap: var(--spacing-sm);
  align-items: center;
}
</style>
