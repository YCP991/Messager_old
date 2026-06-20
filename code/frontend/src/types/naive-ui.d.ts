// Naive UI 类型声明
declare module 'naive-ui' {
  // 允许从 naive-ui 导入任何内容
  // 具体类型由 naive-ui/es/index.d.ts 提供
  
  // 常用导出
  export function useMessage(): any
  export function useDialog(): any
  export function useNotification(): any
  export function useLoadingBar(): any
  
  // 导出所有组件
  export const NButton: any
  export const NInput: any
  export const NCard: any
  export const NLayout: any
  export const NLayoutSider: any
  export const NLayoutContent: any
  export const NList: any
  export const NListItem: any
  export const NThing: any
  export const NSpace: any
  export const NAvatar: any
  export const NDivider: any
  export const NScrollbar: any
  export const NTag: any
  export const NText: any
  export const NEmpty: any
  export const NConfigProvider: any
  export const NMessageProvider: any
  export const NDialogProvider: any
  export const NForm: any
  export const NFormItem: any
  export const darkTheme: any
  
  // 导出类型
  export interface GlobalTheme {}
  
  // FormInst 接口
  export interface FormInst {
    validate: () => Promise<void>;
    validateField: (field: string | string[]) => Promise<void>;
    resetFields: () => void;
    clearValidate: (field?: string | string[]) => void;
  }
  
  // FormRules 接口
  export interface FormRules {
    [key: string]: FormItemRule | FormItemRule[];
  }
  
  // FormItemRule 接口
  export interface FormItemRule {
    required?: boolean;
    message?: string;
    trigger?: string | string[];
    min?: number;
    max?: number;
    pattern?: RegExp;
    validator?: (rule: FormItemRule, value: any) => boolean | Promise<boolean>;
  }
}