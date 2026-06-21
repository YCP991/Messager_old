<template>
  <div class="contacts-container">
    <n-card title="通讯录" :bordered="false" class="contacts-card">
      <template #header-extra>
        <n-space>
          <n-input 
            v-model:value="searchKeyword" 
            placeholder="搜索好友或用户" 
            clearable
            style="width: 220px"
            :loading="searching"
            @keyup.enter="handleSearch"
          >
            <template #prefix>
              <n-icon :component="SearchOutline" />
            </template>
            <template #suffix>
              <n-icon v-if="searchKeyword" :component="CloseOutline" class="clear-icon" @click="clearSearch" />
            </template>
          </n-input>
          <n-button type="primary" @click="showAddFriendModal = true">
            <template #icon>
              <n-icon :component="PersonAddOutline" />
            </template>
            添加好友
          </n-button>
        </n-space>
      </template>

      <!-- 好友分类标签 -->
      <n-tabs v-model:value="activeTab" type="line">
        <n-tab name="friends">
          好友列表
          <n-badge :value="friends.length" :max="99" />
        </n-tab>
        <n-tab name="requests">
          好友请求
          <n-badge v-if="pendingRequestCount > 0" :value="pendingRequestCount" :max="99" type="warning" />
        </n-tab>
        <n-tab name="search">搜索用户</n-tab>
      </n-tabs>

      <!-- 好友请求列表 -->
      <div v-if="activeTab === 'requests'" class="friend-list">
        <n-scrollbar style="max-height: calc(100vh - 300px)">
          <div v-if="friendRequests.length === 0" class="empty-state">
            <n-empty description="暂无好友请求" />
          </div>
          <div v-else class="friend-items">
            <div 
              v-for="request in friendRequests" 
              :key="request.id"
              class="friend-item request-item"
            >
              <n-badge :dot="request.isOnline === 1" :color="onlineColor">
                <n-avatar :size="48" :src="request.fromAvatar || '/avatar/default.png'" round />
              </n-badge>
              <div class="friend-info">
                <div class="friend-name">
                  {{ request.fromRealName || request.fromUsername }}
                  <n-tag v-if="request.isOnline === 1" type="success" size="small" style="margin-left: 8px;">在线</n-tag>
                </div>
                <div class="friend-meta">
                  <n-text depth="3" style="font-size: 12px;">
                    {{ request.remark || '请求添加你为好友' }}
                  </n-text>
                </div>
                <div class="friend-meta">
                  <n-text depth="4" style="font-size: 11px;">
                    {{ formatTime(request.createTime) }}
                  </n-text>
                </div>
              </div>
              <div class="friend-actions">
                <n-space>
                  <n-button type="primary" size="small" @click="handleAcceptRequest(request)" :loading="requestHandlingId === request.id">
                    同意
                  </n-button>
                  <n-button size="small" @click="handleRejectRequest(request)" :loading="requestHandlingId === request.id">
                    拒绝
                  </n-button>
                </n-space>
              </div>
            </div>
          </div>
        </n-scrollbar>
      </div>

      <!-- 好友列表 -->
      <div v-if="activeTab === 'friends'" class="friend-list">
        <n-scrollbar style="max-height: calc(100vh - 300px)">
          <!-- 置顶好友 -->
          <div v-if="pinnedFriends.length > 0" class="friend-section">
            <div class="section-title">
              <n-icon :component="PinOutline" />
              置顶好友
              <n-badge :value="pinnedFriends.length" :max="9" />
            </div>
            <div class="friend-items">
              <div 
                v-for="friend in pinnedFriends" 
                :key="friend.friendId"
                class="friend-item"
                @click="showFriendDetail(friend)"
              >
                <n-badge :dot="friend.isOnline === 1" :color="onlineColor">
                  <n-avatar :size="48" :src="friend.avatar || '/avatar/default.png'" round />
                </n-badge>
                <div class="friend-info">
                  <div class="friend-name">
                    {{ friend.remark || friend.realName || friend.username }}
                  </div>
                  <div class="friend-meta">
                    <n-text depth="3" style="font-size: 12px;">
                      {{ friend.isOnline === 1 ? '在线' : '离线' }}
                    </n-text>
                  </div>
                </div>
                <div class="friend-actions">
                  <n-dropdown 
                    :options="getFriendActions(friend)" 
                    @select="(key: string) => handleFriendAction(key, friend)"
                    trigger="click"
                  >
                    <n-button quaternary circle size="small" @click.stop>
                      <template #icon>
                        <n-icon :component="EllipsisHorizontal" />
                      </template>
                    </n-button>
                  </n-dropdown>
                </div>
              </div>
            </div>
          </div>

          <!-- 普通好友 -->
          <div class="friend-section">
            <div class="section-title">
              <n-icon :component="PeopleOutline" />
              其他好友
              <n-badge :value="unpinnedFriends.length" :max="99" />
            </div>
            <div v-if="unpinnedFriends.length === 0" class="empty-state">
              <n-empty description="暂无好友" />
              <n-button type="primary" text @click="showAddFriendModal = true">
                添加第一个好友
              </n-button>
            </div>
            <div v-else class="friend-items">
              <div 
                v-for="friend in unpinnedFriends" 
                :key="friend.friendId"
                class="friend-item"
                @click="showFriendDetail(friend)"
              >
                <n-badge :dot="friend.isOnline === 1" :color="onlineColor">
                  <n-avatar :size="48" :src="friend.avatar || '/avatar/default.png'" round />
                </n-badge>
                <div class="friend-info">
                  <div class="friend-name">
                    {{ friend.remark || friend.realName || friend.username }}
                  </div>
                  <div class="friend-meta">
                    <n-text depth="3" style="font-size: 12px;">
                      {{ friend.isOnline === 1 ? '在线' : '离线' }}
                    </n-text>
                  </div>
                </div>
                <div class="friend-actions">
                  <n-dropdown 
                    :options="getFriendActions(friend)" 
                    @select="(key: string) => handleFriendAction(key, friend)"
                    trigger="click"
                  >
                    <n-button quaternary circle size="small" @click.stop>
                      <template #icon>
                        <n-icon :component="EllipsisHorizontal" />
                      </template>
                    </n-button>
                  </n-dropdown>
                </div>
              </div>
            </div>
          </div>
        </n-scrollbar>
      </div>

      <!-- 搜索用户 -->
      <div v-else class="search-results">
        <n-scrollbar style="max-height: calc(100vh - 300px)">
          <div v-if="searchResults.length === 0 && hasSearched" class="empty-state">
            <n-empty description="未找到用户" />
          </div>
          <div v-else-if="searching" class="empty-state">
            <n-spin size="large" />
          </div>
          <div v-else-if="searchResults.length > 0" class="friend-items">
            <div 
              v-for="user in searchResults" 
              :key="user.friendId"
              class="friend-item"
              :class="{ selected: selectedSearchUser?.friendId === user.friendId }"
              @click="selectSearchUser(user)"
            >
              <n-badge :dot="user.isOnline === 1" :color="onlineColor">
                <n-avatar :size="48" :src="user.avatar || '/avatar/default.png'" round />
              </n-badge>
              <div class="friend-info">
                <div class="friend-name">
                  {{ user.realName || user.username }}
                  <n-tag v-if="user.isOnline === 1" type="success" size="small" style="margin-left: 8px;">在线</n-tag>
                </div>
                <div class="friend-meta">
                  <n-text depth="3" style="font-size: 12px;">
                    {{ user.username }} · {{ user.classNo || user.department || '暂无信息' }}
                  </n-text>
                </div>
              </div>
              <div class="friend-actions">
                <n-tag v-if="user.isFriend" type="success" size="small">已添加</n-tag>
                <n-button 
                  v-else 
                  type="primary" 
                  size="small" 
                  @click.stop="handleAddFriend(user)"
                >
                  添加
                </n-button>
              </div>
            </div>
          </div>
          <div v-else class="empty-state">
            <n-empty description="输入关键词搜索用户" />
          </div>
        </n-scrollbar>
      </div>
    </n-card>

    <!-- 添加好友弹窗 -->
    <n-modal v-model:show="showAddFriendModal" preset="card" title="添加好友" style="width: 550px;" :mask-closable="false">
      <n-form>
        <n-form-item label="搜索用户">
          <n-input 
            v-model:value="addFriendKeyword" 
            placeholder="输入用户名、学号或姓名搜索"
            :loading="addingSearching"
            @keyup.enter="handleSearchForAdd"
          >
            <template #prefix>
              <n-icon :component="SearchOutline" />
            </template>
            <template #suffix>
              <n-icon v-if="addFriendKeyword" :component="CloseOutline" class="clear-icon" @click="clearAddFriendSearch" />
            </template>
          </n-input>
        </n-form-item>
        
        <!-- 搜索结果 -->
        <div v-if="addFriendSearchResults.length > 0" class="search-result-list">
          <n-divider title="搜索结果" title-placement="left" />
          <div 
            v-for="user in addFriendSearchResults" 
            :key="user.friendId"
            class="friend-item"
            :class="{ selected: selectedUserForAdd?.friendId === user.friendId }"
            @click="selectUserForAdd(user)"
          >
            <n-badge :dot="user.isOnline === 1" :color="onlineColor">
              <n-avatar :size="40" :src="user.avatar || '/avatar/default.png'" round />
            </n-badge>
            <div class="friend-info">
              <div class="friend-name">
                {{ user.realName || user.username }}
                <n-tag v-if="user.isOnline === 1" type="success" size="small" style="margin-left: 8px;">在线</n-tag>
              </div>
              <div class="friend-meta">
                <n-text depth="3" style="font-size: 12px;">
                  {{ user.username }} · {{ user.classNo || user.department }}
                </n-text>
              </div>
            </div>
            <div class="friend-actions">
              <n-tag v-if="user.isFriend" type="success" size="small">已添加</n-tag>
              <n-button 
                v-else 
                type="primary" 
                size="small" 
                :disabled="user.isFriend"
                @click.stop="handleSendAddFriendRequest(user)"
              >
                添加
              </n-button>
            </div>
          </div>
        </div>

        <!-- 已选用户信息 -->
        <n-divider v-if="selectedUserForAdd" title="已选用户" title-placement="left" />
        <div v-if="selectedUserForAdd" class="selected-user-info">
          <n-space>
            <n-avatar :size="56" :src="selectedUserForAdd.avatar || '/avatar/default.png'" round />
            <div>
              <div class="friend-name">{{ selectedUserForAdd.realName || selectedUserForAdd.username }}</div>
              <div class="friend-meta">
                <n-text depth="3" style="font-size: 12px;">{{ selectedUserForAdd.username }}</n-text>
              </div>
            </div>
          </n-space>
        </div>
        
        <n-form-item label="备注名（可选）">
          <n-input v-model:value="addFriendRemark" placeholder="添加备注名（对方不会看到）" />
        </n-form-item>
      </n-form>
      
      <template #footer>
        <n-space justify="end">
          <n-button @click="closeAddFriendModal">取消</n-button>
          <n-button 
            type="primary" 
            :disabled="!selectedUserForAdd"
            :loading="sendingRequest"
            @click="confirmAddFriend"
          >
            发送好友请求
          </n-button>
        </n-space>
      </template>
    </n-modal>

    <!-- 好友详情弹窗 -->
    <n-modal v-model:show="showFriendDetailModal" preset="card" title="好友信息" style="width: 420px;" :mask-closable="false">
      <div class="friend-detail">
        <n-space vertical size="large">
          <n-space justify="center" style="width: 100%">
            <n-badge :dot="currentFriend?.isOnline === 1" :color="onlineColor" :offset="[10, 10]">
              <n-avatar :size="80" :src="currentFriend?.avatar || '/avatar/default.png'" round />
            </n-badge>
          </n-space>
          
          <n-descriptions :column="1" label-placement="left" bordered>
            <n-descriptions-item label="备注">
              {{ currentFriend?.remark || '-' }}
            </n-descriptions-item>
            <n-descriptions-item label="用户名">
              {{ currentFriend?.username }}
            </n-descriptions-item>
            <n-descriptions-item label="姓名">
              {{ currentFriend?.realName || '-' }}
            </n-descriptions-item>
            <n-descriptions-item label="班级">
              {{ currentFriend?.classNo || '-' }}
            </n-descriptions-item>
            <n-descriptions-item label="院系">
              {{ currentFriend?.department || '-' }}
            </n-descriptions-item>
            <n-descriptions-item label="状态">
              <n-tag :type="currentFriend?.isOnline === 1 ? 'success' : 'default'" size="small">
                {{ currentFriend?.isOnline === 1 ? '在线' : '离线' }}
              </n-tag>
            </n-descriptions-item>
          </n-descriptions>
        </n-space>
      </div>
      
      <template #footer>
        <n-space justify="center" :size="16">
          <n-button type="primary" @click="handleStartChat">
            <template #icon>
              <n-icon :component="ChatbubbleOutline" />
            </template>
            发起私聊
          </n-button>
          <n-button type="warning" @click="handleEditRemark">
            <template #icon>
              <n-icon :component="CreateOutline" />
            </template>
            编辑备注
          </n-button>
          <n-button type="error" @click="handleDeleteFriend">
            <template #icon>
              <n-icon :component="PersonRemoveOutline" />
            </template>
            删除好友
          </n-button>
        </n-space>
      </template>
    </n-modal>

    <!-- 编辑备注弹窗 -->
    <n-modal v-model:show="showEditRemarkModal" preset="card" title="编辑备注" style="width: 400px;" :mask-closable="false">
      <n-form>
        <n-form-item label="备注名">
          <n-input v-model:value="editRemark" placeholder="输入备注名" :maxlength="20" />
        </n-form-item>
      </n-form>
      <template #footer>
        <n-space justify="end">
          <n-button @click="showEditRemarkModal = false">取消</n-button>
          <n-button type="primary" :loading="updatingRemark" @click="confirmEditRemark">保存</n-button>
        </n-space>
      </template>
    </n-modal>

    <!-- 删除好友确认 -->
    <n-modal v-model:show="showDeleteConfirmModal" preset="card" title="删除好友" style="width: 400px;" :mask-closable="false">
      <n-alert type="warning" title="确认删除">
        确定要删除与 <strong>{{ currentFriend?.remark || currentFriend?.realName || currentFriend?.username }}</strong> 的好友关系吗？
        <br />
        <br />
        删除后将同时从对方的好友列表中移除您。此操作不可撤销。
      </n-alert>
      <template #footer>
        <n-space justify="end">
          <n-button @click="showDeleteConfirmModal = false">取消</n-button>
          <n-button type="error" :loading="deletingFriend" @click="confirmDeleteFriend">确认删除</n-button>
        </n-space>
      </template>
    </n-modal>

    <!-- 添加好友确认 -->
    <n-modal v-model:show="showConfirmAddModal" preset="card" title="发送好友请求" style="width: 420px;" :mask-closable="false">
      <n-space vertical size="medium">
        <div class="friend-item">
          <n-badge :dot="confirmTargetUser?.isOnline === 1" :color="onlineColor">
            <n-avatar :size="48" :src="confirmTargetUser?.avatar || '/avatar/default.png'" round />
          </n-badge>
          <div class="friend-info">
            <div class="friend-name">
              {{ confirmTargetUser?.realName || confirmTargetUser?.username }}
              <n-tag v-if="confirmTargetUser?.isOnline === 1" type="success" size="small" style="margin-left: 8px;">在线</n-tag>
            </div>
            <div class="friend-meta">
              <n-text depth="3" style="font-size: 12px;">
                {{ confirmTargetUser?.username }} · {{ confirmTargetUser?.classNo || confirmTargetUser?.department || '暂无信息' }}
              </n-text>
            </div>
          </div>
        </div>
        <n-alert type="info" style="margin-top: 16px;">
          确定要发送好友请求给 <strong>{{ confirmTargetUser?.realName || confirmTargetUser?.username }}</strong> 吗？
        </n-alert>
        <n-form-item label="请求备注（可选）">
          <n-input v-model:value="requestRemark" placeholder="添加请求备注（选填）" :maxlength="50" />
        </n-form-item>
      </n-space>
      <template #footer>
        <n-space justify="end">
          <n-button @click="showConfirmAddModal = false">取消</n-button>
          <n-button type="primary" :loading="sendingRequest" @click="confirmSendRequest">发送请求</n-button>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue';
