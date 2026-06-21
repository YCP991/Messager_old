<template>
  <n-modal v-model:show="showModal" preset="card" title="创建群聊" style="width: 500px;">
    <n-form ref="formRef" :model="formData" :rules="rules" label-placement="top">
      <n-form-item label="群聊名称" path="groupName">
        <n-input 
          v-model:value="formData.groupName" 
          placeholder="请输入群聊名称"
          maxlength="20"
          show-count
        />
      </n-form-item>
      
      <n-form-item label="群聊类型" path="groupType">
        <n-radio-group v-model:value="formData.groupType" name="groupType">
          <n-space>
            <n-radio :value="0">
              <n-tag :bordered="false" size="small">普通群</n-tag>
            </n-radio>
            <n-radio :value="1">
              <n-tag :bordered="false" type="primary" size="small">班级群</n-tag>
            </n-radio>
            <n-radio :value="2">
              <n-tag :bordered="false" type="success" size="small">课程群</n-tag>
            </n-radio>
          </n-space>
        </n-radio-group>
      </n-form-item>
      
      <!-- 班级群专属字段 -->
      <n-form-item v-if="formData.groupType === 1" label="班级号" path="classNo">
        <n-input 
          v-model:value="formData.classNo" 
          placeholder="请输入班级号，如：2024级软件工程1班"
          maxlength="50"
        />
      </n-form-item>
      
      <!-- 课程群专属字段 -->
      <n-form-item v-if="formData.groupType === 2" label="课程代码" path="courseCode">
        <n-input 
          v-model:value="formData.courseCode" 
          placeholder="请输入课程代码，如：CS101"
          maxlength="20"
        />
      </n-form-item>
      
      <n-form-item label="群描述（可选）" path="description">
        <n-input
          v-model:value="formData.description"
          type="textarea"
          placeholder="请输入群描述"
          :rows="2"
          maxlength="100"
          show-count
        />
      </n-form-item>
      
      <n-form-item label="群公告（可选）" path="announcement">
        <n-input
          v-model:value="formData.announcement"
          type="textarea"
          placeholder="请输入群公告"
          :rows="3"
          maxlength="200"
          show-count
        />
      </n-form-item>
      
      <n-form-item label="最大成员数（可选）" path="maxMembers">
        <n-input-number 
          v-model:value="formData.maxMembers" 
          :min="2" 
          :max="500"
          placeholder="不填则无限制"
          style="width: 100%"
        />
      </n-form-item>
    </n-form>
    
    <template #footer>
      <n-space justify="end">
        <n-button @click="handleCancel">取消</n-button>
        <n-button type="primary" :loading="loading" @click="handleCreate">创建</n-button>
      </n-space>
    </template>
  </n-modal>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue';
import { useMessage } from 'naive-ui';
import { createGroup, type CreateGroupDTO } from '@/api/group';

const props = defineProps<{
  show: boolean;
}>();

const emit = defineEmits<{
  (e: 'update:show', value: boolean): void;
  (e: 'created', group: any): void;
}>();

const message = useMessage();

const showModal = ref(props.show);
const loading = ref(false);

const formData = ref<CreateGroupDTO>({
  groupName: '',
  groupType: 0,
  classNo: undefined,
  courseCode: undefined,
  description: undefined,
  announcement: undefined,
  maxMembers: undefined
});

const rules = {
  groupName: {
    required: true,
    message: '请输入群聊名称',
    trigger: 'blur'
  }
};

// 监听 props.show 变化
watch(() => props.show, (val) => {
  showModal.value = val;
  if (!val) {
    // 重置表单
    formData.value = {
      groupName: '',
      groupType: 0,
      classNo: undefined,
      courseCode: undefined,
      description: undefined,
      announcement: undefined,
      maxMembers: undefined
    };
  }
});

// 监听 showModal 变化，同步到父组件
watch(showModal, (val) => {
  emit('update:show', val);
});

async function handleCreate() {
  if (!formData.value.groupName.trim()) {
    message.warning('请输入群聊名称');
    return;
  }
  
  loading.value = true;
  try {
    const group = await createGroup(formData.value);
    message.success('群聊创建成功');
    emit('created', group);
    handleCancel();
  } catch (error) {
    console.error('创建群聊失败:', error);
    message.error('创建群聊失败');
  } finally {
    loading.value = false;
  }
}

function handleCancel() {
  showModal.value = false;
}
</script>
