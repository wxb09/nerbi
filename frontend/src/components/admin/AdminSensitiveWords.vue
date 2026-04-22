<template>
  <div>
    <div class="flex items-center justify-between mb-5">
      <div>
        <h3 class="text-base font-bold text-[#3D3426]">敏感词管理</h3>
        <p class="text-xs text-[#B8AE9E] mt-0.5">管理论坛内容审核敏感词库</p>
      </div>
      <div class="flex gap-2 items-center">
        <div class="flex items-center gap-2 px-3 py-2 bg-[#FFF9EE] border border-[#E8D48B]/30 rounded-xl">
          <span class="text-xs text-[#9A9082]">审核开关</span>
          <button
            class="relative w-10 h-5 rounded-full transition-colors flex-shrink-0"
            :class="auditEnabled ? 'bg-[#4A9D6E]' : 'bg-[#D4D4D4]'"
            @click="toggleAuditStatus"
          >
            <span
              class="absolute top-0.5 left-0.5 w-4 h-4 bg-white rounded-full shadow transition-transform"
              :class="auditEnabled ? 'translate-x-5' : 'translate-x-0'"
            ></span>
          </button>
        </div>
        <button class="btn-primary flex items-center gap-1" @click="openAddModal">
          <span class="iconify" data-icon="solar:add-circle-bold"></span>
          添加敏感词
        </button>
      </div>
    </div>

    <div class="flex gap-2 mb-5">
      <input
        v-model="keyword"
        class="px-3 py-2 bg-[#FFF9EE] border border-[#E8D48B]/30 rounded-xl text-sm outline-none focus:ring-2 focus:ring-[#C9A227]/30 w-48"
        placeholder="搜索敏感词"
        @keyup.enter="search"
      />
      <select v-model="categoryFilter" class="px-3 py-2 bg-[#FFF9EE] border border-[#E8D48B]/30 rounded-xl text-sm outline-none" @change="search">
        <option value="">全部分类</option>
        <option v-for="cat in categories" :key="cat" :value="cat">{{ getCategoryLabel(cat) }}</option>
      </select>
      <button class="btn-secondary flex items-center gap-1" @click="search">
        <span class="iconify" data-icon="solar:magnifer-bold"></span>
        搜索
      </button>
      <button class="btn-secondary flex items-center gap-1 ml-auto" @click="refreshBuffer">
        <span class="iconify" data-icon="solar:refresh-bold"></span>
        刷新缓存
      </button>
    </div>

    <div v-if="loading" class="text-center py-12 text-[#9A9082]">加载中...</div>
    <div v-else-if="error" class="text-center py-12 text-[#D4644A]">{{ error }}</div>
    <template v-else>
      <div v-if="!isEmpty" class="card overflow-hidden">
        <table class="data-table w-full">
          <thead>
            <tr>
              <th>敏感词</th>
              <th>分类</th>
              <th>严重程度</th>
              <th>状态</th>
              <th>创建时间</th>
              <th class="text-center">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="word in words" :key="word.id">
              <td>
                <span class="font-semibold text-sm text-[#3D3426]">{{ word.word }}</span>
              </td>
              <td>
                <span class="text-xs font-bold px-2 py-0.5 rounded-full" :class="getCategoryClass(word.category)">
                  {{ getCategoryLabel(word.category) }}
                </span>
              </td>
              <td>
                <div class="flex items-center gap-1">
                  <span
                    v-for="i in 3"
                    :key="i"
                    class="w-2 h-2 rounded-full"
                    :class="i <= word.severity ? getSeverityColor(word.severity) : 'bg-gray-200'"
                  ></span>
                  <span class="text-xs text-[#9A9082] ml-1">{{ word.severity }}</span>
                </div>
              </td>
              <td>
                <span
                  class="text-xs font-bold px-2 py-0.5 rounded-full"
                  :class="word.status === 1 ? 'bg-emerald-50 text-emerald-600' : 'bg-gray-100 text-gray-500'"
                >
                  {{ word.status === 1 ? '启用' : '禁用' }}
                </span>
              </td>
              <td class="text-sm text-[#9A9082]">{{ formatDate(word.createdAt) }}</td>
              <td>
                <div class="flex items-center justify-center gap-1.5">
                  <button
                    class="action-btn"
                    :class="word.status === 1 ? 'reject' : 'approve'"
                    :title="word.status === 1 ? '禁用' : '启用'"
                    @click="toggleStatus(word)"
                  >
                    <span class="iconify text-base" :data-icon="word.status === 1 ? 'solar:eye-closed-bold' : 'solar:eye-bold'"></span>
                  </button>
                  <button class="action-btn reject" title="删除" @click="deleteWord(word)">
                    <span class="iconify text-base" data-icon="solar:trash-bin-trash-bold"></span>
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <div v-else class="text-center py-20 text-[#B8AE9E]">
        <span class="iconify text-5xl inline-block" data-icon="solar:shield-check-bold"></span>
        <p class="mt-4">暂无敏感词</p>
      </div>

      <Pagination
        v-if="totalPages > 1"
        :page="currentPage"
        :total-pages="totalPages"
        :total-elements="totalElements"
        @change="goToPage"
        class="mt-5"
      />
    </template>

    <Teleport to="body">
      <div v-if="addModal.show" class="modal-overlay" @click.self="addModal.show = false">
        <div class="modal-content">
          <div class="flex items-center justify-between mb-5">
            <h3 class="text-lg font-bold text-[#3D3426]">添加敏感词</h3>
            <button class="p-1 hover:bg-[#FFF9EE] rounded-lg" @click="addModal.show = false">
              <span class="iconify text-xl text-[#9A9082]" data-icon="solar:close-circle-bold"></span>
            </button>
          </div>

          <div class="space-y-4">
            <div>
              <label class="block text-sm font-medium text-[#3D3426] mb-1.5">敏感词</label>
              <input
                v-model="addModal.word"
                class="w-full px-3 py-2 bg-[#FFF9EE] border border-[#E8D48B]/30 rounded-xl text-sm outline-none focus:ring-2 focus:ring-[#C9A227]/30"
                placeholder="请输入敏感词"
              />
            </div>
            <div>
              <label class="block text-sm font-medium text-[#3D3426] mb-1.5">分类</label>
              <select v-model="addModal.category" class="w-full px-3 py-2 bg-[#FFF9EE] border border-[#E8D48B]/30 rounded-xl text-sm outline-none">
                <option v-for="cat in categories" :key="cat" :value="cat">{{ getCategoryLabel(cat) }}</option>
              </select>
            </div>
            <div>
              <label class="block text-sm font-medium text-[#3D3426] mb-1.5">严重程度</label>
              <div class="flex gap-2">
                <button
                  v-for="s in [1, 2, 3]"
                  :key="s"
                  class="flex-1 py-2 rounded-xl text-sm font-medium transition-all"
                  :class="addModal.severity === s ? 'bg-[#C9A227] text-white' : 'bg-[#FFF9EE] border border-[#E8D48B]/30 text-[#9A9082] hover:border-[#C9A227]'"
                  @click="addModal.severity = s"
                >
                  {{ s }}级
                </button>
              </div>
            </div>
          </div>

          <div class="flex gap-3 mt-6">
            <button class="flex-1 py-2.5 rounded-xl text-sm font-medium border border-[#E8D48B]/30 text-[#9A9082] hover:bg-[#FFF9EE]" @click="addModal.show = false">取消</button>
            <button class="flex-1 py-2.5 rounded-xl text-sm font-medium bg-[#C9A227] text-white hover:bg-[#B8911F]" :disabled="!addModal.word.trim() || addModal.loading" @click="addWord">
              {{ addModal.loading ? '添加中...' : '确认添加' }}
            </button>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import { auditApi, type SensitiveWord } from '../../api/audit'