import { useRouter } from 'vue-router';
import { useMessage } from 'naive-ui';
import { 
  SearchOutline, 
  PersonAddOutline, 
  PinOutline, 
  PeopleOutline,
  EllipsisHorizontal,
  ChatbubbleOutline,
  CreateOutline,
  PersonRemoveOutline,
  CloseOutline
} from '@vicons/ionicons5';
import { 
  getFriends, 
  searchUsers, 
  deleteFriend, 
  updateFriendRemark,
  updateFriendSort,
  checkFriend,
  sendFriendRequest,
  getReceivedRequests,
  handleFriendRequest,
  type FriendVO,
  type FriendRequestVO
} from '@/api/friend';

const router = useRouter();
const message = useMessage();

// 状态
const friends = ref<FriendVO[]>([]);
const searchKeyword = ref('');
const searchResults = ref<FriendVO[]>([]);
const hasSearched = ref(false);
const activeTab = ref('friends');
const onlineColor = '#18a058';

// 搜索状态
const searching = ref(false);
const selectedSearchUser = ref<FriendVO | null>(null);

// 添加好友相关
const showAddFriendModal = ref(false);
const addFriendKeyword = ref('');
const addFriendSearchResults = ref<FriendVO[]>([]);
const addFriendRemark = ref('');
const selectedUserForAdd = ref<FriendVO | null>(null);
const addingSearching = ref(false);
const sendingRequest = ref(false);
const requestRemark = ref('');

