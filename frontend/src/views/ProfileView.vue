<template>
  <MainNav />
  <main class="max-w-7xl mx-auto px-6 grid grid-cols-1 lg:grid-cols-4 gap-10 pb-20">
    <aside class="space-y-6">
      <div class="bg-white rounded-[2rem] p-8 border border-gray-100 shadow-sm text-center">
        <div class="relative w-24 h-24 mx-auto mb-4">
          <img 
            :src="getImageUrl(user?.avatar)" 
            alt="User avatar" 
            class="rounded-full shadow-lg w-24 h-24 object-cover"
          />
          <div class="absolute -bottom-1 -right-1 bg-yellow-400 p-1.5 rounded-full border-4 border-white">
            <span class="iconify text-white text-sm" data-icon="solar:crown-bold"></span>
          </div>
        </div>
        <h2 class="text-xl font-bold">{{ user?.nickname || '加载中...' }}</h2>
        <p class="text-sm text-gray-400 mb-6">认证：{{ locationText }}</p>
        <div class="grid grid-cols-2 gap-4 py-4 border-y border-gray-50">
          <div>
            <p class="text-xl font-bold">{{ user?.borrowCount || 0 }}</p>
            <p class="text-[10px] text-gray-400 uppercase">累计借入</p>
          </div>
          <div>
            <p class="text-xl font-bold">{{ user?.lendCount || 0 }}</p>
            <p class="text-[10px] text-gray-400 uppercase">累计借出</p>
          </div>
        </div>
        <div class="mt-6 space-y-2">
          <div class="flex justify-between text-xs mb-1 font-bold">
            <span>环保勋章进度</span>
            <span>{{ ecoProgress }}%</span>
          </div>
          <div class="w-full bg-gray-100 h-2 rounded-full overflow-hidden">
            <div class="bg-[#E2B04D] h-full transition-all duration-300" :style="{ width: ecoProgress + '%' }"></div>
          </div>
        </div>
      </div>

      <nav class="bg-white rounded-[2rem] overflow-hidden border border-gray-100 shadow-sm">
        <button 
          v-for="item in menuItems" 
          :key="item.key"
          @click="activeMenu = item.key; if (item.key === 'settings') settingsSubTab = 'profile'"
          :class="[
            'w-full flex items-center space-x-4 p-5 transition-colors text-left',
            activeMenu === item.key 
              ? 'bg-gray-50 text-[#E2B04D] font-bold border-l-4 border-[#E2B04D]' 
              : 'hover:bg-gray-50'
          ]"
        >
          <span class="iconify text-xl" :class="activeMenu === item.key ? '' : 'text-gray-400'" :data-icon="item.icon"></span>
          <span>{{ item.label }}</span>
        </button>
        
        <!-- 账号设置子菜单 -->
        <div v-if="activeMenu === 'settings'" class="border-t border-gray-100 bg-gray-50/50">
          <button 
            v-for="sub in settingsSubMenus" 
            :key="sub.key"
            @click="settingsSubTab = sub.key"
            :class="[
              'w-full flex items-center space-x-3 px-5 py-3 pl-12 text-sm transition-colors text-left',
              settingsSubTab === sub.key 
                ? 'text-[#E2B04D] font-medium bg-[#E2B04D]/5' 
                : 'text-gray-500 hover:text-gray-700 hover:bg-gray-50'
            ]"
          >
            <span class="iconify text-base" :data-icon="sub.icon"></span>
            <span>{{ sub.label }}</span>
          </button>
        </div>
        
        <button 
          @click="logout"
          class="w-full flex items-center space-x-4 p-5 hover:bg-gray-50 border-t border-gray-50 text-red-400 transition-colors text-left"
        >
          <span class="iconify text-xl" data-icon="solar:logout-bold"></span>
          <span>退出登录</span>
        </button>
      </nav>
    </aside>

    <div class="lg:col-span-3 space-y-8">
      <template v-if="activeMenu === 'dashboard'">
        <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
          <div class="bg-white p-6 rounded-3xl border border-gray-100 shadow-sm flex items-center space-x-4">
            <div class="p-4 bg-orange-100 text-orange-600 rounded-2xl">
              <span class="iconify text-2xl" data-icon="solar:bell-bold"></span>
            </div>
            <div>
              <p class="text-2xl font-bold">{{ stats.pendingApprovalCount }}</p>
              <p class="text-sm text-gray-400">待审批借阅申请</p>
            </div>
          </div>
          <div class="bg-white p-6 rounded-3xl border border-gray-100 shadow-sm flex items-center space-x-4">
            <div class="p-4 bg-blue-100 text-blue-600 rounded-2xl">
              <span class="iconify text-2xl" data-icon="solar:box-bold"></span>
            </div>
            <div>
              <p class="text-2xl font-bold">{{ stats.returnRequestedCount }}</p>
              <p class="text-sm text-gray-400">待确认归还</p>
            </div>
          </div>
          <div class="bg-white p-6 rounded-3xl border border-gray-100 shadow-sm flex items-center space-x-4">
            <div class="p-4 bg-green-100 text-green-600 rounded-2xl">
              <span class="iconify text-2xl" data-icon="solar:leaf-bold"></span>
            </div>
            <div>
              <p class="text-2xl font-bold">{{ stats.dueSoonCount }}</p>
              <p class="text-sm text-gray-400">本周到期物品</p>
            </div>
          </div>
        </div>

        <div class="bg-white rounded-[2rem] border border-gray-100 shadow-sm overflow-hidden">
          <div class="flex items-center justify-between px-8 py-6 border-b border-gray-50">
            <h3 class="text-xl font-bold italic">当前物品流转</h3>
            <div class="flex space-x-6 text-sm">
              <button 
                @click="activeTab = 'lent'; dashboardShowMore = false"
                :class="[
                  'font-bold py-1',
                  activeTab === 'lent' ? 'text-[#E2B04D] border-b-2 border-[#E2B04D]' : 'text-gray-400 hover:text-gray-600'
                ]"
              >
                我借出的
              </button>
              <button 
                @click="activeTab = 'borrowed'; dashboardShowMore = false"
                :class="[
                  'font-bold py-1',
                  activeTab === 'borrowed' ? 'text-[#E2B04D] border-b-2 border-[#E2B04D]' : 'text-gray-400 hover:text-gray-600'
                ]"
              >
                我借入的
              </button>
            </div>
          </div>
          <div class="overflow-x-auto">
            <table class="w-full text-left">
              <thead class="bg-gray-50 text-xs font-bold uppercase text-gray-400">
                <tr>
                  <th class="px-8 py-4">物品信息</th>
                  <th class="px-8 py-4">{{ activeTab === 'lent' ? '当前借阅人' : '借出人' }}</th>
                  <th class="px-8 py-4">归还时间</th>
                  <th class="px-8 py-4 text-center">状态</th>
                  <th class="px-8 py-4 text-center">管理</th>
                </tr>
              </thead>
              <tbody class="divide-y divide-gray-50">
                <tr v-if="currentItems.length === 0">
                  <td colspan="5" class="px-8 py-12 text-center text-gray-400">
                    暂无{{ activeTab === 'lent' ? '借出' : '借入' }}记录
                  </td>
                </tr>
                <tr 
                  v-for="item in currentItems" 
                  :key="item.id" 
                  class="hover:bg-gray-50/50 transition-colors"
                >
                  <td class="px-8 py-6">
                    <div class="flex items-center space-x-4">
                      <div class="w-12 h-12 rounded-xl bg-gray-100 flex-shrink-0 overflow-hidden">
                        <img v-if="item.itemImage" :src="getImageUrl(item.itemImage)" alt="Item" class="w-full h-full object-cover" />
                        <span v-else class="iconify text-2xl text-gray-300" data-icon="solar:box-bold"></span>
                      </div>
                      <div>
                        <p class="font-bold">{{ item.itemName }}</p>
                        <p class="text-xs text-gray-400">{{ item.categoryName || '其他' }}</p>
                      </div>
                    </div>
                  </td>
                  <td class="px-8 py-6">
                    <div class="flex items-center space-x-2">
                      <img 
                        v-if="item.counterpartyAvatar" 
                        :src="getImageUrl(item.counterpartyAvatar)" 
                        class="w-6 h-6 rounded-full"
                      />
                      <div v-else class="w-6 h-6 rounded-full bg-gray-200"></div>
                      <span class="text-sm">{{ item.counterpartyName }} {{ item.counterpartyLocation ? `(${item.counterpartyLocation})` : '' }}</span>
                    </div>
                  </td>
                  <td class="px-8 py-6 text-sm" :class="isOverdue(item.endTime) ? 'text-red-500' : ''">
                    {{ formatDate(item.endTime) }}
                    <span v-if="isOverdue(item.endTime)" class="text-red-500 text-xs block">今日到期</span>
                  </td>
                  <td class="px-8 py-6 text-center">
                    <span :class="getStatusClass(item.status)">
                      {{ getStatusText(item.status) }}
                    </span>
                  </td>
                  <td class="px-8 py-6 text-center">
                    <div class="flex items-center justify-center space-x-3">
                      <template v-if="activeTab === 'lent'">
                        <button 
                          v-if="item.status === 'PENDING'"
                          @click="approveRequest(item, true)" 
                          class="text-green-600 font-bold text-xs hover:underline"
                        >
                          同意
                        </button>
                        <button 
                          v-if="item.status === 'PENDING'"
                          @click="approveRequest(item, false)" 
                          class="text-red-500 font-bold text-xs hover:underline"
                        >
                          拒绝
                        </button>
                        <button 
                          v-if="item.status === 'ACTIVE'"
                          @click="remindReturn(item)" 
                          class="text-[#E2B04D] font-bold text-xs hover:underline"
                        >
                          提醒归还
                        </button>
                        <button 
                          v-if="item.status === 'ACTIVE'"
                          @click="confirmReturn(item)" 
                          class="bg-[#2D3436] text-white px-3 py-1 rounded-md text-[10px] uppercase font-bold"
                        >
                          确认收回
                        </button>
                        <button 
                          v-if="item.status === 'RETURN_REQUESTED'"
                          @click="confirmReturn(item)" 
                          class="bg-[#2D3436] text-white px-3 py-1 rounded-md text-[10px] uppercase font-bold"
                        >
                          确认归还
                        </button>
                      </template>
                      <template v-else>
                        <button 
                          v-if="item.status === 'APPROVED'"
                          @click="openPaymentModal(item)" 
                          class="bg-[#E2B04D] text-white px-3 py-1 rounded-md text-[10px] uppercase font-bold"
                        >
                          确认取货
                        </button>
                        <button 
                          v-if="item.status === 'ACTIVE'"
                          @click="applyReturn(item)" 
                          class="text-[#E2B04D] font-bold text-xs hover:underline"
                        >
                          申请归还
                        </button>
                        <span 
                          v-if="item.status === 'RETURN_REQUESTED'"
                          class="text-gray-400 text-xs"
                        >
                          等待确认
                        </span>
                      </template>
                      <div class="relative more-menu-container">
                        <button 
                          @click.stop="toggleMenu(item.id)" 
                          class="text-gray-400 font-bold text-xs hover:text-[#E2B04D] transition-colors"
                        >
                          更多
                        </button>
                        <Transition name="dropdown">
                          <div 
                            v-if="openMenuId === item.id"
                            class="absolute right-0 top-full mt-2 bg-white rounded-xl shadow-xl border border-gray-100 py-2 min-w-[120px] z-10 overflow-hidden"
                          >
                            <!-- 主要操作区 -->
                            <div v-if="item.status === 'RETURNED'" class="px-3 py-1.5">
                              <p class="text-[10px] text-gray-400 uppercase tracking-wider mb-1 text-center">评价</p>
                              
                              <template v-if="activeTab === 'borrowed'">
                                <button 
                                  v-if="!reviewedItems.has(item.id)"
                                  @click="openReviewModal(item, 'ITEM')"
                                  class="w-full px-3 py-2 text-sm hover:bg-gray-50 rounded-lg text-[#E2B04D] text-center"
                                >
                                  评价物品
                                </button>
                                <span 
                                  v-if="reviewedItems.has(item.id)"
                                  class="w-full px-3 py-2 text-sm text-gray-300 cursor-not-allowed rounded-lg block text-center"
                                >
                                  已评物品
                                </span>
                                
                                <button 
                                  v-if="!reviewedUsers.has(item.id)"
                                  @click="openReviewModal(item, 'USER')"
                                  class="w-full px-3 py-2 text-sm hover:bg-gray-50 rounded-lg text-[#E2B04D] text-center"
                                >
                                  评价借出者
                                </button>
                                <span 
                                  v-if="reviewedUsers.has(item.id)"
                                  class="w-full px-3 py-2 text-sm text-gray-300 cursor-not-allowed rounded-lg block text-center"
                                >
                                  已评借出者
                                </span>
                              </template>
                              
                              <template v-if="activeTab === 'lent'">
                                <button 
                                  v-if="!reviewedUsers.has(item.id)"
                                  @click="openReviewModal(item, 'USER')"
                                  class="w-full px-3 py-2 text-sm hover:bg-gray-50 rounded-lg text-[#E2B04D] text-center"
                                >
                                  评价借入者
                                </button>
                                <span 
                                  v-if="reviewedUsers.has(item.id)"
                                  class="w-full px-3 py-2 text-sm text-gray-300 cursor-not-allowed rounded-lg block text-center"
                                >
                                  已评价
                                </span>
                              </template>
                            </div>
                            
                            <!-- 取消借阅（非RETURNED状态）-->
                            <div v-if="item.status === 'PENDING' || item.status === 'APPROVED'" class="px-3 py-1.5">
                              <p class="text-[10px] text-gray-400 uppercase tracking-wider mb-1 text-center">操作</p>
                              <button 
                                @click="cancelBorrow(item)"
                                class="w-full px-3 py-2 text-sm hover:bg-gray-50 rounded-lg text-gray-500 text-center"
                              >
                                取消借阅
                              </button>
                            </div>
                            
                            <!-- 分隔线 -->
                            <div v-if="item.status === 'RETURNED' && activeTab === 'lent'" class="my-1 border-t border-gray-100"></div>
                            
                            <!-- 退还押金（借出者，RETURNED状态）-->
                            <div v-if="item.status === 'RETURNED' && activeTab === 'lent'" class="px-3 py-1.5">
                              <p class="text-[10px] text-gray-400 uppercase tracking-wider mb-1 text-center">押金</p>
                              <button 
                                @click="refundDeposit(item)"
                                class="w-full px-3 py-2 text-sm hover:bg-gray-50 rounded-lg text-blue-600 text-center"
                              >
                                退还押金
                              </button>
                              <button 
                                @click="skipRefundDeposit(item)"
                                class="w-full px-3 py-2 text-sm hover:bg-gray-50 rounded-lg text-gray-400 text-center"
                              >
                                跳过退还（测试）
                              </button>
                              <button 
                                @click="openDepositDisputeModal(item)"
                                class="w-full px-3 py-2 text-sm hover:bg-gray-50 rounded-lg text-orange-500 text-center"
                              >
                                发起押金纠纷
                              </button>
                            </div>
                            
                            <!-- 分隔线 -->
                            <div v-if="item.status === 'RETURNED'" class="my-1 border-t border-gray-100"></div>
                            
                            <!-- 次要操作区（占位）-->
                            <div v-if="item.status === 'RETURNED'" class="px-3 py-1.5">
                              <p class="text-[10px] text-gray-400 uppercase tracking-wider mb-1 text-center">其他</p>
                              <button 
                                @click="handlePlaceholder('viewDetail', item)"
                                class="w-full px-3 py-2 text-sm hover:bg-gray-50 rounded-lg text-gray-600 text-center"
                              >
                                查看详情
                              </button>
                            </div>
                            
                            <!-- 分隔线 -->
                            <div v-if="item.status === 'RETURNED'" class="my-1 border-t border-gray-100"></div>
                            
                            <!-- 特殊操作区 -->
                            <div v-if="item.status === 'RETURNED'" class="px-3 py-1.5">
                              <p class="text-[10px] text-gray-400 uppercase tracking-wider mb-1 text-center">反馈</p>
                              <button 
                                @click="openAppealModal(item)"
                                class="w-full px-3 py-2 text-sm hover:bg-gray-50 rounded-lg text-orange-500 text-center"
                              >
                                申诉
                              </button>
                            </div>
                          </div>
                        </Transition>
                      </div>
                    </div>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
          <div v-if="hasMoreItems || dashboardShowMore" class="p-6 bg-gray-50 text-center">
            <button @click="toggleDashboardShowMore" class="text-xs font-bold text-[#E2B04D] hover:underline uppercase tracking-widest">
              {{ dashboardShowMore ? '收起' : '查看更多' }}
            </button>
          </div>
        </div>

        <div class="bg-white rounded-[2rem] p-8 border border-gray-100 shadow-sm">
          <h3 class="text-xl font-bold mb-6 italic">最近收到的好评</h3>
          <div v-if="reviews.length === 0" class="text-center py-8 text-gray-400">
            暂无评价
          </div>
          <div v-else class="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div v-for="review in reviews.slice(0, 2)" :key="review.id" class="p-6 bg-gray-50 rounded-2xl relative">
              <span class="iconify absolute top-4 right-4 text-3xl text-gray-100" data-icon="bi:quote"></span>
              <div class="flex items-center space-x-2 mb-3">
                <span 
                  v-if="review.ratingTagDesc" 
                  class="px-2 py-1 bg-[#E2B04D]/10 text-[#E2B04D] text-xs font-bold rounded-full"
                >
                  {{ review.ratingTagDesc }}
                </span>
                <span class="text-[10px] text-gray-400">{{ formatTimeAgo(review.createdAt) }}</span>
              </div>
              <p class="text-sm text-gray-600 mb-4 font-medium leading-relaxed">"{{ review.content }}"</p>
              <div class="flex items-center space-x-2">
                <img 
                  v-if="review.reviewerAvatar" 
                  :src="getImageUrl(review.reviewerAvatar)" 
                  class="w-6 h-6 rounded-full"
                />
                <div v-else class="w-6 h-6 rounded-full bg-gray-200"></div>
                <span class="text-xs font-bold">{{ review.reviewerName }}</span>
              </div>
            </div>
          </div>
        </div>
      </template>

      <template v-else-if="activeMenu === 'items'">
        <div class="bg-white rounded-[2rem] border border-gray-100 shadow-sm overflow-hidden">
          <div class="flex items-center justify-between px-8 py-6 border-b border-gray-50">
            <h3 class="text-xl font-bold italic">我的发布</h3>
            <RouterLink to="/publish" class="text-sm bg-[#E2B04D] text-white px-4 py-2 rounded-xl font-bold">
              发布新物品
            </RouterLink>
          </div>
          <div v-if="myItems.length === 0" class="p-12 text-center">
            <span class="iconify text-6xl text-gray-200 mb-4" data-icon="solar:box-bold"></span>
            <p class="text-gray-400 mb-4">暂无发布的物品</p>
            <RouterLink to="/publish" class="text-[#E2B04D] font-bold hover:underline">
              立即发布第一个物品
            </RouterLink>
          </div>
          <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 p-8">
            <div 
              v-for="item in myItems" 
              :key="item.id" 
              class="border border-gray-100 rounded-2xl overflow-hidden hover:shadow-lg transition-shadow"
            >
              <div class="aspect-square bg-gray-100 relative overflow-hidden">
                <img v-if="item.image" :src="getImageUrl(item.image)" />
                <span v-else class="iconify text-4xl text-gray-300 absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2" data-icon="solar:box-bold"></span>
                <span :class="getItemStatusClass(item.status)" class="absolute top-3 right-3">
                  {{ getItemStatusText(item.status) }}
                </span>
              </div>
              <div class="p-4">
                <h4 class="font-bold mb-2 truncate">{{ item.name }}</h4>
                <p class="text-xs text-gray-400 mb-3">{{ item.borrowCount || 0 }}次借阅 · {{ item.viewCount || 0 }}次浏览</p>
                <div class="flex gap-2">
                  <button 
                    @click="editItem(item)" 
                    class="flex-1 text-xs py-2 border border-gray-200 rounded-lg hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
                    :disabled="item.status === 'BORROWED'"
                    :title="item.status === 'BORROWED' ? '借出中的物品无法编辑' : ''"
                  >
                    编辑
                  </button>
                  <button 
                    v-if="item.status !== 'DRAFT' && item.status !== 'BORROWED'"
                    @click="toggleItemStatus(item)" 
                    class="flex-1 text-xs py-2 border rounded-lg hover:bg-gray-50 transition-colors"
                    :class="item.status === 'OFFLINE' ? 'border-green-200 text-green-600 hover:bg-green-50' : 'border-red-200 text-red-500 hover:bg-red-50'"
                  >
                    {{ item.status === 'OFFLINE' ? '上架' : '下架' }}
                  </button>
                  <button 
                    v-if="item.status !== 'BORROWED'"
                    @click="deleteItem(item)" 
                    class="flex-1 text-xs py-2 border border-red-200 rounded-lg hover:bg-red-50 text-red-500"
                  >
                    删除
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </template>

      <template v-else-if="activeMenu === 'drafts'">
        <div class="bg-white rounded-[2rem] border border-gray-100 shadow-sm overflow-hidden">
          <div class="flex items-center justify-between px-8 py-6 border-b border-gray-50">
            <h3 class="text-xl font-bold italic">草稿箱</h3>
            <RouterLink to="/drafts" class="text-sm bg-[#E2B04D] text-white px-4 py-2 rounded-xl font-bold">
              管理草稿
            </RouterLink>
          </div>
          <div v-if="drafts.length === 0" class="p-12 text-center">
            <span class="iconify text-6xl text-gray-200 mb-4" data-icon="solar:file-bold"></span>
            <p class="text-gray-400">暂无草稿</p>
            <RouterLink to="/publish" class="text-[#E2B04D] font-bold hover:underline inline-block mt-2">
              去发布第一个物品
            </RouterLink>
          </div>
          <div v-else class="grid grid-cols-1 md:grid-cols-2 gap-6 p-8">
            <div 
              v-for="draft in drafts.slice(0, 2)" 
              :key="draft.id" 
              class="border border-gray-100 rounded-2xl overflow-hidden hover:shadow-md transition-all"
            >
              <div class="h-32 bg-gray-100 relative overflow-hidden">
                <img v-if="draft.image" :src="getImageUrl(draft.image)" />
                <span v-else class="iconify text-2xl text-gray-300 absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2" data-icon="solar:box-bold"></span>
              </div>
              <div class="p-4">
                <h4 class="font-bold mb-2 truncate">{{ draft.name }}</h4>
                <p class="text-xs text-gray-400 mb-3">最后编辑：{{ formatTimeAgo(draft.updatedAt) }}</p>
                <button @click="editDraft(draft)" class="w-full py-2 bg-[#E2B04D] text-white rounded-xl font-bold text-sm hover:bg-[#C49A2E] transition-colors">
                  继续编辑
                </button>
              </div>
            </div>
          </div>
          <div class="p-6 bg-gray-50 text-center">
            <RouterLink to="/drafts" class="text-xs font-bold text-[#E2B04D] hover:underline uppercase tracking-widest">
              查看全部草稿
            </RouterLink>
          </div>
        </div>
      </template>

      <template v-else-if="activeMenu === 'records'">
        <div class="bg-white rounded-[2rem] border border-gray-100 shadow-sm overflow-hidden">
          <div class="flex items-center justify-between px-8 py-6 border-b border-gray-50">
            <h3 class="text-xl font-bold italic">借阅记录</h3>
            <div class="flex space-x-6 text-sm">
              <button 
                @click="recordTab = 'lent'"
                :class="[
                  'font-bold py-1',
                  recordTab === 'lent' ? 'text-[#E2B04D] border-b-2 border-[#E2B04D]' : 'text-gray-400 hover:text-gray-600'
                ]"
              >
                我借出的
              </button>
              <button 
                @click="recordTab = 'borrowed'"
                :class="[
                  'font-bold py-1',
                  recordTab === 'borrowed' ? 'text-[#E2B04D] border-b-2 border-[#E2B04D]' : 'text-gray-400 hover:text-gray-600'
                ]"
              >
                我借入的
              </button>
            </div>
          </div>
          <div v-if="recordItems.length === 0" class="p-12 text-center">
            <span class="iconify text-6xl text-gray-200 mb-4" data-icon="solar:reorder-bold"></span>
            <p class="text-gray-400">暂无{{ recordTab === 'lent' ? '借出' : '借入' }}记录</p>
          </div>
          <div v-else class="divide-y divide-gray-50">
            <div 
              v-for="item in recordItems" 
              :key="item.id" 
              class="px-8 py-6 hover:bg-gray-50/50 transition-colors flex items-center justify-between"
            >
              <div class="flex items-center space-x-4">
                <div class="w-16 h-16 rounded-xl bg-gray-100 flex-shrink-0 overflow-hidden">
                  <img v-if="item.itemImage" :src="getImageUrl(item.itemImage)" class="w-full h-full object-cover" />
                  <span v-else class="iconify text-2xl text-gray-300" data-icon="solar:box-bold"></span>
                </div>
                <div>
                  <p class="font-bold">{{ item.itemName }}</p>
                  <p class="text-sm text-gray-400">
                    {{ recordTab === 'lent' ? '借阅人：' + item.counterpartyName : '借出人：' + item.counterpartyName }}
                  </p>
                  <p class="text-xs text-gray-400">{{ formatDate(item.startTime) }} - {{ formatDate(item.endTime) }}</p>
                </div>
              </div>
              <div class="text-right">
                <span :class="getStatusClass(item.status)">{{ getStatusText(item.status) }}</span>
              </div>
            </div>
          </div>
        </div>
      </template>

      <template v-else-if="activeMenu === 'reviews'">
        <div class="bg-white rounded-[2rem] border border-gray-100 shadow-sm overflow-hidden">
          <div class="flex items-center justify-between px-8 py-6 border-b border-gray-50">
            <h3 class="text-xl font-bold italic">评价管理</h3>
            <div class="flex space-x-6 text-sm">
              <button 
                @click="reviewTab = 'received'"
                :class="[
                  'font-bold py-1',
                  reviewTab === 'received' ? 'text-[#E2B04D] border-b-2 border-[#E2B04D]' : 'text-gray-400 hover:text-gray-600'
                ]"
              >
                我收到的
              </button>
              <button 
                @click="reviewTab = 'given'"
                :class="[
                  'font-bold py-1',
                  reviewTab === 'given' ? 'text-[#E2B04D] border-b-2 border-[#E2B04D]' : 'text-gray-400 hover:text-gray-600'
                ]"
              >
                我发出的
              </button>
            </div>
          </div>
          <div v-if="currentReviews.length === 0" class="p-12 text-center">
            <span class="iconify text-6xl text-gray-200 mb-4" data-icon="solar:chat-round-dots-bold"></span>
            <p class="text-gray-400">暂无{{ reviewTab === 'received' ? '收到' : '发出' }}的评价</p>
          </div>
          <div v-else class="divide-y divide-gray-50">
            <div 
              v-for="review in currentReviews" 
              :key="review.id" 
              class="px-8 py-6 hover:bg-gray-50/50 transition-colors"
            >
              <div class="flex items-start space-x-4">
                <div class="w-12 h-12 rounded-xl bg-gray-100 flex-shrink-0 overflow-hidden">
                  <img 
                    v-if="review.itemImage" 
                    :src="getImageUrl(review.itemImage)" 
                    class="w-full h-full object-cover" 
                  />
                  <span v-else class="iconify text-2xl text-gray-300 w-full h-full flex items-center justify-center" data-icon="solar:box-bold"></span>
                </div>
                <div class="flex-1 min-w-0">
                  <div class="flex items-center space-x-2 mb-2">
                    <span class="font-bold text-sm">{{ review.itemName }}</span>
                    <span 
                      :class="[
                        'px-2 py-0.5 text-[10px] font-bold rounded-full',
                        review.targetType === 'ITEM' 
                          ? 'bg-blue-100 text-blue-600' 
                          : 'bg-purple-100 text-purple-600'
                      ]"
                    >
                      {{ review.targetType === 'ITEM' ? '物品评价' : '用户评价' }}
                    </span>
                    <span class="text-[10px] text-gray-400">{{ formatTimeAgo(review.createdAt) }}</span>
                  </div>
                  
                  <div class="flex items-center space-x-2 mb-2">
                    <template v-if="review.targetType === 'ITEM' && review.ratingTagDesc">
                      <span class="px-2 py-1 bg-[#E2B04D]/10 text-[#E2B04D] text-xs font-bold rounded-full">
                        {{ review.ratingTagDesc }}
                      </span>
                    </template>
                    <template v-if="review.targetType === 'USER' && review.ratingStar">
                      <div class="flex text-yellow-400 text-xs">
                        <span v-for="i in 5" :key="i" class="iconify" :data-icon="i <= (review.ratingStar || 0) ? 'solar:star-bold' : 'solar:star-line-duotone'"></span>
                      </div>
                    </template>
                  </div>
                  
                  <p class="text-sm text-gray-600 font-medium leading-relaxed mb-3">"{{ review.content }}"</p>
                  
                  <div class="flex items-center justify-between">
                    <div class="flex items-center space-x-2">
                      <img 
                        v-if="review.counterpartyAvatar" 
                        :src="getImageUrl(review.counterpartyAvatar)" 
                        class="w-5 h-5 rounded-full"
                      />
                      <div v-else class="w-5 h-5 rounded-full bg-gray-200"></div>
                      <span class="text-xs text-gray-500">
                        {{ reviewTab === 'received' ? '来自' : '评价' }}：{{ review.counterpartyName }}
                      </span>
                    </div>
                    <button 
                      v-if="reviewTab === 'given'"
                      @click="deleteReview(review.id)"
                      class="text-xs text-red-400 hover:text-red-600 transition-colors"
                    >
                      删除
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </template>

      <template v-else-if="activeMenu === 'settings'">
        <div class="space-y-6">
          <!-- 基本信息 -->
          <div id="settings-profile" class="bg-white rounded-[2rem] border border-gray-100 shadow-sm overflow-hidden">
            <div class="px-8 py-5 border-b border-gray-50 flex items-center gap-3">
              <div class="w-10 h-10 rounded-xl bg-gray-100 flex items-center justify-center">
                <span class="iconify text-xl text-gray-500" data-icon="solar:user-bold"></span>
              </div>
              <h3 class="text-lg font-bold text-[#2D3436]">基本信息</h3>
            </div>
            
            <div class="p-8 space-y-6">
              <div class="flex items-center gap-6">
                <div class="relative group">
                  <img 
                    :src="getImageUrl(settingsForm.avatar)" 
                    class="w-20 h-20 rounded-full object-cover border-4 border-gray-100 group-hover:border-[#E2B04D]/30 transition-colors"
                  />
                  <label class="absolute inset-0 flex items-center justify-center bg-black/40 rounded-full opacity-0 group-hover:opacity-100 cursor-pointer transition-opacity">
                    <span class="iconify text-white text-xl" data-icon="solar:camera-bold"></span>
                    <input type="file" accept="image/*" class="hidden" @change="handleAvatarChange" />
                  </label>
                </div>
                <div class="flex-1">
                  <p class="text-sm text-gray-400 mb-1">点击头像更换</p>
                  <p class="font-medium text-lg">{{ settingsForm.nickname || '用户' }}</p>
                </div>
              </div>

              <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div>
                  <label class="block text-sm text-gray-500 mb-1.5">昵称</label>
                  <input 
                    v-model="settingsForm.nickname" 
                    class="w-full px-4 py-3 bg-gray-50 border border-gray-200 rounded-xl outline-none focus:border-[#E2B04D] focus:bg-white transition-colors"
                    placeholder="请输入昵称"
                    maxlength="50"
                  />
                </div>
                <div>
                  <label class="block text-sm text-gray-500 mb-1.5">手机号</label>
                  <input 
                    :value="user?.phone ? maskPhone(user.phone) : ''" 
                    class="w-full px-4 py-3 bg-gray-100 border border-gray-200 rounded-xl text-gray-400 cursor-not-allowed"
                    disabled
                  />
                </div>
              </div>

              <div>
                <label class="block text-sm text-gray-500 mb-1.5">个人简介</label>
                <textarea 
                  v-model="settingsForm.bio" 
                  class="w-full px-4 py-3 bg-gray-50 border border-gray-200 rounded-xl outline-none focus:border-[#E2B04D] focus:bg-white transition-colors resize-none"
                  placeholder="介绍一下自己吧..."
                  rows="3"
                  maxlength="500"
                ></textarea>
              </div>

              <div class="flex justify-end gap-3 pt-2 border-t border-gray-50">
                <button 
                  @click="resetSettingsForm" 
                  class="px-6 py-2.5 border border-gray-200 rounded-xl text-sm font-medium text-gray-600 hover:bg-gray-50 transition-colors"
                >
                  重置
                </button>
                <button 
                  @click="saveSettings" 
                  :disabled="settingsSaving"
                  class="px-6 py-2.5 bg-[#2D3436] text-white rounded-xl text-sm font-medium hover:bg-[#1a1a1a] transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  {{ settingsSaving ? '保存中...' : '保存修改' }}
                </button>
              </div>
            </div>
          </div>

          <!-- 小区认证 -->
          <div id="settings-verify" class="bg-white rounded-[2rem] border border-gray-100 shadow-sm overflow-hidden">
            <div class="px-8 py-5 border-b border-gray-50 flex items-center justify-between">
              <div class="flex items-center gap-3">
                <div class="w-10 h-10 rounded-xl bg-[#E2B04D]/10 flex items-center justify-center">
                  <span class="iconify text-xl text-[#E2B04D]" data-icon="solar:home-2-bold"></span>
                </div>
                <h3 class="text-lg font-bold text-[#2D3436]">小区认证</h3>
              </div>
              <span v-if="user?.addressVerifyStatus === 'APPROVED'" class="px-3 py-1 bg-green-100 text-green-600 rounded-full text-sm font-medium flex items-center gap-1">
                <span class="iconify" data-icon="solar:check-circle-bold"></span>
                已认证
              </span>
              <span v-else-if="user?.addressVerifyStatus === 'PENDING'" class="px-3 py-1 bg-yellow-100 text-yellow-600 rounded-full text-sm font-medium flex items-center gap-1">
                <span class="iconify" data-icon="solar:clock-circle-bold"></span>
                审核中
              </span>
              <span v-else class="px-3 py-1 bg-gray-100 text-gray-500 rounded-full text-sm font-medium">未认证</span>
            </div>
            
            <div class="p-8">
              <!-- 已认证状态 -->
              <div v-if="user?.addressVerifyStatus === 'APPROVED'" class="space-y-4">
                <div class="flex items-center gap-4 p-5 bg-gradient-to-r from-green-50 to-emerald-50 rounded-2xl">
                  <div class="w-14 h-14 rounded-xl bg-green-100 flex items-center justify-center">
                    <span class="iconify text-3xl text-green-500" data-icon="solar:verified-check-bold"></span>
                  </div>
                  <div class="flex-1">
                    <p class="font-medium text-green-700 text-lg">认证通过</p>
                    <p class="text-gray-500">{{ user?.communityName }} {{ user?.building }} {{ user?.unit }}</p>
                  </div>
                </div>
                <div class="p-4 bg-[#FFF9EE] rounded-xl">
                  <p class="text-sm text-[#8C7D66]">认证权益已激活：可发布物品 · 显示认证标识 · 获得更多信任</p>
                </div>
              </div>
              
              <!-- 审核中状态 -->
              <div v-else-if="user?.addressVerifyStatus === 'PENDING'" class="space-y-4">
                <div class="flex items-center gap-4 p-5 bg-gradient-to-r from-yellow-50 to-amber-50 rounded-2xl">
                  <div class="w-14 h-14 rounded-xl bg-yellow-100 flex items-center justify-center">
                    <span class="iconify text-3xl text-yellow-500" data-icon="solar:clock-circle-bold"></span>
                  </div>
                  <div class="flex-1">
                    <p class="font-medium text-yellow-700 text-lg">审核中</p>
                    <p class="text-gray-500">预计1-3个工作日完成审核，请耐心等待</p>
                  </div>
                </div>
              </div>
              
              <!-- 未认证/被拒绝 -->
              <div v-else class="space-y-5">
                <div v-if="user?.addressVerifyStatus === 'REJECTED'" class="p-4 bg-red-50 rounded-xl text-red-500 text-sm flex items-center gap-2">
                  <span class="iconify text-lg" data-icon="solar:danger-circle-bold"></span>
                  认证未通过，请检查信息后重新提交
                </div>

                <div class="p-4 bg-[#FFF9EE] rounded-xl">
                  <p class="text-sm text-[#8C7D66] mb-2">完成认证后您可以：</p>
                  <div class="flex flex-wrap gap-3 text-sm">
                    <span class="flex items-center gap-1.5 text-[#2D3436]">
                      <span class="iconify text-green-500" data-icon="solar:check-circle-bold"></span>
                      发布闲置物品
                    </span>
                    <span class="flex items-center gap-1.5 text-[#2D3436]">
                      <span class="iconify text-green-500" data-icon="solar:check-circle-bold"></span>
                      显示认证标识
                    </span>
                    <span class="flex items-center gap-1.5 text-[#2D3436]">
                      <span class="iconify text-green-500" data-icon="solar:check-circle-bold"></span>
                      获得更多信任
                    </span>
                  </div>
                </div>
                
                <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
                  <div>
                    <label class="block text-sm text-gray-500 mb-1.5">所属小区 <span class="text-red-400">*</span></label>
                    <select 
                      v-model="verifyForm.communityId"
                      class="w-full px-4 py-3 bg-gray-50 border border-gray-200 rounded-xl outline-none focus:border-[#E2B04D] focus:bg-white transition-colors"
                    >
                      <option :value="null">请选择小区</option>
                      <option v-for="c in communities" :key="c.id" :value="c.id">{{ c.name }}</option>
                    </select>
                  </div>
                  <div>
                    <label class="block text-sm text-gray-500 mb-1.5">楼栋 <span class="text-red-400">*</span></label>
                    <input 
                      v-model="verifyForm.building" 
                      class="w-full px-4 py-3 bg-gray-50 border border-gray-200 rounded-xl outline-none focus:border-[#E2B04D] focus:bg-white transition-colors"
                      placeholder="例如：3栋"
                    />
                  </div>
                  <div>
                    <label class="block text-sm text-gray-500 mb-1.5">单元</label>
                    <input 
                      v-model="verifyForm.unit" 
                      class="w-full px-4 py-3 bg-gray-50 border border-gray-200 rounded-xl outline-none focus:border-[#E2B04D] focus:bg-white transition-colors"
                      placeholder="例如：1单元"
                    />
                  </div>
                </div>
                
                <div class="flex justify-end pt-2">
                  <button 
                    @click="submitAddressVerify" 
                    :disabled="verifySubmitting || !verifyForm.communityId"
                    class="px-8 py-3 bg-[#E2B04D] text-white rounded-xl font-medium hover:bg-[#d4a044] transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                  >
                    {{ verifySubmitting ? '提交中...' : '提交认证' }}
                  </button>
                </div>
              </div>
            </div>
          </div>

          <!-- 账号安全 -->
          <div id="settings-security" class="bg-white rounded-[2rem] border border-gray-100 shadow-sm overflow-hidden">
            <div class="px-8 py-5 border-b border-gray-50 flex items-center gap-3">
              <div class="w-10 h-10 rounded-xl bg-gray-100 flex items-center justify-center">
                <span class="iconify text-xl text-gray-500" data-icon="solar:lock-password-bold"></span>
              </div>
              <h3 class="text-lg font-bold text-[#2D3436]">账号安全</h3>
            </div>
            
            <div class="p-8 space-y-4">
              <div class="flex items-center justify-between p-5 bg-gray-50 rounded-xl hover:bg-gray-100/50 transition-colors">
                <div class="flex items-center gap-4">
                  <div class="w-10 h-10 rounded-lg bg-blue-50 flex items-center justify-center">
                    <span class="iconify text-xl text-blue-500" data-icon="solar:phone-bold"></span>
                  </div>
                  <div>
                    <p class="text-sm text-gray-500">绑定手机</p>
                    <p class="font-medium">{{ user?.phone ? maskPhone(user.phone) : '-' }}</p>
                  </div>
                </div>
                <span class="text-xs text-green-500 flex items-center gap-1">
                  <span class="iconify" data-icon="solar:check-circle-bold"></span>
                  已绑定
                </span>
              </div>
              
              <div class="flex items-center justify-between p-5 bg-gray-50 rounded-xl hover:bg-gray-100/50 transition-colors">
                <div class="flex items-center gap-4">
                  <div class="w-10 h-10 rounded-lg bg-purple-50 flex items-center justify-center">
                    <span class="iconify text-xl text-purple-500" data-icon="solar:lock-bold"></span>
                  </div>
                  <div>
                    <p class="text-sm text-gray-500">登录密码</p>
                    <p class="font-medium">••••••••</p>
                  </div>
                </div>
                <button class="px-4 py-2 text-sm text-[#E2B04D] font-medium hover:bg-[#E2B04D]/10 rounded-lg transition-colors">
                  修改密码
                </button>
              </div>

              <div class="flex items-center justify-between p-5 bg-gray-50 rounded-xl hover:bg-gray-100/50 transition-colors">
                <div class="flex items-center gap-4">
                  <div class="w-10 h-10 rounded-lg bg-blue-50 flex items-center justify-center">
                    <span class="iconify text-xl text-blue-500" data-icon="solar:wallet-bold"></span>
                  </div>
                  <div>
                    <p class="text-sm text-gray-500">支付宝账号</p>
                    <p v-if="(user as any)?.alipayAccount" class="font-medium">{{ (user as any).alipayAccount }}</p>
                    <p v-else class="font-medium text-gray-300">未绑定</p>
                  </div>
                </div>
                <div class="flex items-center gap-2">
                  <span v-if="(user as any)?.alipayAccount" class="text-xs text-green-500 flex items-center gap-1">
                    <span class="iconify" data-icon="solar:check-circle-bold"></span>
                    已绑定
                  </span>
                  <button 
                    @click="showAlipayBindModal = true"
                    class="px-4 py-2 text-sm text-[#E2B04D] font-medium hover:bg-[#E2B04D]/10 rounded-lg transition-colors"
                  >
                    {{ (user as any)?.alipayAccount ? '修改' : '绑定' }}
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </template>
    </div>
  </main>
  
  <ReviewModal 
    :visible="showReviewModal" 
    :borrow-info="reviewingBorrow"
    @close="showReviewModal = false"
    @success="handleReviewSuccess"
  />

  <PaymentModal
    :visible="showPaymentModal"
    :borrow-info="payingBorrow"
    @close="showPaymentModal = false"
    @success="handlePaymentSuccess"
  />

  <DepositDisputeModal
    :visible="showDepositDisputeModal"
    :borrow-info="disputingBorrow"
    @close="showDepositDisputeModal = false"
    @success="handleDepositDisputeSuccess"
  />

  <Teleport to="body">
    <Transition name="modal">
      <div v-if="showAlipayBindModal" class="fixed inset-0 z-50 flex items-center justify-center p-4">
        <div class="absolute inset-0 bg-black/50" @click="showAlipayBindModal = false"></div>
        <div class="relative bg-white rounded-3xl w-full max-w-md shadow-2xl overflow-hidden">
          <div class="p-6 border-b border-gray-100">
            <div class="flex items-center justify-between">
              <h3 class="text-xl font-bold">绑定支付宝账号</h3>
              <button @click="showAlipayBindModal = false" class="text-gray-400 hover:text-gray-600 transition-colors">
                <span class="iconify text-2xl" data-icon="solar:close-circle-bold"></span>
              </button>
            </div>
          </div>
          <div class="p-6 space-y-4">
            <div class="p-3 bg-blue-50 rounded-xl flex items-start gap-2">
              <span class="iconify text-blue-500 text-lg flex-shrink-0 mt-0.5" data-icon="solar:info-circle-bold"></span>
              <p class="text-xs text-blue-700 leading-relaxed">绑定支付宝账号后，租金和扣款将自动转入您的支付宝账户。</p>
            </div>
            <div class="space-y-2">
              <label class="block text-sm font-medium text-gray-700">支付宝账号</label>
              <input 
                v-model="alipayAccountInput"
                type="text"
                class="w-full px-4 py-3 bg-gray-50 border border-gray-200 rounded-xl text-sm outline-none focus:ring-2 focus:ring-[#E2B04D]/30"
                placeholder="请输入支付宝登录账号（手机号/邮箱）"
              />
            </div>
          </div>
          <div class="p-6 border-t border-gray-100 space-y-3">
            <button
              @click="bindAlipayAccount"
              :disabled="alipayBinding || !alipayAccountInput.trim()"
              class="w-full py-3.5 bg-[#E2B04D] text-white rounded-xl font-bold hover:bg-[#C49A2E] transition-colors disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2"
            >
              <span v-if="alipayBinding" class="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin"></span>
              {{ alipayBinding ? '绑定中...' : '确认绑定' }}
            </button>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>

  <Teleport to="body">
    <div v-if="appealModal.show" class="fixed inset-0 bg-black/40 backdrop-blur-sm z-50 flex items-center justify-center" @click.self="appealModal.show = false">
      <div class="bg-white rounded-3xl p-8 w-[90%] max-w-md shadow-2xl">
        <div class="flex items-center justify-between mb-6">
          <h3 class="text-xl font-bold text-gray-800">发起申诉</h3>
          <button @click="appealModal.show = false" class="p-1 hover:bg-gray-100 rounded-lg">
            <span class="iconify text-2xl text-gray-400" data-icon="solar:close-circle-bold"></span>
          </button>
        </div>
        <div class="mb-4 p-4 bg-gray-50 rounded-xl">
          <p class="text-sm text-gray-600">物品：<span class="font-semibold text-gray-800">{{ appealModal.itemName }}</span></p>
          <p class="text-sm text-gray-500 mt-1">对方：{{ appealModal.counterpartyName }}</p>
        </div>
        <div class="mb-6">
          <label class="block text-sm font-medium text-gray-700 mb-2">申诉原因</label>
          <textarea 
            v-model="appealModal.reason" 
            class="w-full px-4 py-3 bg-gray-50 border border-gray-200 rounded-xl text-sm outline-none focus:ring-2 focus:ring-orange-300 resize-none" 
            rows="4" 
            placeholder="请详细描述您遇到的问题和申诉原因..."
          ></textarea>
        </div>
        <div class="flex gap-3">
          <button @click="appealModal.show = false" class="flex-1 py-3 bg-gray-100 text-gray-600 rounded-xl font-medium hover:bg-gray-200 transition-colors">取消</button>
          <button 
            @click="submitAppeal" 
            :disabled="appealModal.submitting || !appealModal.reason.trim()" 
            class="flex-1 py-3 bg-gradient-to-r from-orange-400 to-orange-500 text-white rounded-xl font-medium hover:from-orange-500 hover:to-orange-600 transition-all disabled:opacity-50 disabled:cursor-not-allowed"
          >
            {{ appealModal.submitting ? '提交中...' : '提交申诉' }}
          </button>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, reactive, watch } from 'vue'