import Pagination from '../common/Pagination.vue'
import { usePagination } from '../../composables/usePagination'

const categories = ref<string[]>(['INSULT', 'PORN', 'POLITICAL', 'AD', 'OTHER'])
const auditEnabled = ref(true)
const keyword = ref('')
const categoryFilter = ref('')

const addModal = ref({
  show: false,
  word: '',
  category: 'OTHER',
  severity: 1,
  loading: false
})

const categoryLabels: Record<string, string> = {
  INSULT: '辱骂',
  PORN: '色情',
  POLITICAL: '政治',
  AD: '广告',
  OTHER: '其他'
}

const categoryClasses: Record<string, string> = {
  INSULT: 'bg-red-50 text-red-600',
  PORN: 'bg-pink-50 text-pink-600',
  POLITICAL: 'bg-orange-50 text-orange-600',
  AD: 'bg-blue-50 text-blue-600',
  OTHER: 'bg-gray-100 text-gray-600'
}

const getCategoryLabel = (cat: string) => categoryLabels[cat] || cat
const getCategoryClass = (cat: string) => categoryClasses[cat] || 'bg-gray-100 text-gray-600'

const getSeverityColor = (severity: number) => {
  if (severity >= 3) return 'bg-red-500'
  if (severity >= 2) return 'bg-orange-500'
  return 'bg-green-500'
}

