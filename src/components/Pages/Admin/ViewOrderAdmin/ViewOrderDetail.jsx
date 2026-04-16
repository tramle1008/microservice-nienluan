// src/components/admin/order/ViewOrderDetail.jsx
import React, { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import api from "../../../../api/api";
import AdminSidebar from "../AdminSidebar";
const ViewOrderDetail = () => {
    const { orderId } = useParams(); // Lấy từ URL: /admin/orders/detail/1
    const navigate = useNavigate();

    const [order, setOrder] = useState(null);
    const [discounts, setDiscounts] = useState({});
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    const fetchOrderDetail = async () => {
        setLoading(true);
        try {
            const token = JSON.parse(localStorage.getItem("auth"))?.jwtToken;
            const response = await api.get(`/orders/${orderId}`, {
                headers: { Authorization: `Bearer ${token}` },
            });
            setOrder(response.data);
        } catch (err) {
            setError("Không thể tải chi tiết đơn hàng.");
            console.error(err);
        } finally {
            setLoading(false);
        }
    };
    const fetchDiscount = async (discountId) => {
        if (!discountId || discounts[discountId]) return; // Đã có rồi thì bỏ qua

        try {
            const token = JSON.parse(localStorage.getItem("auth"))?.jwtToken;
            const response = await api.get(`/discounts/${discountId}`, {
                headers: { Authorization: `Bearer ${token}` },
            });
            setDiscounts(prev => ({
                ...prev,
                [discountId]: response.data
            }));
        } catch (err) {
            console.error(`Không tải được discount ID ${discountId}:`, err);
            // Vẫn lưu null để tránh fetch lại nhiều lần
            setDiscounts(prev => ({
                ...prev,
                [discountId]: null
            }));
        }
    };
    useEffect(() => {
        if (order) {
            const discountIds = [...new Set(
                order.items
                    .map(item => item.discountId)
                    .filter(id => id != null) // Loại bỏ null/undefined
            )];

            discountIds.forEach(id => fetchDiscount(id));
        }
    }, [order]);

    useEffect(() => {
        fetchOrderDetail();
    }, [orderId]);

    if (loading)
        return (
            <div className="flex justify-center items-center h-96">
                <p className="text-xl text-gray-600">Đang tải chi tiết đơn hàng...</p>
            </div>
        );

    if (error)
        return (
            <div className="text-center py-10">
                <p className="text-red-500 text-lg">{error}</p>
            </div>
        );

    // Tính số tiền giảm thực tế cho một item
    const calculateDiscountAmount = (item) => {
        const discount = discounts[item.discountId];
        if (!discount || discount.type !== "PERCENTAGE") return 0;

        const discountPercent = discount.percentage / 100;
        const maxDiscount = discount.maxAmount || Infinity;
        const possibleDiscount = item.subtotal * discountPercent;
        return Math.min(possibleDiscount, maxDiscount);
    };


    if (!order) return null;

    return (
        <div className="min-h-screen bg-gray-50 py-8 px-4">
            <AdminSidebar />
            <div className="max-w-4xl mx-auto mt-10">
                {/* Header + Back Button */}
                <div className="flex items-center gap-4 mb-8">
                    <button
                        onClick={() => navigate(-1)} // Quay lại trang trước (rất tiện!)
                        className="flex items-center gap-2 px-4 py-2 bg-gray-200 hover:bg-gray-300 rounded-lg transition"
                    >

                        <span>Quay lại</span>
                    </button>
                    <h1 className="text-xl font-bold text-gray-800">
                        Chi tiết đơn hàng #{order.code}
                    </h1>
                </div>

                <div className="bg-white rounded-xl shadow-lg overflow-hidden">
                    {/* Thông tin chung */}
                    <div className="bg-[#b55858] text-white p-6">
                        <div className="grid grid-cols-2 md:grid-cols-4 gap-4 text-sm">
                            <div>
                                <p className="opacity-90">Mã đơn hàng</p>
                                <p className="text-xl font-bold">{order.code}</p>
                            </div>
                            <div>
                                <p className="opacity-90">Trạng thái</p>
                                <p className="text-xl font-bold">
                                    <span
                                        className={`px-3 py-1 rounded-full text-xs font-medium ${order.status === "PENDING"
                                            ? "bg-yellow-500"
                                            : order.status === "CONFIRMED"
                                                ? "bg-blue-500"
                                                : order.status === "SHIPPED"
                                                    ? "bg-purple-500"
                                                    : order.status === "DELIVERED"
                                                        ? "bg-green-500"
                                                        : order.status === "CANCELLED"
                                                            ? "bg-red-500"
                                                            : "bg-gray-500"
                                            } text-white`}
                                    >
                                        {order.status}
                                    </span>
                                </p>
                            </div>
                            <div>
                                <p className="opacity-90">Ngày đặt</p>
                                <p className="font-medium">
                                    {new Date(order.orderDate).toLocaleString("vi-VN")}
                                </p>
                            </div>
                            <div>
                                <p className="opacity-90">Tổng tiền</p>
                                <p className="text-2xl font-bold">
                                    {Number(order.finalAmount).toLocaleString()}đ
                                </p>
                            </div>
                        </div>
                    </div>

                    {/* Danh sách sản phẩm */}
                    <div className="p-6 border-b">
                        <h2 className="text-xl font-semibold mb-4 text-gray-800">
                            Sản phẩm trong đơn hàng
                        </h2>
                        <div className="space-y-4">
                            {order.items.map((item, index) => {
                                const discount = discounts[item.discountId];
                                const origin = discount && discount.percentage
                                    ? Math.round(item.unitPrice / (1 - discount.percentage / 100))
                                    : item.unitPrice;
                                return (
                                    <div
                                        key={index}
                                        className="p-4 bg-gray-50 rounded-lg hover:bg-gray-100 transition"
                                    >
                                        <div className="flex items-center gap-4">
                                            <img
                                                src={item.imageUrl}
                                                alt={item.productName}
                                                className="w-20 h-20 object-cover rounded-lg border"
                                            />
                                            <div className="flex-1">
                                                <h3 className="font-medium text-gray-800">
                                                    {item.productName}
                                                </h3>
                                                <p className="text-sm text-gray-600">
                                                    Phân loại: {item.variantColor || "Không có"}
                                                </p>
                                                <p className="text-sm">
                                                    Số lượng: <strong>{item.quantity}</strong> ×{" "}
                                                    {Number(item.unitPrice).toLocaleString()}đ
                                                </p>
                                                <p className="text-sm">
                                                    Giá gốc: {origin}
                                                </p>
                                            </div>
                                            <div className="text-right">
                                                <p className="text-lg font-semibold text-blue-600">
                                                    {Number(item.subtotal).toLocaleString()}đ
                                                </p>
                                            </div>
                                        </div>

                                        {/* Hiển thị chi tiết khuyến mãi nếu có */}
                                        {discount && (
                                            <div className="mt-3 ml-24 pl-4 border-l-4 border-green-500 bg-green-50 rounded-r-lg p-3">
                                                <p className="font-medium text-green-800">
                                                    {discount.name}
                                                </p>
                                                <p className="text-xs text-gray-600 mt-1">
                                                    Áp dụng từ {new Date(discount.startDate).toLocaleDateString("vi-VN")}
                                                    đến {new Date(discount.endDate).toLocaleDateString("vi-VN")}
                                                </p>
                                            </div>
                                        )}

                                        {!discount && item.discountId && (
                                            <div className="mt-3 ml-24 text-sm text-gray-500">
                                                Đang tải thông tin khuyến mãi...
                                            </div>
                                        )}
                                    </div>
                                );
                            })}
                        </div>
                    </div>

                    {/* Tổng tiền chi tiết */}
                    <div className="p-6 bg-gray-50">
                        <div className="max-w-md ml-auto space-y-2 text-lg">

                            <div className="flex justify-between">
                                <span>Phí vận chuyển:</span>
                                <span>{Number(order.shippingFee).toLocaleString()}đ</span>
                            </div>
                            <div className="flex justify-between font-bold text-xl text-blue-600 pt-3 border-t">
                                <span>Tổng thanh toán:</span>
                                <span>{Number(order.finalAmount).toLocaleString()}đ</span>
                            </div>
                        </div>
                    </div>

                </div>


            </div>
        </div>
    );
};

export default ViewOrderDetail;