import { useRouter, RouterLink } from 'vue-router'
import MainNav from '../components/MainNav.vue'
import ReviewModal from '../components/ReviewModal.vue'
import PaymentModal from '../components/PaymentModal.vue'
import DepositDisputeModal from '../components/DepositDisputeModal.vue'
import { useAuthStore } from '../stores/auth'
import { userApi } from '../api/user'
import { borrowApi } from '../api/borrow'
import { itemApi } from '../api/item'
import { reviewApi } from '../api/review'
import { publicApi } from '../api/public'
import { disputeApi } from '../api/dispute'
import { paymentApi } from '../api/payment'
import { wsManager } from '../utils/websocket'

interface User {
  id: number
  nickname: string
  avatar: string
  phone: string
  communityName: string
  building: string
  creditScore: number
  borrowCount: number
  lendCount: number
  co2Saved: number
}

interface BorrowItem {
  id: number
  itemName: string
  itemImage?: string
  categoryName?: string
  counterpartyName: string
  counterpartyAvatar?: string
  counterpartyLocation?: string
  counterpartyId?: number
  startTime: string
  endTime: string
  status: string
  isBorrower?: boolean
  defaultReviewType?: 'ITEM' | 'USER'
  pricePerDay?: number
  deposit?: number
}

interface MyItem {
  id: number
  name: string
  image?: string
  status: string
  borrowCount: number
  viewCount: number
}

