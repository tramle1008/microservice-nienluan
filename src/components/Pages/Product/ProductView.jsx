import { Dialog, DialogBackdrop, DialogPanel, DialogTitle } from "@headlessui/react";
import { FaShoppingCart } from "react-icons/fa";
import { useDispatch } from "react-redux";

function ProductView({ open, setOpen, product, isAvailable }) {
    const dispatch = useDispatch(); // THÊM

    if (!product) return null;

    const {
        productName,
        longDescription,
        shortDescription,
        price,
        finalPrice,
        appliedDiscounts,
        selectedVariant,
        forceMainImage,
        imageUrl
    } = product;

    const close = () => setOpen(false);

    const displayImage = forceMainImage
        ? imageUrl
        : (selectedVariant?.imageUrl || imageUrl);

    const displayPrice = selectedVariant?.finalPrice || finalPrice || price;
    const stockQuantity = selectedVariant?.stockQuantity ?? 0;
    const color = selectedVariant?.color || "Mặc định";

    const handleAddToCart = () => {
        const variantToAdd = selectedVariant || product.variants?.[0];
        if (!variantToAdd) return;

        dispatch(addToCart(variantToAdd.variantId, 1));
    };

    return (
        <Dialog open={open} onClose={close} className="relative z-50">
            <DialogBackdrop className="fixed inset-0 bg-black/30" />

            <div className="fixed inset-0 z-10 w-screen overflow-y-auto">
                <div className="flex min-h-full items-center justify-center p-4">
                    <DialogPanel
                        transition
                        className="w-full max-w-lg rounded-2xl bg-white p-6 shadow-xl duration-300 ease-out data-[closed]:scale-95 data-[closed]:opacity-0"
                    >
                        <DialogTitle as="h3" className="text-xl font-semibold text-gray-800 mb-4">
                            {productName}
                        </DialogTitle>

                        <img
                            src={displayImage}
                            alt={productName}
                            className="w-full max-h-80 object-contain rounded-xl border mb-4"
                        />

                        <p className="text-gray-600 text-sm mb-3">
                            {shortDescription || "Không có mô tả chi tiết cho sản phẩm này."}
                        </p>

                        <div className="mb-3 text-sm text-gray-700">
                            <p><strong>Màu:</strong> {color}</p>
                            <p><strong>Số lượng tồn:</strong> {stockQuantity}</p>
                        </div>

                        {appliedDiscounts?.length > 0 && (
                            <div className="flex flex-col mb-2">
                                <span className="text-red-500 text-sm">
                                    Giảm {appliedDiscounts[0].percentage}%
                                </span>
                                <span className="text-gray-400 line-through text-sm">
                                    {price.toLocaleString()} đ
                                </span>
                            </div>
                        )}

                        <div className="text-lg font-bold text-gray-800 mb-4">
                            {displayPrice.toLocaleString()} đ
                        </div>

                        <div className="flex gap-3">
                            <button
                                onClick={handleAddToCart}
                                disabled={!isAvailable}
                                className={`flex-1 flex items-center justify-center gap-2 py-2 rounded-lg text-white transition-colors duration-300 
                                    ${isAvailable
                                        ? "bg-emerald-700 hover:bg-emerald-900"
                                        : "bg-gray-400 cursor-not-allowed"
                                    }`}
                            >
                                <FaShoppingCart />
                                {isAvailable ? "Thêm vào giỏ" : "Hết hàng"}
                            </button>

                            <button
                                onClick={close}
                                className="flex-1 py-2 rounded-lg border border-gray-300 text-gray-700 hover:bg-gray-100 transition-colors duration-300"
                            >
                                Đóng
                            </button>
                        </div>
                    </DialogPanel>
                </div>
            </div>
        </Dialog>
    );
}

export default ProductView;
