/**
 * 日志管理工具
 * 提供统一的日志输出接口，支持不同级别的日志
 */

// 日志级别枚举
enum LogLevel {
  DEBUG = 0,
  INFO = 1,
  WARN = 2,
  ERROR = 3,
  NONE = 4
}

// 日志配置
interface LogConfig {
  level: LogLevel;
  enableTimestamp: boolean;
  enableColor: boolean;
  prefix: string;
}

// 默认配置
const defaultConfig: LogConfig = {
  level: LogLevel.DEBUG, // 开发环境显示所有日志
  enableTimestamp: true,
  enableColor: true,
  prefix: 'Messager'
};

// 当前配置
let currentConfig = defaultConfig;

/**
 * 格式化时间戳
 */
function formatTimestamp(): string {
  const now = new Date();
  const hours = now.getHours().toString().padStart(2, '0');
  const minutes = now.getMinutes().toString().padStart(2, '0');
  const seconds = now.getSeconds().toString().padStart(2, '0');
  const milliseconds = now.getMilliseconds().toString().padStart(3, '0');
  return `${hours}:${minutes}:${seconds}.${milliseconds}`;
}

/**
 * 获取日志颜色
 */
function getLogColor(level: LogLevel): string {
  switch (level) {
    case LogLevel.DEBUG:
      return 'color: #999';
    case LogLevel.INFO:
      return 'color: #2196F3';
    case LogLevel.WARN:
      return 'color: #FF9800';
    case LogLevel.ERROR:
      return 'color: #F44336';
    default:
      return '';
  }
}

/**
 * 构建日志前缀
 */
function buildPrefix(level: LogLevel): string {
  const parts = [];
  
  if (currentConfig.prefix) {
    parts.push(currentConfig.prefix);
  }
  
  if (currentConfig.enableTimestamp) {
    parts.push(formatTimestamp());
  }
  
  const levelNames = ['DEBUG', 'INFO', 'WARN', 'ERROR'];
  parts.push(levelNames[level]);
  
  return `[${parts.join(' | ')}]`;
}

/**
 * 输出日志
 */
function log(level: LogLevel, ...args: any[]): void {
  if (level < currentConfig.level) {
    return;
  }
  
  const prefix = buildPrefix(level);
  const consoleMethod = level === LogLevel.DEBUG ? 'debug' : 
                        level === LogLevel.INFO ? 'info' : 
                        level === LogLevel.WARN ? 'warn' : 'error';
  
  if (currentConfig.enableColor) {
    const color = getLogColor(level);
    console[consoleMethod](`%c${prefix}`, color, ...args);
  } else {
    console[consoleMethod](prefix, ...args);
  }
}

/**
 * Logger 类
 */
class Logger {
  /**
   * 设置日志级别
   */
  setLevel(level: LogLevel): void {
    currentConfig.level = level;
  }
  
  /**
   * 设置日志配置
   */
  setConfig(config: Partial<LogConfig>): void {
    currentConfig = { ...currentConfig, ...config };
  }
  
  /**
   * 获取当前配置
   */
  getConfig(): LogConfig {
    return currentConfig;
  }
  
  /**
   * DEBUG级别日志
   */
  debug(...args: any[]): void {
    log(LogLevel.DEBUG, ...args);
  }
  
  /**
   * INFO级别日志
   */
  info(...args: any[]): void {
    log(LogLevel.INFO, ...args);
  }
  
  /**
   * WARN级别日志
   */
  warn(...args: any[]): void {
    log(LogLevel.WARN, ...args);
  }
  
  /**
   * ERROR级别日志
   */
  error(...args: any[]): void {
    log(LogLevel.ERROR, ...args);
  }
  
  /**
   * 分组日志开始
   */
  group(label: string): void {
    console.group(label);
  }
  
  /**
   * 分组日志结束
   */
  groupEnd(): void {
    console.groupEnd();
  }
  
  /**
   * 表格日志
   */
  table(data: any): void {
    console.table(data);
  }
  
  /**
   * 性能计时开始
   */
  time(label: string): void {
    console.time(label);
  }
  
  /**
   * 性能计时结束
   */
  timeEnd(label: string): void {
    console.timeEnd(label);
  }
}

// 创建全局Logger实例
const logger = new Logger();

// 生产环境自动关闭DEBUG日志
if (import.meta.env.PROD) {
  logger.setLevel(LogLevel.INFO);
}

export { logger };
export type { LogLevel, LogConfig };
export default logger;