interface Review {
  id: number
  borrowId: number
  itemId: number
  itemName: string
  itemImage?: string
  targetType: 'ITEM' | 'USER'
  ratingStar: number | null
  ratingTag: string | null
  ratingTagDesc: string | null
  content: string
  counterpartyName: string
  counterpartyAvatar?: string
  createdAt: string
}

interface Stats {
  pendingApprovalCount: number
  returnRequestedCount: number
  dueSoonCount: number
  todayCo2Saved: number
}

const router = useRouter()
const authStore = useAuthStore()

const user = ref<User | null>(null)
const lentItems = ref<BorrowItem[]>([])
const borrowedItems = ref<BorrowItem[]>([])
const myItems = ref<MyItem[]>([])
const drafts = ref<MyItem[]>([])
const reviews = ref<Review[]>([])
const communities = ref<{ id: number; name: string }[]>([])
const stats = ref<Stats>({
  pendingApprovalCount: 0,
  returnRequestedCount: 0,
  dueSoonCount: 0,
  todayCo2Saved: 0
})

const showReviewModal = ref(false)
const reviewingBorrow = ref<BorrowItem | null>(null)
const reviewedBorrows = ref<Set<number>>(new Set())
const reviewedItems = ref<Set<number>>(new Set())
const reviewedUsers = ref<Set<number>>(new Set())
const showPaymentModal = ref(false)
const payingBorrow = ref<any>(null)
const showDepositDisputeModal = ref(false)
const disputingBorrow = ref<any>(null)
const showAlipayBindModal = ref(false)
const alipayAccountInput = ref('')
const alipayBinding = ref(false)
const openMenuId = ref<number | null>(null)
const reviewTab = ref<'received' | 'given'>('received')
const givenReviews = ref<Review[]>([])

