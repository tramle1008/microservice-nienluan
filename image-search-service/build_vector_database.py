#build_vector_database.py
from tqdm import tqdm
import argparse
import faiss
import torch
import PIL
import os

from src.modules import FeatureExtractor
from src.config import *


def main(args=None):
    # initialize the feature extractor with the base model specified in the arguments
    feature_extractor = FeatureExtractor(base_model=args.feat_extractor)
    # initialize the vector database indexing
    index = faiss.IndexFlatIP(feature_extractor.feat_dims)
    # get the list of images in sorted order
    image_list = sorted(os.listdir(IMAGES_DIR))

    with torch.no_grad():
        # iterate over the images and add their extracted features to the index
        for img_filename in tqdm(image_list):
            # load image
            img = PIL.Image.open(os.path.join(IMAGES_DIR, img_filename)).convert("RGB")
            # extract features
            output = feature_extractor.extract_features(img)
            # keep only batch dimension
            output = output.view(output.size(0), -1)
            # normalize the output since we are using the inner product as the similarity measure (cosine similarity)
            output = output / output.norm(p=2, dim=1, keepdim=True)
            # add to the index
            index.add(output.numpy())

    # save the index
    index_filepath = os.path.join(DATA_DIR, f"db_{args.feat_extractor}.index")
    faiss.write_index(index, index_filepath)


if __name__ == "__main__":
    # parse arguments
    args = argparse.ArgumentParser()
    args.add_argument(
        "--feat_extractor",
        type=str,
        default="resnet50",
        choices=FEATURE_EXTRACTOR_MODELS,
    )
    args = args.parse_args()

    # run the main function
    main(args)
    