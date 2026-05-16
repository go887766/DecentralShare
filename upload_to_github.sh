#!/bin/bash

# DecentralShare - GitHub 上传脚本
# 使用方法：
# 1. 先在 GitHub 创建仓库 https://github.com/new
# 2. 运行此脚本：./upload_to_github.sh <你的 GitHub token>

if [ -z "$1" ]; then
    echo "请提供 GitHub Token！"
    echo "使用方法：./upload_to_github.sh ghp_xxxxxxxxxxxxx"
    exit 1
fi

GITHUB_TOKEN=$1
REPO_URL="https://go887766:${GITHUB_TOKEN}@github.com/go887766/DecentralShare.git"

echo "========================================="
echo "DecentralShare GitHub 上传工具"
echo "========================================="

echo "正在配置远程仓库..."
git remote set-url origin "$REPO_URL"

echo "正在推送代码到 GitHub..."
git push -u origin main --force

if [ $? -eq 0 ]; then
    echo ""
    echo "✅ 上传成功！"
    echo "你的仓库地址：https://github.com/go887766/DecentralShare"
    echo ""
    echo "下一步：使用 Android Studio 构建 APK！"
    echo "  - 打开 Android Studio"
    echo "  - 导入项目"
    echo "  - Build -> Build Bundle(s)/APK(s) -> Build APK(s)"
else
    echo ""
    echo "❌ 上传失败，请检查："
    echo "  1. Token 是否正确"
    echo "  2. GitHub 仓库是否已创建"
    echo "  3. Token 是否有 repo 权限"
fi