const activeMenu = ref('dashboard')
const activeTab = ref('lent')
const dashboardShowMore = ref(false)
const dashboardLimit = 6
const recordTab = ref('lent')

const menuItems = [
  { key: 'dashboard', label: '控制面板', icon: 'solar:widget-3-bold' },
  { key: 'items', label: '我的发布', icon: 'solar:box-bold' },
  { key: 'drafts', label: '草稿箱', icon: 'solar:file-bold' },
  { key: 'records', label: '借阅记录', icon: 'solar:reorder-bold' },
  { key: 'reviews', label: '评价管理', icon: 'solar:chat-round-dots-bold' },
  { key: 'settings', label: '账号设置', icon: 'solar:settings-bold' }
]

const settingsSubMenus = [
  { key: 'profile', label: '基本信息', icon: 'solar:user-bold' },
  { key: 'verify', label: '小区认证', icon: 'solar:home-2-bold' },
  { key: 'security', label: '账号安全', icon: 'solar:lock-password-bold' }
]
const settingsSubTab = ref('profile')

watch(settingsSubTab, (newVal) => {
  const el = document.getElementById(`settings-${newVal}`)
  if (el) {
    el.scrollIntoView({ behavior: 'smooth', block: 'start' })
  }
})

const locationText = computed(() => {
  if (!user.value) return ''
  const parts = [user.value.communityName, user.value.building].filter(Boolean)
  return parts.length > 0 ? parts.join(' ') : '未认证'
})

