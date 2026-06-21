<template>
  <n-drawer v-model:show="showPanel" :width="400" placement="right">
    <n-drawer-content :title="groupName" closable>
      <template #header>
        <n-space vertical size="small">
          <n-text strong style="font-size: 18px">{{ groupName }}</n-text>
          <n-tag :type="getGroupTypeColor(groupInfo?.groupType)" size="small">
            {{ getGroupTypeName(groupInfo?.groupType) }}
          </n-tag>
        </n-space>
      </template>
      
      <n-tabs type="line" animated>
        <!-- 群信息 -->
        <n-tab-pane name="info" tab="群信息">
          <n-scrollbar style="max-height: calc(100vh - 200px)">
            <n-space vertical size="large">
              <!-- 群头像 -->
              <n-space justify="center" style="width: 100%">
                <n-avatar :size="80" :src="groupInfo?.groupAvatar || '/avatar/default.png'" round />
              </n-space>
              
              <!-- 群描述 -->
              <n-form-item v-if="groupInfo?.description" label="群描述">
                <n-text depth="3">{{ groupInfo.description }}</n-text>
              </n-form-item>
              
              <!-- 群公告 -->
              <n-form-item label="群公告">
                <n-input
                  v-model:value="announcement"
                  type="textarea"
                  placeholder="暂无群公告"
                  :rows="3"
                  :disabled="!canEditAnnouncement"
                  @blur="handleUpdateAnnouncement"
                />
              </n-form-item>
              
              <n-divider />
              
              <!-- 群成员数 -->
              <n-space justify="space-between" align="center">
                <n-text depth="3">群成员</n-text>
                <n-text>{{ members.length }} 人</n-text>
              </n-space>
              
              <!-- 查看全部成员 -->
              <n-button block @click="showAllMembers = true">
                <template #icon>
                  <n-icon :component="PeopleOutline" />
                </template>
                查看全部成员
              </n-button>
            </n-space>
          </n-scrollbar>
        </n-tab-pane>
        
        <!-- 群成员管理 -->
        <n-tab-pane name="members" tab="成员管理">
          <n-scrollbar style="max-height: calc(100vh - 200px)">
            <n-list hoverable clickable>
              <n-list-item 
                v-for="member in members" 
                :key="member.userId"
              >
                <n-thing>
                  <template #header>
                    <n-space>
                      <n-avatar :size="40" :src="member.avatar || '/avatar/default.png'" round />
                      <div>
                        <n-text>{{ member.groupNickname || member.realName || member.username }}</n-text>
                        <n-tag v-if="member.role === 2" type="warning" size="small" style="margin-left: 8px;">群主</n-tag>
                        <n-tag v-if="member.role === 1" type="info" size="small" style="margin-left: 8px;">管理员</n-tag>
                      </div>
                    </n-space>
                  </template>
                </n-thing>
                
                <template #suffix>
                  <n-space v-if="canManageMember(member)">
                    <n-dropdown 
                      :options="getMemberOptions(member)" 
                      @select="(key: string) => handleMemberAction(key, member)"
                    >
                      <n-button quaternary circle size="small">
                        <template #icon>
                          <n-icon :component="EllipsisHorizontal" />
                        </template>
                      </n-button>
                    </n-dropdown>
                  </n-space>
                </template>
              </n-list-item>
            </n-list>
          </n-scrollbar>
        </n-tab-pane>
        
        <!-- 群设置 (仅群主) -->
        <n-tab-pane v-if="isOwner" name="settings" tab="群设置">
          <n-space vertical size="large">
            <n-divider>危险操作</n-divider>
            
            <n-space vertical size="small">
              <n-button block type="warning" @click="showTransferModal = true">
                转让群主
              </n-button>
              <n-button block type="error" @click="showDisbandModal = true">
                解散群聊
              </n-button>
            </n-space>
          </n-space>
        </n-tab-pane>
      </n-tabs>
      
      <template #footer>
        <n-space>
          <n-button block @click="handleQuitGroup">
            {{ isOwner ? '解散群聊' : '退出群聊' }}
          </n-button>
        </n-space>
      </template>
    </n-drawer-content>
  </n-drawer>
  
  <!-- 转让群主弹窗 -->
  <n-modal v-model:show="showTransferModal" preset="card" title="转让群主" style="width: 400px;">
    <n-alert type="warning" style="margin-bottom: 16px;">
      转让群主后，你将失去群主身份，成为普通成员。
    </n-alert>
    <n-select
      v-model:value="selectedNewOwner"
      :options="memberOptions"
      placeholder="选择新群主"
      filterable
    />
    <template #footer>
      <n-space justify="end">
        <n-button @click="showTransferModal = false">取消</n-button>
        <n-button type="warning" :loading="actionLoading" @click="handleTransfer">确认转让</n-button>
      </n-space>
    </template>
  </n-modal>
  
  <!-- 解散群聊确认弹窗 -->
  <n-modal v-model:show="showDisbandModal" preset="card" title="解散群聊" style="width: 400px;">
    <n-alert type="error">
      确定要解散该群聊吗？此操作不可恢复，所有聊天记录将被删除。
    </n-alert>
    <template #footer>
      <n-space justify="end">
        <n-button @click="showDisbandModal = false">取消</n-button>
        <n-button type="error" :loading="actionLoading" @click="handleDisband">确认解散</n-button>
      </n-space>
    </template>
  </n-modal>
  
  <!-- 全部成员弹窗 -->
  <n-modal v-model:show="showAllMembers" preset="card" title="全部成员" style="width: 500px;">
    <n-scrollbar style="max-height: 400px;">
      <n-list>
        <n-list-item v-for="member in members" :key="member.userId">
          <n-space>
            <n-avatar :size="40" :src="member.avatar || '/avatar/default.png'" round />
            <div>
              <n-text>{{ member.groupNickname || member.realName || member.username }}</n-text>
              <n-tag v-if="member.role === 2" type="warning" size="small" style="margin-left: 8px;">群主</n-tag>
              <n-tag v-if="member.role === 1" type="info" size="small" style="margin-left: 8px;">管理员</n-tag>
            </div>
          </n-space>
        </n-list-item>
      </n-list>
    </n-scrollbar>
  </n-modal>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue';