// 好友详情相关
const showFriendDetailModal = ref(false);
const currentFriend = ref<FriendVO | null>(null);
const showEditRemarkModal = ref(false);
const editRemark = ref('');
const showDeleteConfirmModal = ref(false);
const updatingRemark = ref(false);
const deletingFriend = ref(false);
const updatingPin = ref(false);

// 好友请求相关
const friendRequests = ref<FriendRequestVO[]>([]);
const pendingRequestCount = ref(0);
const requestHandlingId = ref<number | null>(null);

// 添加好友确认对话框
const showConfirmAddModal = ref(false);
const confirmTargetUser = ref<FriendVO | null>(null);

// 好友操作选项（根据好友状态动态生成）
function getFriendActions(friend: FriendVO) {
  const baseActions = [
    { label: '发起私聊', key: 'chat' },
    { label: '编辑备注', key: 'remark' }
  ];
  
  if (friend.isPinned === 1) {
    baseActions.push({ label: '取消置顶', key: 'unpin' });
  } else {
    baseActions.push({ label: '置顶', key: 'pin' });
  }
  
  baseActions.push({ label: '删除好友', key: 'delete' });
  return baseActions;
}

// 计算属性：置顶好友
const pinnedFriends = computed(() => {
  return friends.value.filter(f => f.isPinned === 1);
});

