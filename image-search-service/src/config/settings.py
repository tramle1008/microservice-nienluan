import os

# Lấy thư mục gốc của dự án
WORK_DIR = os.path.abspath(os.path.join(os.path.dirname(__file__), "../../.."))

# Đường dẫn đến thư mục data, images và results
DATA_DIR = os.path.join(WORK_DIR, "data")
IMAGES_DIR = os.path.join(WORK_DIR, "images")
RESULTS_DIR = os.path.join(WORK_DIR, "results")

# Danh sách model hỗ trợ
FEATURE_EXTRACTOR_MODELS = [
    "resnet18",
    "resnet34",
    "resnet50",
    "resnet101",
    "resnet152",
    "vit_b_16",
    "vit_b_32",
    "vit_l_16",
    "vit_l_32",
    "vit_h_14",
]