import { useMessage, useDialog } from 'naive-ui';
import { PeopleOutline, EllipsisHorizontal } from '@vicons/ionicons5';
import { useUserStore } from '@/stores/user';
import { 
  getGroupMembers, 
  updateAnnouncement, 
  kickMember, 
  disbandGroup, 
  transferGroupOwner,
  muteMember,
  unmuteMember,
  type GroupMemberVO 
} from '@/api/group';

const props = defineProps<{
  show: boolean;
  groupInfo: any;
  groupId: number;
}>();

const emit = defineEmits<{
  (e: 'update:show', value: boolean): void;
  (e: 'quit'): void;
}>();

const message = useMessage();
const dialog = useDialog();
const userStore = useUserStore();

const showPanel = ref(props.show);
const members = ref<GroupMemberVO[]>([]);
const announcement = ref('');
const actionLoading = ref(false);

// 弹窗状态
const showTransferModal = ref(false);
const showDisbandModal = ref(false);
const showAllMembers = ref(false);
const selectedNewOwner = ref<number | null>(null);

// 计算属性
const groupName = computed(() => props.groupInfo?.groupName || '群聊设置');
const isOwner = computed(() => props.groupInfo?.myRole === 2); // myRole 2 表示群主
const isAdmin = computed(() => props.groupInfo?.myRole === 1 || isOwner.value);
const canEditAnnouncement = computed(() => isAdmin.value);

const memberOptions = computed(() => {
  return members.value
    .filter(m => m.userId !== userStore.userId)
    .map(m => ({
      label: m.groupNickname || m.realName || m.username,
      value: m.userId
    }));
});

// 监听 props.show 变化
watch(() => props.show, async (val) => {
  showPanel.value = val;
  if (val) {
    await loadMembers();
  }
});

// 监听 showPanel 变化
watch(showPanel, (val) => {
  emit('update:show', val);
});

// 加载群成员
async function loadMembers() {
  try {
    members.value = await getGroupMembers(props.groupId);
  } catch (error) {
    console.error('加载群成员失败:', error);
    message.error('加载群成员失败');
  }
}

// 更新群公告
async function handleUpdateAnnouncement() {
  if (!canEditAnnouncement.value) return;
  try {
    await updateAnnouncement(props.groupId, announcement.value);
    message.success('群公告已更新');
  } catch (error) {
    console.error('更新群公告失败:', error);
    message.error('更新群公告失败');
  }
}