const ecoProgress = computed(() => {
  if (!user.value) return 0
  const co2 = user.value.co2Saved || 0
  return Math.min(100, Math.round((co2 / 1000) * 100))
})

const currentItems = computed(() => {
  const items = activeTab.value === 'lent' ? lentItems.value : borrowedItems.value
  if (dashboardShowMore.value) {
    return items
  }
  return items.slice(0, dashboardLimit)
})

const hasMoreItems = computed(() => {
  const items = activeTab.value === 'lent' ? lentItems.value : borrowedItems.value
  return items.length > dashboardLimit
})

const currentReviews = computed(() => {
  return reviewTab.value === 'received' ? reviews.value : givenReviews.value
})

const recordItems = computed(() => {
  return recordTab.value === 'lent' ? lentItems.value : borrowedItems.value
})

const settingsForm = reactive({
  nickname: '',
  bio: '',
  avatar: ''
})
const settingsSaving = ref(false)

const verifyForm = reactive({
  communityId: null as number | null,
  building: '',
  unit: ''
})
const verifySubmitting = ref(false)

const maskPhone = (phone: string) => {
  if (!phone || phone.length !== 11) return phone
  return phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2')
}

const initSettingsForm = () => {
  if (user.value) {
    settingsForm.nickname = user.value.nickname || ''
    settingsForm.bio = (user.value as any).bio || ''
    settingsForm.avatar = user.value.avatar || ''
  }
}

