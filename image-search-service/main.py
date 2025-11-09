from fastapi import FastAPI, UploadFile, File
from py_eureka_client import eureka_client
import uvicorn
import torch
import faiss
from PIL import Image
import io
import numpy as np
import os

from src.modules import FeatureExtractor
from src.config.settings import IMAGES_DIR, DATA_DIR

# ======================
# Cấu hình Eureka
# ======================
APP_NAME = "IMAGE-SEARCH-SERVICE"
PORT = 8081
EUREKA_SERVER = "http://localhost:8761/eureka"

# Khởi tạo Eureka client
eureka_client.init(
    eureka_server=EUREKA_SERVER,
    app_name=APP_NAME,
    instance_port=PORT
)

# ======================
# Khởi tạo FastAPI
# ======================
app = FastAPI(title="Image Search Service")

# ======================
# Load mô hình & FAISS index
# ======================
print("Loading model and FAISS index...")

feature_extractor = FeatureExtractor(base_model="resnet50")
index_path = os.path.join(DATA_DIR, "db_resnet50.index")

if not os.path.exists(index_path):
    raise FileNotFoundError(f"Không tìm thấy file index: {index_path}")

index = faiss.read_index(index_path)
image_list = sorted(os.listdir(IMAGES_DIR))

print(f"Loaded {len(image_list)} images from {IMAGES_DIR}")

# ======================
# Endpoint test
# ======================
@app.get("/api/image/hello")
def hello():
    return {"message": "Hello from Python Service"}

# ======================
# Endpoint tìm ảnh tương tự
# ======================
@app.post("/api/image/search")
async def search_similar(file: UploadFile = File(...)):
    """
    Nhận ảnh upload, trích xuất đặc trưng, so sánh với FAISS index
    và trả về danh sách ảnh tương tự nhất.
    """
    # 1 Nhận file upload
    contents = await file.read()
    image = Image.open(io.BytesIO(contents)).convert("RGB")

    # 2 Trích xuất đặc trưng
    with torch.no_grad():
        feat = feature_extractor.extract_features(image)
        feat = feat.view(feat.size(0), -1)
        feat = feat / feat.norm(p=2, dim=1, keepdim=True)

    # 3 Tìm kiếm trong FAISS,, k = 5
    D, I = index.search(feat.numpy(), k=5)

    # 4 Trả về danh sách ảnh tương tự
    similar_images = [image_list[idx] for idx in I[0]]

    return {
        "query_image": file.filename,
        "similar_images": similar_images,
        "distances": D[0].tolist()
    }

# ======================
# Hàm main
# ======================
def main():
    uvicorn.run(app, host="0.0.0.0", port=PORT)

# ======================
# Chạy khi thực thi trực tiếp
# ======================
if __name__ == "__main__":
    main()