// 获取成员操作选项
function getMemberOptions(member: GroupMemberVO) {
  const options = [];
  
  // 禁言选项（管理员和群主可以操作，不能禁言自己）
  if (member.userId !== userStore.userId) {
    if (member.isMuted) {
      options.push({ label: '解除禁言', key: 'unmute' });
    } else {
      options.push({ label: '禁言30分钟', key: 'mute30' });
      options.push({ label: '禁言1小时', key: 'mute60' });
      options.push({ label: '永久禁言', key: 'mutePermanent' });
    }
  }
  
  if (isOwner.value && member.userId !== userStore.userId && member.role !== 2) {
    options.push({ label: '设为管理员', key: 'setAdmin' });
    options.push({ label: '移除出群', key: 'kick' });
  } else if (isAdmin.value && member.role !== 2 && member.userId !== userStore.userId) {
    options.push({ label: '移除出群', key: 'kick' });
  }
  
  return options;
}

// 判断是否可以管理某成员
function canManageMember(member: GroupMemberVO) {
  if (member.userId === userStore.userId) return false;
  if (isOwner.value) return true;
  if (isAdmin.value && member.role !== 2) return true;
  return false;
}

// 处理成员操作
async function handleMemberAction(key: string, member: GroupMemberVO) {
  switch (key) {
    case 'mute30':
      try {
        await muteMember(props.groupId, member.userId, 30);
        message.success(`已禁言 ${member.groupNickname || member.realName || member.username} 30分钟`);
        await loadMembers();
      } catch (error) {
        message.error('禁言失败');
      }
      break;
    case 'mute60':
      try {
        await muteMember(props.groupId, member.userId, 60);
        message.success(`已禁言 ${member.groupNickname || member.realName || member.username} 1小时`);
        await loadMembers();
      } catch (error) {
        message.error('禁言失败');
      }
      break;
    case 'mutePermanent':
      dialog.warning({
        title: '永久禁言',
        content: `确定要永久禁言 ${member.groupNickname || member.realName || member.username} 吗？`,
        positiveText: '确定',
        negativeText: '取消',
        onPositiveClick: async () => {
          try {
            await muteMember(props.groupId, member.userId, -1);
            message.success(`已永久禁言 ${member.groupNickname || member.realName || member.username}`);
            await loadMembers();
          } catch (error) {
            message.error('禁言失败');
          }
        }
      });
      break;
    case 'unmute':
      try {
        await unmuteMember(props.groupId, member.userId);
        message.success(`已解除禁言`);
        await loadMembers();
      } catch (error) {
        message.error('解除禁言失败');
      }
      break;
    case 'setAdmin':
      message.info('设置管理员功能开发中');
      break;
    case 'kick':
      dialog.warning({
        title: '移除成员',
        content: `确定要将 ${member.groupNickname || member.realName || member.username} 移出群聊吗？`,
        positiveText: '确定',
        negativeText: '取消',
        onPositiveClick: async () => {
          try {
            await kickMember(props.groupId, member.userId);
            message.success('已移出群聊');
            await loadMembers();
          } catch (error) {
            message.error('移除失败');
          }
        }
      });
      break;
  }
}

// 退出群聊
function handleQuitGroup() {
  if (isOwner.value) {
    showDisbandModal.value = true;
  } else {
    dialog.warning({
      title: '退出群聊',
      content: '确定要退出该群聊吗？',
      positiveText: '确定',
      negativeText: '取消',
      onPositiveClick: async () => {
        try {
          // TODO: 调用退出群聊接口
          message.success('已退出群聊');
          emit('quit');
          showPanel.value = false;
        } catch (error) {
          message.error('退出失败');
        }
      }
    });
  }
}

// 转让群主
async function handleTransfer() {
  if (!selectedNewOwner.value) {
    message.warning('请选择新群主');
    return;
  }
  
  actionLoading.value = true;
  try {
    await transferGroupOwner(props.groupId, selectedNewOwner.value);
    message.success('群主已转让');
    showTransferModal.value = false;
    emit('quit');
  } catch (error) {
    message.error('转让失败');
  } finally {
    actionLoading.value = false;
  }
}

// 解散群聊
async function handleDisband() {
  actionLoading.value = true;
  try {
    await disbandGroup(props.groupId);
    message.success('群聊已解散');
    showDisbandModal.value = false;
    showPanel.value = false;
    emit('quit');
  } catch (error) {
    message.error('解散失败');
  } finally {
    actionLoading.value = false;
  }
}

// 获取群组类型名称
function getGroupTypeName(type?: number): string {
  const types: Record<number, string> = { 0: '普通群', 1: '班级群', 2: '课程群' };
  return types[type || 0] || '未知';
}

// 获取群组类型颜色
function getGroupTypeColor(type?: number): 'default' | 'primary' | 'success' {
  const colors: Record<number, 'default' | 'primary' | 'success'> = { 0: 'default', 1: 'primary', 2: 'success' };
  return colors[type || 0] || 'default';
}
</script>
