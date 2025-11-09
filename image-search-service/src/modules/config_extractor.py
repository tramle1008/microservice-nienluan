import torchvision


# Config for the models that are supported by the extractor
MODEL_CONFIG = {
    "resnet18": {
        "weights": torchvision.models.ResNet18_Weights.DEFAULT,
        "model": torchvision.models.resnet18,
        "feat_layer": "flatten",
        "feat_dims": 512,
    },
    "resnet34": {
        "weights": torchvision.models.ResNet34_Weights.DEFAULT,
        "model": torchvision.models.resnet34,
        "feat_layer": "flatten",
        "feat_dims": 512,
    },
    "resnet50": {
        "weights": torchvision.models.ResNet50_Weights.DEFAULT,
        "model": torchvision.models.resnet50,
        "feat_layer": "flatten",
        "feat_dims": 2048,
    },
}