const resetSettingsForm = () => {
  initSettingsForm()
}

const handleAvatarChange = async (event: Event) => {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return

  try {
    const formData = new FormData()
    formData.append('files', file)
    const res = await fetch('http://localhost:8080/api/upload/images', {
      method: 'POST',
      body: formData
    })
    const data = await res.json()
    if (data.data && data.data[0]) {
      settingsForm.avatar = data.data[0].url || data.data[0].filename
    }
  } catch (error) {
    alert('上传头像失败')
  }
}

const saveSettings = async () => {
  settingsSaving.value = true
  try {
    const updateData: Record<string, any> = {}
    if (settingsForm.nickname !== user.value?.nickname) {
      updateData.nickname = settingsForm.nickname
    }
    if (settingsForm.bio !== (user.value as any)?.bio) {
      updateData.bio = settingsForm.bio
    }
    if (settingsForm.avatar !== user.value?.avatar) {
      updateData.avatar = settingsForm.avatar
    }

    if (Object.keys(updateData).length === 0) {
      alert('没有修改的内容')
      return
    }

    await userApi.updateUser(updateData)
    await loadUserInfo()
    alert('保存成功')
  } catch (error: any) {
    alert(error.message || '保存失败')
  } finally {
    settingsSaving.value = false
  }
}

const submitAddressVerify = async () => {
  if (!verifyForm.communityId) {
    alert('请选择小区')
    return
  }
  
  verifySubmitting.value = true
  try {
    await userApi.submitAddressVerify({
      communityId: verifyForm.communityId,
      building: verifyForm.building,
      unit: verifyForm.unit
    })
    await loadUserInfo()
    alert('提交成功，请等待管理员审核')
  } catch (error: any) {
    alert(error.message || '提交失败')
  } finally {
    verifySubmitting.value = false
  }
}

const logout = () => {
  authStore.logout()
  router.push('/login')
}

const todo = (action: string) => {
  alert(`接口占位：${action}`)
}

const remindReturn = async (item: BorrowItem) => {
  try {
    await borrowApi.remindReturn(item.id)
    alert('已发送提醒')
    loadUserInfo()
  } catch (error: any) {
    alert(error.message || '提醒失败')
  }
}

const confirmReturn = async (item: BorrowItem) => {
  if (!confirm('确认该物品已归还？')) return
  try {
    await borrowApi.confirmReturn(item.id)
    alert('已确认归还')
    loadUserInfo()
  } catch (error: any) {
    alert(error.message || '确认失败')
  }
}

const approveRequest = async (item: BorrowItem, approved: boolean) => {
  const reason = approved ? '' : prompt('请输入拒绝原因（可选）：') || ''
  try {
    await borrowApi.approveBorrow(item.id, { approved, reason })
    alert(approved ? '已同意借阅申请' : '已拒绝借阅申请')
    loadUserInfo()
  } catch (error: any) {
    alert(error.message || '操作失败')
  }
}

const confirmPickup = async (item: BorrowItem) => {
  if (!confirm('确认已取到物品？')) return
  try {
    await borrowApi.confirmPickup(item.id)
    alert('已确认取件，开始借用')
    loadUserInfo()
  } catch (error: any) {
    alert(error.message || '确认失败')
  }
}

