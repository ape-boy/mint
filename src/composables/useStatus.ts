import { h, type Component } from 'vue'
import {
  CheckCircleOutlined,
  CloseCircleOutlined,
  SyncOutlined,
  ClockCircleOutlined,
  StopOutlined,
  MinusCircleOutlined,
  LoadingOutlined,
  ExclamationCircleOutlined,
} from '@ant-design/icons-vue'
import type { BuildStatus, BuildStageStatus, ProjectStatus, TRStatus } from '@/types'

export interface StatusConfig {
  icon: Component
  color: string
  bgColor: string
  label: string
}

/**
 * Composable for handling all status-related logic
 * Centralizes status icons, colors, and labels
 */
export function useStatus() {
  // ========================================
  // Build Status
  // ========================================

  const buildStatusConfig: Record<BuildStatus, StatusConfig> = {
    success: {
      icon: CheckCircleOutlined,
      color: 'var(--color-accent-success)',
      bgColor: 'var(--color-bg-success)',
      label: 'Success',
    },
    failed: {
      icon: CloseCircleOutlined,
      color: 'var(--color-accent-danger)',
      bgColor: 'var(--color-bg-danger)',
      label: 'Failed',
    },
    running: {
      icon: LoadingOutlined,
      color: 'var(--color-accent-primary)',
      bgColor: 'var(--color-bg-info)',
      label: 'Running',
    },
    pending: {
      icon: ClockCircleOutlined,
      color: 'var(--color-text-muted)',
      bgColor: 'rgba(148, 163, 184, 0.15)',
      label: 'Pending',
    },
    cancelled: {
      icon: StopOutlined,
      color: 'var(--color-accent-warning)',
      bgColor: 'var(--color-bg-warning)',
      label: 'Cancelled',
    },
  }

  function getBuildStatusConfig(status: BuildStatus): StatusConfig {
    return buildStatusConfig[status] || buildStatusConfig.pending
  }

  function getBuildStatusIcon(status: BuildStatus): Component {
    return getBuildStatusConfig(status).icon
  }

  function getBuildStatusColor(status: BuildStatus): string {
    return getBuildStatusConfig(status).color
  }

  // ========================================
  // Build Stage Status
  // ========================================

  const stageStatusConfig: Record<BuildStageStatus, StatusConfig> = {
    success: {
      icon: CheckCircleOutlined,
      color: '#00c896',
      bgColor: 'var(--color-bg-success)',
      label: 'Success',
    },
    failed: {
      icon: CloseCircleOutlined,
      color: '#f87171',
      bgColor: 'var(--color-bg-danger)',
      label: 'Failed',
    },
    running: {
      icon: SyncOutlined,
      color: '#2dd4bf',
      bgColor: 'var(--color-bg-info)',
      label: 'Running',
    },
    pending: {
      icon: ClockCircleOutlined,
      color: '#94a3b8',
      bgColor: 'rgba(148, 163, 184, 0.15)',
      label: 'Pending',
    },
    skipped: {
      icon: MinusCircleOutlined,
      color: '#64748b',
      bgColor: 'rgba(100, 116, 139, 0.15)',
      label: 'Skipped',
    },
  }

  function getStageStatusConfig(status: BuildStageStatus): StatusConfig {
    return stageStatusConfig[status] || stageStatusConfig.pending
  }

  function getStageStatusColor(status: BuildStageStatus): string {
    return getStageStatusConfig(status).color
  }

  function getStageStatusIcon(status: BuildStageStatus): Component {
    return getStageStatusConfig(status).icon
  }

  // ========================================
  // Project Status
  // ========================================

  const projectStatusConfig: Record<ProjectStatus, StatusConfig> = {
    active: {
      icon: CheckCircleOutlined,
      color: 'var(--color-accent-success)',
      bgColor: 'var(--color-bg-success)',
      label: 'Active',
    },
    inactive: {
      icon: MinusCircleOutlined,
      color: 'var(--color-accent-warning)',
      bgColor: 'var(--color-bg-warning)',
      label: 'Inactive',
    },
    archived: {
      icon: ClockCircleOutlined,
      color: 'var(--color-text-muted)',
      bgColor: 'rgba(148, 163, 184, 0.15)',
      label: 'Archived',
    },
  }

  function getProjectStatusConfig(status: ProjectStatus): StatusConfig {
    return projectStatusConfig[status] || projectStatusConfig.inactive
  }

  function getProjectStatusIcon(status: ProjectStatus): Component {
    return getProjectStatusConfig(status).icon
  }

  function getProjectStatusColor(status: ProjectStatus): string {
    return getProjectStatusConfig(status).color
  }

  // ========================================
  // TR Status
  // ========================================

  const trStatusConfig: Record<TRStatus, StatusConfig> = {
    available: {
      icon: CheckCircleOutlined,
      color: 'var(--color-accent-success)',
      bgColor: 'var(--color-bg-success)',
      label: 'Available',
    },
    pending_approval: {
      icon: ClockCircleOutlined,
      color: 'var(--color-accent-warning)',
      bgColor: 'var(--color-bg-warning)',
      label: 'Pending Approval',
    },
    approved: {
      icon: CheckCircleOutlined,
      color: 'var(--color-accent-secondary)',
      bgColor: 'var(--color-bg-info)',
      label: 'Approved',
    },
    rejected: {
      icon: CloseCircleOutlined,
      color: 'var(--color-accent-danger)',
      bgColor: 'var(--color-bg-danger)',
      label: 'Rejected',
    },
    released: {
      icon: CheckCircleOutlined,
      color: 'var(--color-accent-primary)',
      bgColor: 'var(--color-bg-info)',
      label: 'Released',
    },
  }

  function getTRStatusConfig(status: TRStatus): StatusConfig {
    return trStatusConfig[status] ?? trStatusConfig.pending_approval
  }

  function getTRStatusColor(status: TRStatus): string {
    return getTRStatusConfig(status).color
  }

  // ========================================
  // Milestone Status
  // ========================================

  function getMilestoneStatusConfig(status: 'pending' | 'in_progress' | 'completed'): StatusConfig {
    switch (status) {
      case 'completed':
        return {
          icon: CheckCircleOutlined,
          color: 'var(--color-accent-success)',
          bgColor: 'var(--color-bg-success)',
          label: 'Completed',
        }
      case 'in_progress':
        return {
          icon: LoadingOutlined,
          color: 'var(--color-accent-primary)',
          bgColor: 'var(--color-bg-info)',
          label: 'In Progress',
        }
      case 'pending':
      default:
        return {
          icon: ClockCircleOutlined,
          color: 'var(--color-text-muted)',
          bgColor: 'rgba(148, 163, 184, 0.15)',
          label: 'Pending',
        }
    }
  }

  return {
    // Build Status
    getBuildStatusConfig,
    getBuildStatusIcon,
    getBuildStatusColor,

    // Stage Status
    getStageStatusConfig,
    getStageStatusColor,
    getStageStatusIcon,

    // Project Status
    getProjectStatusConfig,
    getProjectStatusIcon,
    getProjectStatusColor,

    // TR Status
    getTRStatusConfig,
    getTRStatusColor,

    // Milestone Status
    getMilestoneStatusConfig,
  }
}