// 计算属性：普通好友
const unpinnedFriends = computed(() => {
  return friends.value.filter(f => f.isPinned !== 1);
});

// 加载好友列表
async function loadFriends() {
  try {
    friends.value = await getFriends();
  } catch (error) {
    console.error('加载好友列表失败:', error);
    message.error('加载好友列表失败');
  }
}

// 加载好友请求
async function loadFriendRequests() {
  try {
    friendRequests.value = await getReceivedRequests();
    pendingRequestCount.value = friendRequests.value.length;
  } catch (error) {
    console.error('加载好友请求失败:', error);
  }
}

// 搜索用户
async function handleSearch() {
  if (!searchKeyword.value.trim()) {
    message.warning('请输入搜索关键词');
    return;
  }
  
  searching.value = true;
  try {
    searchResults.value = await searchUsers(searchKeyword.value);
    hasSearched.value = true;
    
    // 检查哪些用户已经是好友
    for (const user of searchResults.value) {
      try {
        user.isFriend = await checkFriend(user.friendId);
      } catch {
        user.isFriend = false;
      }
    }
  } catch (error) {
    console.error('搜索失败:', error);
    message.error('搜索失败');
  } finally {
    searching.value = false;
  }
}

// 清除搜索
function clearSearch() {
  searchKeyword.value = '';
  searchResults.value = [];
  hasSearched.value = false;
  selectedSearchUser.value = null;
}

