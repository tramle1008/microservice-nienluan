import { useParams } from "react-router-dom";
import { useEffect, useState } from "react";
import { FaShoppingCart, FaPlus, FaMinus } from "react-icons/fa";
import { useDispatch } from "react-redux";
import { addToCart } from "../../../store/actions";

const ProductDetail = () => {
    const dispatch = useDispatch();
    const { productId } = useParams();
    const [product, setProduct] = useState(null);
    const [selectedVariant, setSelectedVariant] = useState(null);
    const [displayImage, setDisplayImage] = useState("");
    const [quantity, setQuantity] = useState(1);           // giá trị thực tế dùng để thêm vào giỏ
    const [tempQuantity, setTempQuantity] = useState("1"); // giá trị tạm hiển thị khi đang nhập

    useEffect(() => {
        const fetchProduct = async () => {
            try {
                const res = await fetch(`/api/products/public/${productId}`);
                const data = await res.json();
                setProduct(data);
                setDisplayImage(data.imageUrl);

                if (data.variants && data.variants.length > 0) {
                    const firstInStock = data.variants.find(v => v.stockQuantity > 0) || data.variants[0];
                    setSelectedVariant(firstInStock);
                    setDisplayImage(firstInStock.imageUrl || data.imageUrl);
                }
            } catch (error) {
                console.error("Lỗi tải sản phẩm:", error);
            }
        };
        fetchProduct();
    }, [productId]);

    // Khi thay đổi variant → reset quantity về 1
    useEffect(() => {
        setQuantity(1);
        setTempQuantity("1");
    }, [selectedVariant?.variantId]);

    if (!product) {
        return (
            <div className="flex justify-center items-center py-20">
                <div className="animate-spin rounded-full h-12 w-12 border-t-4 border-emerald-700"></div>
                <span className="ml-4 text-lg">Đang tải sản phẩm...</span>
            </div>
        );
    }

    const currentVariant = selectedVariant || product.variants?.[0];
    const displayPrice = currentVariant?.finalPrice ?? product.finalPrice ?? product.price;
    const originalPrice = currentVariant?.priceOverride ?? product.price;
    const hasDiscount = product.appliedDiscounts?.length > 0;
    const discountPercent = hasDiscount ? product.appliedDiscounts[0].percentage : 0;

    const stockQuantity = currentVariant?.stockQuantity ?? 0;
    const isAvailable = stockQuantity > 0;

    const handleVariantClick = (variant) => {
        setSelectedVariant(variant);
        setDisplayImage(variant.imageUrl || product.imageUrl);
    };

    // Chỉ cho phép nhập số
    const handleQuantityChange = (value) => {
        const cleaned = value.replace(/[^0-9]/g, "");
        setTempQuantity(cleaned);
    };

    // Validate khi rời khỏi ô input hoặc nhấn Enter
    const handleQuantityBlur = () => {
        let num = parseInt(tempQuantity) || 1;

        if (num < 1) num = 1;
        if (num > stockQuantity) num = stockQuantity;

        setQuantity(num);
        setTempQuantity(num.toString());
    };

    const increment = () => {
        if (quantity < stockQuantity) {
            const newQty = quantity + 1;
            setQuantity(newQty);
            setTempQuantity(newQty.toString());
        }
    };

    const decrement = () => {
        if (quantity > 1) {
            const newQty = quantity - 1;
            setQuantity(newQty);
            setTempQuantity(newQty.toString());
        }
    };

    const handleAddToCart = () => {
        if (!currentVariant || !isAvailable) return;

        dispatch(addToCart(currentVariant.variantId, quantity));

    };

    return (
        <div className="min-h-screen bg-[#f1f7f0]">


            <div className="max-w-7xl mx-auto p-6 lg:p-10">
                <div className="grid grid-cols-1 md:grid-cols-2 gap-10 lg:gap-16">
                    {/* Ảnh sản phẩm */}
                    <div className="relative">
                        <div className="aspect-square overflow-hidden rounded-2xl shadow-2xl bg-gray-50">
                            <img
                                src={displayImage || product.imageUrl}
                                alt={product.productName}
                                className="w-full h-full object-cover transition-transform duration-500 hover:scale-105"
                            />
                        </div>

                        {hasDiscount && (
                            <div className="absolute top-4 left-4 bg-red-600 text-white text-3xl font-extrabold px-6 py-4 rounded-2xl shadow-2xl animate-pulse">
                                -{discountPercent}%
                            </div>
                        )}
                    </div>

                    {/* Thông tin */}
                    <div className="flex flex-col justify-center space-y-6">
                        <div>
                            <h1 className="text-3xl md:text-4xl font-bold text-gray-900">
                                {product.productName}
                            </h1>
                            <p className="mt-4 text-lg text-gray-600">{product.shortDescription}</p>
                        </div>

                        {/* Chọn màu + tồn kho */}
                        {product.variants && product.variants.length > 1 && (
                            <div>
                                <h3 className="text-lg font-semibold text-gray-800 mb-3">Màu sắc:</h3>
                                <div className="flex flex-wrap gap-3">
                                    {product.variants.map((variant) => {
                                        const isSelected = selectedVariant?.variantId === variant.variantId;
                                        const inStock = variant.stockQuantity > 0;

                                        return (
                                            <button
                                                key={variant.variantId}
                                                onClick={() => handleVariantClick(variant)}
                                                disabled={!inStock}
                                                className={`relative px-5 py-3 rounded-lg border-2 font-medium transition-all
                                                ${isSelected
                                                        ? "bg-emerald-700 text-white border-emerald-700 shadow-lg scale-105"
                                                        : "bg-white text-gray-800 border-gray-300 hover:border-emerald-600"
                                                    }
                                                ${!inStock ? "opacity-50 cursor-not-allowed" : "hover:shadow-md"}
                                            `}
                                            >
                                                <span>{variant.color}</span>
                                                <span className="block text-xs mt-1 font-normal">
                                                    {inStock ? `Còn ${variant.stockQuantity} sp` : "Hết hàng"}
                                                </span>
                                            </button>
                                        );
                                    })}
                                </div>
                            </div>
                        )}

                        {/* Tồn kho hiện tại */}
                        <div className="text-lg">
                            <span className="font-semibold">Tồn kho:</span>{" "}
                            <span className={`${isAvailable ? "text-green-600" : "text-red-600"} font-bold`}>
                                {isAvailable ? `Còn ${stockQuantity} sản phẩm` : "Hết hàng"}
                            </span>
                        </div>

                        {/* Giá */}
                        <div className="space-y-2">
                            <div className="flex items-baseline gap-4">
                                <span className="text-4xl font-bold text-red-600">
                                    {Number(displayPrice).toLocaleString('vi-VN')} ₫
                                </span>
                                {originalPrice > displayPrice && (
                                    <span className="text-xl text-gray-500 line-through">
                                        {Number(originalPrice).toLocaleString('vi-VN')} ₫
                                    </span>
                                )}
                            </div>
                        </div>

                        {/* Chọn số lượng + Thêm vào giỏ */}
                        <div className="flex items-center gap-4">
                            {/* Ô số lượng */}
                            <div className="flex items-center border-2 border-gray-300 rounded-xl overflow-hidden">
                                <button
                                    onClick={decrement}
                                    className="p-3 hover:bg-gray-100 transition"
                                    disabled={quantity <= 1}
                                >
                                    <FaMinus />
                                </button>
                                <input
                                    type="text"
                                    value={tempQuantity}
                                    onChange={(e) => handleQuantityChange(e.target.value)}
                                    onBlur={handleQuantityBlur}
                                    onKeyDown={(e) => e.key === "Enter" && handleQuantityBlur()}
                                    className="w-16 text-center font-semibold text-lg outline-none"
                                    placeholder="1"
                                />
                                <button
                                    onClick={increment}
                                    className="p-3 hover:bg-gray-100 transition"
                                    disabled={quantity >= stockQuantity}
                                >
                                    <FaPlus />
                                </button>
                            </div>

                            {/* Nút thêm giỏ */}
                            <button
                                onClick={handleAddToCart}
                                disabled={!isAvailable}
                                className={`flex-1 py-4 rounded-xl text-lg font-semibold flex items-center justify-center gap-3 transition-all
                                ${isAvailable
                                        ? "bg-emerald-700 hover:bg-emerald-800 text-white shadow-lg hover:shadow-xl"
                                        : "bg-gray-400 text-gray-700 cursor-not-allowed"
                                    }
                            `}
                            >
                                <FaShoppingCart className="text-xl" />
                                {isAvailable ? "Thêm vào giỏ hàng" : "Hết hàng"}
                            </button>
                        </div>

                        {/* Mô tả chi tiết */}

                    </div>
                </div>

                <div className="border-t pt-6 mt-6">
                    <h3 className="text-2xl font-semibold mb-3">Mô tả sản phẩm</h3>
                    <p className="text-gray-700 leading-relaxed whitespace-pre-line">
                        {product.longDescription}
                    </p>
                </div>
            </div>
        </div>
    );
};

export default ProductDetail;