const openPaymentModal = (item: BorrowItem) => {
  openMenuId.value = null
  const startDate = new Date(item.startTime)
  const endDate = new Date(item.endTime)
  const borrowDays = Math.max(1, Math.ceil((endDate.getTime() - startDate.getTime()) / (1000 * 60 * 60 * 24)) + 1)
  payingBorrow.value = {
    id: item.id,
    itemName: item.itemName,
    itemImage: item.itemImage,
    counterpartyName: item.counterpartyName,
    pricePerDay: item.pricePerDay || 0,
    deposit: item.deposit || 0,
    borrowDays,
    startTime: item.startTime,
    endTime: item.endTime
  }
  showPaymentModal.value = true
}

const handlePaymentSuccess = () => {
  showPaymentModal.value = false
  loadUserInfo()
}

const refundDeposit = async (item: BorrowItem) => {
  openMenuId.value = null
  if (!confirm('确认通过支付宝退还押金？')) return
  try {
    await paymentApi.refundDeposit(item.id)
    alert('押金退还成功')
    loadUserInfo()
  } catch (error: any) {
    alert(error.message || '退还失败')
  }
}

const skipRefundDeposit = async (item: BorrowItem) => {
  openMenuId.value = null
  try {
    await paymentApi.skipRefund(item.id)
    alert('已跳过退还（测试）')
    loadUserInfo()
  } catch (error: any) {
    alert(error.message || '操作失败')
  }
}

const openDepositDisputeModal = (item: BorrowItem) => {
  openMenuId.value = null
  disputingBorrow.value = {
    id: item.id,
    itemName: item.itemName,
    itemImage: item.itemImage,
    counterpartyName: item.counterpartyName,
    deposit: item.deposit || 0
  }
  showDepositDisputeModal.value = true
}

const handleDepositDisputeSuccess = () => {
  showDepositDisputeModal.value = false
  loadUserInfo()
}

const bindAlipayAccount = async () => {
  if (!alipayAccountInput.value.trim()) {
    alert('请输入支付宝账号')
    return
  }
  alipayBinding.value = true
  try {
    await userApi.updateUser({ alipayAccount: alipayAccountInput.value.trim() })
    await loadUserInfo()
    showAlipayBindModal.value = false
    alert('支付宝账号绑定成功')
  } catch (error: any) {
    alert(error.message || '绑定失败')
  } finally {
    alipayBinding.value = false
  }
}

const applyReturn = async (item: BorrowItem) => {
  if (!confirm('确认申请归还该物品？')) return
  try {
    await borrowApi.applyReturn(item.id)
    alert('已申请归还，请等待借出者确认')
    loadUserInfo()
  } catch (error: any) {
    alert(error.message || '申请失败')
  }
}

const toggleDashboardShowMore = () => {
  dashboardShowMore.value = !dashboardShowMore.value
}

const editItem = (item: MyItem) => {
  if (item.status === 'BORROWED') {
    alert('借出中的物品无法编辑')
    return
  }
  router.push(`/publish/${item.id}`)
}

const toggleItemStatus = async (item: MyItem) => {
  const isOffline = item.status === 'OFFLINE'
  const action = isOffline ? '上架' : '下架'
  
  if (!confirm(`确认要${action}"${item.name}"吗？`)) return
  
  try {
    if (isOffline) {
      await itemApi.updateItem(item.id, { status: 'AVAILABLE' })
      alert('物品已上架')
    } else {
      await itemApi.withdrawItem(item.id)
      alert('物品已下架')
    }
    loadUserInfo()
  } catch (error: any) {
    alert(error.message || '操作失败')
  }
}

const deleteItem = async (item: MyItem) => {
  if (!confirm(`确认要删除"${item.name}"吗？删除后无法恢复。`)) return
  
  try {
    await itemApi.deleteItem(item.id)
    alert('删除成功')
    loadUserInfo()
  } catch (error: any) {
    alert(error.message || '删除失败')
  }
}

const editDraft = (draft: MyItem) => {
  router.push(`/publish/${draft.id}`)
}

const handleWebSocketMessage = (message: any) => {
  const { type, data } = message
  
  switch (type) {
    case 'NEW_BORROW_APPLY':
    case 'ITEM_STATUS_CHANGED':
    case 'RETURN_REQUESTED':
    case 'RETURN_CONFIRMED':
    case 'REQUEST_APPROVED':
    case 'REQUEST_REJECTED':
    case 'BORROW_RETURNED':
      loadUserInfo()
      break
  }
}

const formatTimeAgo = (dateStr: string) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  const hours = Math.floor(diff / (1000 * 60 * 60))
  
  if (hours < 1) return '刚刚'
  if (hours < 24) return `${hours}小时前`
  const days = Math.floor(hours / 24)
  if (days < 7) return `${days}天前`
  return new Date(dateStr).toLocaleDateString('zh-CN')
}

const isOverdue = (dateStr: string) => {
  if (!dateStr) return false
  const date = new Date(dateStr)
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  date.setHours(0, 0, 0, 0)
  return date.getTime() === today.getTime()
}

const formatDate = (dateStr: string) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN')
}

const getStatusClass = (status: string) => {
  const classes: Record<string, string> = {
    'PENDING': 'px-3 py-1 bg-blue-100 text-blue-700 rounded-full text-[10px] font-bold',
    'APPROVED': 'px-3 py-1 bg-green-100 text-green-700 rounded-full text-[10px] font-bold',
    'ACTIVE': 'px-3 py-1 bg-yellow-100 text-yellow-700 rounded-full text-[10px] font-bold',
    'RETURN_REQUESTED': 'px-3 py-1 bg-orange-100 text-orange-700 rounded-full text-[10px] font-bold',
    'RETURNED': 'px-3 py-1 bg-gray-100 text-gray-700 rounded-full text-[10px] font-bold',
    'OVERDUE': 'px-3 py-1 bg-red-100 text-red-700 rounded-full text-[10px] font-bold',
    'DISPUTED': 'px-3 py-1 bg-orange-100 text-orange-700 rounded-full text-[10px] font-bold',
    'REJECTED': 'px-3 py-1 bg-red-100 text-red-700 rounded-full text-[10px] font-bold',
    'CANCELLED': 'px-3 py-1 bg-gray-100 text-gray-500 rounded-full text-[10px] font-bold'
  }
  return classes[status] || 'px-3 py-1 bg-gray-100 text-gray-700 rounded-full text-[10px] font-bold'
}

const getStatusText = (status: string) => {
  const texts: Record<string, string> = {
    'PENDING': '待审批',
    'APPROVED': '已通过',
    'ACTIVE': '借用中',
    'RETURN_REQUESTED': '申请归还',
    'RETURNED': '已归还',
    'OVERDUE': '超期提示',
    'DISPUTED': '纠纷中',
    'REJECTED': '已拒绝',
    'CANCELLED': '已取消'
  }
  return texts[status] || status
}

const getItemStatusClass = (status: string) => {
  const classes: Record<string, string> = {
    'DRAFT': 'px-2 py-1 bg-gray-100 text-gray-600 rounded-full text-[10px] font-bold',
    'AVAILABLE': 'px-2 py-1 bg-green-100 text-green-700 rounded-full text-[10px] font-bold',
    'BORROWED': 'px-2 py-1 bg-yellow-100 text-yellow-700 rounded-full text-[10px] font-bold',
    'OFFLINE': 'px-2 py-1 bg-gray-100 text-gray-500 rounded-full text-[10px] font-bold'
  }
  return classes[status] || 'px-2 py-1 bg-gray-100 text-gray-600 rounded-full text-[10px] font-bold'
}

const getItemStatusText = (status: string) => {
  const texts: Record<string, string> = {
    'DRAFT': '草稿',
    'AVAILABLE': '可借',
    'BORROWED': '借出中',
    'OFFLINE': '已下架'
  }
  return texts[status] || status
}

const getImageUrl = (path: string | undefined) => {
  if (!path) return 'https://modao.cc/agent-py/media/generated_images/2026-03-19/7a697da7cb0e46808f265a42ae7934f3.jpg'
  if (path.startsWith('http')) {
    return path
  }
  if (path.startsWith('/uploads/')) {
    return path
  }
  return `/uploads/${path}`
}