// 选择搜索结果中的用户
function selectSearchUser(user: FriendVO) {
  selectedSearchUser.value = user;
}

// 显示好友详情
function showFriendDetail(friend: FriendVO) {
  currentFriend.value = friend;
  editRemark.value = friend.remark || '';
  showFriendDetailModal.value = true;
}

// 处理好友操作
async function handleFriendAction(key: string, friend: FriendVO) {
  switch (key) {
    case 'chat':
      handleStartChatWithFriend(friend);
      break;
    case 'remark':
      currentFriend.value = friend;
      editRemark.value = friend.remark || '';
      showEditRemarkModal.value = true;
      break;
    case 'pin':
      await handleTogglePin(friend, 1);
      break;
    case 'unpin':
      await handleTogglePin(friend, 0);
      break;
    case 'delete':
      currentFriend.value = friend;
      showDeleteConfirmModal.value = true;
      break;
  }
}

// 发起私聊
function handleStartChatWithFriend(friend: FriendVO) {
  // 跳转到私聊页面，传递 friendId
  router.push({
    path: '/chat/private',
    query: { friendId: friend.friendId.toString() }
  });
}

// 直接从详情发起私聊
function handleStartChat() {
  if (currentFriend.value) {
    handleStartChatWithFriend(currentFriend.value);
    showFriendDetailModal.value = false;
  }
}

