# main.py
import asyncio
import os
from fastapi import FastAPI, UploadFile, File, HTTPException
import uvicorn
import torch
import faiss
import numpy as np
from PIL import Image
import io

from src.modules import FeatureExtractor
from src.config.settings import DATA_DIR

# ====================== CẤU HÌNH ======================
APP_NAME = "IMAGE-SEARCH-SERVICE"
PORT = 8086
EUREKA_SERVER = "http://localhost:8761/eureka"
MODEL_NAME = "resnet50"          # ← Đổi ở đây nếu bạn dùng model khác
K_DEFAULT = 5

# ====================== EUREKA ASYNC (CHẠY TRƯỚC UVICORN) ======================
from py_eureka_client import eureka_client

async def register_to_eureka():
    try:
        await eureka_client.init_async(
            eureka_server=EUREKA_SERVER,
            app_name=APP_NAME,
            instance_port=PORT,
            instance_host="localhost",      # hoặc IP thật nếu deploy
            instance_ip="127.0.0.1",
        )
        print("Đã đăng ký thành công lên Eureka!")
    except Exception as e:
        print(f"Warning: Không kết nối được Eureka: {e}")
        print("   → Vẫn chạy bình thường (chỉ không hiện trên Eureka)")

# ====================== FASTAPI APP ======================
app = FastAPI(title="Image Search Service", version="1.0")

# Load model + index + mapping khi khởi động
print("Đang load model và FAISS index...")
INDEX_PATH = os.path.join(DATA_DIR, f"db_{MODEL_NAME}.index")
MAPPING_PATH = os.path.join(DATA_DIR, f"db_{MODEL_NAME}_mapping.npy")

if not os.path.exists(INDEX_PATH):
    raise FileNotFoundError(f"Không tìm thấy {INDEX_PATH}\n-> Chạy: python build_vector_database.py --feat_extractor {MODEL_NAME}")

if not os.path.exists(MAPPING_PATH):
    raise FileNotFoundError(f"Không tìm thấy {MAPPING_PATH}\n-> Phải build lại index có lưu mapping!")

index = faiss.read_index(INDEX_PATH)
mapping = np.load(MAPPING_PATH, allow_pickle=True).tolist()

feature_extractor = FeatureExtractor(base_model=MODEL_NAME)


print(f"Load thành công {len(mapping)} ảnh với model {MODEL_NAME}")

# ====================== ENDPOINTS ======================
@app.get("/api/image/hello")
def hello():
    return {"message": f"{APP_NAME} đang chạy ngon lành!", "total_images": len(mapping)}

@app.post("/api/image/search")
async def search_similar(file: UploadFile = File(...), k: int = K_DEFAULT):
    if not file.content_type.startswith("image/"):
        raise HTTPException(400, detail="File phải là ảnh!")

    contents = await file.read()
    try:
        img = Image.open(io.BytesIO(contents)).convert("RGB")
    except:
        raise HTTPException(400, detail="Không đọc được ảnh!")

    with torch.no_grad():
        feat = feature_extractor.extract_features(img)
        feat = feat / feat.norm(p=2, dim=1, keepdim=True)
        query_vec = feat.cpu().numpy()

    D, I = index.search(query_vec, k)

    results = [
    {
        "filename": mapping[idx],
        "similarity": round(float(D[0][i]), 4),           # cosine similarity (0 → 1)
        "score_percent": round(float(D[0][i]) * 100, 2)   # ← SỬA DÒNG NÀY: nhân trực tiếp với 100
    }
    for i, idx in enumerate(I[0])
]

    return {
        "query": file.filename,
        "results_count": len(results),
        "similar_images": results
    }

# ====================== CHẠY SERVER ======================
if __name__ == "__main__":
    # Bước 1: Đăng ký Eureka trước (dùng asyncio.run vì chưa có loop nào chạy)
    asyncio.run(register_to_eureka())

    # Bước 2: Chạy Uvicorn (bây giờ đã an toàn)
    uvicorn.run(app, host="0.0.0.0", port=PORT, log_level="info")