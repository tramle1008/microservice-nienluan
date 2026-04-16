import { useState } from "react";
import SetQuantity from "./SetQuantity";
import toast from "react-hot-toast";
import DeleteItem from "./DeleteItem";

const ItemContent = ({ item, onUpdate, onRemove }) => {
    const [currentQuantity, setCurrentQuantity] = useState(item.quantity);

    const handleQuantityChange = (newQty) => {
        setCurrentQuantity(newQty);
    };

    const handleRemove = () => {
        DeleteItem(item.itemId, () => {
            if (onRemove) onRemove();
        });
    };

    return (
        <div className="grid grid-cols-7 items-center py-4 border-b border-slate-200 text-sm md:text-base">
            {/* SẢN PHẨM */}
            <div className="col-span-2 flex items-center gap-4">
                <img
                    src={item.imageUrl}
                    alt={item.productName}
                    className="w-20 h-20 object-cover rounded-md"
                />
                <div>
                    <h4 className="font-semibold">{item.productName}</h4>
                    {/* Không có description → bỏ hoặc thêm sau */}
                </div>
            </div>
            <div className="text-center text-black font-semibold">
                {item.color}
            </div>
            {/* GIÁ */}
            <div className="text-center text-emerald-700 font-semibold">
                {Number(item.unitPrice).toLocaleString()} đ
            </div>

            {/* SỐ LƯỢNG */}
            <div className="flex justify-center">
                <SetQuantity
                    itemId={item.itemId}
                    quantity={item.quantity}
                    onUpdate={onUpdate}
                    onQuantityChange={handleQuantityChange}
                />
            </div>

            {/* THÀNH TIỀN */}
            <div className="text-center font-semibold text-gray-700">
                {Number(currentQuantity * item.unitPrice).toLocaleString()} đ
            </div>

            {/* XÓA */}
            <div className="flex justify-center">
                <button
                    onClick={handleRemove}
                    className="flex items-center font-bold px-4 py-1 border border-rose-600 rounded-md hover:bg-red-50 transition-colors duration-200"
                >
                    Xóa
                </button>
            </div>
        </div>
    );
};

export default ItemContent;