// 切换置顶状态
async function handleTogglePin(friend: FriendVO, isPinned: number) {
  updatingPin.value = true;
  try {
    // 调用后端接口更新置顶状态
    await updateFriendSort({
      friendId: friend.friendId,
      sortOrder: isPinned === 1 ? 1 : 0,
      isPinned: isPinned
    });
    message.success(isPinned === 1 ? '已置顶' : '已取消置顶');
    await loadFriends();
  } catch (error) {
    console.error('更新置顶状态失败:', error);
    message.error('操作失败');
  } finally {
    updatingPin.value = false;
  }
}

// 编辑备注
function handleEditRemark() {
  if (currentFriend.value) {
    editRemark.value = currentFriend.value.remark || '';
    showEditRemarkModal.value = true;
    showFriendDetailModal.value = false;
  }
}

// 确认编辑备注
async function confirmEditRemark() {
  if (!currentFriend.value) return;
  
  updatingRemark.value = true;
  try {
    await updateFriendRemark({
      friendId: currentFriend.value.friendId,
      remark: editRemark.value
    });
    message.success('备注已更新');
    showEditRemarkModal.value = false;
    await loadFriends();
    
    // 更新当前好友信息
    const updatedFriend = friends.value.find(
      f => f.friendId === currentFriend.value?.friendId
    );
    if (updatedFriend) {
      currentFriend.value = updatedFriend;
    }
  } catch (error) {
    console.error('更新备注失败:', error);
    message.error('更新备注失败');
  } finally {
    updatingRemark.value = false;
  }
}

