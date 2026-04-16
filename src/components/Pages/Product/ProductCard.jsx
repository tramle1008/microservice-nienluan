import { useState } from "react";
import { FaShoppingCart } from "react-icons/fa";
import ProductView from "./ProductView";
import { Link } from "react-router-dom";
import { truncateWords } from "../../../utils/truncate";
import { useDispatch } from "react-redux";
import { addToCart } from "../../../store/actions";
import toast from "react-hot-toast";



const ProductCard = ({ product }) => {
    const dispatch = useDispatch();

    if (!product) return null;

    const {
        productId,
        productName,
        imageUrl,
        price,
        finalPrice,
        appliedDiscounts,
        variants
    } = product;

    const [openProductView, setOpenProductView] = useState(false);
    const [selectedVariant, setSelectedVariant] = useState(null);
    const [forceMainImage, setForceMainImage] = useState(false);

    const isAvailable = selectedVariant ? selectedVariant.stockQuantity > 0 : true;

    const handleProductView = () => setOpenProductView(true);

    const addToCartHandle = () => {
        const variantToAdd = selectedVariant;
        if (!variantToAdd) {
            toast("Vui lòng chọn màu", {
                icon: "⚠️",
            });


            return;
        }
        dispatch(addToCart(variantToAdd.variantId, 1));

    };

    const handleVariantClick = (v) => {
        if (selectedVariant?.variantId === v.variantId) {
            setForceMainImage(prev => !prev);
        } else {
            setSelectedVariant(v);
            setForceMainImage(false);
        }
    };

    const displayImage = () => {
        if (!selectedVariant) return imageUrl;
        if (forceMainImage) return imageUrl;
        return selectedVariant.imageUrl && selectedVariant.imageUrl !== imageUrl
            ? selectedVariant.imageUrl
            : imageUrl;
    };

    const displayPrice = selectedVariant?.finalPrice || finalPrice || price;

    return (
        <div className="border rounded-lg shadow-xl overflow-hidden transition-shadow duration-300 bg-white">
            <div onClick={handleProductView} className="w-full overflow-hidden aspect-square cursor-pointer">
                <img
                    className="w-full h-full transition-transform duration-300 transform hover:scale-105"
                    src={displayImage()}
                    alt={productName}
                />
            </div>

            <div className="p-4 flex flex-col gap-2">
                <div className="min-h-14 py-1"> {/* cho phép co giãn nhẹ nếu cần */}
                    <Link
                        to={`/product/${productId}`}
                        className="block text-[22px] font-semibold text-emerald-700 line-clamp-2 hover:text-gray-600 transition-colors"
                    >
                        {truncateWords(product.productName, 5)}
                    </Link>
                </div>

                <p className="text-gray-400 text-sm min-h-20 max-h-20 line-clamp-2">
                    {truncateWords(product.shortDescription, 15)}
                </p>

                <div className="flex flex-col">
                    {/* Luôn hiển thị giá mới (finalPrice) ở vị trí chính giữa/dưới cùng */}
                    <div className="flex items-end gap-2 min-h-8">
                        {/* Phần giảm giá + giá gốc gạch ngang (nếu có) */}
                        {appliedDiscounts?.length > 0 && (
                            <div className="flex flex-col">
                                <span className="text-red-500 text-xs font-semibold bg-red-50 px-2 py-0.5 rounded">
                                    -{appliedDiscounts[0].percentage}%
                                </span>
                                <span className="text-gray-400 line-through text-xs">
                                    {price.toLocaleString()}đ
                                </span>
                            </div>
                        )}

                        {/* Giá chính - luôn hiển thị, căn dưới cùng để các card bằng nhau */}
                        <span className={`font-bold text-xl text-black ${appliedDiscounts?.length > 0 ? 'text-2xl text-red-600' : 'text-xl'}`}>
                            {displayPrice.toLocaleString()}đ
                        </span>
                    </div>
                </div>

                {variants?.length > 1 && (
                    <div className="flex gap-2 mt-1 flex-wrap">
                        {variants.map((v) => (
                            <button
                                key={v.variantId}
                                onClick={(e) => {
                                    e.stopPropagation();
                                    handleVariantClick(v);
                                }}
                                className={`px-3 py-1 text-xs border rounded transition-all ${selectedVariant?.variantId === v.variantId && !forceMainImage
                                    ? "bg-emerald-700 text-white border-emerald-700"
                                    : "bg-white text-gray-700 border-gray-300 hover:border-emerald-700"
                                    }`}
                            >
                                {v.color}
                            </button>
                        ))}
                    </div>
                )}

                <button
                    className={`bg-emerald-700 text-white py-2 px-2 mt-2 rounded-lg w-full transition-colors duration-300 ${isAvailable ? "hover:bg-emerald-900" : "opacity-60 cursor-not-allowed"
                        }`}
                    disabled={!isAvailable}
                    onClick={addToCartHandle}
                >

                    <FaShoppingCart className="inline mr-2" />
                    {isAvailable ? "Thêm vào giỏ" : "Hết hàng"}
                </button>
            </div>

            <ProductView
                open={openProductView}
                setOpen={setOpenProductView}
                product={{ ...product, selectedVariant, forceMainImage, variants }}
                isAvailable={isAvailable}
            />
        </div>
    );
};

export default ProductCard;
