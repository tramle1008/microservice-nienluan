import os

# Lấy thư mục gốc của dự án
WORK_DIR = os.path.abspath(os.path.join(os.path.dirname(__file__), "../../.."))

# Đường dẫn đến thư mục data, images và results
DATA_DIR = os.path.join(WORK_DIR, "data")
# IMAGES_DIR = os.path.join(WORK_DIR, "images")
IMAGES_DIR = os.path.join(WORK_DIR, "images", "products") 
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
print("File hiện tại:", __file__)
print("Thư mục chứa file:", os.path.dirname(__file__))
print("WORK_DIR:", WORK_DIR)
print("DATA_DIR:", DATA_DIR)
print("IMAGES_DIR:", IMAGES_DIR)
print("RESULTS_DIR:", RESULTS_DIR)