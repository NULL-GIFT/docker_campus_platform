// 等待页面加载完成后执行
document.addEventListener('DOMContentLoaded', function() {
    console.log("✅ 全局脚本 script.js 已加载！");
});

/**
 * 通用跳转函数 (给 onclick 用的)
 * 用法: onclick="jumpTo('/auth/login')"
 */
function jumpTo(url) {
    window.location.href = url;
}

/**
 * 简单的确认框 (删除时用)
 * 用法: onclick="return confirmAction('确定要删除吗？')"
 */
function confirmAction(msg) {
    return confirm(msg || "确定要执行此操作吗？");
}