// 删除好友确认
function handleDeleteFriend() {
  showDeleteConfirmModal.value = true;
  showFriendDetailModal.value = false;
}

// 确认删除好友
async function confirmDeleteFriend() {
  if (!currentFriend.value) return;
  
  deletingFriend.value = true;
  try {
    await deleteFriend(currentFriend.value.friendId);
    message.success('已删除好友');
    showDeleteConfirmModal.value = false;
    currentFriend.value = null;
    await loadFriends();
  } catch (error) {
    console.error('删除好友失败:', error);
    message.error('删除好友失败');
  } finally {
    deletingFriend.value = false;
  }
}

// 搜索添加好友
async function handleSearchForAdd() {
  if (!addFriendKeyword.value.trim()) {
    message.warning('请输入搜索关键词');
    return;
  }
  
  addingSearching.value = true;
  try {
    addFriendSearchResults.value = await searchUsers(addFriendKeyword.value);
    
    // 检查是否已经是好友
    for (const user of addFriendSearchResults.value) {
      try {
        user.isFriend = await checkFriend(user.friendId);
      } catch {
        user.isFriend = false;
      }
    }
  } catch (error) {
    console.error('搜索失败:', error);
    message.error('搜索失败');
  } finally {
    addingSearching.value = false;
  }
}

// 清除添加好友搜索
function clearAddFriendSearch() {
  addFriendKeyword.value = '';
  addFriendSearchResults.value = [];
}

// 选择要添加的用户
function selectUserForAdd(user: FriendVO) {
  if (!user.isFriend) {
    selectedUserForAdd.value = user;
  }
}

// 选择要添加的用户
function handleAddFriend(user: FriendVO) {
  if (user.isFriend) {
    message.info('已经是好友了');
    return;
  }
  selectedUserForAdd.value = user;
}

// 发送添加好友请求
function handleSendAddFriendRequest(user: FriendVO) {
  if (user.isFriend) {
    message.info('已经是好友了');
    return;
  }
  
  // 显示确认对话框
  showConfirmAddModal.value = true;
  confirmTargetUser.value = user;
  requestRemark.value = '';
}

// 确认发送好友请求
async function confirmSendRequest() {
  if (!confirmTargetUser.value) return;
  
  sendingRequest.value = true;
  try {
    await sendFriendRequest(confirmTargetUser.value.friendId, requestRemark.value);
    message.success('已发送好友请求，请等待对方确认');
    
    // 更新状态
    const user = addFriendSearchResults.value.find(
      u => u.friendId === confirmTargetUser.value?.friendId
    );
    if (user) {
      user.isFriend = true;
    }
    
    await loadFriendRequests();
  } catch (error) {
    console.error('发送好友请求失败:', error);
    message.error('发送好友请求失败');
  } finally {
    sendingRequest.value = false;
    showConfirmAddModal.value = false;
    confirmTargetUser.value = null;
    requestRemark.value = '';
  }
}

// 关闭添加好友弹窗
function closeAddFriendModal() {
  showAddFriendModal.value = false;
  addFriendKeyword.value = '';
  addFriendSearchResults.value = [];
  addFriendRemark.value = '';
  selectedUserForAdd.value = null;
}

