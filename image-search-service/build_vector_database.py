# #build_vector_database.py
# from tqdm import tqdm
# import argparse
# import faiss
# import torch
# import PIL
# import os

# from src.modules import FeatureExtractor
# from src.config import *


# def main(args=None):
#     # initialize the feature extractor with the base model specified in the arguments
#     feature_extractor = FeatureExtractor(base_model=args.feat_extractor)
#     # initialize the vector database indexing
#     index = faiss.IndexFlatIP(feature_extractor.feat_dims)
#     # get the list of images in sorted order
#     image_list = sorted(os.listdir(IMAGES_DIR))

#     with torch.no_grad():
#         # iterate over the images and add their extracted features to the index
#         for img_filename in tqdm(image_list):
#             # load image
#             img = PIL.Image.open(os.path.join(IMAGES_DIR, img_filename)).convert("RGB")
#             # extract features
#             output = feature_extractor.extract_features(img)
#             # keep only batch dimension
#             output = output.view(output.size(0), -1)
#             # normalize the output since we are using the inner product as the similarity measure (cosine similarity)
#             output = output / output.norm(p=2, dim=1, keepdim=True)
#             # add to the index
#             index.add(output.numpy())

#     # save the index
#     index_filepath = os.path.join(DATA_DIR, f"db_{args.feat_extractor}.index")
#     faiss.write_index(index, index_filepath)


# if __name__ == "__main__":
#     # parse arguments
#     args = argparse.ArgumentParser()
#     args.add_argument(
#         "--feat_extractor",
#         type=str,
#         default="resnet50",
#         choices=FEATURE_EXTRACTOR_MODELS,
#     )
#     args = args.parse_args()

#     # run the main function
#     main(args)
    # build_vector_database.py  ← bản CHUẨN cho FastAPI
import argparse
import os
import faiss
import torch
import numpy as np
from tqdm import tqdm
from PIL import Image

from src.modules import FeatureExtractor
from src.config.settings import IMAGES_DIR, DATA_DIR, FEATURE_EXTRACTOR_MODELS

def main(args):
    print(f"Đang build index với model: {args.feat_extractor}")
    print(f"Thư mục ảnh: {IMAGES_DIR}")

    extractor = FeatureExtractor(base_model=args.feat_extractor)
    dim = extractor.feat_dims
    index = faiss.IndexFlatIP(dim)

    # Lọc chỉ file ảnh
    image_files = sorted([
        f for f in os.listdir(IMAGES_DIR)
        if f.lower().endswith(('.png', '.jpg', '.jpeg', '.webp', '.bmp'))
    ])

    filenames = []  # ← Đây là mapping cực kỳ quan trọng!

    with torch.no_grad():
        for filename in tqdm(image_files, desc="Building index"):
            try:
                img_path = os.path.join(IMAGES_DIR, filename)
                img = Image.open(img_path).convert("RGB")
                feat = extractor.extract_features(img)
                feat = feat / feat.norm(p=2, dim=1, keepdim=True)
                index.add(feat.cpu().numpy())
                filenames.append(filename)
            except Exception as e:
                print(f"Lỗi ảnh {filename}: {e}")

    # Lưu cả 2 file
    index_path = os.path.join(DATA_DIR, f"db_{args.feat_extractor}.index")
    mapping_path = os.path.join(DATA_DIR, f"db_{args.feat_extractor}_mapping.npy")

    faiss.write_index(index, index_path)
    np.save(mapping_path, np.array(filenames))

    print(f"HOÀN TẤT! Đã index {len(filenames)} ảnh")
    print(f"→ {index_path}")
    print(f"→ {mapping_path}")

if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("--feat_extractor", type=str, default="resnet50",
                        choices=FEATURE_EXTRACTOR_MODELS)
    args = parser.parse_args()
    main(args)

    # python build_vector_database.py --feat_extractor resnet50
    