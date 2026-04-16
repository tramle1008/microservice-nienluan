
import { useDispatch, useSelector } from "react-redux";

import { applyDiscountToProduct, applyDiscountToVariant } from "../../../../store/actions";
import toast from "react-hot-toast";
import { useState } from "react";
const DiscountSelector = ({ row, discounts = [], loadingDiscount = false }) => {
    const dispatch = useDispatch();
    const [open, setOpen] = useState(false);
    const [selectedDiscount, setSelectedDiscount] = useState("");

    const currentDiscount = row.discount;

    const handleApply = async () => {
        if (!selectedDiscount) return;

        const selectedDiscountObj = discounts.find(d => d.discountId === Number(selectedDiscount));
        if (!selectedDiscountObj) {
            toast.error("Không tìm thấy khuyến mãi!");
            return;
        }

        let result;

        if (selectedDiscountObj.target === "VARIANT") {
            if (!row.variantId) {
                toast.error("Khuyến mãi này chỉ áp dụng cho biến thể cụ thể!");
                return;
            }
            result = await dispatch(applyDiscountToVariant(row.variantId, Number(selectedDiscount)));
        } else {
            // target === "PRODUCT"
            result = await dispatch(applyDiscountToProduct(row.productId, Number(selectedDiscount)));
        }

        if (result?.success) {
            toast.success(`Đã gán "${selectedDiscountObj.name}" thành công!`);
            setOpen(false);
            setSelectedDiscount("");
            onSuccess?.();
        } else {
            toast.error(result?.error || "Gán khuyến mãi thất bại");
        }
    };

    if (loadingDiscount) {
        return <span className="text-xs text-gray-400">Đang tải...</span>;
    }

    return (
        <td className="px-4 py-4 text-center">
            <div className="relative inline-block text-center">
                {/* Hiện tại đang có khuyến mãi */}
                {currentDiscount ? (
                    <div className="space-y-2">

                        <button
                            onClick={() => setOpen(true)}
                            className="text-xs text-blue-600 hover:text-blue-800 font-medium underline underline-offset-2 transition"
                        >
                            Đổi khuyến mãi
                        </button>
                    </div>
                ) : (
                    /* Chưa có khuyến mãi */
                    <button
                        onClick={() => setOpen(true)}
                        className="text-xs text-emerald-600 hover:text-emerald-800 font-medium underline underline-offset-2 transition"
                    >
                        + Gán khuyến mãi
                    </button>
                )}

                {/* Dropdown chọn khuyến mãi */}
                {open && (
                    <div
                        className="absolute left-1/2 -translate-x-1/2 w-80 bg-white rounded-2xl shadow-2xl border border-gray-200 z-[9999] p-5"
                        style={{
                            // Logic đảo chiều thông minh
                            top: open ? 'auto' : '100%',
                            bottom: open ? '100%' : 'auto',
                            marginTop: open ? '8px' : '0',
                            marginBottom: open ? '0' : '8px',
                        }}
                        ref={(node) => {
                            if (node) {
                                const rect = node.getBoundingClientRect();
                                const spaceBelow = window.innerHeight - rect.bottom;
                                const spaceAbove = rect.top;

                                // Nếu không đủ chỗ bên dưới (dưới 300px) → mở lên trên
                                if (spaceBelow < 320 && spaceAbove > spaceBelow) {
                                    node.style.top = 'auto';
                                    node.style.bottom = '100%';
                                    node.style.marginBottom = '12px';
                                    node.style.marginTop = '0';
                                } else {
                                    node.style.top = '100%';
                                    node.style.bottom = 'auto';
                                    node.style.marginTop = '12px';
                                    node.style.marginBottom = '0';
                                }
                            }
                        }}
                    >
                        {/* Nội dung dropdown như cũ */}
                        <h4 className="font-bold text-gray-800 mb-3 text-sm">Chọn chương trình khuyến mãi</h4>

                        <select
                            value={selectedDiscount}
                            onChange={(e) => setSelectedDiscount(e.target.value)}
                            className="w-full px-4 py-3 border border-gray-300 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:border-emerald-500 transition"
                        >
                            <option value="">-- Chọn khuyến mãi --</option>
                            {(discounts || []).map((d) => (
                                <option key={d.discountId} value={d.discountId}>
                                    [{d.target === "VARIANT" ? "Biến thể" : "Sản phẩm"}] {d.name} - Giảm {d.percentage}%
                                    {d.endDate && ` đến ${new Date(d.endDate).toLocaleDateString("vi-VN")}`}
                                </option>
                            ))}
                        </select>

                        <div className="flex gap-3 mt-4">
                            <button
                                onClick={handleApply}
                                disabled={!selectedDiscount}
                                className="flex-1 bg-emerald-600 hover:bg-emerald-700 disabled:bg-gray-400 text-white font-bold py-3 rounded-xl transition shadow-md"
                            >
                                Gán ngay
                            </button>
                            <button
                                onClick={() => {
                                    setOpen(false);
                                    setSelectedDiscount("");
                                }}
                                className="flex-1 bg-gray-200 hover:bg-gray-300 text-gray-800 font-bold py-3 rounded-xl transition"
                            >
                                Hủy
                            </button>
                        </div>
                    </div>
                )}
            </div>
        </td>
    );
};
export default DiscountSelector; 