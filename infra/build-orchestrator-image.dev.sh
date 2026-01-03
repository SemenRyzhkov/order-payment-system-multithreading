#!/bin/bash
set -e

# получаем директорию, где лежит этот скрипт
SCRIPT_DIR="$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"
# вычисляем путь к корню проекта относительно скрипта
REPO_ROOT="$( cd "$SCRIPT_DIR/.." &> /dev/null && pwd )"

VERSION_TAG="latest"
BASE_IMAGE_NAME="order-orchestrator"
IMAGE_NAME="${BASE_IMAGE_NAME}:${VERSION_TAG}"

echo "🚀 Building Docker image: $IMAGE_NAME ..."
docker build -t "$IMAGE_NAME" -f "$REPO_ROOT/order-orchestrator/Dockerfile" "$REPO_ROOT"

echo "✅ Done. Image built:"
docker images "$BASE_IMAGE_NAME"

echo ""
echo "👉 Run container with:"
echo "docker run -it ${IMAGE_NAME}"
