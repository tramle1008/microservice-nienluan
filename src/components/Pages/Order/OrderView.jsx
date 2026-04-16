
import { useDispatch, useSelector } from "react-redux";
import { useEffect, useState } from "react";
import { fetchUserOrders } from "../../../store/actions";
import axios from "axios";
import api from "../../../api/api";
import toast from "react-hot-toast";
import AdminOrderPagination from "../Admin/AdminOrderPagination";
import { Link, useSearchParams } from "react-router-dom";


const OrderView = () => {
    const dispatch = useDispatch();
    // Thay đổi này là quan trọng nhất!
    const { orders = [], loading, error, totalPages = 0 } = useSelector(
        (state) => state.orderUser   // ← đổi từ state.order thành state.orderUser
    );



    const [searchParams] = useSearchParams();
    const page = parseInt(searchParams.get("page") ?? "0", 10);

    useEffect(() => {
        dispatch(fetchUserOrders(page));
    }, [dispatch, page]);

    // Hủy đơn hàng
    const handleCancel = async (orderId) => {
        if (!window.confirm("Bạn có chắc muốn hủy đơn hàng này?")) return;

        try {
            const auth = JSON.parse(localStorage.getItem("auth") || "{}");
            const token = auth?.jwtToken;

            await api.put(
                `/orders/${orderId}/cancel`,
                {},
                {
                    headers: { Authorization: `Bearer ${token}` },
                }
            );

            toast.success("Hủy đơn hàng thành công!");
            dispatch(fetchUserOrders()); // reload
        } catch (err) {
            toast.error("Không thể hủy đơn hàng");
            console.error(err);
        }
    };

    // Format ngày
    const formatDate = (dateString) => {
        return new Date(dateString).toLocaleString("vi-VN", {
            year: "numeric",
            month: "2-digit",
            day: "2-digit",
            hour: "2-digit",
            minute: "2-digit",
        });
    };

    // Format tiền
    const formatPrice = (price) => {
        return price.toLocaleString("vi-VN") + " VNĐ";
    };

    // Trạng thái đơn hàng
    const getStatusText = (status) => {
        const map = {
            PENDING: "Đang xử lý",
            CONFIRMED: "Đã xác nhận",
            SHIPPED: "Đang giao",
            DELIVERED: "Đã giao",
            REJECTED: "Đã hủy",
            CANCELLED: "Đã hủy",
        };
        return map[status] || status;
    };

    const getStatusColor = (status) => {
        const colors = {
            PENDING: "bg-yellow-100 text-yellow-800",
            CONFIRMED: "bg-blue-100 text-blue-800",
            SHIPPED: "bg-purple-100 text-purple-800",
            DELIVERED: "bg-green-100 text-green-800",
            REJECTED: "bg-red-100 text-red-800",
            CANCELLED: "bg-red-100 text-red-800",
        };
        return colors[status] || "bg-gray-100 text-gray-800";
    };

    if (loading) return <p className="text-center py-8">Đang tải đơn hàng...</p>;
    if (error) return <p className="text-red-500 text-center py-8">{error}</p>;

    return (
        <div className="max-w-4xl mx-auto p-4 space-y-6">
            <h2 className="text-2xl font-bold text-center mb-6">Lịch sử đơn hàng</h2>

            {orders.length === 0 ? (
                <div className="text-center py-12 bg-gray-50 rounded-lg">
                    <p className="text-gray-500">Bạn chưa có đơn hàng nào.</p>
                </div>
            ) : (
                <div className="space-y-6">
                    {orders.map((order) => (
                        <div
                            key={order.orderId}
                            className="bg-white border rounded-lg shadow-sm overflow-hidden"
                        >
                            {/* Header */}
                            <div className="bg-gray-50 p-4 border-b">
                                <div className="flex flex-wrap justify-between items-center gap-2">
                                    <div>
                                        <span className="font-semibold text-lg">Mã đơn: {order.code}</span>

                                    </div>
                                    <div className="flex items-center gap-3">
                                        <span
                                            className={`px-3 py-1 rounded-full text-xs font-medium ${getStatusColor(
                                                order.status
                                            )}`}
                                        >
                                            {getStatusText(order.status)}
                                        </span>
                                        {order.status === "PENDING" && (
                                            <button
                                                onClick={() => handleCancel(order.orderId)}
                                                className="text-sm px-3 py-1 bg-red-100 text-red-700 rounded hover:bg-red-200 transition"
                                            >
                                                Hủy đơn
                                            </button>
                                        )}
                                    </div>
                                </div>
                                <p className="text-md text-gray-600">
                                    {order.shippingAddress}
                                </p>
                                <p className="text-sm text-gray-600 mt-1">
                                    Đặt lúc: {formatDate(order.orderDate)}
                                </p>
                            </div>

                            {/* Sản phẩm */}
                            <div className="p-4">
                                <div className="space-y-3">
                                    {order.items.map((item, idx) => (
                                        <div key={idx} className="flex gap-4 items-start">
                                            <Link
                                                to={`/product/${item.productId}`}
                                            ><img
                                                    src={item.imageUrl}
                                                    alt={item.productName}
                                                    className="w-16 h-16 object-cover rounded border"
                                                /></Link>

                                            <div className="flex-1">
                                                <Link
                                                    to={`/product/${item.productId}`}>
                                                    <h4 className="font-medium text-gray-900">
                                                        {item.productName}
                                                    </h4>
                                                </Link>
                                                <p className="text-sm text-gray-600">
                                                    Màu: {item.variantColor || "Không có"}
                                                </p>
                                                <div className="flex justify-between text-sm mt-1">
                                                    <span>
                                                        SL: <strong>{item.quantity}</strong>
                                                    </span>
                                                    <span>
                                                        Giá: <strong>{formatPrice(item.unitPrice)}</strong>
                                                    </span>
                                                    <span>
                                                        Thành tiền: <strong>{formatPrice(item.subtotal)}</strong>
                                                    </span>
                                                </div>
                                            </div>
                                        </div>
                                    ))}
                                </div>
                            </div>
                            <div className="flex">
                                <div className="ml-auto mr-10">
                                    Phí vận chuyển: {order.shippingFee} VNĐ
                                </div>
                            </div>


                            {/* Tổng tiền */}
                            <div className="bg-green-50 p-4 border-t">
                                <div className="flex justify-between items-center font-semibold text-lg">
                                    <span>Tổng thanh toán:</span>
                                    <span className="text-green-700">
                                        {formatPrice(order.finalAmount)}
                                    </span>
                                </div>
                            </div>
                        </div>
                    ))}
                </div>
            )}
            <AdminOrderPagination totalPages={totalPages} />
        </div>
    );
};

export default OrderView;