const formatDate = (dateStr: string) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN')
}

const params = computed(() => ({
  keyword: keyword.value || undefined,
  category: categoryFilter.value || undefined
}))

const {
  loading,
  error,
  data: words,
  page: currentPage,
  totalPages,
  totalElements,
  isEmpty,
  refresh,
  goToPage
} = usePagination<SensitiveWord>(
  (page, size) => auditApi.getWords({ ...params.value, page, size }),
  { pageSize: 10, mode: 'replace' }
)

// 监听筛选条件变化，自动刷新
watch([keyword, categoryFilter], () => {
  refresh()
})

const loadAuditStatus = async () => {
  try {
    const res = await auditApi.getAuditStatus() as any
    auditEnabled.value = res
  } catch (e) {
    console.error('获取审核状态失败', e)
  }
}

const toggleAuditStatus = async () => {
  try {
    await auditApi.setAuditStatus(!auditEnabled.value)
    auditEnabled.value = !auditEnabled.value
  } catch (e) {
    console.error('设置审核状态失败', e)
  }
}

const search = () => refresh()

const openAddModal = () => {
  addModal.value = {
    show: true,
    word: '',
    category: 'OTHER',
    severity: 1,
    loading: false
  }
}

const addWord = async () => {
  if (!addModal.value.word.trim()) return
  addModal.value.loading = true
  try {
    await auditApi.addWord({
      word: addModal.value.word.trim(),
      category: addModal.value.category,
      severity: addModal.value.severity
    })
    addModal.value.show = false
    refresh()
  } catch (e: any) {
    alert(e.message || '添加失败')
  } finally {
    addModal.value.loading = false
  }
}

const toggleStatus = async (word: SensitiveWord) => {
  try {
    await auditApi.toggleWordStatus(word.id)
    refresh()
  } catch (e: any) {
    alert(e.message || '操作失败')
  }
}

const deleteWord = async (word: SensitiveWord) => {
  if (!confirm(`确定要删除敏感词"${word.word}"吗？`)) return
  try {
    await auditApi.deleteWord(word.id)
    refresh()
  } catch (e: any) {
    alert(e.message || '删除失败')
  }
}

const refreshBuffer = async () => {
  try {
    await auditApi.refreshWordBuffer()
    alert('敏感词缓存已刷新')
  } catch (e: any) {
    alert(e.message || '刷新失败')
  }
}

onMounted(() => {
  refresh()
  loadAuditStatus()
})
</script>
<!-- 引入 Tailwind CSS -->
<style scoped>
@reference "tailwindcss"; 

.btn-primary {
  @apply px-4 py-2 bg-[#C9A227] text-white rounded-xl text-sm font-medium hover:bg-[#B8911F] transition-colors;
}
.btn-secondary {
  @apply px-4 py-2 bg-[#FFF9EE] border border-[#E8D48B]/30 text-[#9A9082] rounded-xl text-sm font-medium hover:border-[#C9A227] transition-colors;
}
.card {
  @apply bg-white rounded-2xl border border-[#E8D48B]/20;
}
.data-table th {
  @apply px-4 py-3 text-left text-xs font-bold text-[#9A9082] uppercase tracking-wider bg-[#FFF9EE];
}
.data-table td {
  @apply px-4 py-3 border-t border-[#F5F0EA];
}
.action-btn {
  @apply w-8 h-8 flex items-center justify-center rounded-lg transition-colors;
}
.action-btn.approve {
  @apply text-emerald-500 hover:bg-emerald-50;
}
.action-btn.reject {
  @apply text-red-400 hover:bg-red-50;
}
.page-btn {
  @apply px-3 py-1.5 text-sm rounded-lg border border-[#E8D48B]/30 text-[#9A9082] hover:border-[#C9A227] disabled:opacity-50 disabled:cursor-not-allowed;
}
.modal-overlay {
  @apply fixed inset-0 bg-black/50 flex items-center justify-center z-50;
}
.modal-content {
  @apply bg-white rounded-2xl p-6 w-full max-w-md mx-4 shadow-xl;
}
</style>