// 确认添加好友
async function confirmAddFriend() {
  if (!selectedUserForAdd.value) {
    message.warning('请先选择一个用户');
    return;
  }
  
  sendingRequest.value = true;
  try {
    await sendFriendRequest(selectedUserForAdd.value.friendId, addFriendRemark.value);
    message.success('已发送好友请求，请等待对方确认');
    closeAddFriendModal();
    await loadFriendRequests();
  } catch (error) {
    console.error('发送好友请求失败:', error);
    message.error('发送好友请求失败');
  } finally {
    sendingRequest.value = false;
  }
}

// 处理好友请求（同意）
async function handleAcceptRequest(request: FriendRequestVO) {
  requestHandlingId.value = request.id;
  try {
    await handleFriendRequest(request.id, true);
    message.success('已同意好友请求');
    await loadFriendRequests();
    await loadFriends();
  } catch (error) {
    console.error('处理好友请求失败:', error);
    message.error('处理好友请求失败');
  } finally {
    requestHandlingId.value = null;
  }
}

// 处理好友请求（拒绝）
async function handleRejectRequest(request: FriendRequestVO) {
  requestHandlingId.value = request.id;
  try {
    await handleFriendRequest(request.id, false);
    message.info('已拒绝好友请求');
    await loadFriendRequests();
  } catch (error) {
    console.error('处理好友请求失败:', error);
    message.error('处理好友请求失败');
  } finally {
    requestHandlingId.value = null;
  }
}

// 格式化时间
function formatTime(timeString?: string) {
  if (!timeString) return '';
  
  try {
    const date = new Date(timeString);
    const now = new Date();
    const diff = now.getTime() - date.getTime();
    
    const minutes = Math.floor(diff / (1000 * 60));
    const hours = Math.floor(diff / (1000 * 60 * 60));
    const days = Math.floor(diff / (1000 * 60 * 60 * 24));
    
    if (minutes < 1) return '刚刚';
    if (minutes < 60) return `${minutes}分钟前`;
    if (hours < 24) return `${hours}小时前`;
    if (days < 7) return `${days}天前`;
    
    return `${date.getMonth() + 1}月${date.getDate()}日`;
  } catch {
    return timeString;
  }
}

// 监听搜索关键词变化，支持实时搜索
watch(searchKeyword, (newKeyword) => {
  if (newKeyword.trim()) {
    // 延迟搜索，避免频繁请求
    const timer = setTimeout(async () => {
      searching.value = true;
      try {
        searchResults.value = await searchUsers(newKeyword.trim());
        hasSearched.value = true;
        
        for (const user of searchResults.value) {
          try {
            user.isFriend = await checkFriend(user.friendId);
          } catch {
            user.isFriend = false;
          }
        }
      } catch (error) {
        console.error('实时搜索失败:', error);
      } finally {
        searching.value = false;
      }
    }, 300);
    
    return () => clearTimeout(timer);
  } else {
    searchResults.value = [];
    hasSearched.value = false;
    selectedSearchUser.value = null;
  }
});

// 页面加载时获取好友列表和好友请求
onMounted(async () => {
  await loadFriends();
  await loadFriendRequests();
});
</script>

<style scoped>
.contacts-container {
  height: 100vh;
  padding: 16px;
  background: #f5f5f5;
}

.contacts-card {
  height: 100%;
}

.friend-list,
.search-results {
  margin-top: 16px;
}

.friend-section {
  margin-bottom: 24px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
  font-size: 14px;
  font-weight: 500;
  color: #666;
}

.friend-items {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.friend-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: #fff;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
}

.friend-item:hover {
  background: #f0f0f0;
}

.friend-info {
  flex: 1;
  min-width: 0;
}

.friend-name {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.friend-meta {
  margin-top: 4px;
}

.friend-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.empty-state {
  padding: 40px 0;
  text-align: center;
}

.friend-detail {
  padding: 20px 0;
}

.search-result-list {
  margin: 16px 0;
  display: flex;
  flex-direction: column;
  gap: 8px;
}
</style>