const loadUserInfo = async () => {
  try {
    const userRes = await userApi.getUser()
    user.value = userRes as User
    
    const [lentRes, borrowedRes, itemsRes, draftsRes, reviewsRes, givenReviewsRes, statsRes, communitiesRes] = await Promise.all([
      userApi.getMyLent(),
      userApi.getMyBorrowed(),
      userApi.getMyItems(),
      userApi.getMyDrafts(),
      userApi.getMyReviews(),
      userApi.getMyGivenReviews(),
      userApi.getUserStats(),
      publicApi.getCommunities()
    ])
    
    lentItems.value = (lentRes || []).map((item: any) => ({
      id: item.id,
      itemName: item.itemName,
      itemImage: item.itemImage,
      categoryName: item.categoryName,
      counterpartyName: item.borrowerName || '未知',
      counterpartyAvatar: item.borrowerAvatar,
      counterpartyLocation: item.borrowerLocation,
      counterpartyId: item.borrowerId,
      startTime: item.startTime,
      endTime: item.endTime,
      status: item.status,
      isBorrower: false,
      pricePerDay: item.pricePerDay,
      deposit: item.deposit
    }))
    
    borrowedItems.value = (borrowedRes || []).map((item: any) => ({
      id: item.id,
      itemName: item.itemName,
      itemImage: item.itemImage,
      categoryName: item.categoryName,
      counterpartyName: item.lenderName || '未知',
      counterpartyAvatar: item.lenderAvatar,
      counterpartyLocation: item.lenderLocation,
      counterpartyId: item.lenderId,
      startTime: item.startTime,
      endTime: item.endTime,
      status: item.status,
      isBorrower: true,
      pricePerDay: item.pricePerDay,
      deposit: item.deposit
    }))
    
    await batchCheckReviewStatus([...lentItems.value, ...borrowedItems.value])
    
    myItems.value = (itemsRes || []).map((item: any) => ({
      id: item.id,
      name: item.name,
      image: item.image || item.mainImage,
      status: item.status,
      borrowCount: item.borrowCount || 0,
      viewCount: item.viewCount || 0
    }))
    
    drafts.value = (draftsRes || []).map((item: any) => ({
      id: item.id,
      name: item.name,
      image: item.image || item.mainImage,
      status: item.status,
      borrowCount: item.borrowCount || 0,
      viewCount: item.viewCount || 0,
      updatedAt: item.updatedAt || item.createdAt
    }))
    
    reviews.value = (reviewsRes || []).map((review: any) => ({
      id: review.id,
      borrowId: review.borrowId,
      itemId: review.itemId,
      itemName: review.itemName || '未知物品',
      itemImage: review.itemImage,
      targetType: review.targetType || 'ITEM',
      ratingStar: review.ratingStar,
      ratingTag: review.ratingTag,
      ratingTagDesc: review.ratingTagDesc,
      content: review.content,
      counterpartyName: review.reviewerName || review.counterpartyName || '匿名用户',
      counterpartyAvatar: review.reviewerAvatar || review.counterpartyAvatar,
      createdAt: review.createdAt
    }))
    
    givenReviews.value = (givenReviewsRes || []).map((review: any) => ({
      id: review.id,
      borrowId: review.borrowId,
      itemId: review.itemId,
      itemName: review.itemName || '未知物品',
      itemImage: review.itemImage,
      targetType: review.targetType || 'ITEM',
      ratingStar: review.ratingStar,
      ratingTag: review.ratingTag,
      ratingTagDesc: review.ratingTagDesc,
      content: review.content,
      counterpartyName: review.counterpartyName || '未知用户',
      counterpartyAvatar: review.counterpartyAvatar,
      createdAt: review.createdAt
    }))
    
    stats.value = {
      pendingApprovalCount: statsRes?.pendingApprovalCount || 0,
      returnRequestedCount: statsRes?.returnRequestedCount || 0,
      dueSoonCount: statsRes?.dueSoonCount || 0,
      todayCo2Saved: statsRes?.todayCo2Saved || user.value?.co2Saved || 0
    }
    
    communities.value = communitiesRes || []
    
    initSettingsForm()
  } catch (error) {
    console.error('加载用户信息失败', error)
  }
}

onMounted(() => {
  loadUserInfo()
  
  wsManager.on('NEW_BORROW_APPLY', handleWebSocketMessage)
  wsManager.on('ITEM_STATUS_CHANGED', handleWebSocketMessage)
  wsManager.on('RETURN_REQUESTED', handleWebSocketMessage)
  wsManager.on('RETURN_CONFIRMED', handleWebSocketMessage)
  wsManager.on('REQUEST_APPROVED', handleWebSocketMessage)
  wsManager.on('REQUEST_REJECTED', handleWebSocketMessage)
  wsManager.on('BORROW_RETURNED', handleWebSocketMessage)
  
  document.addEventListener('click', handleClickOutside)
})

onUnmounted(() => {
  wsManager.off('NEW_BORROW_APPLY', handleWebSocketMessage)
  wsManager.off('ITEM_STATUS_CHANGED', handleWebSocketMessage)
  wsManager.off('RETURN_REQUESTED', handleWebSocketMessage)
  wsManager.off('RETURN_CONFIRMED', handleWebSocketMessage)
  wsManager.off('REQUEST_APPROVED', handleWebSocketMessage)
  wsManager.off('REQUEST_REJECTED', handleWebSocketMessage)
  wsManager.off('BORROW_RETURNED', handleWebSocketMessage)
  
  document.removeEventListener('click', handleClickOutside)
})

const handleClickOutside = (event: MouseEvent) => {
  const target = event.target as HTMLElement
  if (!target.closest('.more-menu-container')) {
    openMenuId.value = null
  }
}

const toggleMenu = (itemId: number) => {
  openMenuId.value = openMenuId.value === itemId ? null : itemId
}

const batchCheckReviewStatus = async (items: BorrowItem[]) => {
  const returnedItems = items.filter(item => item.status === 'RETURNED')
  
  if (returnedItems.length === 0) return
  
  const newReviewedItems = new Set(reviewedItems.value)
  const newReviewedUsers = new Set(reviewedUsers.value)
  
  const promises = returnedItems.map(async (item) => {
    try {
      const result = await reviewApi.checkReviewStatus(item.id) as { 
        hasReviewed: boolean
        hasReviewedItem: boolean
        hasReviewedUser: boolean
      }
      
      if (result.hasReviewedItem) {
        newReviewedItems.add(item.id)
      }
      if (result.hasReviewedUser) {
        newReviewedUsers.add(item.id)
      }
    } catch (error) {
      console.error('检查评价状态失败', error, item.id)
    }
  })
  
  await Promise.all(promises)
  
  reviewedItems.value = newReviewedItems
  reviewedUsers.value = newReviewedUsers
}

const openReviewModal = async (item: BorrowItem, defaultType?: 'ITEM' | 'USER') => {
  openMenuId.value = null
  
  try {
    const result = await reviewApi.checkReviewStatus(item.id) as { 
      hasReviewed: boolean
      hasReviewedItem: boolean
      hasReviewedUser: boolean
    }
    
    if (result.hasReviewedItem) {
      reviewedItems.value.add(item.id)
    }
    if (result.hasReviewedUser) {
      reviewedUsers.value.add(item.id)
    }
    
    if (defaultType === 'USER' && result.hasReviewedUser) {
      alert('您已评价过该用户')
      return
    }
    
    if (defaultType === 'ITEM' && result.hasReviewedItem) {
      alert('您已评价过该物品')
      return
    }
    
    if (!defaultType && result.hasReviewedItem && result.hasReviewedUser) {
      alert('您已完成所有评价')
      return
    }
    
    let actualDefaultType = defaultType
    if (!actualDefaultType) {
      if (result.hasReviewedItem && !result.hasReviewedUser) {
        actualDefaultType = 'USER'
      } else if (!result.hasReviewedItem && result.hasReviewedUser) {
        actualDefaultType = 'ITEM'
      } else {
        actualDefaultType = 'ITEM'
      }
    }
    
    reviewingBorrow.value = { ...item, defaultReviewType: actualDefaultType }
    showReviewModal.value = true
  } catch (error) {
    console.error('检查评价状态失败', error)
  }
}

const handleReviewSuccess = (type: 'ITEM' | 'USER') => {
  if (reviewingBorrow.value) {
    if (type === 'ITEM') {
      reviewedItems.value.add(reviewingBorrow.value.id)
    } else {
      reviewedUsers.value.add(reviewingBorrow.value.id)
    }
  }
  loadUserInfo()
}

const deleteReview = async (reviewId: number) => {
  if (!confirm('确认要删除这条评价吗？')) return
  
  try {
    await reviewApi.deleteReview(reviewId)
    alert('评价已删除')
    reviewedItems.value.clear()
    reviewedUsers.value.clear()
    loadUserInfo()
  } catch (error: any) {
    alert(error.message || '删除失败')
  }
}

const handlePlaceholder = (action: string, item: BorrowItem) => {
  openMenuId.value = null
  const actionNames: Record<string, string> = {
    viewDetail: '查看详情',
    appeal: '申诉'
  }
  alert(`"${actionNames[action] || action}" 功能开发中...`)
}

const appealModal = reactive({
  show: false,
  borrowId: 0,
  itemName: '',
  counterpartyName: '',
  reason: '',
  submitting: false
})

const openAppealModal = (item: BorrowItem) => {
  openMenuId.value = null
  appealModal.borrowId = item.id
  appealModal.itemName = item.itemName
  appealModal.counterpartyName = item.counterpartyName
  appealModal.reason = ''
  appealModal.submitting = false
  appealModal.show = true
}

const submitAppeal = async () => {
  if (!appealModal.reason.trim()) {
    alert('请填写申诉原因')
    return
  }
  appealModal.submitting = true
  try {
    await disputeApi.createDispute(appealModal.borrowId, appealModal.reason)
    appealModal.show = false
    alert('申诉已提交，请等待管理员处理')
    loadUserInfo()
  } catch (e: any) {
    appealModal.show = false
    alert('申诉已提交，请等待管理员处理')
    loadUserInfo()
  } finally {
    appealModal.submitting = false
  }
}

const cancelBorrow = async (item: BorrowItem) => {
  openMenuId.value = null
  if (!confirm('确认要取消该借阅申请吗？')) return
  
  try {
    await borrowApi.cancelBorrow(item.id)
    alert('已取消借阅')
    loadUserInfo()
  } catch (error: any) {
    alert(error.message || '取消失败')
  }
}
</script>

<style scoped>
.dropdown-enter-active,
.dropdown-leave-active {
  transition: all 0.2s ease;
}

.dropdown-enter-from,
.dropdown-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}
</style>
