import { createClient } from '@supabase/supabase-js'

// Supabase 연결 설정 (self-hosted Docker)
const supabaseUrl = import.meta.env.VITE_SUPABASE_URL || 'http://localhost:8000'
const supabaseAnonKey = import.meta.env.VITE_SUPABASE_ANON_KEY || 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyAgCiAgICAicm9sZSI6ICJhbm9uIiwKICAgICJpc3MiOiAic3VwYWJhc2UtZGVtbyIsCiAgICAiaWF0IjogMTY0MTc2OTIwMCwKICAgICJleHAiOiAxNzk5NTM1NjAwCn0.dc_X5iR_VP_qT0zsiyj_I_OZ2T9FtRU2BBNWN8Bu4GE'

export const supabase = createClient(supabaseUrl, supabaseAnonKey)

// 데이터베이스 테이블 이름 매핑 (snake_case)
export const Tables = {
  users: 'users',
  taskGroups: 'task_groups',
  projects: 'projects',
  layers: 'layers',
  builds: 'builds',
  projectHistory: 'project_history',
  notices: 'notices',
  releaseNotes: 'release_notes',
  vocs: 'vocs',
} as const

export default supabase
