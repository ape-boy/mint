/**
 * Composable for handling all formatting logic
 * Centralizes date, time, and duration formatting
 */
export function useFormat() {
  /**
   * Format duration in seconds to human readable string
   * @param seconds - Duration in seconds (can be null/undefined)
   * @returns Formatted string like "5m 30s" or "-"
   */
  function formatDuration(seconds: number | null | undefined): string {
    if (seconds === null || seconds === undefined) return '-'

    const hours = Math.floor(seconds / 3600)
    const mins = Math.floor((seconds % 3600) / 60)
    const secs = seconds % 60

    if (hours > 0) {
      return `${hours}h ${mins}m ${secs}s`
    }
    if (mins > 0) {
      return `${mins}m ${secs}s`
    }
    return `${secs}s`
  }

  /**
   * Format date string to localized string
   * @param dateStr - ISO date string
   * @returns Localized date string
   */
  function formatDate(dateStr: string | null | undefined): string {
    if (!dateStr) return '-'
    try {
      return new Date(dateStr).toLocaleDateString()
    } catch {
      return '-'
    }
  }

  /**
   * Format date string to localized date and time
   * @param dateStr - ISO date string
   * @returns Localized datetime string
   */
  function formatDateTime(dateStr: string | null | undefined): string {
    if (!dateStr) return '-'
    try {
      return new Date(dateStr).toLocaleString()
    } catch {
      return '-'
    }
  }

  /**
   * Format date string to relative time (e.g., "2 hours ago")
   * @param dateStr - ISO date string
   * @returns Relative time string
   */
  function formatTimeAgo(dateStr: string | null | undefined): string {
    if (!dateStr) return '-'

    try {
      const date = new Date(dateStr)
      const now = new Date()
      const diffMs = now.getTime() - date.getTime()

      const diffSeconds = Math.floor(diffMs / 1000)
      const diffMinutes = Math.floor(diffSeconds / 60)
      const diffHours = Math.floor(diffMinutes / 60)
      const diffDays = Math.floor(diffHours / 24)
      const diffWeeks = Math.floor(diffDays / 7)
      const diffMonths = Math.floor(diffDays / 30)

      if (diffMonths > 0) return `${diffMonths}mo ago`
      if (diffWeeks > 0) return `${diffWeeks}w ago`
      if (diffDays > 0) return `${diffDays}d ago`
      if (diffHours > 0) return `${diffHours}h ago`
      if (diffMinutes > 0) return `${diffMinutes}m ago`
      return 'Just now'
    } catch {
      return '-'
    }
  }

  /**
   * Format a number with thousand separators
   * @param num - Number to format
   * @returns Formatted string
   */
  function formatNumber(num: number | null | undefined): string {
    if (num === null || num === undefined) return '-'
    return num.toLocaleString()
  }

  /**
   * Format a percentage value
   * @param value - Percentage value (0-100)
   * @returns Formatted string like "85%"
   */
  function formatPercent(value: number | null | undefined): string {
    if (value === null || value === undefined) return '-'
    return `${value}%`
  }

  /**
   * Truncate a string with ellipsis
   * @param str - String to truncate
   * @param maxLength - Maximum length before truncation
   * @returns Truncated string
   */
  function truncate(str: string | null | undefined, maxLength: number = 50): string {
    if (!str) return ''
    if (str.length <= maxLength) return str
    return `${str.slice(0, maxLength)}...`
  }

  /**
   * Format commit hash to short version
   * @param hash - Full commit hash
   * @param length - Length of short hash (default: 7)
   * @returns Short hash
   */
  function formatCommitHash(hash: string | null | undefined, length: number = 7): string {
    if (!hash) return '-'
    return hash.slice(0, length)
  }

  return {
    formatDuration,
    formatDate,
    formatDateTime,
    formatTimeAgo,
    formatNumber,
    formatPercent,
    truncate,
    formatCommitHash,
  }
}
