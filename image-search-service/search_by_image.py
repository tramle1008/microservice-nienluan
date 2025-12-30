# search_by_image.py
# DÙNG ĐỂ: Tìm ảnh tương tự với 1 ảnh bạn gửi vào (từ máy tính, điện thoại, web...)

import argparse
import os
import faiss
import torch
import numpy as np
from PIL import Image
import matplotlib.pyplot as plt

from src.modules import FeatureExtractor
from src.config import DATA_DIR, IMAGES_DIR, FEATURE_EXTRACTOR_MODELS


def search_similar_images(query_image_path, feat_extractor_name, k=5):
    # 1. Load index + mapping
    index_path = os.path.join(DATA_DIR, f"db_{feat_extractor_name}.index")
    mapping_path = os.path.join(DATA_DIR, f"db_{feat_extractor_name}_mapping.npy")

    if not os.path.exists(index_path):
        print(f"Không tìm thấy index: {index_path}")
        print("Chạy lại build_vector_database.py trước!")
        return

    index = faiss.read_index(index_path)
    mapping = np.load(mapping_path, allow_pickle=True)  # ← tên file tương ứng với mỗi vector

    # 2. Load feature extractor
    extractor = FeatureExtractor(base_model=feat_extractor_name)
    extractor.eval()

    # 3. Load và extract ảnh query
    query_img = Image.open(query_image_path).convert("RGB")
    with torch.no_grad():
        feat = extractor.extract_features(query_img)                    # [1, D]
        feat = feat / feat.norm(p=2, dim=1, keepdim=True)               # L2 normalize
        query_vec = feat.cpu().numpy()

    # 4. Tìm k ảnh gần nhất
    distances, indices = index.search(query_vec, k)

    # 5. Load ảnh kết quả
    result_images = []
    for idx, dist in zip(indices[0], distances[0]):
        filename = mapping[idx]
        img_path = os.path.join(IMAGES_DIR, filename)
        result_images.append((Image.open(img_path).copy(), dist, filename))

    # 6. Vẽ kết quả đẹp
    cols = 6
    rows = (k // cols) + 1
    plt.figure(figsize=(20, 8))

    # Query image
    plt.subplot(rows, cols, 1)
    plt.imshow(query_img)
    plt.title(f"QUERY\n{os.path.basename(query_image_path)}", fontsize=12, color="red")
    plt.axis("off")

    # Result images
    for i, (img, dist, fname) in enumerate(result_images):
        plt.subplot(rows, cols, i + 2)
        plt.imshow(img)
        plt.title(f"{dist:.4f}\n{fname}", fontsize=9)
        plt.axis("off")

    plt.tight_layout()
    plt.show()


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Tìm ảnh giống với ảnh bạn gửi vào")
    parser.add_argument("image_path", type=str, help="Đường dẫn đến ảnh bạn muốn tìm giống")
    parser.add_argument("--feat_extractor", type=str, required=True,
                        choices=FEATURE_EXTRACTOR_MODELS,
                        help="Model đã dùng để build index (phải trùng!)")
    parser.add_argument("--k", type=int, default=5, help="Số ảnh trả về (mặc định 5)")

    args = parser.parse_args()

    if not os.path.exists(args.image_path):
        print(f"Không tìm thấy ảnh: {args.image_path}")
    else:
        search_similar_images(args.image_path, args.feat_extractor, args.k)